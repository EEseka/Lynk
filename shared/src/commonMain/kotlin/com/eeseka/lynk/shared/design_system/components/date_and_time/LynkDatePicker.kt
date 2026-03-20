package com.eeseka.lynk.shared.design_system.components.date_and_time

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButtonStyle
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkBottomSheet
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.util.PlatformUtils.isIOS
import com.slapps.cupertino.CupertinoDatePickerNative
import com.slapps.cupertino.ExperimentalCupertinoApi
import com.slapps.cupertino.rememberCupertinoDatePickerState
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.cancel
import lynk.shared.generated.resources.confirm
import lynk.shared.generated.resources.confirm_date
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCupertinoApi::class)
@Composable
fun LynkDatePicker(
    onDismissRequest: () -> Unit,
    onDateSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isIOS()) {
        val cupertinoDatePickerState = rememberCupertinoDatePickerState()

        LynkBottomSheet(
            onDismissRequest = onDismissRequest,
            modifier = modifier
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                CupertinoDatePickerNative(
                    state = cupertinoDatePickerState,
                    containerColor = LynkTheme.colors.surface,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            LynkButton(
                text = stringResource(Res.string.confirm_date),
                onClick = {
                    onDateSelected(cupertinoDatePickerState.selectedDateMillis)
                    onDismissRequest()
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                style = LynkButtonStyle.PRIMARY
            )
        }
    } else {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = onDismissRequest,
            colors = DatePickerDefaults.colors(containerColor = LynkTheme.colors.surface),
            confirmButton = {
                LynkButton(
                    text = stringResource(Res.string.confirm),
                    onClick = {
                        onDateSelected(datePickerState.selectedDateMillis)
                        onDismissRequest()
                    },
                    style = LynkButtonStyle.TEXT
                )
            },
            dismissButton = {
                LynkButton(
                    text = stringResource(Res.string.cancel),
                    onClick = onDismissRequest,
                    style = LynkButtonStyle.TEXT
                )
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = LynkTheme.colors.surface,
                    titleContentColor = LynkTheme.colors.onSurface,
                    headlineContentColor = LynkTheme.colors.onSurface,
                    weekdayContentColor = LynkTheme.colors.onSurfaceVariant,
                    subheadContentColor = LynkTheme.colors.onSurface,
                    yearContentColor = LynkTheme.colors.onSurface,
                    currentYearContentColor = LynkTheme.colors.primary,
                    selectedYearContentColor = LynkTheme.colors.onPrimary,
                    selectedYearContainerColor = LynkTheme.colors.primary,
                    dayContentColor = LynkTheme.colors.onSurface,
                    selectedDayContentColor = LynkTheme.colors.onPrimary,
                    selectedDayContainerColor = LynkTheme.colors.primary,
                    todayContentColor = LynkTheme.colors.primary,
                    todayDateBorderColor = LynkTheme.colors.primary
                )
            )
        }
    }
}