package com.eeseka.lynk.shared.design_system.components.modals_and_overlays

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
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