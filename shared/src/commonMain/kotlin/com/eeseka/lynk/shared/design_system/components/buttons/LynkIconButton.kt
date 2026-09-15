package com.eeseka.lynk.shared.design_system.components.buttons

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.mohamedrejeb.calf.ui.button.AdaptiveIconButton

@Composable
fun LynkIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    AdaptiveIconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        content()
    }
}

@PreviewLightDark
@Composable
private fun LynkIconButtonPreview() {
    LynkTheme {
        LynkIconButton(
            onClick = {}
        ) {
            Icon(
                imageVector = Lucide.Info,
                contentDescription = null
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun LynkDisabledIconButtonPreview() {
    LynkTheme {
        LynkIconButton(
            onClick = {},
            enabled = false
        ) {
            Icon(
                imageVector = Lucide.Info,
                contentDescription = null
            )
        }
    }
}