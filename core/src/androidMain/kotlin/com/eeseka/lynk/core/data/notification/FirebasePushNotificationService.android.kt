package com.eeseka.lynk.core.data.notification

import com.eeseka.lynk.core.domain.logging.LynkLogger
import com.eeseka.lynk.core.domain.notification.PushNotificationService
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
        val fcmToken = try {
            Firebase.messaging.token.await().also { logger.info("Initial FCM token received") }
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            logger.error("Failed to get FCM token", e)
            null
        }
        emit(fcmToken)
    }
}
