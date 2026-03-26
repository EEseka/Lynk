package com.eeseka.lynk.shared.design_system.components.progress_indicator

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.mohamedrejeb.calf.ui.progress.AdaptiveCircularProgressIndicator

@Composable
fun LynkProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 4.dp
) {
    AdaptiveCircularProgressIndicator(
        modifier = modifier,
        color = color,
        strokeWidth = strokeWidth
    )
}

@Preview
@Composable
private fun LynkProgressIndicatorPreview() {
    LynkTheme {
        LynkProgressIndicator()
    }
}

@Preview
@Composable
private fun LynkProgressIndicatorPreviewDark() {
    LynkTheme(darkTheme = true) {
        LynkProgressIndicator()
    }
}