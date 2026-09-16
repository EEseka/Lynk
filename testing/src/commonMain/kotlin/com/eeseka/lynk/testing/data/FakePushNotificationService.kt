package com.eeseka.lynk.testing.data

import com.eeseka.lynk.shared.domain.notification.PushNotificationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePushNotificationService : PushNotificationService {
    // Null until the platform hands the app a push token
    val deviceToken = MutableStateFlow<String?>(null)

    override fun observeDeviceToken(): Flow<String?> = deviceToken
}
