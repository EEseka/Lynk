package com.eeseka.lynk.shared.design_system.components.date_and_time

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButtonStyle
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkBottomSheet
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.util.PlatformUtils.isIOS
import com.slapps.cupertino.CupertinoTimePickerNative
import com.slapps.cupertino.ExperimentalCupertinoApi
import com.slapps.cupertino.rememberCupertinoTimePickerState
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.cancel
import lynk.shared.generated.resources.confirm
import lynk.shared.generated.resources.confirm_time
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCupertinoApi::class)
@Composable
fun LynkTimePicker(
    onDismissRequest: () -> Unit,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isIOS()) {
        val cupertinoTimePickerState = rememberCupertinoTimePickerState()

        LynkBottomSheet(
            onDismissRequest = onDismissRequest,
            modifier = modifier
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                CupertinoTimePickerNative(
                    state = cupertinoTimePickerState,
                    containerColor = LynkTheme.colors.surface,
                    modifier = Modifier.fillMaxWidth()
                )

                LynkButton(
                    text = stringResource(Res.string.confirm_time),
                    onClick = {
                        onTimeSelected(
                            cupertinoTimePickerState.hour,
                            cupertinoTimePickerState.minute
                        )
                        onDismissRequest()
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    style = LynkButtonStyle.PRIMARY
                )
            }
        }
    } else {
        val timePickerState = rememberTimePickerState()

        AlertDialog(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            containerColor = LynkTheme.colors.surface,
            text = {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = LynkTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                            clockDialSelectedContentColor = LynkTheme.colors.surface,
                            clockDialUnselectedContentColor = LynkTheme.colors.textMain,
                            selectorColor = LynkTheme.colors.primary,
                            containerColor = LynkTheme.colors.surface,
                            periodSelectorBorderColor = LynkTheme.colors.outlineVariant,
                            periodSelectorSelectedContainerColor = LynkTheme.colors.primary.copy(
                                alpha = 0.2f
                            ),
                            periodSelectorUnselectedContainerColor = LynkTheme.colors.surface,
                            periodSelectorSelectedContentColor = LynkTheme.colors.primary,
                            periodSelectorUnselectedContentColor = LynkTheme.colors.textMain,
                            timeSelectorSelectedContainerColor = LynkTheme.colors.primary.copy(alpha = 0.2f),
                            timeSelectorUnselectedContainerColor = LynkTheme.colors.surfaceVariant,
                            timeSelectorSelectedContentColor = LynkTheme.colors.primary,
                            timeSelectorUnselectedContentColor = LynkTheme.colors.textMain
                        )
                    )
                }
            },
            confirmButton = {
                LynkButton(
                    text = stringResource(Res.string.confirm),
                    onClick = {
                        onTimeSelected(timePickerState.hour, timePickerState.minute)
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
        )
    }
}