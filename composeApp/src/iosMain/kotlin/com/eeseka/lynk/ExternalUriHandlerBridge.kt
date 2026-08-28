package com.eeseka.lynk

import com.eeseka.lynk.shared.presentation.navigation.ExternalUriHandler

/**
 * Swift can only see what the ComposeApp framework exports, and :shared is not exported on
 * its own. This is the AppDelegate's door into ExternalUriHandler, for a tapped push banner.
 */
object ExternalUriHandlerBridge {
    fun onNewUri(uri: String) {
        ExternalUriHandler.onNewUri(uri)
    }
}