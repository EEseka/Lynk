package com.eeseka.lynk.shared.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavController
import androidx.navigation.NavUri
import androidx.navigation.navOptions

@Composable
fun DeepLinkListener(navController: NavController) {
    DisposableEffect(Unit) {
        ExternalUriHandler.listener = { uri ->
            navController.navigate(
                NavUri(uri),
                navOptions { launchSingleTop = true }
            )
        }

        onDispose {
            ExternalUriHandler.listener = null
        }
    }
}
