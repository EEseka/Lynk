package com.eeseka.lynk.shared.design_system.components.buttons

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.util.PlatformUtils.isIOS
import com.slapps.cupertino.CupertinoButton
import com.slapps.cupertino.CupertinoButtonDefaults
import com.slapps.cupertino.ExperimentalCupertinoApi

@OptIn(ExperimentalCupertinoApi::class)
@Composable
fun LynkFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    if (isIOS()) {
        CupertinoButton(
            onClick = onClick,
            modifier = modifier.size(56.dp),
            enabled = enabled,
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            colors = CupertinoButtonDefaults.filledButtonColors(
                containerColor = LynkTheme.colors.primary,
                contentColor = LynkTheme.colors.onPrimary,
                disabledContainerColor = LynkTheme.colors.onSurface.copy(alpha = 0.12f),
                disabledContentColor = LynkTheme.colors.onSurface.copy(alpha = 0.38f)
            )
        ) {
            content()
        }
    } else {
        FloatingActionButton(
            onClick = { if (enabled) onClick() },
            modifier = modifier,
            shape = CircleShape,
            containerColor = if (enabled) LynkTheme.colors.primary
                else LynkTheme.colors.onSurface.copy(alpha = 0.12f),
            contentColor = if (enabled) LynkTheme.colors.onPrimary
                else LynkTheme.colors.onSurface.copy(alpha = 0.38f),
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            )
        ) {
            content()
        }
    }
}