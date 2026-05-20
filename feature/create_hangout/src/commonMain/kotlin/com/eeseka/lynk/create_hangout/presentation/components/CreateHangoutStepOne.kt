package com.eeseka.lynk.create_hangout.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Minus
import com.composables.icons.lucide.Plus
import com.eeseka.lynk.create_hangout.presentation.CreateHangoutState
import com.eeseka.lynk.shared.design_system.components.buttons.LynkTonalIconButton
import com.eeseka.lynk.shared.design_system.components.date_and_time.LynkDatePicker
import com.eeseka.lynk.shared.design_system.components.date_and_time.LynkTimePicker
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.textfields.LynkTextField
import com.eeseka.lynk.shared.design_system.components.toggles_and_control.LynkSegmentedControl
import com.eeseka.lynk.shared.design_system.components.toggles_and_control.LynkSegmentedItem
import com.eeseka.lynk.shared.design_system.components.toggles_and_control.LynkSegmentedStyle
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.presentation.hangout.mappers.getIcon
import com.eeseka.lynk.shared.presentation.hangout.mappers.getTitle
import com.eeseka.lynk.shared.presentation.util.UiText
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import lynk.feature.create_hangout.generated.resources.Res
import lynk.feature.create_hangout.generated.resources.date
import lynk.feature.create_hangout.generated.resources.decrease_max_people
import lynk.feature.create_hangout.generated.resources.hangout_description
import lynk.feature.create_hangout.generated.resources.hangout_description_placeholder
import lynk.feature.create_hangout.generated.resources.hangout_name
import lynk.feature.create_hangout.generated.resources.hangout_name_placeholder
import lynk.feature.create_hangout.generated.resources.increase_max_people
import lynk.feature.create_hangout.generated.resources.max_people
import lynk.feature.create_hangout.generated.resources.people
import lynk.feature.create_hangout.generated.resources.pick_date
import lynk.feature.create_hangout.generated.resources.pick_time
import lynk.feature.create_hangout.generated.resources.time
import lynk.feature.create_hangout.generated.resources.unlimited
import lynk.feature.create_hangout.generated.resources.vibe
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

private enum class PickerType { DATE, TIME }

@Composable
fun CreateHangoutStepOne(
    state: CreateHangoutState,
    onVibeSelected: (HangoutVibe) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onTimeSelected: (LocalTime) -> Unit,
    onIncrementAttendees: () -> Unit,
    onDecrementAttendees: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = rememberAppHaptic()
    val scrollState = rememberScrollState()
    var expandedPicker by remember { mutableStateOf<PickerType?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        LynkTextField(
            state = state.hangoutNameTextState,
            label = stringResource(Res.string.hangout_name),
            placeholder = stringResource(Res.string.hangout_name_placeholder),
            isError = state.hangoutNameError != null,
            errorMessage = state.hangoutNameError?.asString(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        LynkTextField(
            state = state.hangoutDescriptionTextState,
            label = stringResource(Res.string.hangout_description),
            placeholder = stringResource(Res.string.hangout_description_placeholder),
            singleLine = false,
            isError = state.hangoutDescriptionError != null,
            errorMessage = state.hangoutDescriptionError?.asString(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        LynkText(
            text = stringResource(Res.string.vibe),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        val vibes = HangoutVibe.entries
        LynkSegmentedControl(
            items = vibes.map {
                LynkSegmentedItem(title = it.getTitle(), icon = it.getIcon())
            },
            selectedIndex = vibes.indexOf(state.hangoutVibe),
            onItemSelected = {
                hapticFeedback(AppHaptic.Selection)
                onVibeSelected(vibes[it])
            },
            style = LynkSegmentedStyle.WRAPPING_CHIPS,
            contentPadding = PaddingValues(0.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        LynkDateTimeTile(
            title = stringResource(Res.string.date),
            value = state.hangoutDate?.toString(),
            placeholder = stringResource(Res.string.pick_date),
            icon = Lucide.Calendar,
            isError = state.hangoutDateError != null,
            errorMessage = state.hangoutDateError?.asString(),
            isExpanded = expandedPicker == PickerType.DATE,
            onClick = {
                hapticFeedback(AppHaptic.ImpactLight)
                expandedPicker = if (expandedPicker == PickerType.DATE) null else PickerType.DATE
            },
            pickerContent = {
                LynkDatePicker(
                    onDateSelected = { millis ->
                        if (millis != null) {
                            val date = Instant.fromEpochMilliseconds(millis)
                                .toLocalDateTime(TimeZone.currentSystemDefault()).date
                            onDateSelected(date)
                        }
                        expandedPicker = null
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        LynkDateTimeTile(
            title = stringResource(Res.string.time),
            value = state.hangoutTime?.toString(),
            placeholder = stringResource(Res.string.pick_time),
            icon = Lucide.Clock,
            isError = state.hangoutTimeError != null,
            errorMessage = state.hangoutTimeError?.asString(),
            isExpanded = expandedPicker == PickerType.TIME,
            onClick = {
                hapticFeedback(AppHaptic.ImpactLight)
                expandedPicker = if (expandedPicker == PickerType.TIME) null else PickerType.TIME
            },
            pickerContent = {
                LynkTimePicker(
                    onTimeSelected = { hour, minute ->
                        onTimeSelected(LocalTime(hour, minute))
                        expandedPicker = null
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val people = stringResource(Res.string.people)
            Column {
                LynkText(
                    text = stringResource(Res.string.max_people),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                LynkText(
                    text = state.maxAttendees?.let { "$it $people" }
                        ?: stringResource(Res.string.unlimited),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LynkTonalIconButton(
                    onClick = {
                        hapticFeedback(AppHaptic.Selection)
                        onDecrementAttendees()
                    },
                    enabled = state.maxAttendees != null
                ) {
                    Icon(
                        imageVector = Lucide.Minus,
                        contentDescription = stringResource(Res.string.decrease_max_people)
                    )
                }

                LynkTonalIconButton(
                    onClick = {
                        hapticFeedback(AppHaptic.Selection)
                        onIncrementAttendees()
                    },
                    enabled = state.maxAttendees == null || state.maxAttendees < 50
                ) {
                    Icon(
                        imageVector = Lucide.Plus,
                        contentDescription = stringResource(Res.string.increase_max_people)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@PreviewLightDark
@Composable
private fun CreateHangoutStepOnePreview() {
    LynkTheme {
        CreateHangoutStepOne(
            state = CreateHangoutState(
                hangoutVibe = HangoutVibe.CHILL,
                hangoutDate = LocalDate(2026, 5, 20),
                hangoutTime = LocalTime(20, 0),
                maxAttendees = null
            ),
            onVibeSelected = {},
            onDateSelected = {},
            onTimeSelected = {},
            onIncrementAttendees = {},
            onDecrementAttendees = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}

@PreviewLightDark
@Composable
private fun CreateHangoutStepOneErrorPreview() {
    LynkTheme {
        CreateHangoutStepOne(
            state = CreateHangoutState(
                hangoutNameError = UiText.DynamicString("Hangout name cannot be blank"),
                hangoutDateError = UiText.DynamicString("Please select a date"),
                hangoutTimeError = UiText.DynamicString("Please select a time"),
                maxAttendees = 6
            ),
            onVibeSelected = {},
            onDateSelected = {},
            onTimeSelected = {},
            onIncrementAttendees = {},
            onDecrementAttendees = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}
