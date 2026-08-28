package com.eeseka.lynk.hangouts.presentation.hangouts_list_detail

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.eeseka.lynk.create_hangout.presentation.CreateHangoutRoot
import com.eeseka.lynk.hangouts.presentation.hangout_detail.HangoutDetailAction
import com.eeseka.lynk.hangouts.presentation.hangout_detail.HangoutDetailScreen
import com.eeseka.lynk.hangouts.presentation.hangout_detail.HangoutDetailViewModel
import com.eeseka.lynk.hangouts.presentation.hangouts_list.HangoutsListAction
import com.eeseka.lynk.hangouts.presentation.hangouts_list.HangoutsListScreen
import com.eeseka.lynk.hangouts.presentation.hangouts_list.HangoutsListViewModel
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
    onDetailPaneFullScreenChange: (Boolean) -> Unit,
    mainShellPadding: PaddingValues,
    unreadNotificationCount: Int,
    onNavigateToNotifications: () -> Unit
) {
    val scaffoldDirective = createNoSpacingPaneScaffoldDirective()
    val scaffoldNavigator = rememberListDetailPaneScaffoldNavigator(
        scaffoldDirective = scaffoldDirective
    )
    val scope = rememberCoroutineScope()

    // The id arrives as a route argument and never changes, so without this guard the effect
    // opens the detail pane again every time this screen is recomposed from scratch — which is
    // what happens on the way back from the notifications screen.
    var hasOpenedInitialHangout by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(initialHangoutId) {
        if (initialHangoutId == null || hasOpenedInitialHangout) return@LaunchedEffect

        hasOpenedInitialHangout = true
        onAction(HangoutsListDetailAction.OnSelectHangout(initialHangoutId))
        scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
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
    val listPane = scaffoldNavigator.scaffoldValue[ListDetailPaneScaffoldRole.List]

    val isDetailPaneFullScreen =
        detailPane == PaneAdaptedValue.Expanded && listPane != PaneAdaptedValue.Expanded

    LaunchedEffect(detailPane, sharedState.selectedHangoutId) {
        if (detailPane == PaneAdaptedValue.Hidden && sharedState.selectedHangoutId != null) {
            onAction(HangoutsListDetailAction.OnSelectHangout(null))
        }
    }

    LaunchedEffect(isDetailPaneFullScreen) {
        onDetailPaneFullScreenChange(isDetailPaneFullScreen)
    }
    // Safety net: if this whole layout is torn down while the detail pane owns the screen
    // (e.g. a notification/deep-link jumps out of it), the LaunchedEffect never gets to
    // report that it is gone — so guarantee it here on dispose.
    DisposableEffect(Unit) {
        onDispose { onDetailPaneFullScreenChange(false) }
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
                    unreadNotificationCount = unreadNotificationCount,
                    onNavigateToNotifications = onNavigateToNotifications,
                    mainShellPadding = mainShellPadding
                )
            }
        },
        detailPane = {
            AnimatedPane {
                val viewModel = koinViewModel<HangoutDetailViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()

                LaunchedEffect(sharedState.selectedHangoutId) {
                    viewModel.onAction(HangoutDetailAction.OnSelectHangout(sharedState.selectedHangoutId))
                }

                HangoutDetailScreen(
                    state = state,
                    events = viewModel.events,
                    onAction = viewModel::onAction,
                    isDetailPaneFullScreen = isDetailPaneFullScreen,
                    onBackClick = {
                        scope.launch {
                            if (scaffoldNavigator.canNavigateBack()) {
                                scaffoldNavigator.navigateBack()
                            }
                        }
                    },
                    onLeave = {
                        scope.launch {
                            if (scaffoldNavigator.canNavigateBack()) {
                                scaffoldNavigator.navigateBack()
                            }
                            onAction(HangoutsListDetailAction.OnSelectHangout(null))
                            onAction(HangoutsListDetailAction.RefreshList)
                        }
                    },
                    onEditClick = {
                        state.hangout?.let { hangout ->
                            onAction(HangoutsListDetailAction.OnEditHangoutClick(hangout))
                        }
                    }
                )
            }
        }
    )

    CreateHangoutRoot(
        visible = sharedState.sheetState is SheetState.CreateHangout || sharedState.sheetState is SheetState.EditHangout,
        originalHangout = (sharedState.sheetState as? SheetState.EditHangout)?.hangout,
        onDismiss = { onAction(HangoutsListDetailAction.OnDismissCurrentSheet) },
        onSuccess = { hangoutId ->
            val wasEdit = sharedState.sheetState is SheetState.EditHangout

            onAction(HangoutsListDetailAction.OnDismissCurrentSheet)
            if (!wasEdit) {
                onAction(HangoutsListDetailAction.RefreshList)
            }
            onAction(HangoutsListDetailAction.OnSelectHangout(hangoutId))
            scope.launch {
                scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
            }
        }
    )
}