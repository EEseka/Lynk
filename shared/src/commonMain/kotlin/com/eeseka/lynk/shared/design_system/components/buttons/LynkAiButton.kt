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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Sparkles
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.design_system.theme.extended
import com.mohamedrejeb.calf.ui.button.AdaptiveButton
import com.mohamedrejeb.calf.ui.button.LiquidGlassButtonColors
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.ask_lynk_ai
import org.jetbrains.compose.resources.stringResource

@Composable
fun LynkAiButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val scheme = MaterialTheme.colorScheme
    val extendedColors = scheme.extended

    AdaptiveButton(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = extendedColors.violetContainer,
            contentColor = extendedColors.onVioletContainer,
            disabledContainerColor = scheme.onSurface.copy(alpha = 0.1f),
            disabledContentColor = scheme.onSurfaceVariant.copy(alpha = 0.38f)
        ),
        liquidGlassColors = LiquidGlassButtonColors(
            tintColor = extendedColors.violetContainer,
            surfaceColor = extendedColors.violetContainer,
            contentColor = extendedColors.onVioletContainer,
            disabledContentColor = scheme.onSurfaceVariant.copy(alpha = 0.38f)
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            imageVector = Lucide.Sparkles,
            contentDescription = stringResource(Res.string.ask_lynk_ai)
        )
    }
}

@PreviewLightDark
@Composable
private fun LynkAiButtonPreview() {
    LynkTheme {
        LynkAiButton(
            onClick = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        )
    }
}

