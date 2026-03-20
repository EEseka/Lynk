package com.eeseka.lynk.shared.design_system.components.toggles_and_control

import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.util.PlatformUtils.isIOS
import com.slapps.cupertino.CupertinoSlider
import com.slapps.cupertino.CupertinoSliderDefaults
import com.slapps.cupertino.ExperimentalCupertinoApi

@OptIn(ExperimentalCupertinoApi::class)
@Composable
fun LynkSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null
) {
    if (isIOS()) {
        CupertinoSlider(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            enabled = enabled,
            valueRange = valueRange,
            steps = steps,
            onValueChangeFinished = onValueChangeFinished,
            colors = CupertinoSliderDefaults.colors(
                activeTrackColor = LynkTheme.colors.primary
            )
        )
    } else {
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier,
            enabled = enabled,
            valueRange = valueRange,
            steps = steps,
            onValueChangeFinished = onValueChangeFinished,
            colors = SliderDefaults.colors(
                thumbColor = LynkTheme.colors.primary,
                activeTrackColor = LynkTheme.colors.primary,
                inactiveTrackColor = LynkTheme.colors.surfaceContainerHigh
            )
        )
    }
}