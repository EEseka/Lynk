package com.eeseka.lynk.create_hangout.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.eeseka.lynk.create_hangout.presentation.components.CreateHangoutStepOne
import com.eeseka.lynk.create_hangout.presentation.components.CreateHangoutStepThree
import com.eeseka.lynk.create_hangout.presentation.components.CreateHangoutStepTwo
import com.eeseka.lynk.create_hangout.presentation.components.LynkStepIndicator
import com.eeseka.lynk.create_hangout.presentation.model.SearchTab
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkTonalIconButton
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkAdaptiveSheet
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUi
import com.eeseka.lynk.shared.domain.util.onSuccess
import com.eeseka.lynk.shared.presentation.location.rememberLocationController
import com.eeseka.lynk.shared.presentation.permissions.LocationPermissionEffect
import com.eeseka.lynk.shared.presentation.permissions.PermissionState
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import com.eeseka.lynk.shared.presentation.util.DialogSheetScopedViewModel
import com.eeseka.lynk.shared.presentation.util.ObserveAsEvents
import com.eeseka.lynk.shared.presentation.util.clearFocusOnTap
import com.eeseka.lynk.shared.presentation.util.toDateLabel
import com.eeseka.lynk.shared.presentation.util.toDateTimeLabel
import com.eeseka.lynk.shared.presentation.util.toPickerMillis
import com.eeseka.lynk.shared.presentation.util.toTimeLabel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import lynk.feature.create_hangout.generated.resources.Res
import lynk.feature.create_hangout.generated.resources.close_sheet
import lynk.feature.create_hangout.generated.resources.create_hangout
import lynk.feature.create_hangout.generated.resources.create_hangout_loading
import lynk.feature.create_hangout.generated.resources.empty_favorite_search_message
import lynk.feature.create_hangout.generated.resources.empty_search_message
import lynk.feature.create_hangout.generated.resources.go_back
import lynk.feature.create_hangout.generated.resources.next_pick_location
import lynk.feature.create_hangout.generated.resources.no_favorite_spots_message
import lynk.feature.create_hangout.generated.resources.next_review_and_confirm
import lynk.feature.create_hangout.generated.resources.pick_location
import lynk.feature.create_hangout.generated.resources.review_and_confirm
import lynk.feature.create_hangout.generated.resources.save_changes
import lynk.feature.create_hangout.generated.resources.save_changes_loading
import lynk.feature.create_hangout.generated.resources.tbd
import lynk.feature.create_hangout.generated.resources.the_basics
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateHangoutRoot(
    visible: Boolean,
    spot: SpotUi? = null,
    originalHangout: HangoutUi? = null,
    onDismiss: () -> Unit,
    onSuccess: (hangoutId: String) -> Unit
) {
    val hapticFeedback = rememberAppHaptic()
    val locationController = rememberLocationController()
    var permissionState by remember { mutableStateOf(PermissionState.NOT_DETERMINED) }

    LocationPermissionEffect(isEnabled = visible) { resolvedPermissionState ->
        permissionState = resolvedPermissionState
    }

    DialogSheetScopedViewModel(visible = visible) {
        val viewModel = koinViewModel<CreateHangoutViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(permissionState) {
            if (permissionState == PermissionState.GRANTED) {
                locationController.observeCurrentLocation().collect { locationResult ->
                    locationResult.onSuccess { coordinate ->
                        viewModel.onAction(
                            CreateHangoutAction.OnLocationFetched(coordinate.latitude, coordinate.longitude)
                        )
                    }
                }
            }
        }

        LaunchedEffect(spot, originalHangout) {
            if (originalHangout != null) {
                viewModel.onAction(CreateHangoutAction.InitEditMode(originalHangout))
            } else {
                viewModel.onAction(CreateHangoutAction.InitCreateMode(spot))
            }
        }

        ObserveAsEvents(viewModel.events) { event ->
            when (event) {
                is CreateHangoutEvent.Success -> {
                    hapticFeedback(AppHaptic.Success)
                    onSuccess(event.hangoutId)
                }
            }
        }

        CreateHangoutSheet(
            state = state,
            onAction = viewModel::onAction,
            onDismissRequest = onDismiss
        )
    }
}

@Composable
fun CreateHangoutSheet(
    state: CreateHangoutState,
    onAction: (CreateHangoutAction) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    LynkAdaptiveSheet(
        onDismissRequest = onDismissRequest,
        isDismissibleByGesture = false
    ) {
        CreateHangoutSheetContent(
            state = state,
            onAction = onAction,
            onDismissRequest = onDismissRequest,
            modifier = modifier
        )
    }
}

@Composable
private fun CreateHangoutSheetContent(
    state: CreateHangoutState,
    onAction: (CreateHangoutAction) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = rememberAppHaptic()
    val isEditing = state.originalHangout != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .clearFocusOnTap()
    ) {
        // Pinned Header Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedContent(
                    targetState = state.currentStep > 1,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "HeaderButtonTransition"
                ) { canGoBack ->
                    LynkTonalIconButton(
                        onClick = {
                            hapticFeedback(AppHaptic.ImpactLight)
                            if (canGoBack) {
                                onAction(CreateHangoutAction.OnPreviousStep)
                            } else {
                                onDismissRequest()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (canGoBack) Lucide.ArrowLeft else Lucide.X,
                            contentDescription = if (canGoBack) stringResource(Res.string.go_back)
                            else stringResource(Res.string.close_sheet)
                        )
                    }
                }

                AnimatedContent(
                    targetState = state.currentStep,
                    transitionSpec = {
                        // Matches the body below so the header travels with it
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut())
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut())
                        }.using(SizeTransform(clip = false))
                    },
                    label = "StepTitleTransition"
                ) { step ->
                    LynkText(
                        text = when (step) {
                            1 -> stringResource(Res.string.the_basics)
                            2 -> stringResource(Res.string.pick_location)
                            3 -> stringResource(Res.string.review_and_confirm)
                            else -> ""
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LynkStepIndicator(
                numberOfSteps = 3,
                currentStep = state.currentStep
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = state.currentStep,
                transitionSpec = {
                    // Slide in from right when going forward, slide in from left when going back
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut())
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut())
                    }.using(SizeTransform(clip = false))
                },
                label = "StepContentTransition"
            ) { step ->
                when (step) {
                    1 -> {
                        CreateHangoutStepOne(
                            nameState = state.hangoutNameTextState,
                            nameErrorMessage = state.hangoutNameError?.asString(),
                            descriptionState = state.hangoutDescriptionTextState,
                            descriptionErrorMessage = state.hangoutDescriptionError?.asString(),
                            vibe = state.hangoutVibe,
                            dateValue = state.hangoutDate?.toDateLabel(),
                            dateMillis = state.hangoutDate?.toPickerMillis(),
                            dateErrorMessage = state.hangoutDateError?.asString(),
                            timeValue = state.hangoutTime?.toTimeLabel(),
                            timeHour = state.hangoutTime?.hour,
                            timeMinute = state.hangoutTime?.minute,
                            timeErrorMessage = state.hangoutTimeError?.asString(),
                            expandedPicker = state.expandedPicker,
                            maxAttendees = state.maxAttendees,
                            onVibeSelected = { onAction(CreateHangoutAction.OnVibeSelected(it)) },
                            onPickerToggled = { onAction(CreateHangoutAction.OnPickerToggled(it)) },
                            onDateSelected = { onAction(CreateHangoutAction.OnDateSelected(it)) },
                            onTimeSelected = { hour, minute ->
                                onAction(CreateHangoutAction.OnTimeSelected(hour, minute))
                            },
                            onIncrementAttendees = { onAction(CreateHangoutAction.IncrementAttendees) },
                            onDecrementAttendees = { onAction(CreateHangoutAction.DecrementAttendees) }
                        )
                    }

                    2 -> {
                        val isSearchActive = state.spotSearchTextState.text.isNotBlank()
                        val isAllSpots = state.activeSearchTab == SearchTab.ALL_SPOTS

                        val spots = when {
                            !isAllSpots -> state.favoriteSpotSearchResults
                            isSearchActive -> state.spotSearchResults
                            else -> state.trendingSpots
                        }
                        val isLoading = when {
                            !isAllSpots -> state.isFavoriteSpotSearchLoading
                            isSearchActive -> state.isSpotSearchLoading
                            else -> state.isTrendingLoading
                        }
                        val isEndReached = when {
                            !isAllSpots -> state.favoriteSpotSearchEndReached
                            isSearchActive -> state.spotSearchEndReached
                            else -> true
                        }

                        CreateHangoutStepTwo(
                            isVotingMode = state.isVotingMode,
                            activeSearchTab = state.activeSearchTab,
                            searchState = state.spotSearchTextState,
                            selectedSpot = state.selectedSpot,
                            userLatitude = state.userLatitude,
                            userLongitude = state.userLongitude,
                            spots = spots,
                            isLoading = isLoading,
                            isEndReached = isEndReached,
                            showEmptyState = spots.isEmpty() && !isLoading && isEndReached && (!isAllSpots || isSearchActive),
                            emptyStateMessage = when {
                                isAllSpots -> stringResource(Res.string.empty_search_message)
                                isSearchActive -> stringResource(Res.string.empty_favorite_search_message)
                                else -> stringResource(Res.string.no_favorite_spots_message)
                            },
                            errorMessage = if (isAllSpots) {
                                state.spotSearchError?.asString()
                            } else {
                                state.favoriteSpotSearchError?.asString()
                            },
                            resetKey = if (isAllSpots) {
                                state.spotSearchResetEpoch
                            } else {
                                state.favoriteSpotSearchResetEpoch
                            },
                            onVotingModeChanged = {
                                onAction(CreateHangoutAction.OnLocationModeChanged(it))
                            },
                            onTabSelected = {
                                onAction(CreateHangoutAction.OnSearchTabSelected(it))
                            },
                            onSpotSelected = { onAction(CreateHangoutAction.OnSpotSelected(it)) },
                            onLoadNextPage = {
                                if (isAllSpots) {
                                    onAction(CreateHangoutAction.LoadNextSpotSearchPage)
                                } else {
                                    onAction(CreateHangoutAction.LoadNextFavoriteSpotSearchPage)
                                }
                            }
                        )
                    }

                    3 -> {
                        val date = state.hangoutDate
                        val time = state.hangoutTime

                        CreateHangoutStepThree(
                            name = state.hangoutNameTextState.text.toString(),
                            description = state.hangoutDescriptionTextState.text.toString(),
                            vibe = state.hangoutVibe,
                            scheduledLabel = if (date != null && time != null) {
                                LocalDateTime(date, time)
                                    .toInstant(TimeZone.currentSystemDefault())
                                    .toDateTimeLabel()
                            } else {
                                stringResource(Res.string.tbd)
                            },
                            maxAttendees = state.maxAttendees,
                            isVotingMode = state.isVotingMode,
                            selectedSpotName = state.selectedSpot?.name
                        )
                    }
                }
            }
        }

        // Pinned Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            AnimatedVisibility(
                visible = state.submitError != null && state.currentStep == 3,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                LynkText(
                    text = state.submitError?.asString() ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
            }

            LynkButton(
                text = when (state.currentStep) {
                    1 -> stringResource(Res.string.next_pick_location)
                    2 -> stringResource(Res.string.next_review_and_confirm)
                    3 -> if (isEditing) {
                        stringResource(Res.string.save_changes)
                    } else {
                        stringResource(Res.string.create_hangout)
                    }

                    else -> ""
                },
                enabled = when (state.currentStep) {
                    1 -> true
                    2 -> state.canProceedToStepThree
                    3 -> state.canSubmit
                    else -> false
                },
                isLoading = state.isSubmitting,
                loadingText = if (isEditing) {
                    stringResource(Res.string.save_changes_loading)
                } else {
                    stringResource(Res.string.create_hangout_loading)
                },
                onClick = {
                    hapticFeedback(AppHaptic.ImpactMedium)
                    if (state.currentStep == 3) {
                        onAction(CreateHangoutAction.OnSubmitClick)
                    } else {
                        onAction(CreateHangoutAction.OnNextStep)
                    }
                }
            )
        }
    }
}

@PreviewLightDark
@Preview(name = "Tablet landscape", widthDp = 1280, heightDp = 800)
@Composable
private fun CreateHangoutSheetPreview() {
    LynkTheme {
        CreateHangoutSheetContent(
            state = CreateHangoutState(
                currentStep = 1,
                hangoutNameTextState = TextFieldState("Suya Night 🔥"),
                hangoutDescriptionTextState = TextFieldState("Friday night chills with the guys."),
                hangoutVibe = HangoutVibe.CHILL,
                hangoutDate = LocalDate(2026, 5, 20),
                hangoutTime = LocalTime(20, 0),
                maxAttendees = 8,
                canProceedToStepThree = true,
                canSubmit = true
            ),
            onAction = {},
            onDismissRequest = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}
