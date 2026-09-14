package com.eeseka.lynk.hangouts.presentation.hangouts_list

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.SlidersHorizontal
import com.eeseka.lynk.hangouts.presentation.hangouts_list.components.HangoutSummaryCard
import com.eeseka.lynk.hangouts.presentation.hangouts_list.components.HangoutsEmptyState
import com.eeseka.lynk.hangouts.presentation.hangouts_list.components.HangoutsSearchEmptyState
import com.eeseka.lynk.hangouts.presentation.hangouts_list.components.NotificationBell
import com.eeseka.lynk.hangouts.presentation.mappers.getTitle
import com.eeseka.lynk.hangouts.presentation.model.HangoutStatusFilter
import com.eeseka.lynk.shared.design_system.components.buttons.LynkFloatingActionButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkTonalIconButton
import com.eeseka.lynk.shared.design_system.components.layouts.LynkScaffold
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDropDownItem
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDropDownMenu
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFlashType
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.showFlashMessage
import com.eeseka.lynk.shared.design_system.components.navigation.LynkIosBarButtonItem
import com.eeseka.lynk.shared.design_system.components.navigation.LynkTopAppBar
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.textfields.LynkSearchField
import com.eeseka.lynk.shared.design_system.components.toggles_and_control.LynkSegmentedControl
import com.eeseka.lynk.shared.design_system.components.toggles_and_control.LynkSegmentedItem
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.presentation.components.GuestPromptSheet
import com.eeseka.lynk.shared.presentation.components.LynkErrorState
import com.eeseka.lynk.shared.presentation.hangout.mappers.getIcon
import com.eeseka.lynk.shared.presentation.hangout.mappers.getTitle
import com.eeseka.lynk.shared.presentation.permissions.Permission
import com.eeseka.lynk.shared.presentation.permissions.PermissionState
import com.eeseka.lynk.shared.presentation.permissions.rememberPermissionController
import com.eeseka.lynk.shared.presentation.preview.previewHangoutSummaries
import com.eeseka.lynk.shared.presentation.util.ObserveAsEvents
import com.eeseka.lynk.shared.presentation.util.PaginationScrollListener
import com.eeseka.lynk.shared.presentation.util.UiText
import com.eeseka.lynk.shared.presentation.util.clearFocusOnTap
import com.eeseka.lynk.shared.presentation.util.currentDeviceConfiguration
import com.eeseka.lynk.shared.presentation.util.toDateTimeLabel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.any_vibe
import lynk.feature.hangouts.generated.resources.create_a_hangout
import lynk.feature.hangouts.generated.resources.create_hangout
import lynk.feature.hangouts.generated.resources.filter_vibe
import lynk.feature.hangouts.generated.resources.hangouts
import lynk.feature.hangouts.generated.resources.hangouts_load_error_title
import lynk.feature.hangouts.generated.resources.search_cancelled_hangouts_hint
import lynk.feature.hangouts.generated.resources.search_completed_hangouts_hint
import lynk.feature.hangouts.generated.resources.search_ongoing_hangouts_hint
import lynk.feature.hangouts.generated.resources.search_upcoming_hangouts_hint
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HangoutsListRoot(
    selectedHangoutId: String?,
    onHangoutClick: (String?) -> Unit,
    onCreateHangoutClick: () -> Unit,
    navigateToNotifications: () -> Unit,
    unreadNotificationCount: Int,
    mainShellPadding: PaddingValues,
    viewModel: HangoutsListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(selectedHangoutId) {
        viewModel.onAction(HangoutsListAction.OnSelectHangout(selectedHangoutId))
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is HangoutsListEvent.Error -> {
                snackbarHostState.showFlashMessage(
                    message = event.message.asStringAsync(),
                    type = LynkFlashType.Error
                )
            }
        }
    }

    HangoutsListScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is HangoutsListAction.OnSelectHangout -> onHangoutClick(action.hangoutId)
                else -> Unit
            }
            viewModel.onAction(action)
        },
        snackbarHostState = snackbarHostState,
        onCreateHangoutClick = onCreateHangoutClick,
        unreadNotificationCount = unreadNotificationCount,
        navigateToNotifications = navigateToNotifications,
        mainShellPadding = mainShellPadding
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HangoutsListScreen(
    state: HangoutsListState,
    onAction: (HangoutsListAction) -> Unit,
    snackbarHostState: SnackbarHostState,
    onCreateHangoutClick: () -> Unit,
    unreadNotificationCount: Int,
    navigateToNotifications: () -> Unit,
    mainShellPadding: PaddingValues
) {
    val hapticFeedback = rememberAppHaptic()

    val configuration = currentDeviceConfiguration()
    val showRail = configuration.isWideScreen

    val listState = rememberLazyListState()
    var showVibeMenu by remember { mutableStateOf(false) }
    var showGuestPrompt by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val inputModeManager = LocalInputModeManager.current
    var hasTappedSearch by remember { mutableStateOf(false) }

    val permissionController = rememberPermissionController()

    LaunchedEffect(state.currentUserId, state.isGuest) {
        if (state.currentUserId == null || state.isGuest) return@LaunchedEffect

        var permissionState = permissionController.getPermissionState(Permission.NOTIFICATIONS)
        if (permissionState == PermissionState.NOT_DETERMINED || permissionState == PermissionState.DENIED) {
            permissionState = permissionController.requestPermission(Permission.NOTIFICATIONS)
        }
        if (permissionState != PermissionState.GRANTED) {
            onAction(HangoutsListAction.OnNotificationPermissionDenied)
        }
    }

    PaginationScrollListener(
        lazyListState = listState,
        itemCount = state.hangouts.size,
        isPaginationLoading = state.isLoading,
        isEndReached = state.isEndReached,
        onNearBottom = { onAction(HangoutsListAction.LoadNextPage) },
        resetKey = state.searchResetEpoch
    )

    LynkScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            if (!showRail) {
                LynkTopAppBar(
                    title = stringResource(Res.string.hangouts),
                    actions = {
                        if (!state.isGuest) {
                            NotificationBell(
                                unreadCount = unreadNotificationCount,
                                onClick = {
                                    hapticFeedback(AppHaptic.ImpactLight)
                                    navigateToNotifications()
                                }
                            )
                        }
                    },
                    iosTrailingItems = if (state.isGuest) {
                        persistentListOf()
                    } else {
                        persistentListOf(
                            LynkIosBarButtonItem(
                                sfSymbol = if (unreadNotificationCount > 0) "bell.badge" else "bell",
                                onClick = {
                                    hapticFeedback(AppHaptic.ImpactLight)
                                    navigateToNotifications()
                                }
                            )
                        )
                    }
                )
            }
        }
    ) { scaffoldPadding ->
        val listMaxWidth = if (configuration.isMobile) Dp.Unspecified else 640.dp

        Box(
            modifier = Modifier.fillMaxSize().clearFocusOnTap(),
            contentAlignment = Alignment.TopCenter
        ) {
            val isSearchActive = state.searchTextState.text.toString().isNotBlank() || state.selectedVibe != null
            val showEmptyList = !isSearchActive && state.hangouts.isEmpty() && !state.isLoading && state.isEndReached
            val showEmptySearch = isSearchActive && state.hangouts.isEmpty() && !state.isLoading && state.isEndReached
            val showLoadError = state.hangouts.isEmpty() && state.loadError != null && !state.isLoading

            if (showLoadError) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LynkErrorState(
                        title = stringResource(Res.string.hangouts_load_error_title),
                        message = state.loadError.asString(),
                        onRetry = {
                            hapticFeedback(AppHaptic.ImpactLight)
                            onAction(HangoutsListAction.OnRetryClick)
                        }
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(
                        top = scaffoldPadding.calculateTopPadding() + 120.dp,
                        bottom = mainShellPadding.calculateBottomPadding() + 80.dp,
                        start = 16.dp,
                        end = 16.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.widthIn(max = listMaxWidth).fillMaxSize()
                ) {
                    if (showEmptyList) {
                        item {
                            HangoutsEmptyState(currentFilter = state.selectedStatusFilter)
                        }
                    } else if (showEmptySearch) {
                        item {
                            HangoutsSearchEmptyState(
                                modifier = Modifier.padding(top = 64.dp)
                            )
                        }
                    } else {
                        items(items = state.hangouts, key = { it.id }) { hangout ->
                            Box(modifier = Modifier.animateItem()) {
                                HangoutSummaryCard(
                                    hangout = hangout,
                                    scheduledDate = hangout.scheduledAt.toDateTimeLabel(),
                                    isSelected = hangout.id == state.selectedHangoutId,
                                    isHost = hangout.hostId == state.currentUserId,
                                    onClick = {
                                        hapticFeedback(AppHaptic.ImpactLight)
                                        onAction(HangoutsListAction.OnSelectHangout(hangout.id))
                                    }
                                )
                            }
                        }
                    }

                    if (state.isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                LynkProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 480.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = scaffoldPadding.calculateTopPadding(), bottom = 4.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LynkSearchField(
                        state = state.searchTextState,
                        placeholder = when (state.selectedStatusFilter) {
                            HangoutStatusFilter.UPCOMING -> stringResource(Res.string.search_upcoming_hangouts_hint)
                            HangoutStatusFilter.ONGOING -> stringResource(Res.string.search_ongoing_hangouts_hint)
                            HangoutStatusFilter.COMPLETED -> stringResource(Res.string.search_completed_hangouts_hint)
                            HangoutStatusFilter.CANCELLED -> stringResource(Res.string.search_cancelled_hangouts_hint)
                        },
                        modifier = Modifier
                            .weight(1f)
                            // The pane hands this field focus on entry; take it back unless tapped.
                            // Touch only: with a keyboard, focus arriving is navigation
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    awaitFirstDown(
                                        requireUnconsumed = false,
                                        pass = PointerEventPass.Initial
                                    )
                                    hasTappedSearch = true
                                }
                            }
                            .onFocusChanged {
                                if (it.isFocused && !hasTappedSearch && inputModeManager.inputMode == InputMode.Touch) {
                                    focusManager.clearFocus(force = true)
                                }
                            }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    LynkDropDownMenu(
                        expanded = showVibeMenu,
                        onDismissRequest = { showVibeMenu = false },
                        items = (listOf(
                            LynkDropDownItem(
                                title = stringResource(Res.string.any_vibe),
                                icon = if (state.selectedVibe == null) Lucide.Check else null,
                                sfSymbol = if (state.selectedVibe == null) "checkmark" else null,
                                onClick = {
                                    hapticFeedback(AppHaptic.Selection)
                                    onAction(HangoutsListAction.OnVibeSelected(null))
                                    showVibeMenu = false
                                }
                            )
                        ) + HangoutVibe.entries.map { vibe ->
                            val isSelected = state.selectedVibe == vibe
                            LynkDropDownItem(
                                title = vibe.getTitle(),
                                icon = if (isSelected) Lucide.Check else vibe.getIcon(),
                                sfSymbol = if (isSelected) "checkmark" else null,
                                onClick = {
                                    hapticFeedback(AppHaptic.Selection)
                                    onAction(HangoutsListAction.OnVibeSelected(vibe))
                                    showVibeMenu = false
                                }
                            )
                        }).toImmutableList(),
                        anchor = {
                            val isActive = state.selectedVibe != null
                            LynkTonalIconButton(
                                onClick = {
                                    hapticFeedback(AppHaptic.ImpactLight)
                                    showVibeMenu = true
                                },
                                containerColor = if (isActive) MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isActive) MaterialTheme.colorScheme.onSecondaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            ) {
                                Icon(
                                    imageVector = Lucide.SlidersHorizontal,
                                    contentDescription = stringResource(Res.string.filter_vibe)
                                )
                            }
                        }
                    )

                    if (showRail && !state.isGuest) {
                        Spacer(modifier = Modifier.width(12.dp))

                        NotificationBell(
                            unreadCount = unreadNotificationCount,
                            onClick = {
                                hapticFeedback(AppHaptic.ImpactLight)
                                navigateToNotifications()
                            },
                            isTonal = true
                        )
                    }
                }

                // Status filter chips
                LynkSegmentedControl(
                    items = HangoutStatusFilter.entries.map { filter ->
                        LynkSegmentedItem(title = filter.getTitle())
                    }.toImmutableList(),
                    selectedIndex = HangoutStatusFilter.entries.indexOf(state.selectedStatusFilter),
                    onItemSelected = { index ->
                        hapticFeedback(AppHaptic.Selection)
                        onAction(HangoutsListAction.OnStatusFilterSelected(HangoutStatusFilter.entries[index]))
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            LynkFloatingActionButton(
                onClick = {
                    hapticFeedback(AppHaptic.ImpactMedium)
                    if (state.isGuest) {
                        showGuestPrompt = true
                    } else {
                        onCreateHangoutClick()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        bottom = mainShellPadding.calculateBottomPadding() + 16.dp,
                        end = 16.dp
                    )
            ) {
                Icon(
                    imageVector = Lucide.Plus,
                    contentDescription = stringResource(Res.string.create_hangout)
                )
            }
        }

        if (showGuestPrompt) {
            GuestPromptSheet(
                actionStr = stringResource(Res.string.create_a_hangout),
                onCreateAccountClick = { onAction(HangoutsListAction.SignOutGuest) },
                onDismissRequest = { showGuestPrompt = false },
                isLoading = state.isGuestSigningOut
            )
        }
    }
}

@PreviewLightDark
@Preview(name = "Tablet landscape", widthDp = 1280, heightDp = 800)
@Composable
private fun HangoutsListScreenFilledPreview() =
    HangoutsListScreenPreview(HangoutsListState(hangouts = previewHangoutSummaries))

@PreviewLightDark
@Composable
private fun HangoutsListScreenErrorPreview() = HangoutsListScreenPreview(
    HangoutsListState(
        loadError = UiText.DynamicString(
            "Couldn't reach the server. Check your connection and try again."
        )
    )
)

@Composable
private fun HangoutsListScreenPreview(state: HangoutsListState) {
    LynkTheme {
        HangoutsListScreen(
            state = state,
            snackbarHostState = remember { SnackbarHostState() },
            onCreateHangoutClick = {},
            unreadNotificationCount = 3,
            navigateToNotifications = {},
            onAction = {},
            mainShellPadding = PaddingValues()
        )
    }
}
