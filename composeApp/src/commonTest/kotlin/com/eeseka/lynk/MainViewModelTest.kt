package com.eeseka.lynk

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.eeseka.lynk.shared.domain.auth.model.AuthInfo
import com.eeseka.lynk.shared.domain.auth.model.AuthProvider
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.domain.settings.AppTheme
import com.eeseka.lynk.testing.collectInBackground
import com.eeseka.lynk.testing.data.FakeAppPreferences
import com.eeseka.lynk.testing.data.FakeOnboardingStorage
import com.eeseka.lynk.testing.data.FakeDeviceTokenService
import com.eeseka.lynk.testing.data.FakePushNotificationService
import com.eeseka.lynk.testing.data.FakeSessionStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var sessionStorage: FakeSessionStorage
    private lateinit var appPreferences: FakeAppPreferences
    private lateinit var onboardingStorage: FakeOnboardingStorage
    private lateinit var deviceTokenService: FakeDeviceTokenService
    private lateinit var pushNotificationService: FakePushNotificationService

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        sessionStorage = FakeSessionStorage()
        appPreferences = FakeAppPreferences()
        onboardingStorage = FakeOnboardingStorage()
        deviceTokenService = FakeDeviceTokenService()
        pushNotificationService = FakePushNotificationService()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `a saved session is shown once the auth check finishes`() = runTest {
        signIn()

        val viewModel = createViewModel()

        assertThat(viewModel.state.value.user).isNotNull()
        assertThat(viewModel.state.value.isCheckingAuth).isFalse()
    }

    @Test
    fun `no saved session leaves the user signed out`() = runTest {
        val viewModel = createViewModel()

        assertThat(viewModel.state.value.user).isNull()
        assertThat(viewModel.state.value.isCheckingAuth).isFalse()
    }

    @Test
    fun `the theme and the onboarding flag come from the saved settings`() = runTest {
        appPreferences.setTheme(AppTheme.DARK)
        onboardingStorage.setOnboardingCompleted()

        val viewModel = createViewModel()

        assertThat(viewModel.state.value.theme).isEqualTo(AppTheme.DARK)
        assertThat(viewModel.state.value.hasSeenOnboarding).isTrue()
    }

    @Test
    fun `a session that disappears signs the user out`() = runTest {
        signIn()
        val viewModel = createViewModel()

        viewModel.events.test {
            // What the token refresh does when it can no longer keep the session alive
            sessionStorage.set(null)
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(MainEvent.OnSessionExpired)
            assertThat(viewModel.state.value.user).isNull()
            assertThat(sessionStorage.get()).isNull()
        }
    }

    @Test
    fun `signing in for the first time is not treated as an expiry`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            signIn()
            advanceUntilIdle()

            expectNoEvents()
        }
    }

    @Test
    fun `the push token is registered once the user is signed in`() = runTest {
        signIn()
        createViewModel()

        pushNotificationService.deviceToken.value = "push_token_1"
        advanceUntilIdle()

        assertThat(deviceTokenService.registeredTokens.map { it.first }).isEqualTo(listOf("push_token_1"))
    }

    @Test
    fun `the same push token is never registered twice`() = runTest {
        signIn()
        createViewModel()
        pushNotificationService.deviceToken.value = "push_token_1"
        advanceUntilIdle()

        appPreferences.setTheme(AppTheme.DARK) // any other change that re-runs the push check
        advanceUntilIdle()

        assertThat(deviceTokenService.registeredTokens.size).isEqualTo(1)
    }

    @Test
    fun `no push token is registered while signed out`() = runTest {
        createViewModel()

        pushNotificationService.deviceToken.value = "push_token_1"
        advanceUntilIdle()

        assertThat(deviceTokenService.registeredTokens).isEmpty()
    }

    @Test
    fun `turning push notifications off removes the token from the server`() = runTest {
        signIn()
        createViewModel()
        pushNotificationService.deviceToken.value = "push_token_1"
        advanceUntilIdle()

        appPreferences.setPushNotificationsEnabled(false)
        advanceUntilIdle()

        assertThat(deviceTokenService.unregisteredTokens).isEqualTo(listOf("push_token_1"))
    }

    @Test
    fun `turning push notifications back on registers the token again`() = runTest {
        signIn()
        createViewModel()
        pushNotificationService.deviceToken.value = "push_token_1"
        advanceUntilIdle()
        appPreferences.setPushNotificationsEnabled(false)
        advanceUntilIdle()

        appPreferences.setPushNotificationsEnabled(true)
        advanceUntilIdle()

        assertThat(deviceTokenService.registeredTokens.size).isEqualTo(2)
    }

    @Test
    fun `signing out forgets the token here instead of asking the server`() = runTest {
        signIn()
        createViewModel()
        pushNotificationService.deviceToken.value = "push_token_1"
        advanceUntilIdle()

        // ProfileViewModel already unregistered it while the session was alive
        sessionStorage.set(null)
        advanceUntilIdle()

        assertThat(deviceTokenService.unregisteredTokens).isEmpty()
    }

    private fun TestScope.createViewModel(): MainViewModel {
        val viewModel = MainViewModel(
            sessionStorage = sessionStorage,
            appPreferences = appPreferences,
            onboardingStorage = onboardingStorage,
            deviceTokenService = deviceTokenService,
            pushNotificationService = pushNotificationService
        )
        collectInBackground(viewModel.state)
        advanceUntilIdle()
        return viewModel
    }

    private suspend fun signIn() {
        sessionStorage.set(session())
    }

    private fun session() = AuthInfo(
        accessToken = "access",
        refreshToken = "refresh",
        user = User.Authenticated(
            id = "user_1",
            provider = AuthProvider.GOOGLE,
            email = "ada@test.com",
            displayName = "Ada",
            username = "ada"
        )
    )
}
