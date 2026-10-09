package com.eeseka.lynk.shared.design_system.components.date_and_time

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TimePickerDialogDefaults
import androidx.compose.material3.TimePickerDisplayMode
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.cancel
import lynk.shared.generated.resources.confirm_time
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun LynkTimePickerDialog(
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    onDismissRequest: () -> Unit,
    initialHour: Int?,
    initialMinute: Int?
) {
    // Material follows the phone's 12 or 24 hour setting on its own
    val state = rememberTimePickerState(
        initialHour = initialHour ?: 0,
        initialMinute = initialMinute ?: 0
    )

    TimePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = { onTimeSelected(state.hour, state.minute) }) {
                LynkText(text = stringResource(Res.string.confirm_time))
            }
        },
        title = { TimePickerDialogDefaults.Title(displayMode = TimePickerDisplayMode.Picker) },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                LynkText(text = stringResource(Res.string.cancel))
            }
        }
    ) {
        TimePicker(
            state = state,
            modifier = Modifier.verticalScroll(rememberScrollState())
        )
    }
}
