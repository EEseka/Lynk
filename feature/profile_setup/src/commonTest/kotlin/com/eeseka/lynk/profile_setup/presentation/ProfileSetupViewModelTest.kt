package com.eeseka.lynk.profile_setup.presentation

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.eeseka.lynk.profile_setup.data.FakeImageCompressionService
import com.eeseka.lynk.profile_setup.data.FakeSessionStorage
import com.eeseka.lynk.profile_setup.data.FakeUserService
import com.eeseka.lynk.shared.domain.auth.model.AuthInfo
import com.eeseka.lynk.shared.domain.auth.model.AuthProvider
import com.eeseka.lynk.shared.domain.auth.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileSetupViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var userService: FakeUserService
    private lateinit var sessionStorage: FakeSessionStorage
    private lateinit var imageCompressor: FakeImageCompressionService

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        userService = FakeUserService()
        sessionStorage = FakeSessionStorage()
        imageCompressor = FakeImageCompressionService()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun createViewModel(): ProfileSetupViewModel {
        sessionStorage.set(
            AuthInfo(
                accessToken = "token",
                refreshToken = "refresh",
                user = User.ProfileIncomplete(
                    id = "1", email = "test@test.com",
                    provider = AuthProvider.GOOGLE,
                    displayName = "Test",
                    profilePictureUrl = null
                )
            )
        )
        return ProfileSetupViewModel(userService, sessionStorage, imageCompressor)
    }

    @Test
    fun `picking image updates state with local URI and sets compressing state`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            skipItems(1) // Skip initial state

            viewModel.onAction(ProfileSetupAction.OnImagePicked("local/path.jpg", "image/jpeg"))

            val state = awaitItem()
            assertThat(state.localPhotoUri).isEqualTo("local/path.jpg")
            assertThat(state.isCompressingImage).isFalse()
        }
    }

    @Test
    fun `username availability check runs after debounce and updates state`() = runTest {
        val viewModel = createViewModel()
        userService.isAvailable = true

        viewModel.state.test {
            val initialState = awaitItem()
            assertThat(initialState.isUsernameAvailable).isNull()

            // User types a valid username
            viewModel.state.value.usernameTextState.setTextAndPlaceCursorAtEnd("valid_user")

            // Fast-forward virtual time to bypass the 500ms debounce!
            advanceTimeBy(501)

            // The flow will emit a few times (checking -> true)
            // We look for the final emission where the network check finishes
            val finalState = expectMostRecentItem()
            assertThat(finalState.isCheckingUsername).isFalse()
            assertThat(finalState.isUsernameAvailable).isEqualTo(true)
            assertThat(finalState.usernameError).isNull()
        }
    }

    @Test
    fun `taken username sets error and blocks submission`() = runTest {
        val viewModel = createViewModel()
        userService.isAvailable = false // Simulate database saying username is taken

        viewModel.state.test {
            skipItems(1)

            viewModel.state.value.displayNameTextState.setTextAndPlaceCursorAtEnd("Valid Name")
            viewModel.state.value.usernameTextState.setTextAndPlaceCursorAtEnd("taken_user")

            advanceTimeBy(501)

            val finalState = expectMostRecentItem()
            assertThat(finalState.isUsernameAvailable).isEqualTo(false)
            assertThat(finalState.canSubmit).isFalse()
            assertThat(finalState.usernameError).isNotNull()
        }
    }

    @Test
    fun `submission blocked if local validation fails`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            skipItems(1)

            // Set valid username, but BLANK display name
            viewModel.state.value.usernameTextState.setTextAndPlaceCursorAtEnd("valid_user")
            viewModel.state.value.displayNameTextState.setTextAndPlaceCursorAtEnd("   ")

            advanceTimeBy(501)

            // Attempt to submit
            viewModel.onAction(ProfileSetupAction.OnSubmitClick)

            val finalState = expectMostRecentItem()
            assertThat(finalState.canSubmit).isFalse()
            assertThat(finalState.displayNameError).isNotNull() // Should show blank error
            assertThat(finalState.isSubmitting).isFalse() // Should abort early
        }
    }

    @Test
    fun `image upload failure aborts submission and emits error`() = runTest {
        val viewModel = createViewModel()

        // Force the fake API to fail when getting the upload URL
        userService.shouldReturnError = true

        viewModel.state.test {
            skipItems(1)

            viewModel.state.value.usernameTextState.setTextAndPlaceCursorAtEnd("valid_user")
            viewModel.state.value.displayNameTextState.setTextAndPlaceCursorAtEnd("Valid Name")

            // Pick an image so the submit function tries to upload it
            viewModel.onAction(ProfileSetupAction.OnImagePicked("local/path.jpg", "image/jpeg"))

            advanceTimeBy(501) // Clear the username debounce

            viewModel.onAction(ProfileSetupAction.OnSubmitClick)

            val finalState = expectMostRecentItem()
            assertThat(finalState.isSubmitting).isFalse() // Submission aborted
            assertThat(finalState.imageError).isNotNull() // Error surfaced to UI
        }
    }

    @Test
    fun `successful profile submit emits Success event`() = runTest {
        val viewModel = createViewModel()
        userService.isAvailable = true

        viewModel.events.test {
            viewModel.state.value.usernameTextState.setTextAndPlaceCursorAtEnd("valid_username")
            viewModel.state.value.displayNameTextState.setTextAndPlaceCursorAtEnd("Valid Name")

            advanceTimeBy(501) // Clear debounce

            viewModel.onAction(ProfileSetupAction.OnSubmitClick)

            val event = awaitItem()
            assertThat(event).isEqualTo(ProfileSetupEvent.Success)
        }
    }
}