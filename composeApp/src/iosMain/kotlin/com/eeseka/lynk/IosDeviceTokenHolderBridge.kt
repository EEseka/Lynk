package com.eeseka.lynk

import com.eeseka.lynk.shared.data.notification.IosDeviceTokenHolder

/**
 * Swift can only see what the ComposeApp framework exports, and :shared is not exported
 * on its own. This is the AppDelegate's door into IosDeviceTokenHolder.
 */
object IosDeviceTokenHolderBridge {
    fun updateToken(token: String) {
        IosDeviceTokenHolder.updateToken(token)
    }
}
