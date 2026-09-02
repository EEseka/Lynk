package com.eeseka.lynk.shared.design_system.components.toggles_and_control

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.mohamedrejeb.calf.ui.toggle.AdaptiveSwitch

@Composable
fun LynkSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    AdaptiveSwitch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled
    )
}

@PreviewLightDark
@Composable
private fun LynkSwitchPreview() {
    LynkTheme {
        LynkSwitch(
            checked = true,
            onCheckedChange = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun LynkDisabledSwitchPreview() {
    LynkTheme {
        LynkSwitch(
            checked = false,
            onCheckedChange = {},
            enabled = false
        )
    }
}