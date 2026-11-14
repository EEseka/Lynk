package com.eeseka.lynk

import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import platform.Foundation.NSProcessInfo
import platform.Foundation.iOSAppOnMac
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {

    // Initialize Google Auth FIRST (Runs once when the iOS app starts)
    GoogleAuthProvider.create(
        credentials = GoogleAuthCredentials(serverId = AppConfig.WEB_CLIENT_ID)
    )
    return ComposeUIViewController(
        configure = {
            if (NSProcessInfo.processInfo.iOSAppOnMac) {
                onFocusBehavior = OnFocusBehavior.DoNothing // A Mac reports a keyboard on first focus and never closes it
            }
        }
    ) { App() }
}
