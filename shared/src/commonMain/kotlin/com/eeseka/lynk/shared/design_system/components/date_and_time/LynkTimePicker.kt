package com.eeseka.lynk.shared.design_system.components.date_and_time

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButtonStyle
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.mohamedrejeb.calf.ui.timepicker.AdaptiveTimePicker
import com.mohamedrejeb.calf.ui.timepicker.rememberAdaptiveTimePickerState
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.confirm_time
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LynkTimePicker(
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val state = rememberAdaptiveTimePickerState()
    val scheme = MaterialTheme.colorScheme

    Column(modifier = modifier.fillMaxWidth()) {
        AdaptiveTimePicker(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = TimePickerDefaults.colors(
                containerColor = scheme.surfaceContainerHigh
            )
        )

        LynkButton(
            text = stringResource(Res.string.confirm_time),
            onClick = { onTimeSelected(state.hour, state.minute) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            style = LynkButtonStyle.TEXT
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LynkTimePickerPreview() {
    LynkTheme {
        LynkTimePicker(onTimeSelected = { _, _ -> })
    }
}

@Preview
@Composable
private fun LynkTimePickerPreviewDark() {
    LynkTheme(true) {
        LynkTimePicker(onTimeSelected = { _, _ -> })
    }
}
