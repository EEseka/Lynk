package com.eeseka.lynk.hangouts.presentation.hangout_detail.payments.components

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.core.net.toUri

@Composable
actual fun PaymentCheckout(
    url: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    // Survives the screen being rebuilt while the tab is open, so coming back never opens Paystack a second time
    var hasOpenedTab by rememberSaveable(url) { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner, url) {
        // Rebuilt after the tab was opened means the person is already back from it
        if (hasOpenedTab) {
            currentOnDismiss()
            return@DisposableEffect onDispose { }
        }

        // A Custom Tab tells nobody when it closes; Lynk coming back to the front after hiding behind it is the sign
        var isBehindTab = false
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> isBehindTab = true
                Lifecycle.Event.ON_RESUME -> if (isBehindTab) currentOnDismiss()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        hasOpenedTab = true
        CustomTabsIntent.Builder().build().launchUrl(context, url.toUri())

        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
