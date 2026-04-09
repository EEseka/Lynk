package com.eeseka.lynk

import androidx.compose.ui.window.ComposeUIViewController
import com.eeseka.lynk.di.initKoin
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {

    // Initialize Google Auth FIRST (Runs once when the iOS app starts)
    GoogleAuthProvider.create(
        credentials = GoogleAuthCredentials(serverId = AppConfig.WEB_CLIENT_ID)
    )

    // Return the UI
    return ComposeUIViewController(
        configure = {
            initKoin()
        }
    ) {
        App()
    }
}