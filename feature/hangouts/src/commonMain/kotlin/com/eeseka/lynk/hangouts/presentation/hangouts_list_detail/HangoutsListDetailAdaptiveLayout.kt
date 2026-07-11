package com.eeseka.lynk.hangouts.presentation.hangouts_list_detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.eeseka.lynk.create_hangout.presentation.CreateHangoutRoot
import com.eeseka.lynk.hangouts.presentation.hangouts_list.HangoutsListAction
import com.eeseka.lynk.hangouts.presentation.hangouts_list.HangoutsListScreen
import com.eeseka.lynk.hangouts.presentation.hangouts_list.HangoutsListViewModel
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.presentation.util.ObserveAsEvents
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun HangoutsListDetailAdaptiveLayout(
    initialHangoutId: String?,
    sharedState: HangoutsListDetailState,
    events: Flow<HangoutsListDetailEvent>,
    onAction: (HangoutsListDetailAction) -> Unit,
    mainShellPadding: PaddingValues
) {
    val scaffoldDirective = createNoSpacingPaneScaffoldDirective()
    val scaffoldNavigator = rememberListDetailPaneScaffoldNavigator(
        scaffoldDirective = scaffoldDirective
    )
    val scope = rememberCoroutineScope()

    LaunchedEffect(initialHangoutId) {
        if (initialHangoutId != null) {
            onAction(HangoutsListDetailAction.OnSelectHangout(initialHangoutId))
            scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
        }
    }

    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = scaffoldNavigator.canNavigateBack(),
        onBackCompleted = {
            scope.launch {
                scaffoldNavigator.navigateBack()
                onAction(HangoutsListDetailAction.OnSelectHangout(null))
            }
        }
    )

    val detailPane = scaffoldNavigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail]
    LaunchedEffect(detailPane, sharedState.selectedHangoutId) {
        if (detailPane == PaneAdaptedValue.Hidden && sharedState.selectedHangoutId != null) {
            onAction(HangoutsListDetailAction.OnSelectHangout(null))
        }
    }

    ListDetailPaneScaffold(
        directive = scaffoldDirective,
        value = scaffoldNavigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                val viewModel = koinViewModel<HangoutsListViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()

                LaunchedEffect(sharedState.selectedHangoutId) {
                    viewModel.onAction(HangoutsListAction.OnSelectHangout(sharedState.selectedHangoutId))
                }

                ObserveAsEvents(events) { event ->
                    when (event) {
                        HangoutsListDetailEvent.RefreshList -> {
                            viewModel.onAction(HangoutsListAction.Refresh)
                        }
                    }
                }

                HangoutsListScreen(
                    state = state,
                    events = viewModel.events,
                    onAction = { action ->
                        when (action) {
                            is HangoutsListAction.OnSelectHangout -> {
                                onAction(HangoutsListDetailAction.OnSelectHangout(action.hangoutId))
                                scope.launch {
                                    scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
                                }
                            }

                            else -> Unit
                        }
                        viewModel.onAction(action)
                    },
                    onCreateHangoutClick = {
                        onAction(HangoutsListDetailAction.OnCreateHangoutClick)
                    },
                    mainShellPadding = mainShellPadding
                )
            }
        },
        detailPane = {
            AnimatedPane {
                Box(contentAlignment = Alignment.Center) {
                    LynkText(
                        text = "Welcome to Hangouts Detail Screen with ID: ${sharedState.selectedHangoutId ?: ""}",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
            }
        }
    )

    CreateHangoutRoot(
        visible = sharedState.sheetState is SheetState.CreateHangout,
        onDismiss = { onAction(HangoutsListDetailAction.OnDismissCurrentSheet) },
        onSuccess = { newHangoutId ->
            onAction(HangoutsListDetailAction.OnDismissCurrentSheet)
            onAction(HangoutsListDetailAction.RefreshList)
            onAction(HangoutsListDetailAction.OnSelectHangout(newHangoutId))
            scope.launch {
                scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
            }
        }
    )
}
