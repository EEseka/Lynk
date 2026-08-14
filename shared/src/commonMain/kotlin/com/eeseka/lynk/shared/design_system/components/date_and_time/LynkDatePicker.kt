package com.eeseka.lynk.shared.design_system.components.date_and_time

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButtonStyle
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.mohamedrejeb.calf.ui.datepicker.AdaptiveDatePicker
import com.mohamedrejeb.calf.ui.datepicker.rememberAdaptiveDatePickerState
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.confirm_date
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LynkDatePicker(
    onDateSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val state = rememberAdaptiveDatePickerState()
    val scheme = MaterialTheme.colorScheme

    Column(modifier = modifier.fillMaxWidth()) {
        AdaptiveDatePicker(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(MaterialTheme.shapes.medium),
            colors = DatePickerDefaults.colors(
                containerColor = scheme.surfaceContainerHigh
            )
        )

        LynkButton(
            text = stringResource(Res.string.confirm_date),
            onClick = { onDateSelected(state.selectedDateMillis) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            style = LynkButtonStyle.TEXT
        )
    }
}

@PreviewLightDark
@Composable
private fun LynkDatePickerPreview() {
    LynkTheme {
        LynkDatePicker(
            onDateSelected = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)
        )
    }
}