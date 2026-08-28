package com.eeseka.lynk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.lynk.shared.domain.auth.SessionStorage
import com.eeseka.lynk.shared.domain.notification.DeviceTokenService
import com.eeseka.lynk.shared.domain.notification.PushNotificationService
import com.eeseka.lynk.shared.domain.notification.model.DevicePlatform
import com.eeseka.lynk.shared.domain.settings.AppPreferences
import com.eeseka.lynk.shared.domain.util.PlatformUtils
import com.eeseka.lynk.shared.domain.util.onSuccess
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val sessionStorage: SessionStorage,
    private val appPreferences: AppPreferences,
    private val deviceTokenService: DeviceTokenService,
    private val pushNotificationService: PushNotificationService
) : ViewModel() {

    private val eventChannel = Channel<MainEvent>()
    val events = eventChannel.receiveAsFlow()

    private var hasLoadedInitialData = false

    private val _state = MutableStateFlow(MainState())

    val state = combine(
        _state,
        appPreferences.theme,
        appPreferences.hasSeenOnboarding
    ) { authState, theme, hasSeenOnboarding ->
        authState.copy(
            theme = theme,
            hasSeenOnboarding = hasSeenOnboarding
        )
    }
        .onStart {
            if (!hasLoadedInitialData) {
                observeSessionExpiry()
                observePushRegistration()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = MainState()
        )

    private var previousRefreshToken: String? = null
    private var registeredDeviceToken: String? = null

    init {
        viewModelScope.launch {
            val authInfo = sessionStorage.observeAuthInfo().firstOrNull()
            _state.update {
                it.copy(
                    isCheckingAuth = false,
                    user = authInfo?.user
                )
            }
        }
    }

    private fun observeSessionExpiry() {
        sessionStorage
            .observeAuthInfo()
            .onEach { authInfo ->
                val currentRefreshToken = authInfo?.refreshToken
                val isSessionExpired = previousRefreshToken != null && currentRefreshToken == null
                if (isSessionExpired) {
                    sessionStorage.set(null)
                    _state.update { it.copy(user = null) }
                    eventChannel.send(MainEvent.OnSessionExpired)
                }

                previousRefreshToken = currentRefreshToken
            }
            .launchIn(viewModelScope)
    }

    private fun observePushRegistration() {
        combine(
            sessionStorage.observeAuthInfo(),
            pushNotificationService.observeDeviceToken(),
            appPreferences.arePushNotificationsEnabled
        ) { authInfo, deviceToken, arePushNotificationsEnabled ->
            syncDeviceToken(
                isSignedIn = authInfo != null,
                deviceToken = deviceToken,
                arePushNotificationsEnabled = arePushNotificationsEnabled
            )
        }.launchIn(viewModelScope)
    }

    private suspend fun syncDeviceToken(
        isSignedIn: Boolean,
        deviceToken: String?,
        arePushNotificationsEnabled: Boolean
    ) {
        if (!isSignedIn) {
            // No unregister call here on purpose — ProfileViewModel.signOut already made it,
            // on its way out, while the session was still alive. By the time we hear about
            // the sign-out the session is gone, so a deletion would only come back 401. All
            // that is left is to forget the token, so the next sign-in sends it again.
            registeredDeviceToken = null
            return
        }

        if (!arePushNotificationsEnabled) {
            registeredDeviceToken?.let { token ->
                deviceTokenService.unregisterToken(token)
                registeredDeviceToken = null
            }
            return
        }

        if (deviceToken == null || deviceToken == registeredDeviceToken) return

        deviceTokenService
            .registerToken(
                token = deviceToken,
                platform = currentDevicePlatform()
            )
            .onSuccess { registeredDeviceToken = deviceToken }
    }

    private fun currentDevicePlatform(): DevicePlatform {
        return if (PlatformUtils.isIOS()) DevicePlatform.IOS else DevicePlatform.ANDROID
    }
}