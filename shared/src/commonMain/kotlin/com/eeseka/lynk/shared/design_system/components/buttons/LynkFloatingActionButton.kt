package com.eeseka.lynk.shared.design_system.components.buttons

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.mohamedrejeb.calf.ui.button.AdaptiveButton
import com.mohamedrejeb.calf.ui.button.LiquidGlassButtonColors

@Composable
fun LynkFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    AdaptiveButton(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = scheme.primaryContainer,
            contentColor = scheme.onPrimaryContainer,
            disabledContainerColor = scheme.onSurface.copy(alpha = 0.1f),
            disabledContentColor = scheme.onSurfaceVariant.copy(alpha = 0.38f)
        ),
        liquidGlassColors = LiquidGlassButtonColors(
            tintColor = scheme.primaryContainer,
            surfaceColor = scheme.primaryContainer,
            contentColor = scheme.onPrimaryContainer,
            disabledContentColor = scheme.onSurfaceVariant.copy(alpha = 0.38f)
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        content()
    }
}

@Preview
@Composable
private fun LynkFloatingActionButtonPreview() {
    LynkTheme {
        LynkFloatingActionButton(
            onClick = {}
        ) {
            Icon(
                imageVector = Lucide.Plus,
                contentDescription = null
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LynkDisabledFloatingActionButtonPreview() {
    LynkTheme {
        LynkFloatingActionButton(
            onClick = {},
            enabled = false
        ) {
            Icon(
                imageVector = Lucide.Plus,
                contentDescription = null
            )
        }
    }
}

@Preview
@Composable
private fun LynkDisabledFloatingActionButtonPreviewDark() {
    LynkTheme(true) {
        LynkFloatingActionButton(
            onClick = {},
            enabled = false
        ) {
            Icon(
                imageVector = Lucide.Plus,
                contentDescription = null
            )
        }
    }
}