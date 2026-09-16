package com.eeseka.lynk.profile.presentation.profile

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
import com.eeseka.lynk.shared.domain.auth.model.AuthInfo
import com.eeseka.lynk.shared.domain.auth.model.AuthProvider
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStats
import com.eeseka.lynk.shared.domain.settings.AppTheme
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.testing.collectInBackground
import com.eeseka.lynk.testing.data.FakeAppPreferences
import com.eeseka.lynk.testing.data.FakeAuthService
import com.eeseka.lynk.testing.data.FakeDeviceTokenService
import com.eeseka.lynk.testing.data.FakeHangoutService
import com.eeseka.lynk.testing.data.FakeImageCompressionService
import com.eeseka.lynk.testing.data.FakePushNotificationService
import com.eeseka.lynk.testing.data.FakeSessionStorage
import com.eeseka.lynk.testing.data.FakeUserService
import com.eeseka.lynk.testing.typeText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
class ProfileViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var userService: FakeUserService
    private lateinit var hangoutService: FakeHangoutService
    private lateinit var authService: FakeAuthService
    private lateinit var sessionStorage: FakeSessionStorage
    private lateinit var appPreferences: FakeAppPreferences
    private lateinit var imageCompressor: FakeImageCompressionService
    private lateinit var deviceTokenService: FakeDeviceTokenService
    private lateinit var pushNotificationService: FakePushNotificationService

    private val ada = User.Authenticated(
        id = "user_1",
        provider = AuthProvider.GOOGLE,
        email = "ada@test.com",
        displayName = "Ada",
        username = "ada"
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        userService = FakeUserService()
        hangoutService = FakeHangoutService()
        authService = FakeAuthService()
        sessionStorage = FakeSessionStorage()
        appPreferences = FakeAppPreferences()
        imageCompressor = FakeImageCompressionService()
        deviceTokenService = FakeDeviceTokenService()
        pushNotificationService = FakePushNotificationService()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `a signed in user sees their profile and hangout counts`() = runTest {
        hangoutService.statsToReturn = HangoutStats(hostedCount = 5, attendedCount = 3)
        val viewModel = createViewModel(signedInAs = ada)

        val state = viewModel.state.value
        assertThat(state.isGuest).isFalse()
        assertThat(state.username).isEqualTo("ada")
        assertThat(state.email).isEqualTo("ada@test.com")
        assertThat(state.displayNameTextState.text.toString()).isEqualTo("Ada")
        assertThat(state.hostedCount).isEqualTo(5L)
        assertThat(state.attendedCount).isEqualTo(3L)
        assertThat(state.isStatsLoading).isFalse()
    }

    @Test
    fun `a newer copy from the server replaces the saved one`() = runTest {
        userService.currentUser = ada.copy(displayName = "Ada Obi")
        val viewModel = createViewModel(signedInAs = ada)

        assertThat(viewModel.state.value.displayNameTextState.text.toString()).isEqualTo("Ada Obi")
        assertThat((sessionStorage.get()?.user as User.Authenticated).displayName).isEqualTo("Ada Obi")
        assertThat(viewModel.state.value.canSave).isFalse()
    }

    @Test
    fun `a guest sees a guest profile`() = runTest {
        val viewModel = createViewModel(signedInAs = User.Guest(id = "guest_1"))

        assertThat(viewModel.state.value.isGuest).isTrue()
        assertThat(viewModel.state.value.userId).isEqualTo("guest_1")
    }

    @Test
    fun `hangout counts that fail to load stay at zero without a message`() = runTest {
        hangoutService.shouldReturnError = true

        val viewModel = createViewModel(signedInAs = ada)

        viewModel.events.test {
            assertThat(viewModel.state.value.hostedCount).isEqualTo(0L)
            assertThat(viewModel.state.value.isStatsLoading).isFalse()
            expectNoEvents()
        }
    }

    @Test
    fun `save only turns on once the name really changes`() = runTest {
        val viewModel = createViewModel(signedInAs = ada)
        assertThat(viewModel.state.value.canSave).isFalse()

        viewModel.state.value.displayNameTextState.typeText("Ada Obi")
        advanceUntilIdle()
        assertThat(viewModel.state.value.canSave).isTrue()

        viewModel.state.value.displayNameTextState.typeText("Ada ")
        advanceUntilIdle()
        assertThat(viewModel.state.value.canSave).isFalse()
    }

    @Test
    fun `saving a new name updates the session and confirms it`() = runTest {
        val viewModel = createViewModel(signedInAs = ada)
        viewModel.state.value.displayNameTextState.typeText("Ada Obi")
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(ProfileAction.OnSaveClick)
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(ProfileEvent.ProfileSaved)
            assertThat((sessionStorage.get()?.user as User.Authenticated).displayName).isEqualTo("Ada Obi")
            assertThat(viewModel.state.value.canSave).isFalse()
            assertThat(viewModel.state.value.isSaving).isFalse()
        }
    }

    @Test
    fun `saving a blank name shows an error and sends nothing`() = runTest {
        val viewModel = createViewModel(signedInAs = ada)
        viewModel.state.value.displayNameTextState.typeText("   ")
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(ProfileAction.OnSaveClick)
            advanceUntilIdle()

            assertThat(viewModel.state.value.displayNameError).isNotNull()
            assertThat(userService.currentUser.displayName).isEqualTo("Ada")
            expectNoEvents()
        }
    }

    @Test
    fun `saving a new photo uploads it first`() = runTest {
        val viewModel = createViewModel(signedInAs = ada)
        viewModel.onAction(ProfileAction.OnImagePicked("local/photo.jpg", "image/jpeg"))
        advanceUntilIdle()
        assertThat(viewModel.state.value.canSave).isTrue()

        viewModel.events.test {
            viewModel.onAction(ProfileAction.OnSaveClick)
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(ProfileEvent.ProfileSaved)
            assertThat(viewModel.state.value.profilePictureUrl).isEqualTo(userService.publicUrl)
            assertThat(viewModel.state.value.localPhotoUri).isNull()
            assertThat(userService.currentUser.profilePictureUrl).isEqualTo(userService.publicUrl)
        }
    }

    @Test
    fun `a photo that cannot be read stops the save`() = runTest {
        imageCompressor.shouldFailRead = true
        val viewModel = createViewModel(signedInAs = ada)
        viewModel.onAction(ProfileAction.OnImagePicked("local/photo.jpg", "image/jpeg"))
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.onAction(ProfileAction.OnSaveClick)
            advanceUntilIdle()

            assertThat(viewModel.state.value.imageError).isNotNull()
            assertThat(viewModel.state.value.isSaving).isFalse()
            expectNoEvents()
        }
    }

    @Test
    fun `a save the server refuses sends an error`() = runTest {
        val viewModel = createViewModel(signedInAs = ada)
        viewModel.state.value.displayNameTextState.typeText("Ada Obi")
        advanceUntilIdle()
        userService.shouldReturnError = true

        viewModel.events.test {
            viewModel.onAction(ProfileAction.OnSaveClick)
            advanceUntilIdle()

            assertThat(awaitItem()).isInstanceOf(ProfileEvent.Error::class)
            assertThat(viewModel.state.value.isSaving).isFalse()
        }
    }

    @Test
    fun `removing the photo clears it and allows saving`() = runTest {
        val viewModel = createViewModel(signedInAs = ada.copy(profilePictureUrl = "https://photo.jpg"))

        viewModel.onAction(ProfileAction.OnRemoveImageClick)
        advanceUntilIdle()

        assertThat(viewModel.state.value.profilePictureUrl).isNull()
        assertThat(viewModel.state.value.canSave).isTrue()
    }

    @Test
    fun `choosing a theme saves it`() = runTest {
        val viewModel = createViewModel(signedInAs = ada)

        viewModel.onAction(ProfileAction.OnThemeSelected(AppTheme.DARK))
        advanceUntilIdle()

        assertThat(appPreferences.theme.first()).isEqualTo(AppTheme.DARK)
        assertThat(viewModel.state.value.appTheme).isEqualTo(AppTheme.DARK)
    }

    @Test
    fun `turning push notifications off saves it`() = runTest {
        val viewModel = createViewModel(signedInAs = ada)

        viewModel.onAction(ProfileAction.OnPushNotificationsToggled(isEnabled = false))
        advanceUntilIdle()

        assertThat(viewModel.state.value.arePushNotificationsEnabled).isFalse()
    }

    @Test
    fun `the photo and the settings sheet open and close`() = runTest {
        val viewModel = createViewModel(signedInAs = ada)

        viewModel.onAction(ProfileAction.OnImageClick)
        assertThat(viewModel.state.value.showFullScreenImage).isTrue()
        viewModel.onAction(ProfileAction.OnDismissFullScreenImage)
        assertThat(viewModel.state.value.showFullScreenImage).isFalse()

        viewModel.onAction(ProfileAction.OnSettingsClick)
        assertThat(viewModel.state.value.showSettingsSheet).isTrue()
        viewModel.onAction(ProfileAction.OnDismissSettings)
        assertThat(viewModel.state.value.showSettingsSheet).isFalse()
    }

    @Test
    fun `a guest tapping create account gives up the guest session`() = runTest {
        val viewModel = createViewModel(signedInAs = User.Guest(id = "guest_1"))

        viewModel.onAction(ProfileAction.OnCreateAccountClick)
        advanceUntilIdle()

        assertThat(sessionStorage.get()).isNull()
        assertThat(viewModel.state.value.showSettingsSheet).isFalse()
        assertThat(viewModel.state.value.isDeletingAccount).isFalse()
    }

    @Test
    fun `backing out of a confirmation changes nothing`() = runTest {
        val viewModel = createViewModel(signedInAs = ada)

        viewModel.onAction(ProfileAction.OnSignOutClick)
        viewModel.onAction(ProfileAction.OnDismissSignOutConfirmation)
        assertThat(viewModel.state.value.showSignOutConfirmation).isFalse()

        viewModel.onAction(ProfileAction.OnDeleteAccountClick)
        viewModel.onAction(ProfileAction.OnDismissDeleteAccountConfirmation)
        assertThat(viewModel.state.value.showDeleteAccountConfirmation).isFalse()

        assertThat(sessionStorage.get()).isNotNull()
    }

    @Test
    fun `signing out as a user asks for confirmation first`() = runTest {
        val viewModel = createViewModel(signedInAs = ada)

        viewModel.onAction(ProfileAction.OnSignOutClick)

        assertThat(viewModel.state.value.showSignOutConfirmation).isTrue()
        assertThat(sessionStorage.get()).isNotNull()
    }

    @Test
    fun `confirming sign out unregisters this phone and clears the session`() = runTest {
        pushNotificationService.deviceToken.value = "push_token_1"
        val viewModel = createViewModel(signedInAs = ada)

        viewModel.onAction(ProfileAction.OnConfirmSignOut)
        advanceUntilIdle()

        assertThat(deviceTokenService.unregisteredTokens).containsExactly("push_token_1")
        assertThat(sessionStorage.get()).isNull()
        assertThat(viewModel.state.value.isSigningOut).isFalse()
    }

    @Test
    fun `a logout the server refuses still clears the session and says so`() = runTest {
        val viewModel = createViewModel(signedInAs = ada)
        authService.shouldReturnError = true

        viewModel.events.test {
            viewModel.onAction(ProfileAction.OnConfirmSignOut)
            advanceUntilIdle()

            assertThat(awaitItem()).isInstanceOf(ProfileEvent.Error::class)
            assertThat(sessionStorage.get()).isNull()
        }
    }

    @Test
    fun `a guest signing out removes the guest account without asking`() = runTest {
        val viewModel = createViewModel(signedInAs = User.Guest(id = "guest_1"))

        viewModel.onAction(ProfileAction.OnSignOutClick)
        advanceUntilIdle()

        assertThat(viewModel.state.value.showSignOutConfirmation).isFalse()
        assertThat(sessionStorage.get()).isNull()
        assertThat(deviceTokenService.unregisteredTokens).isEmpty()
    }

    @Test
    fun `deleting an account clears the session`() = runTest {
        val viewModel = createViewModel(signedInAs = ada)
        viewModel.onAction(ProfileAction.OnDeleteAccountClick)
        assertThat(viewModel.state.value.showDeleteAccountConfirmation).isTrue()

        viewModel.onAction(ProfileAction.OnConfirmDeleteAccount)
        advanceUntilIdle()

        assertThat(sessionStorage.get()).isNull()
        assertThat(viewModel.state.value.isDeletingAccount).isFalse()
        assertThat(viewModel.state.value.showDeleteAccountConfirmation).isFalse()
    }

    @Test
    fun `an account that is already gone still signs out quietly`() = runTest {
        val viewModel = createViewModel(signedInAs = ada)
        authService.shouldReturnError = true
        authService.errorToReturn = DataError.Remote.NOT_FOUND

        viewModel.events.test {
            viewModel.onAction(ProfileAction.OnConfirmDeleteAccount)
            advanceUntilIdle()

            assertThat(sessionStorage.get()).isNull()
            expectNoEvents()
        }
    }

    @Test
    fun `an account still in an active hangout is not deleted`() = runTest {
        val viewModel = createViewModel(signedInAs = ada)
        authService.shouldReturnError = true
        authService.errorToReturn = DataError.Remote.CONFLICT

        viewModel.events.test {
            viewModel.onAction(ProfileAction.OnConfirmDeleteAccount)
            advanceUntilIdle()

            assertThat(awaitItem()).isInstanceOf(ProfileEvent.Error::class)
            assertThat(sessionStorage.get()).isNotNull()
            assertThat(viewModel.state.value.isDeletingAccount).isFalse()
        }
    }

    private suspend fun TestScope.createViewModel(signedInAs: User): ProfileViewModel {
        sessionStorage.set(AuthInfo(accessToken = "access", refreshToken = "refresh", user = signedInAs))
        // The server's copy starts as the signed-in user, unless the test already gave it a newer one
        if (signedInAs is User.Authenticated && userService.currentUser.id != signedInAs.id) {
            userService.currentUser = signedInAs
        }

        val viewModel = ProfileViewModel(
            userService = userService,
            hangoutService = hangoutService,
            authService = authService,
            sessionStorage = sessionStorage,
            appPreferences = appPreferences,
            imageCompressor = imageCompressor,
            deviceTokenService = deviceTokenService,
            pushNotificationService = pushNotificationService
        )
        collectInBackground(viewModel.state)
        advanceUntilIdle()
        return viewModel
    }
}
