package com.eeseka.lynk

import com.eeseka.lynk.core.domain.notification.UnreadNotificationCounter
import org.koin.mp.KoinPlatform

/**
 * Swift can only see what the Shared framework exports, and :core is not exported on
 * its own. This is the AppDelegate's door into the unread counter, for a push that arrives
 * while the app is open: iOS shows the banner itself, but nothing else tells the badge.
 */
object UnreadNotificationCounterBridge {
    fun onPushReceivedInForeground() {
        KoinPlatform.getKoin().get<UnreadNotificationCounter>().increment()
    }
}
