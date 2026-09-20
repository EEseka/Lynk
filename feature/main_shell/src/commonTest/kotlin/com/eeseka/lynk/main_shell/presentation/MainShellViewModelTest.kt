package com.eeseka.lynk.main_shell.presentation

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.eeseka.lynk.shared.domain.auth.model.AuthInfo
import com.eeseka.lynk.shared.domain.auth.model.AuthProvider
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.testing.collectInBackground
import com.eeseka.lynk.testing.data.FakeAppPreferences
import com.eeseka.lynk.testing.data.FakeSessionStorage
import com.eeseka.lynk.testing.data.FakeUnreadNotificationCounter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainShellViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var unreadNotificationCounter: FakeUnreadNotificationCounter
    private lateinit var sessionStorage: FakeSessionStorage
    private lateinit var appPreferences: FakeAppPreferences
    private lateinit var viewModel: MainShellViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        unreadNotificationCounter = FakeUnreadNotificationCounter()
        sessionStorage = FakeSessionStorage()
        appPreferences = FakeAppPreferences()
        viewModel = MainShellViewModel(unreadNotificationCounter, sessionStorage, appPreferences)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `a signed in user is asked for the notification permission`() = runTest {
        collectInBackground(viewModel.state)

        sessionStorage.set(session(authenticatedUser))
        advanceUntilIdle()

        assertThat(viewModel.state.value.canReceiveNotifications).isTrue()
    }

    @Test
    fun `a guest account is never asked, because it cannot be in a hangout`() = runTest {
        collectInBackground(viewModel.state)

        sessionStorage.set(session(User.Guest(id = "guest_1")))
        advanceUntilIdle()

        assertThat(viewModel.state.value.canReceiveNotifications).isFalse()
    }

    @Test
    fun `refusing the notification permission turns push notifications off`() = runTest {
        collectInBackground(viewModel.state)

        viewModel.onAction(MainShellAction.OnNotificationPermissionDenied)
        advanceUntilIdle()

        assertThat(appPreferences.arePushNotificationsEnabled.first()).isFalse()
    }

    @Test
    fun `unread notifications show a count and light the dot`() = runTest {
        collectInBackground(viewModel.state)

        unreadNotificationCounter.count.value = 3
        advanceUntilIdle()

        assertThat(viewModel.state.value.unreadNotificationCount).isEqualTo(3)
        assertThat(viewModel.state.value.hasUnseenNotifications).isTrue()
    }

    @Test
    fun `opening the hangouts tab puts the dot out and asks for a fresh count`() = runTest {
        collectInBackground(viewModel.state)
        unreadNotificationCounter.count.value = 3
        advanceUntilIdle()

        viewModel.onAction(MainShellAction.OnHangoutsTabActiveChanged(isActive = true))
        advanceUntilIdle()

        assertThat(viewModel.state.value.hasUnseenNotifications).isFalse()
        assertThat(viewModel.state.value.unreadNotificationCount).isEqualTo(3)
        assertThat(unreadNotificationCounter.refreshCount).isEqualTo(1)
    }

    @Test
    fun `notifications arriving while the tab is open do not light the dot`() = runTest {
        collectInBackground(viewModel.state)
        viewModel.onAction(MainShellAction.OnHangoutsTabActiveChanged(isActive = true))
        advanceUntilIdle()

        unreadNotificationCounter.count.value = 4
        advanceUntilIdle()

        assertThat(viewModel.state.value.unreadNotificationCount).isEqualTo(4)
        assertThat(viewModel.state.value.hasUnseenNotifications).isFalse()
    }

    @Test
    fun `notifications arriving after leaving the tab light the dot`() = runTest {
        collectInBackground(viewModel.state)
        viewModel.onAction(MainShellAction.OnHangoutsTabActiveChanged(isActive = true))
        unreadNotificationCounter.count.value = 2
        advanceUntilIdle()

        viewModel.onAction(MainShellAction.OnHangoutsTabActiveChanged(isActive = false))
        unreadNotificationCounter.count.value = 5
        advanceUntilIdle()

        assertThat(viewModel.state.value.hasUnseenNotifications).isTrue()
    }

    @Test
    fun `a count that only drops does not light the dot`() = runTest {
        collectInBackground(viewModel.state)
        viewModel.onAction(MainShellAction.OnHangoutsTabActiveChanged(isActive = true))
        unreadNotificationCounter.count.value = 5
        advanceUntilIdle()
        viewModel.onAction(MainShellAction.OnHangoutsTabActiveChanged(isActive = false))

        unreadNotificationCounter.count.value = 2
        advanceUntilIdle()

        assertThat(viewModel.state.value.unreadNotificationCount).isEqualTo(2)
        assertThat(viewModel.state.value.hasUnseenNotifications).isFalse()
    }

    @Test
    fun `the hangout detail pane remembers it is full screen`() = runTest {
        collectInBackground(viewModel.state)

        viewModel.onAction(MainShellAction.OnHangoutDetailPaneFullScreenChanged(isFullScreen = true))
        assertThat(viewModel.state.value.isHangoutDetailPaneFullScreen).isTrue()

        viewModel.onAction(MainShellAction.OnHangoutDetailPaneFullScreenChanged(isFullScreen = false))
        assertThat(viewModel.state.value.isHangoutDetailPaneFullScreen).isFalse()
    }

    private val authenticatedUser = User.Authenticated(
        id = "user_1",
        provider = AuthProvider.GOOGLE,
        email = "emma@lynk.com.ng",
        displayName = "Emma",
        username = "emma"
    )

    private fun session(user: User) = AuthInfo(
        accessToken = "access",
        refreshToken = "refresh",
        user = user
    )
}
