package com.eeseka.lynk.shared.data.notification

import com.eeseka.lynk.shared.domain.auth.SessionStorage
import com.eeseka.lynk.shared.domain.notification.DeviceTokenService
import com.eeseka.lynk.shared.domain.notification.model.DevicePlatform
import com.eeseka.lynk.shared.domain.settings.AppPreferences
import com.google.firebase.messaging.FirebaseMessagingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class LynkFirebaseMessagingService : FirebaseMessagingService() {

    private val deviceTokenService by inject<DeviceTokenService>()
    private val sessionStorage by inject<SessionStorage>()
    private val appPreferences by inject<AppPreferences>()
    private val applicationScope by inject<CoroutineScope>()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        applicationScope.launch {
            val isSignedIn = sessionStorage.observeAuthInfo().first() != null
            val isEnabled = appPreferences.arePushNotificationsEnabled.first()

            if (isSignedIn && isEnabled) {
                deviceTokenService.registerToken(
                    token = token,
                    platform = DevicePlatform.ANDROID
                )
            }
        }
    }
}
