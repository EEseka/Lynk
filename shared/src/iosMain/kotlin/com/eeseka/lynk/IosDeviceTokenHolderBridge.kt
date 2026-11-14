package com.eeseka.lynk

import com.eeseka.lynk.core.data.notification.IosDeviceTokenHolder

/**
 * Swift can only see what the Shared framework exports, and :core is not exported
 * on its own. This is the AppDelegate's door into IosDeviceTokenHolder.
 */
object IosDeviceTokenHolderBridge {
    fun updateToken(token: String) {
        IosDeviceTokenHolder.updateToken(token)
    }
}
