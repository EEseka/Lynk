package com.eeseka.lynk.hangouts.presentation.hangout_detail.payments.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eeseka.lynk.shared.design_system.components.date_and_time.LynkDatePicker
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkAdaptiveSheet
import com.eeseka.lynk.shared.presentation.util.toPickerDate
import kotlinx.datetime.LocalDate

@Composable
fun PaymentDeadlinePickerSheet(
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LynkAdaptiveSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        LynkDatePicker(
            onDateSelected = { millis ->
                if (millis == null) {
                    onDismiss()
                } else {
                    onDateSelected(millis.toPickerDate())
                }
            }
        )
    }
}
