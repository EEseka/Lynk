package com.eeseka.lynk.shared.design_system.components.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Locate
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.mohamedrejeb.calf.ui.button.AdaptiveButton
import com.mohamedrejeb.calf.ui.button.LiquidGlassButtonColors

@Composable
fun LynkTonalIconButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    AdaptiveButton(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = scheme.onSurface.copy(alpha = 0.1f),
            disabledContentColor = scheme.onSurfaceVariant.copy(alpha = 0.38f)
        ),
        liquidGlassColors = LiquidGlassButtonColors(
            tintColor = containerColor,
            surfaceColor = containerColor,
            contentColor = contentColor,
            disabledContentColor = scheme.onSurfaceVariant.copy(alpha = 0.38f)
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        content()
    }
}

@PreviewLightDark
@Composable
private fun LynkTonalIconButtonPreview() {
    LynkTheme {
        LynkTonalIconButton(
            onClick = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {
            Icon(
                imageVector = Lucide.Locate,
                contentDescription = null
            )
        }
    }
}