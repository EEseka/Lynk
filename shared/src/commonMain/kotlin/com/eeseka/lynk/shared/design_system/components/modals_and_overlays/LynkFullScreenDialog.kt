package com.eeseka.lynk.shared.design_system.components.modals_and_overlays

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// Covers the whole screen, under the status and navigation bars too, for viewing photos
@Composable
fun LynkFullScreenDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = fullScreenDialogProperties()
    ) {
        LightSystemBarIcons()
        content()
    }
}

internal expect fun fullScreenDialogProperties(): DialogProperties

// The dialog is always dark, so the clock and battery must stay light even in light mode
@Composable
internal expect fun LightSystemBarIcons()
