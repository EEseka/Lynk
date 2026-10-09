package com.eeseka.lynk.shared.design_system.components.date_and_time

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.presentation.util.toPickerMillis
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.cancel
import lynk.shared.generated.resources.confirm_date
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun LynkDatePickerDialog(
    onDateSelected: (Long) -> Unit,
    onDismissRequest: () -> Unit,
    initialSelectedDateMillis: Long?
) {
    val seedMillis = remember(initialSelectedDateMillis) {
        initialSelectedDateMillis ?: Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .toPickerMillis()
    }
    val state = rememberDatePickerState(initialSelectedDateMillis = seedMillis)

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedMillis = state.selectedDateMillis
                    if (selectedMillis != null) onDateSelected(selectedMillis)
                }
            ) {
                LynkText(text = stringResource(Res.string.confirm_date))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                LynkText(text = stringResource(Res.string.cancel))
            }
        }
    ) {
        DatePicker(
            state = state,
            modifier = Modifier.verticalScroll(rememberScrollState())
        )
    }
}
