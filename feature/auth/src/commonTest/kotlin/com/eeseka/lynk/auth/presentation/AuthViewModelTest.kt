package com.eeseka.lynk.auth.presentation

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.eeseka.lynk.auth.data.FakeAuthService
import com.eeseka.lynk.auth.data.FakeSessionStorage
import com.eeseka.lynk.shared.domain.util.DataError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    // Set up the Dispatcher required by the blueprint
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var authService: FakeAuthService
    private lateinit var sessionStorage: FakeSessionStorage
    private lateinit var viewModel: AuthViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Initialize our Fakes and ViewModel before every single test
        authService = FakeAuthService()
        sessionStorage = FakeSessionStorage()
        viewModel = AuthViewModel(authService, sessionStorage)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `clicking Google sign in sets isGoogleSigningIn state to true`() = runTest {
        viewModel.state.test {
            val initialState = awaitItem()
            assertThat(initialState.isGoogleSigningIn).isFalse()

            // Trigger the action just like the UI would
            viewModel.onAction(AuthAction.OnGoogleSignInClick)

            val loadingState = awaitItem()
            assertThat(loadingState.isGoogleSigningIn).isTrue()
        }
    }

    @Test
    fun `successful Google sign in emits Success event and saves session`() = runTest {
        viewModel.events.test {

            viewModel.onAction(AuthAction.OnGoogleTokenReceived("valid_token"))

            val event = awaitItem()

            assertThat(event).isEqualTo(AuthEvent.Success(authService.authInfoToReturn.user))
            assertThat(sessionStorage.get()).isEqualTo(authService.authInfoToReturn)
        }
    }

    @Test
    fun `failed Google sign in emits Error event and does not save session`() = runTest {
        authService.shouldReturnError = true
        authService.errorToReturn = DataError.Remote.SERVER_ERROR

        viewModel.events.test {

            viewModel.onAction(AuthAction.OnGoogleTokenReceived("invalid_token"))

            val event = awaitItem()

            assertThat(event is AuthEvent.Error).isTrue()
            assertThat(sessionStorage.get() == null).isTrue()
        }
    }

    @Test
    fun `successful Guest sign in emits Success event and saves session`() = runTest {
        viewModel.events.test {

            viewModel.onAction(AuthAction.OnGuestClick)

            val event = awaitItem()

            assertThat(event).isEqualTo(AuthEvent.Success(authService.authInfoToReturn.user))
            assertThat(sessionStorage.get()).isEqualTo(authService.authInfoToReturn)
        }
    }
}