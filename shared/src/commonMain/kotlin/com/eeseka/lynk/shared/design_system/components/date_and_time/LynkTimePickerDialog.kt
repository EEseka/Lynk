package com.eeseka.lynk.shared.design_system.components.date_and_time

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.eeseka.lynk.shared.design_system.theme.LynkTheme

@Composable
expect fun LynkTimePickerDialog(
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    onDismissRequest: () -> Unit,
    initialHour: Int? = null,
    initialMinute: Int? = null
)

@PreviewLightDark
@Composable
private fun LynkTimePickerDialogPreview() {
    LynkTheme {
        LynkTimePickerDialog(
            onTimeSelected = { _, _ -> },
            onDismissRequest = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun LynkTimePickerDialogEditingPreview() {
    LynkTheme {
        LynkTimePickerDialog(
            onTimeSelected = { _, _ -> },
            onDismissRequest = {},
            initialHour = 20,
            initialMinute = 30
        )
    }
}
