package com.eeseka.lynk.shared.domain.notification

import kotlinx.coroutines.flow.Flow

interface PushNotificationService {
    fun observeDeviceToken(): Flow<String?>
}