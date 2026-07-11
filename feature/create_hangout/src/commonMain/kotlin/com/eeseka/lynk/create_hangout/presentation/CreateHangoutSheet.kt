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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.create_hangout.presentation.model.HangoutFormMode
import com.eeseka.lynk.create_hangout.presentation.components.CreateHangoutStepOne
import com.eeseka.lynk.create_hangout.presentation.components.CreateHangoutStepThree
import com.eeseka.lynk.create_hangout.presentation.components.CreateHangoutStepTwo
import com.eeseka.lynk.create_hangout.presentation.components.LynkStepIndicator
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkTonalIconButton
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkAdaptiveSheet
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUi
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import com.eeseka.lynk.shared.presentation.location.rememberLocationController
import com.eeseka.lynk.shared.presentation.permissions.Permission
import com.eeseka.lynk.shared.presentation.permissions.PermissionState
import com.eeseka.lynk.shared.presentation.permissions.rememberPermissionController
import com.eeseka.lynk.shared.presentation.util.DialogSheetScopedViewModel
import com.eeseka.lynk.shared.presentation.util.ObserveAsEvents
import com.eeseka.lynk.shared.presentation.util.clearFocusOnTap
import kotlinx.coroutines.flow.Flow
import lynk.feature.create_hangout.generated.resources.Res
import lynk.feature.create_hangout.generated.resources.create_hangout
import lynk.feature.create_hangout.generated.resources.create_hangout_loading
import lynk.feature.create_hangout.generated.resources.go_back
import lynk.feature.create_hangout.generated.resources.next_pick_location
import lynk.feature.create_hangout.generated.resources.next_review_and_confirm
import lynk.feature.create_hangout.generated.resources.pick_location
import lynk.feature.create_hangout.generated.resources.review_and_confirm
import lynk.feature.create_hangout.generated.resources.save_changes
import lynk.feature.create_hangout.generated.resources.save_changes_loading
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
    val permissionController = rememberPermissionController()
    val locationController = rememberLocationController()
    var permissionState by remember { mutableStateOf(PermissionState.NOT_DETERMINED) }

    LaunchedEffect(Unit) {
        permissionState = permissionController.getPermissionState(Permission.LOCATION)
        if (permissionState == PermissionState.NOT_DETERMINED || permissionState == PermissionState.DENIED) {
            permissionState = permissionController.requestPermission(Permission.LOCATION)
        }
    }

    DialogSheetScopedViewModel(visible = visible) {
        val viewModel = koinViewModel<CreateHangoutViewModel>()
        val state by viewModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(permissionState) {
            if (permissionState == PermissionState.GRANTED) {
                locationController.startTracking()
                val coordinate = locationController.getCurrentLocation()
                locationController.stopTracking()
                viewModel.onAction(
                    CreateHangoutAction.OnLocationFetched(
                        coordinate.latitude,
                        coordinate.longitude
                    )
                )
            }
        }

        LaunchedEffect(spot) {
            viewModel.onAction(
                CreateHangoutAction.InitCreateMode(spot)
            )
        }

        LaunchedEffect(originalHangout) {
            if (originalHangout != null) {
                viewModel.onAction(
                    CreateHangoutAction.InitEditMode(originalHangout)
                )
            }
        }

        CreateHangoutSheet(
            state = state,
            events = viewModel.events,
            onAction = viewModel::onAction,
            onSuccess = onSuccess,
            onDismissRequest = onDismiss
        )
    }
}

@Composable
fun CreateHangoutSheet(
    state: CreateHangoutState,
    events: Flow<CreateHangoutEvent>,
    onAction: (CreateHangoutAction) -> Unit,
    onSuccess: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val hapticFeedback = rememberAppHaptic()

    ObserveAsEvents(events) { event ->
        when (event) {
            is CreateHangoutEvent.Success -> {
                hapticFeedback(AppHaptic.Success)
                onSuccess(event.hangoutId)
            }
        }
    }

    LynkAdaptiveSheet(
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
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
                    AnimatedVisibility(
                        visible = state.currentStep > 1,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        LynkTonalIconButton(
                            onClick = {
                                hapticFeedback(AppHaptic.ImpactLight)
                                onAction(CreateHangoutAction.OnPreviousStep)
                            }
                        ) {
                            Icon(
                                imageVector = Lucide.ArrowLeft,
                                contentDescription = stringResource(Res.string.go_back)
                            )
                        }
                    }

                    AnimatedContent(
                        targetState = state.currentStep,
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
                                state = state,
                                onVibeSelected = { onAction(CreateHangoutAction.OnVibeSelected(it)) },
                                onDateSelected = { date ->
                                    onAction(CreateHangoutAction.OnDateSelected(date))
                                },
                                onTimeSelected = { time ->
                                    onAction(CreateHangoutAction.OnTimeSelected(time))
                                },
                                onIncrementAttendees = { onAction(CreateHangoutAction.IncrementAttendees) },
                                onDecrementAttendees = { onAction(CreateHangoutAction.DecrementAttendees) }
                            )
                        }

                        2 -> {
                            CreateHangoutStepTwo(
                                state = state,
                                onVotingModeChanged = {
                                    onAction(CreateHangoutAction.OnLocationModeChanged(it))
                                },
                                onTabSelected = {
                                    onAction(CreateHangoutAction.OnSearchTabSelected(it))
                                },
                                onSpotSelected = { onAction(CreateHangoutAction.OnSpotSelected(it)) },
                                onLoadNextSpotPage = { onAction(CreateHangoutAction.LoadNextSpotSearchPage) },
                                onLoadNextFavoritePage = { onAction(CreateHangoutAction.LoadNextFavoriteSpotSearchPage) }
                            )
                        }

                        3 -> {
                            CreateHangoutStepThree(state = state)
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
                        3 -> if (state.mode == HangoutFormMode.CREATE) stringResource(Res.string.create_hangout) else stringResource(
                            Res.string.save_changes
                        )

                        else -> ""
                    },
                    enabled = when (state.currentStep) {
                        1 -> state.canProceedToStepTwo
                        2 -> state.canProceedToStepThree
                        3 -> state.canSubmit
                        else -> false
                    },
                    isLoading = state.isSubmitting,
                    loadingText = if (state.mode == HangoutFormMode.CREATE) stringResource(Res.string.create_hangout_loading) else stringResource(
                        Res.string.save_changes_loading
                    ),
                    onClick = {
                        hapticFeedback(AppHaptic.ImpactMedium)
                        if (state.currentStep == 3) {
                            onAction(CreateHangoutAction.OnSubmitClick)
                        } else {
                            onAction(CreateHangoutAction.OnNextStep)
                        }
                    },
                    modifier = Modifier.height(56.dp)
                )
            }
        }
    }
}