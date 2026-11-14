package com.eeseka.lynk

import com.eeseka.lynk.core.presentation.navigation.ExternalUriHandler

/**
 * Swift can only see what the Shared framework exports, and :core is not exported on
 * its own. This is the AppDelegate's door into ExternalUriHandler, for a tapped push banner.
 */
object ExternalUriHandlerBridge {
    fun onNewUri(uri: String) {
        ExternalUriHandler.onNewUri(uri)
    }
}