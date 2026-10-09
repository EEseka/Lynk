package com.eeseka.lynk.shared.design_system.components.date_and_time

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.presentation.util.toPickerMillis
import kotlinx.datetime.LocalDate

@Composable
expect fun LynkDatePickerDialog(
    onDateSelected: (Long) -> Unit,
    onDismissRequest: () -> Unit,
    initialSelectedDateMillis: Long? = null
)

@PreviewLightDark
@Composable
private fun LynkDatePickerDialogPreview() {
    LynkTheme {
        LynkDatePickerDialog(
            onDateSelected = {},
            onDismissRequest = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun LynkDatePickerDialogEditingPreview() {
    LynkTheme {
        LynkDatePickerDialog(
            onDateSelected = {},
            onDismissRequest = {},
            initialSelectedDateMillis = LocalDate(2026, 10, 24).toPickerMillis()
        )
    }
}
