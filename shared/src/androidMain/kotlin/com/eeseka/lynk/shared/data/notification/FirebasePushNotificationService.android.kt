package com.eeseka.lynk.shared.data.notification

import com.eeseka.lynk.shared.domain.logging.LynkLogger
import com.eeseka.lynk.shared.domain.notification.PushNotificationService
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

actual class FirebasePushNotificationService(
    private val logger: LynkLogger
) : PushNotificationService {

    actual override fun observeDeviceToken(): Flow<String?> = flow {
        try {
            val fcmToken = Firebase.messaging.token.await()
            logger.info("Initial FCM token received: $fcmToken")
            emit(fcmToken)
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            logger.error("Failed to get FCM token", e)
            emit(null)
        }
    }
}
