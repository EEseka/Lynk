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
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
import com.eeseka.lynk.create_hangout.presentation.model.PickerType
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
import com.eeseka.lynk.shared.domain.hangout.HangoutConstants.MAX_ATTENDEES
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.presentation.hangout.mappers.getIcon
import com.eeseka.lynk.shared.presentation.hangout.mappers.getTitle
import kotlinx.collections.immutable.toImmutableList
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

@Composable
fun CreateHangoutStepOne(
    nameState: TextFieldState,
    nameErrorMessage: String?,
    descriptionState: TextFieldState,
    descriptionErrorMessage: String?,
    vibe: HangoutVibe,
    dateValue: String?,
    dateMillis: Long?,
    dateErrorMessage: String?,
    timeValue: String?,
    timeHour: Int?,
    timeMinute: Int?,
    timeErrorMessage: String?,
    expandedPicker: PickerType?,
    maxAttendees: Int?,
    onVibeSelected: (HangoutVibe) -> Unit,
    onPickerToggled: (PickerType) -> Unit,
    onDateSelected: (epochMilliseconds: Long?) -> Unit,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    onIncrementAttendees: () -> Unit,
    onDecrementAttendees: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = rememberAppHaptic()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        LynkTextField(
            state = nameState,
            label = stringResource(Res.string.hangout_name),
            placeholder = stringResource(Res.string.hangout_name_placeholder),
            errorMessage = nameErrorMessage,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        LynkTextField(
            state = descriptionState,
            label = stringResource(Res.string.hangout_description),
            placeholder = stringResource(Res.string.hangout_description_placeholder),
            singleLine = false,
            errorMessage = descriptionErrorMessage,
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
            }.toImmutableList(),
            selectedIndex = vibes.indexOf(vibe),
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
            value = dateValue,
            placeholder = stringResource(Res.string.pick_date),
            icon = Lucide.Calendar,
            errorMessage = dateErrorMessage,
            isExpanded = expandedPicker == PickerType.DATE,
            onClick = {
                hapticFeedback(AppHaptic.ImpactLight)
                onPickerToggled(PickerType.DATE)
            },
            pickerContent = {
                LynkDatePicker(
                    onDateSelected = onDateSelected,
                    initialSelectedDateMillis = dateMillis
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        LynkDateTimeTile(
            title = stringResource(Res.string.time),
            value = timeValue,
            placeholder = stringResource(Res.string.pick_time),
            icon = Lucide.Clock,
            errorMessage = timeErrorMessage,
            isExpanded = expandedPicker == PickerType.TIME,
            onClick = {
                hapticFeedback(AppHaptic.ImpactLight)
                onPickerToggled(PickerType.TIME)
            },
            pickerContent = {
                LynkTimePicker(
                    onTimeSelected = onTimeSelected,
                    initialHour = timeHour,
                    initialMinute = timeMinute
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
                    text = maxAttendees?.let { "$it $people" }
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
                    enabled = maxAttendees != null
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
                    enabled = maxAttendees == null || maxAttendees < MAX_ATTENDEES
                ) {
                    Icon(
                        imageVector = Lucide.Plus,
                        contentDescription = stringResource(Res.string.increase_max_people)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CreateHangoutStepOnePreview(
    nameErrorMessage: String? = null,
    dateErrorMessage: String? = null,
    timeErrorMessage: String? = null,
    maxAttendees: Int? = 8
) {
    LynkTheme {
        CreateHangoutStepOne(
            nameState = TextFieldState("Suya Night 🔥"),
            nameErrorMessage = nameErrorMessage,
            descriptionState = TextFieldState("Friday night chills with the guys."),
            descriptionErrorMessage = null,
            vibe = HangoutVibe.CHILL,
            dateValue = "2026-05-20",
            dateMillis = null,
            dateErrorMessage = dateErrorMessage,
            timeValue = "20:00",
            timeHour = 20,
            timeMinute = 0,
            timeErrorMessage = timeErrorMessage,
            expandedPicker = null,
            maxAttendees = maxAttendees,
            onVibeSelected = {},
            onPickerToggled = {},
            onDateSelected = {},
            onTimeSelected = { _, _ -> },
            onIncrementAttendees = {},
            onDecrementAttendees = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}

@PreviewLightDark
@Composable
private fun CreateHangoutStepOneFilledPreview() = CreateHangoutStepOnePreview()

@PreviewLightDark
@Composable
private fun CreateHangoutStepOneErrorPreview() = CreateHangoutStepOnePreview(
    nameErrorMessage = "Hangout name cannot be blank",
    dateErrorMessage = "Please select a date",
    timeErrorMessage = "Please select a time",
    maxAttendees = null
)