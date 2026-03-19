package com.eeseka.lynk.shared.design_system.components.modals_and_overlays

import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButtonStyle
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.util.PlatformUtils.isIOS
import com.slapps.cupertino.AlertActionStyle
import com.slapps.cupertino.CupertinoAlertDialogNative
import com.slapps.cupertino.ExperimentalCupertinoApi

@OptIn(ExperimentalCupertinoApi::class)
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
    if (isIOS()) {
        CupertinoAlertDialogNative(
            onDismissRequest = onDismissRequest,
            title = title,
            message = message
        ) {
            if (dismissText != null) {
                action(
                    onClick = {
                        onDismiss?.invoke()
                        onDismissRequest()
                    },
                    style = AlertActionStyle.Cancel,
                    title = dismissText
                )
            }

            action(
                onClick = {
                    onConfirm()
                    onDismissRequest()
                },
                style = if (isDestructive) AlertActionStyle.Destructive else AlertActionStyle.Default,
                title = confirmText
            )
        }
    } else {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            containerColor = LynkTheme.colors.surface,
            titleContentColor = LynkTheme.colors.textMain,
            textContentColor = LynkTheme.colors.textMuted,
            title = {
                LynkText(text = title, style = LynkTheme.LynkTypography.titleLarge)
            },
            text = {
                LynkText(text = message, style = LynkTheme.LynkTypography.bodyMedium)
            },
            confirmButton = {
                LynkButton(
                    text = confirmText,
                    onClick = {
                        onConfirm()
                        onDismissRequest()
                    },
                    style = if (isDestructive) LynkButtonStyle.DESTRUCTIVE_SECONDARY else LynkButtonStyle.PRIMARY
                )
            },
            dismissButton = if (dismissText != null) {
                {
                    LynkButton(
                        text = dismissText,
                        onClick = {
                            onDismiss?.invoke()
                            onDismissRequest()
                        },
                        style = LynkButtonStyle.TEXT
                    )
                }
            } else null
        )
    }
}