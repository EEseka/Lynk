package com.eeseka.lynk.hangouts.presentation.hangout_detail.payments.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.uikit.LocalUIViewController
import platform.Foundation.NSURL
import platform.SafariServices.SFSafariViewController
import platform.SafariServices.SFSafariViewControllerDelegateProtocol
import platform.darwin.NSObject

@Composable
actual fun PaymentCheckout(
    url: String,
    onDismiss: () -> Unit
) {
    val viewController = LocalUIViewController.current
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val delegate = remember { SafariDoneDelegate(onDone = { currentOnDismiss() }) }

    DisposableEffect(viewController, url) {
        var presenter = viewController
        while (presenter.presentedViewController != null) {
            presenter = presenter.presentedViewController!!
        }

        val safari = SFSafariViewController(uRL = NSURL(string = url))
        safari.delegate = delegate
        presenter.presentViewController(safari, animated = true, completion = null)

        onDispose {
            if (safari.presentingViewController != null) {
                safari.dismissViewControllerAnimated(true, completion = null)
            }
        }
    }
}

private class SafariDoneDelegate(
    private val onDone: () -> Unit
) : NSObject(), SFSafariViewControllerDelegateProtocol {

    override fun safariViewControllerDidFinish(controller: SFSafariViewController) {
        onDone()
    }
}
