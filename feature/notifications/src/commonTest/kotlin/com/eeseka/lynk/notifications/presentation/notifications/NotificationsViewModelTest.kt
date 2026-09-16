package com.eeseka.lynk.notifications.presentation.notifications

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.eeseka.lynk.shared.domain.notification.model.Notification
import com.eeseka.lynk.shared.domain.notification.model.NotificationType
import com.eeseka.lynk.testing.collectInBackground
import com.eeseka.lynk.testing.data.FakeNotificationService
import com.eeseka.lynk.testing.data.FakeUnreadNotificationCounter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var notificationService: FakeNotificationService
    private lateinit var unreadNotificationCounter: FakeUnreadNotificationCounter
    private lateinit var viewModel: NotificationsViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        notificationService = FakeNotificationService()
        unreadNotificationCounter = FakeUnreadNotificationCounter()
        viewModel = NotificationsViewModel(notificationService, unreadNotificationCounter)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `opening the inbox loads the first page`() = runTest {
        notificationService.notifications = mutableListOf(
            notification("n1", NotificationType.SPOT_CHOSEN),
            notification("n2", NotificationType.PAYMENT_RECEIVED)
        )

        collectInBackground(viewModel.state)
        advanceUntilIdle()

        assertThat(viewModel.state.value.notifications.map { it.id }).containsExactly("n1", "n2")
        assertThat(viewModel.state.value.isLoading).isFalse()
    }

    @Test
    fun `a page with nothing older marks the end of the inbox`() = runTest {
        notificationService.notifications = mutableListOf(notification("n1", NotificationType.SPOT_CHOSEN))
        collectInBackground(viewModel.state)
        advanceUntilIdle()

        viewModel.onAction(NotificationsAction.LoadNextPage)
        advanceUntilIdle()

        assertThat(viewModel.state.value.isEndReached).isTrue()
        assertThat(viewModel.state.value.notifications.size).isEqualTo(1)
    }

    @Test
    fun `a failed first page shows an error state and retry loads the inbox`() = runTest {
        notificationService.notifications = mutableListOf(notification("n1", NotificationType.SPOT_CHOSEN))
        notificationService.shouldReturnError = true
        collectInBackground(viewModel.state)
        advanceUntilIdle()
        assertThat(viewModel.state.value.loadError).isNotNull()

        notificationService.shouldReturnError = false
        viewModel.onAction(NotificationsAction.OnRetryClick)
        advanceUntilIdle()

        assertThat(viewModel.state.value.loadError).isNull()
        assertThat(viewModel.state.value.notifications.size).isEqualTo(1)
    }

    @Test
    fun `a failed later page sends a message and keeps the inbox`() = runTest {
        notificationService.notifications = mutableListOf(notification("n1", NotificationType.SPOT_CHOSEN))
        collectInBackground(viewModel.state)
        advanceUntilIdle()

        viewModel.events.test {
            notificationService.shouldReturnError = true
            viewModel.onAction(NotificationsAction.LoadNextPage)
            advanceUntilIdle()

            assertThat(awaitItem()).isInstanceOf(NotificationsEvent.Error::class)
            assertThat(viewModel.state.value.notifications.size).isEqualTo(1)
            assertThat(viewModel.state.value.loadError).isNull()
        }
    }

    @Test
    fun `tapping an unread notification marks it read and lowers the badge`() = runTest {
        notificationService.notifications = mutableListOf(notification("n1", NotificationType.SPOT_CHOSEN))
        unreadNotificationCounter.count.value = 1
        collectInBackground(viewModel.state)
        advanceUntilIdle()

        viewModel.onAction(NotificationsAction.OnNotificationClick("n1", HANGOUT_ID, NotificationType.SPOT_CHOSEN))
        advanceUntilIdle()

        assertThat(viewModel.state.value.notifications.single().isRead).isTrue()
        assertThat(unreadNotificationCounter.count.value).isEqualTo(0L)
        assertThat(notificationService.notifications.single().isRead).isTrue()
    }

    @Test
    fun `a read that fails on the server is undone and the badge is recounted`() = runTest {
        notificationService.notifications = mutableListOf(notification("n1", NotificationType.SPOT_CHOSEN))
        collectInBackground(viewModel.state)
        advanceUntilIdle()
        notificationService.shouldReturnError = true

        viewModel.onAction(NotificationsAction.OnNotificationClick("n1", HANGOUT_ID, NotificationType.SPOT_CHOSEN))
        advanceUntilIdle()

        assertThat(viewModel.state.value.notifications.single().isRead).isFalse()
        assertThat(unreadNotificationCounter.refreshCount).isEqualTo(1)
    }

    @Test
    fun `tapping an already read notification does not lower the badge again`() = runTest {
        notificationService.notifications = mutableListOf(
            notification("n1", NotificationType.SPOT_CHOSEN, isRead = true)
        )
        unreadNotificationCounter.count.value = 3
        collectInBackground(viewModel.state)
        advanceUntilIdle()

        viewModel.onAction(NotificationsAction.OnNotificationClick("n1", HANGOUT_ID, NotificationType.SPOT_CHOSEN))
        advanceUntilIdle()

        assertThat(unreadNotificationCounter.count.value).isEqualTo(3L)
    }

    @Test
    fun `tapping most notifications opens their hangout`() = runTest {
        notificationService.notifications = mutableListOf(notification("n1", NotificationType.HANGOUT_STARTED))
        collectInBackground(viewModel.state)
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(NotificationsAction.OnNotificationClick("n1", HANGOUT_ID, NotificationType.HANGOUT_STARTED))
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(NotificationsEvent.NavigateToHangout(HANGOUT_ID))
        }
    }

    @Test
    fun `tapping an invite opens its preview instead of the hangout`() = runTest {
        notificationService.notifications = mutableListOf(notification("n1", NotificationType.PARTICIPANT_INVITED))
        collectInBackground(viewModel.state)
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(
                NotificationsAction.OnNotificationClick("n1", HANGOUT_ID, NotificationType.PARTICIPANT_INVITED)
            )
            advanceUntilIdle()

            assertThat(viewModel.state.value.previewHangoutId).isEqualTo(HANGOUT_ID)
            expectNoEvents()
        }
    }

    @Test
    fun `tapping a notification for a hangout you are no longer in opens nothing`() = runTest {
        notificationService.notifications = mutableListOf(
            notification("n1", NotificationType.INVITE_CANCELLED),
            notification("n2", NotificationType.REMOVED_FOR_NON_PAYMENT)
        )
        collectInBackground(viewModel.state)
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(NotificationsAction.OnNotificationClick("n1", HANGOUT_ID, NotificationType.INVITE_CANCELLED))
            viewModel.onAction(
                NotificationsAction.OnNotificationClick("n2", HANGOUT_ID, NotificationType.REMOVED_FOR_NON_PAYMENT)
            )
            advanceUntilIdle()

            expectNoEvents()
            assertThat(viewModel.state.value.previewHangoutId).isNull()
        }
    }

    @Test
    fun `dismissing the invite preview closes it`() = runTest {
        collectInBackground(viewModel.state)
        viewModel.onAction(NotificationsAction.OnOpenInvitePreview(HANGOUT_ID))

        viewModel.onAction(NotificationsAction.OnDismissInvitePreview)

        assertThat(viewModel.state.value.previewHangoutId).isNull()
    }

    @Test
    fun `mark all read clears the badge and every unread dot`() = runTest {
        notificationService.notifications = mutableListOf(
            notification("n1", NotificationType.SPOT_CHOSEN),
            notification("n2", NotificationType.PAYMENT_RECEIVED)
        )
        unreadNotificationCounter.count.value = 2
        collectInBackground(viewModel.state)
        advanceUntilIdle()

        viewModel.onAction(NotificationsAction.OnMarkAllReadClick)
        advanceUntilIdle()

        assertThat(viewModel.state.value.notifications.filterNot { it.isRead }).isEmpty()
        assertThat(unreadNotificationCounter.count.value).isEqualTo(0L)
        assertThat(viewModel.state.value.isMarkingAllRead).isFalse()
    }

    @Test
    fun `a failed mark all read sends an error and keeps the unread dots`() = runTest {
        notificationService.notifications = mutableListOf(notification("n1", NotificationType.SPOT_CHOSEN))
        unreadNotificationCounter.count.value = 1
        collectInBackground(viewModel.state)
        advanceUntilIdle()
        notificationService.shouldReturnError = true

        viewModel.events.test {
            viewModel.onAction(NotificationsAction.OnMarkAllReadClick)
            advanceUntilIdle()

            assertThat(awaitItem()).isInstanceOf(NotificationsEvent.Error::class)
            assertThat(viewModel.state.value.notifications.single().isRead).isFalse()
            assertThat(unreadNotificationCounter.count.value).isEqualTo(1L)
            assertThat(viewModel.state.value.isMarkingAllRead).isFalse()
        }
    }

    private fun notification(id: String, type: NotificationType, isRead: Boolean = false) = Notification(
        id = id,
        type = type,
        hangoutId = HANGOUT_ID,
        hangoutName = "Night Out",
        actorDisplayName = "Ada",
        amountKobo = null,
        isRead = isRead,
        createdAt = Instant.fromEpochMilliseconds(4102444800000L)
    )

    private companion object {
        const val HANGOUT_ID = "hangout_1"
    }
}
