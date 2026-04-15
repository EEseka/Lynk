package com.eeseka.lynk.profile_setup.presentation

import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
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
import kotlinx.coroutines.test.advanceUntilIdle
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
                    id = "1",
                    email = "test@test.com",
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
            viewModel.onAction(ProfileSetupAction.OnImagePicked("local/path.jpg", "image/jpeg"))

            advanceUntilIdle()

            val state = expectMostRecentItem()
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

            viewModel.state.value.usernameTextState.setTextAndPlaceCursorAtEnd("valid_user")

            // Fast-forward virtual time to bypass the 500ms debounce
            advanceTimeBy(501)
            advanceUntilIdle()

            val finalState = expectMostRecentItem()
            assertThat(finalState.isCheckingUsername).isFalse()
            assertThat(finalState.isUsernameAvailable).isEqualTo(true)
            assertThat(finalState.usernameError).isNull()
        }
    }

    @Test
    fun `taken username sets error and blocks submission`() = runTest {
        val viewModel = createViewModel()
        userService.isAvailable = false

        viewModel.state.test {
            viewModel.state.value.displayNameTextState.setTextAndPlaceCursorAtEnd("Valid Name")
            viewModel.state.value.usernameTextState.setTextAndPlaceCursorAtEnd("taken_user")

            advanceTimeBy(501)
            advanceUntilIdle()

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
            viewModel.state.value.usernameTextState.setTextAndPlaceCursorAtEnd("valid_user")
            viewModel.state.value.displayNameTextState.setTextAndPlaceCursorAtEnd("   ")

            advanceTimeBy(501)
            advanceUntilIdle()

            viewModel.onAction(ProfileSetupAction.OnSubmitClick)
            advanceUntilIdle()

            val finalState = expectMostRecentItem()
            assertThat(finalState.canSubmit).isFalse()
            assertThat(finalState.displayNameError).isNotNull()
            assertThat(finalState.isSubmitting).isFalse()
        }
    }

    @Test
    fun `image upload failure aborts submission and emits error`() = runTest {
        val viewModel = createViewModel()
        userService.shouldReturnError = true

        viewModel.state.test {
            viewModel.state.value.usernameTextState.setTextAndPlaceCursorAtEnd("valid_user")
            viewModel.state.value.displayNameTextState.setTextAndPlaceCursorAtEnd("Valid Name")

            viewModel.onAction(ProfileSetupAction.OnImagePicked("local/path.jpg", "image/jpeg"))

            advanceTimeBy(501)
            advanceUntilIdle()

            viewModel.onAction(ProfileSetupAction.OnSubmitClick)
            advanceUntilIdle()

            val finalState = expectMostRecentItem()
            assertThat(finalState.isSubmitting).isFalse()
            assertThat(finalState.imageError).isNotNull()
        }
    }

    @Test
    fun `successful profile submit emits Success event`() = runTest {
        val viewModel = createViewModel()
        userService.isAvailable = true

        viewModel.events.test {
            viewModel.state.value.usernameTextState.setTextAndPlaceCursorAtEnd("valid_username")
            viewModel.state.value.displayNameTextState.setTextAndPlaceCursorAtEnd("Valid Name")

            advanceTimeBy(501)
            advanceUntilIdle()

            viewModel.onAction(ProfileSetupAction.OnSubmitClick)

            val event = awaitItem()
            assertThat(event).isEqualTo(ProfileSetupEvent.Success)
        }
    }

    @Test
    fun `removing picked image clears state`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            viewModel.onAction(ProfileSetupAction.OnImagePicked("local/path.jpg", "image/jpeg"))
            advanceUntilIdle()

            assertThat(expectMostRecentItem().localPhotoUri).isEqualTo("local/path.jpg")

            viewModel.onAction(ProfileSetupAction.OnRemoveImageClick)
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertThat(state.localPhotoUri).isNull()
            assertThat(state.localPhotoMimeType).isNull()
        }
    }

    @Test
    fun `loadInitialData populates state with existing user info`() = runTest {
        val authInfo = AuthInfo(
            accessToken = "token",
            refreshToken = "refresh",
            user = User.ProfileIncomplete(
                id = "1", email = "test@test.com",
                provider = AuthProvider.GOOGLE,
                displayName = "Existing Name",
                profilePictureUrl = "https://existing.url/photo.jpg"
            )
        )
        sessionStorage.set(authInfo)

        val viewModel = ProfileSetupViewModel(userService, sessionStorage, imageCompressor)

        viewModel.state.test {
            advanceUntilIdle()

            val state = expectMostRecentItem()
            assertThat(state.displayNameTextState.text.toString()).isEqualTo("Existing Name")
            assertThat(state.email).isEqualTo("test@test.com")
            assertThat(state.profilePictureUrl).isEqualTo("https://existing.url/photo.jpg")
        }
    }

    @Test
    fun `submitProfile with image read failure sets error and aborts`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            viewModel.state.value.usernameTextState.setTextAndPlaceCursorAtEnd("valid_user")
            viewModel.state.value.displayNameTextState.setTextAndPlaceCursorAtEnd("Valid Name")
            viewModel.onAction(ProfileSetupAction.OnImagePicked("local/path.jpg", "image/jpeg"))

            advanceTimeBy(501)
            advanceUntilIdle()

            imageCompressor.shouldFailRead = true
            viewModel.onAction(ProfileSetupAction.OnSubmitClick)

            advanceUntilIdle()

            val finalState = expectMostRecentItem()
            assertThat(finalState.isSubmitting).isFalse()
            assertThat(finalState.imageError).isNotNull()
        }
    }

    @Test
    fun `network error during updateProfile emits Error event`() = runTest {
        val viewModel = createViewModel()
        userService.isAvailable = true

        viewModel.events.test {
            viewModel.state.value.usernameTextState.setTextAndPlaceCursorAtEnd("valid_username")
            viewModel.state.value.displayNameTextState.setTextAndPlaceCursorAtEnd("Valid Name")

            advanceTimeBy(501)
            advanceUntilIdle()

            userService.shouldReturnError = true
            viewModel.onAction(ProfileSetupAction.OnSubmitClick)

            val event = awaitItem()
            assertThat(event).isInstanceOf(ProfileSetupEvent.Error::class)
        }
    }
}