package com.eeseka.lynk.shared.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavController
import androidx.navigation.NavUri

@Composable
fun DeepLinkListener(navController: NavController) {
    DisposableEffect(Unit) {
        ExternalUriHandler.listener = { uri ->
            navController.navigate(NavUri(uri)) {
                launchSingleTop = true
            }
        }

        onDispose {
            ExternalUriHandler.listener = null
        }
    }
}
