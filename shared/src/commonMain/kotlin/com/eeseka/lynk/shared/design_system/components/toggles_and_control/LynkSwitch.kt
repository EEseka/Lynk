package com.eeseka.lynk.shared.design_system.components.toggles_and_control

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.util.PlatformUtils.isIOS
import com.slapps.cupertino.CupertinoSwitch
import com.slapps.cupertino.CupertinoSwitchDefaults
import com.slapps.cupertino.ExperimentalCupertinoApi

@OptIn(ExperimentalCupertinoApi::class)
@Composable
fun LynkSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    if (isIOS()) {
        CupertinoSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled,
            colors = CupertinoSwitchDefaults.colors(
                checkedTrackColor = LynkTheme.colors.primary
            )
        )
    } else {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = LynkTheme.colors.onPrimary,
                checkedTrackColor = LynkTheme.colors.primary,
                uncheckedThumbColor = LynkTheme.colors.onSurfaceVariant,
                uncheckedTrackColor = LynkTheme.colors.surfaceVariant,
                disabledCheckedThumbColor = LynkTheme.colors.onPrimary.copy(alpha = 0.38f),
                disabledCheckedTrackColor = LynkTheme.colors.primary.copy(alpha = 0.38f),
                disabledUncheckedThumbColor = LynkTheme.colors.onSurfaceVariant.copy(alpha = 0.38f),
                disabledUncheckedTrackColor = LynkTheme.colors.surfaceVariant.copy(alpha = 0.38f)
            )
        )
    }
}