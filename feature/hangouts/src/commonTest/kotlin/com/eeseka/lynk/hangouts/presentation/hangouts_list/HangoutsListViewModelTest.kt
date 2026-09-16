package com.eeseka.lynk.hangouts.presentation.hangouts_list

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
import com.eeseka.lynk.hangouts.presentation.model.HangoutStatusFilter
import com.eeseka.lynk.shared.domain.auth.model.AuthInfo
import com.eeseka.lynk.shared.domain.auth.model.AuthProvider
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.testing.collectInBackground
import com.eeseka.lynk.testing.data.FakeAppPreferences
import com.eeseka.lynk.testing.data.FakeAuthService
import com.eeseka.lynk.testing.data.FakeHangoutService
import com.eeseka.lynk.testing.data.FakeSessionStorage
import com.eeseka.lynk.testing.typeText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class HangoutsListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var hangoutService: FakeHangoutService
    private lateinit var sessionStorage: FakeSessionStorage
    private lateinit var authService: FakeAuthService
    private lateinit var appPreferences: FakeAppPreferences
    private lateinit var viewModel: HangoutsListViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        hangoutService = FakeHangoutService()
        sessionStorage = FakeSessionStorage()
        authService = FakeAuthService()
        appPreferences = FakeAppPreferences()
        viewModel = HangoutsListViewModel(hangoutService, sessionStorage, authService, appPreferences)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `opening the list loads the first page of hangouts`() = runTest {
        createHangout("Night Out")
        createHangout("Brunch")

        collectInBackground(viewModel.state)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertThat(state.hangouts.map { it.name }).containsExactly("Night Out", "Brunch")
        assertThat(state.isLoading).isFalse()
        assertThat(state.loadError).isNull()
    }

    @Test
    fun `a signed in user is not flagged as a guest`() = runTest {
        sessionStorage.set(session(authenticatedUser(id = "user_1")))

        collectInBackground(viewModel.state)
        advanceUntilIdle()

        assertThat(viewModel.state.value.isGuest).isFalse()
        assertThat(viewModel.state.value.currentUserId).isEqualTo("user_1")
    }

    @Test
    fun `a guest is flagged as a guest`() = runTest {
        sessionStorage.set(session(User.Guest(id = "guest_1")))

        collectInBackground(viewModel.state)
        advanceUntilIdle()

        assertThat(viewModel.state.value.isGuest).isTrue()
    }

    @Test
    fun `choosing a status filter reloads the list with only that status`() = runTest {
        createHangout("Night Out")
        val finishedId = createHangout("Brunch")
        hangoutService.completeHangout(finishedId)
        collectInBackground(viewModel.state)
        advanceUntilIdle()

        viewModel.onAction(HangoutsListAction.OnStatusFilterSelected(HangoutStatusFilter.COMPLETED))
        advanceUntilIdle()

        assertThat(viewModel.state.value.hangouts.map { it.name }).containsExactly("Brunch")
    }

    @Test
    fun `choosing a vibe reloads the list with only that vibe`() = runTest {
        createHangout("Night Out", vibe = HangoutVibe.CHILL)
        createHangout("Brunch", vibe = HangoutVibe.FOOD)
        collectInBackground(viewModel.state)
        advanceUntilIdle()

        viewModel.onAction(HangoutsListAction.OnVibeSelected(HangoutVibe.FOOD))
        advanceUntilIdle()

        assertThat(viewModel.state.value.hangouts.map { it.name }).containsExactly("Brunch")
    }

    @Test
    fun `typing a search reloads the list after the debounce`() = runTest {
        createHangout("Night Out")
        createHangout("Brunch")
        collectInBackground(viewModel.state)
        advanceUntilIdle()

        viewModel.state.value.searchTextState.typeText("brunch")
        advanceTimeBy(400.milliseconds)
        assertThat(viewModel.state.value.hangouts.size).isEqualTo(2)

        advanceTimeBy(101.milliseconds)
        advanceUntilIdle()
        assertThat(viewModel.state.value.hangouts.map { it.name }).containsExactly("Brunch")
    }

    @Test
    fun `a page with nothing older marks the end of the list`() = runTest {
        createHangout("Night Out")
        collectInBackground(viewModel.state)
        advanceUntilIdle()
        assertThat(viewModel.state.value.isEndReached).isFalse()

        viewModel.onAction(HangoutsListAction.LoadNextPage)
        advanceUntilIdle()

        assertThat(viewModel.state.value.isEndReached).isTrue()
        assertThat(viewModel.state.value.hangouts.size).isEqualTo(1)
    }

    @Test
    fun `a failed first page shows an error state instead of a message`() = runTest {
        hangoutService.shouldReturnError = true

        viewModel.events.test {
            collectInBackground(viewModel.state)
            advanceUntilIdle()

            assertThat(viewModel.state.value.loadError).isNotNull()
            assertThat(viewModel.state.value.hangouts).isEmpty()
            expectNoEvents()
        }
    }

    @Test
    fun `a failed later page sends a message and keeps the list`() = runTest {
        createHangout("Night Out")
        collectInBackground(viewModel.state)
        advanceUntilIdle()

        viewModel.events.test {
            hangoutService.shouldReturnError = true
            viewModel.onAction(HangoutsListAction.LoadNextPage)
            advanceUntilIdle()

            assertThat(awaitItem()).isInstanceOf(HangoutsListEvent.Error::class)
            assertThat(viewModel.state.value.hangouts.size).isEqualTo(1)
            assertThat(viewModel.state.value.loadError).isNull()
        }
    }

    @Test
    fun `retry clears the error and loads the list again`() = runTest {
        createHangout("Night Out")
        hangoutService.shouldReturnError = true
        collectInBackground(viewModel.state)
        advanceUntilIdle()
        assertThat(viewModel.state.value.loadError).isNotNull()

        hangoutService.shouldReturnError = false
        viewModel.onAction(HangoutsListAction.OnRetryClick)
        advanceUntilIdle()

        assertThat(viewModel.state.value.loadError).isNull()
        assertThat(viewModel.state.value.hangouts.size).isEqualTo(1)
    }

    @Test
    fun `refresh loads the list again from the start`() = runTest {
        createHangout("Night Out")
        collectInBackground(viewModel.state)
        advanceUntilIdle()

        createHangout("Brunch")
        viewModel.onAction(HangoutsListAction.Refresh)
        advanceUntilIdle()

        assertThat(viewModel.state.value.hangouts.map { it.name }).containsExactly("Night Out", "Brunch")
    }

    @Test
    fun `selecting a hangout stores its id`() = runTest {
        collectInBackground(viewModel.state)

        viewModel.onAction(HangoutsListAction.OnSelectHangout("hangout_1"))

        assertThat(viewModel.state.value.selectedHangoutId).isEqualTo("hangout_1")
    }

    @Test
    fun `denying notification permission turns push notifications off`() = runTest {
        viewModel.onAction(HangoutsListAction.OnNotificationPermissionDenied)
        advanceUntilIdle()

        assertThat(appPreferences.arePushNotificationsEnabled.first()).isFalse()
    }

    @Test
    fun `signing out a guest clears the session even when the backend fails`() = runTest {
        sessionStorage.set(session(User.Guest(id = "guest_1")))
        authService.shouldReturnError = true
        collectInBackground(viewModel.state)

        viewModel.onAction(HangoutsListAction.SignOutGuest)
        advanceUntilIdle()

        assertThat(sessionStorage.get()).isNull()
        assertThat(viewModel.state.value.isGuestSigningOut).isFalse()
    }

    private suspend fun createHangout(name: String, vibe: HangoutVibe = HangoutVibe.CHILL): String {
        hangoutService.createHangout(
            name = name,
            description = null,
            vibe = vibe,
            scheduledAt = Instant.fromEpochMilliseconds(4102444800000L),
            maxAttendees = null,
            spotId = null
        )
        return hangoutService.hangouts.last().id
    }

    private fun authenticatedUser(id: String) = User.Authenticated(
        id = id,
        provider = AuthProvider.GOOGLE,
        email = "test@test.com",
        displayName = "Tester",
        username = "tester"
    )

    private fun session(user: User) = AuthInfo(
        accessToken = "access",
        refreshToken = "refresh",
        user = user
    )
}
