package com.eeseka.lynk.shared.data.notification

import android.app.PendingIntent
import android.content.Intent
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.eeseka.lynk.shared.R
import com.eeseka.lynk.shared.design_system.theme.primaryContainerLight
import com.eeseka.lynk.shared.domain.auth.SessionStorage
import com.eeseka.lynk.shared.domain.notification.DeviceTokenService
import com.eeseka.lynk.shared.domain.notification.UnreadNotificationCounter
import com.eeseka.lynk.shared.domain.notification.model.DevicePlatform
import com.eeseka.lynk.shared.domain.settings.AppPreferences
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.notification_channel_hangouts
import org.jetbrains.compose.resources.getString
import org.koin.android.ext.android.inject

class LynkFirebaseMessagingService : FirebaseMessagingService() {

    private val deviceTokenService by inject<DeviceTokenService>()
    private val sessionStorage by inject<SessionStorage>()
    private val appPreferences by inject<AppPreferences>()
    private val unreadNotificationCounter by inject<UnreadNotificationCounter>()
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

    /**
     * Only ever called while the app is open. A push that arrives in the background is drawn
     * by the system and never reaches here, so nothing below can post a second copy.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        unreadNotificationCounter.increment()

        val title = message.notification?.title ?: return
        val body = message.notification?.body.orEmpty()

        applicationScope.launch {
            showNotification(
                title = title,
                body = body,
                hangoutId = message.data[DATA_HANGOUT_ID],
                type = message.data[DATA_TYPE]
            )
        }
    }

    private suspend fun showNotification(
        title: String,
        body: String,
        hangoutId: String?,
        type: String?
    ) {
        val notificationManager = NotificationManagerCompat.from(this)
        if (!notificationManager.areNotificationsEnabled()) return

        notificationManager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_HIGH)
                .setName(getString(Res.string.notification_channel_hangouts))
                .build()
        )

        // The activity lives in the app module, which this one cannot see, so the launcher
        // component is looked up instead. The extras are the ones MainActivity already reads.
        val launchComponent = packageManager.getLaunchIntentForPackage(packageName)?.component ?: return
        val tapIntent = Intent().apply {
            component = launchComponent
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(DATA_HANGOUT_ID, hangoutId)
            putExtra(DATA_TYPE, type)
        }

        val notificationId = hangoutId?.hashCode() ?: title.hashCode()
        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_lynk)
            .setColor(primaryContainerLight.toArgb())
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private companion object {
        const val CHANNEL_ID = "lynk_hangout_updates"
        const val DATA_HANGOUT_ID = "hangoutId"
        const val DATA_TYPE = "type"
    }
}
