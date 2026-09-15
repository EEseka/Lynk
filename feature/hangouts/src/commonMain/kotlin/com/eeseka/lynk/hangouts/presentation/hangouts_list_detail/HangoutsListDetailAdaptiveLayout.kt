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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.eeseka.lynk.create_hangout.presentation.CreateHangoutRoot
import com.eeseka.lynk.hangouts.presentation.hangout_detail.HangoutDetailRoot
import com.eeseka.lynk.hangouts.presentation.hangouts_list.HangoutsListAction
import com.eeseka.lynk.hangouts.presentation.hangouts_list.HangoutsListRoot
import com.eeseka.lynk.hangouts.presentation.hangouts_list.HangoutsListViewModel
import com.eeseka.lynk.shared.presentation.util.ObserveAsEvents
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun HangoutsListDetailAdaptiveLayout(
    initialHangoutId: String?,
    onDetailPaneFullScreenChanged: (Boolean) -> Unit,
    mainShellPadding: PaddingValues,
    unreadNotificationCount: Int,
    navigateToNotifications: () -> Unit,
    hangoutsListDetailViewModel: HangoutsListDetailViewModel = koinViewModel()
) {
    val sharedState by hangoutsListDetailViewModel.state.collectAsStateWithLifecycle()
    val scaffoldDirective = createNoSpacingPaneScaffoldDirective()
    val scaffoldNavigator = rememberListDetailPaneScaffoldNavigator(scaffoldDirective = scaffoldDirective)
    val scope = rememberCoroutineScope()

    // Opens the initial hangout only once. Coming back from notifications rebuilds this screen and
    // runs the effect again; rememberSaveable keeps the flag through that rebuild.
    var hasOpenedInitialHangout by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(initialHangoutId) {
        if (initialHangoutId == null || hasOpenedInitialHangout) return@LaunchedEffect

        hasOpenedInitialHangout = true
        hangoutsListDetailViewModel.onAction(HangoutsListDetailAction.OnSelectHangout(initialHangoutId))
        scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
    }

    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = scaffoldNavigator.canNavigateBack(),
        onBackCompleted = {
            scope.launch {
                scaffoldNavigator.navigateBack()
                hangoutsListDetailViewModel.onAction(HangoutsListDetailAction.OnSelectHangout(null))
            }
        }
    )

    val detailPane = scaffoldNavigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail]
    val listPane = scaffoldNavigator.scaffoldValue[ListDetailPaneScaffoldRole.List]

    val isDetailPaneFullScreen =
        detailPane == PaneAdaptedValue.Expanded && listPane != PaneAdaptedValue.Expanded

    LaunchedEffect(detailPane, sharedState.selectedHangoutId) {
        if (detailPane == PaneAdaptedValue.Hidden && sharedState.selectedHangoutId != null) {
            hangoutsListDetailViewModel.onAction(HangoutsListDetailAction.OnSelectHangout(null))
        }
    }

    LaunchedEffect(isDetailPaneFullScreen) {
        onDetailPaneFullScreenChanged(isDetailPaneFullScreen)
    }
    // Safety net: if this whole layout is torn down while the detail pane owns the screen
    // (e.g. a notification/deep-link jumps out of it), the LaunchedEffect never gets to
    // report that it is gone — so guarantee it here on dispose.
    DisposableEffect(Unit) {
        onDispose { onDetailPaneFullScreenChanged(false) }
    }

    ListDetailPaneScaffold(
        directive = scaffoldDirective,
        value = scaffoldNavigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                val listViewModel = koinViewModel<HangoutsListViewModel>()

                ObserveAsEvents(hangoutsListDetailViewModel.events) { event ->
                    when (event) {
                        HangoutsListDetailEvent.RefreshList -> {
                            listViewModel.onAction(HangoutsListAction.Refresh)
                        }
                    }
                }

                HangoutsListRoot(
                    selectedHangoutId = sharedState.selectedHangoutId,
                    onHangoutClick = { hangoutId ->
                        hangoutsListDetailViewModel.onAction(HangoutsListDetailAction.OnSelectHangout(hangoutId))
                        scope.launch {
                            scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
                        }
                    },
                    onCreateHangoutClick = {
                        hangoutsListDetailViewModel.onAction(HangoutsListDetailAction.OnCreateHangoutClick)
                    },
                    navigateToNotifications = navigateToNotifications,
                    unreadNotificationCount = unreadNotificationCount,
                    mainShellPadding = mainShellPadding,
                    viewModel = listViewModel
                )
            }
        },
        detailPane = {
            AnimatedPane {
                HangoutDetailRoot(
                    hangoutId = sharedState.selectedHangoutId,
                    isDetailPaneFullScreen = isDetailPaneFullScreen,
                    navigateBack = {
                        scope.launch {
                            if (scaffoldNavigator.canNavigateBack()) {
                                scaffoldNavigator.navigateBack()
                            }
                        }
                    },
                    onHangoutLeft = {
                        scope.launch {
                            if (scaffoldNavigator.canNavigateBack()) {
                                scaffoldNavigator.navigateBack()
                            }
                            hangoutsListDetailViewModel.onAction(HangoutsListDetailAction.OnSelectHangout(null))
                            hangoutsListDetailViewModel.onAction(HangoutsListDetailAction.RefreshList)
                        }
                    },
                    onEditHangoutClick = { hangout ->
                        hangoutsListDetailViewModel.onAction(HangoutsListDetailAction.OnEditHangoutClick(hangout))
                    }
                )
            }
        }
    )

    CreateHangoutRoot(
        visible = sharedState.sheetState is SheetState.CreateHangout || sharedState.sheetState is SheetState.EditHangout,
        originalHangout = (sharedState.sheetState as? SheetState.EditHangout)?.hangout,
        onDismiss = { hangoutsListDetailViewModel.onAction(HangoutsListDetailAction.OnDismissCurrentSheet) },
        onSuccess = { hangoutId ->
            val wasEdit = sharedState.sheetState is SheetState.EditHangout

            hangoutsListDetailViewModel.onAction(HangoutsListDetailAction.OnDismissCurrentSheet)
            if (!wasEdit) {
                hangoutsListDetailViewModel.onAction(HangoutsListDetailAction.RefreshList)
            }
            hangoutsListDetailViewModel.onAction(HangoutsListDetailAction.OnSelectHangout(hangoutId))
            scope.launch {
                scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
            }
        }
    )
}
