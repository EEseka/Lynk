package com.eeseka.lynk.shared.design_system.components.modals_and_overlays

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.mohamedrejeb.calf.ui.dialog.AdaptiveAlertDialog
import com.mohamedrejeb.calf.ui.dialog.uikit.AlertDialogIosActionStyle

@Composable
fun LynkDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    dismissText: String? = null,
    onDismiss: (() -> Unit)? = null,
    isDestructive: Boolean = false,
) {
    AdaptiveAlertDialog(
        onConfirm = {
            onConfirm()
            onDismissRequest()
        },
        onDismiss = {
            onDismiss?.invoke()
            onDismissRequest()
        },
        confirmText = confirmText,
        dismissText = dismissText ?: "",
        title = title,
        text = message,
        modifier = modifier,
        iosConfirmButtonStyle = if (isDestructive) AlertDialogIosActionStyle.Destructive
        else AlertDialogIosActionStyle.Default,
        iosDismissButtonStyle = AlertDialogIosActionStyle.Cancel,
        materialConfirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismissRequest()
                }
            ) {
                LynkText(
                    text = confirmText,
                    color = if (isDestructive) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
            }
        },
        materialDismissButton = {
            if (dismissText != null) {
                TextButton(
                    onClick = {
                        onDismiss?.invoke()
                        onDismissRequest()
                    }
                ) {
                    LynkText(
                        text = dismissText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    )
}

@Preview
@Composable
private fun LynkDialogPreview() {
    LynkTheme {
        LynkDialog(
            onDismissRequest = {},
            title = "Enable Notifications",
            message = "Get notified when someone likes your post, follows you, or sends a message.",
            confirmText = "Enable",
            dismissText = "Not Now",
            onConfirm = {}
        )
    }
}

@Preview
@Composable
private fun LynkDialogPreviewDark() {
    LynkTheme(true) {
        LynkDialog(
            onDismissRequest = {},
            title = "Enable Notifications",
            message = "Get notified when someone likes your post, follows you, or sends a message.",
            confirmText = "Enable",
            dismissText = "Not Now",
            onConfirm = {}
        )
    }
}

@Preview
@Composable
private fun LynkDestructiveDialogPreview() {
    LynkTheme {
        LynkDialog(
            onDismissRequest = { },
            title = "Delete Account?",
            message = "This action cannot be undone. All your data will be permanently removed from Lynk.",
            confirmText = "Delete",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun LynkDestructiveDialogPreviewDark() {
    LynkTheme(true) {
        LynkDialog(
            onDismissRequest = { },
            title = "Delete Account?",
            message = "This action cannot be undone. All your data will be permanently removed from Lynk.",
            confirmText = "Delete",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {},
            onDismiss = {}
        )
    }
}