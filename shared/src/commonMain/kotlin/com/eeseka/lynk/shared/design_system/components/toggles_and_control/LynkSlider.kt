package com.eeseka.lynk.shared.design_system.components.toggles_and_control

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.mohamedrejeb.calf.ui.slider.AdaptiveSlider

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
    AdaptiveSlider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        onValueChangeFinished = onValueChangeFinished
    )
}

@Preview
@Composable
private fun LynkSliderPreview() {
    LynkTheme {
        LynkSlider(
            value = 0.5f,
            onValueChange = {}
        )
    }
}

@Preview
@Composable
private fun LynkSliderPreviewDark() {
    LynkTheme(true) {
        LynkSlider(
            value = 0.5f,
            onValueChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LynkDisabledSliderPreview() {
    LynkTheme {
        LynkSlider(
            value = 0.5f,
            onValueChange = {},
            enabled = false
        )
    }
}

@Preview
@Composable
private fun LynkDisabledSliderPreviewDark() {
    LynkTheme(true) {
        LynkSlider(
            value = 0.5f,
            onValueChange = {},
            enabled = false
        )
    }
}