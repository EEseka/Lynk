package com.eeseka.lynk.shared.data.notification

import com.eeseka.lynk.shared.domain.notification.PushNotificationService
import kotlinx.coroutines.flow.Flow

expect class FirebasePushNotificationService : PushNotificationService {
    override fun observeDeviceToken(): Flow<String?>
}
