package com.eeseka.lynk.hangouts.presentation.hangouts_list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.SlidersHorizontal
import com.eeseka.lynk.hangouts.presentation.hangouts_list.components.HangoutSummaryCard
import com.eeseka.lynk.hangouts.presentation.hangouts_list.components.HangoutsEmptyState
import com.eeseka.lynk.hangouts.presentation.hangouts_list.components.HangoutsSearchEmptyState
import com.eeseka.lynk.hangouts.presentation.mappers.getTitle
import com.eeseka.lynk.hangouts.presentation.model.HangoutStatusFilter
import com.eeseka.lynk.shared.design_system.components.buttons.LynkFloatingActionButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkTonalIconButton
import com.eeseka.lynk.shared.design_system.components.layouts.LynkScaffold
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDropDownItem
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDropDownMenu
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFlashType
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.showFlashMessage
import com.eeseka.lynk.shared.design_system.components.navigation.LynkTopAppBar
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.textfields.LynkSearchField
import com.eeseka.lynk.shared.design_system.components.toggles_and_control.LynkSegmentedControl
import com.eeseka.lynk.shared.design_system.components.toggles_and_control.LynkSegmentedItem
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.hangout.model.HangoutStatus
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.presentation.components.GuestPromptSheet
import com.eeseka.lynk.shared.presentation.hangout.mappers.getIcon
import com.eeseka.lynk.shared.presentation.hangout.mappers.getTitle
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutSummaryUi
import com.eeseka.lynk.shared.presentation.util.ObserveAsEvents
import com.eeseka.lynk.shared.presentation.util.PaginationScrollListener
import com.eeseka.lynk.shared.presentation.util.clearFocusOnTap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.any_vibe
import lynk.feature.hangouts.generated.resources.create_a_hangout
import lynk.feature.hangouts.generated.resources.create_hangout
import lynk.feature.hangouts.generated.resources.filter_vibe
import lynk.feature.hangouts.generated.resources.hangouts
import lynk.feature.hangouts.generated.resources.search_cancelled_hangouts_hint
import lynk.feature.hangouts.generated.resources.search_completed_hangouts_hint
import lynk.feature.hangouts.generated.resources.search_ongoing_hangouts_hint
import lynk.feature.hangouts.generated.resources.search_upcoming_hangouts_hint
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HangoutsListScreen(
    state: HangoutsListState,
    events: Flow<HangoutsListEvent>,
    onAction: (HangoutsListAction) -> Unit,
    onCreateHangoutClick: () -> Unit,
    mainShellPadding: PaddingValues
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val hapticFeedback = rememberAppHaptic()

    val listState = rememberLazyListState()
    var showVibeMenu by remember { mutableStateOf(false) }
    var showGuestPrompt by remember { mutableStateOf(false) }

    ObserveAsEvents(events) { event ->
        when (event) {
            is HangoutsListEvent.Error -> {
                hapticFeedback(AppHaptic.Error)
                snackbarHostState.showFlashMessage(
                    message = event.error.asStringAsync(),
                    type = LynkFlashType.Error
                )
            }
        }
    }

    PaginationScrollListener(
        lazyListState = listState,
        itemCount = state.hangouts.size,
        isPaginationLoading = state.isSearchLoading,
        isEndReached = state.isSearchEndReached,
        onNearBottom = { onAction(HangoutsListAction.LoadNextPage) },
        resetKey = state.searchResetEpoch
    )

    LynkScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            LynkTopAppBar(title = stringResource(Res.string.hangouts))
        }
    ) { scaffoldPadding ->
        Box(modifier = Modifier.fillMaxSize().clearFocusOnTap()) {
            val isSearchActive =
                state.searchTextState.text.toString().isNotBlank() || state.selectedVibe != null
            val showEmptyList =
                !isSearchActive && state.hangouts.isEmpty() && !state.isSearchLoading && state.isSearchEndReached
            val showEmptySearch =
                isSearchActive && state.hangouts.isEmpty() && !state.isSearchLoading && state.isSearchEndReached

            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    top = scaffoldPadding.calculateTopPadding() + 120.dp,
                    bottom = mainShellPadding.calculateBottomPadding() + 80.dp,
                    start = 16.dp,
                    end = 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
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
                                isSelected = hangout.id == state.selectedHangoutId,
                                isHost = hangout.hostId == state.currentUserId,
                                onClick = {
                                    hapticFeedback(AppHaptic.ImpactMedium)
                                    onAction(HangoutsListAction.OnSelectHangout(hangout.id))
                                }
                            )
                        }
                    }
                }

                if (state.isSearchLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            LynkProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
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
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    LynkDropDownMenu(
                        expanded = showVibeMenu,
                        onDismissRequest = { showVibeMenu = false },
                        items = listOf(
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
                        },
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
                }

                // Status filter chips
                LynkSegmentedControl(
                    items = HangoutStatusFilter.entries.map { filter ->
                        LynkSegmentedItem(title = filter.getTitle())
                    },
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
                    .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.End))
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

private val previewNames = listOf(
    "Late Night Drinks" to HangoutVibe.DRINKS,
    "Sunday BBQ" to HangoutVibe.FOOD,
    "Chill at Mine" to HangoutVibe.CHILL,
    "FIFA Tournament" to HangoutVibe.GAMING,
    "Rooftop Party" to HangoutVibe.PARTY,
    "Hiking Trip" to HangoutVibe.ACTIVE,
    "Board Game Night" to HangoutVibe.OTHER,
    "Brunch Run" to HangoutVibe.FOOD,
    "Go-Kart Race" to HangoutVibe.ACTIVE,
    "Movie Night" to HangoutVibe.CHILL
)

private fun previewHangouts(filter: HangoutStatusFilter): List<HangoutSummaryUi> {
    val statuses = when (filter) {
        HangoutStatusFilter.UPCOMING -> listOf(HangoutStatus.VOTING, HangoutStatus.SCHEDULED)
        HangoutStatusFilter.ONGOING -> listOf(HangoutStatus.ONGOING)
        HangoutStatusFilter.COMPLETED -> listOf(HangoutStatus.COMPLETED)
        HangoutStatusFilter.CANCELLED -> listOf(HangoutStatus.CANCELLED)
    }
    return previewNames.mapIndexed { index, (name, vibe) ->
        HangoutSummaryUi(
            id = "preview-$index",
            hostId = "host-1",
            name = name,
            vibe = vibe,
            status = statuses[index % statuses.size],
            scheduledAt = Instant.fromEpochSeconds(1_800_000_000L + index * 86_400L),
            maxAttendees = if (index % 3 == 0) null else 4 + index % 8,
            participantCount = 1 + index % 5,
            createdAt = Instant.fromEpochSeconds(1_790_000_000L + index * 86_400L)
        )
    }
}

@Composable
private fun HangoutsListScreenPreview(filter: HangoutStatusFilter) {
    LynkTheme {
        HangoutsListScreen(
            state = HangoutsListState(
                hangouts = previewHangouts(filter),
                selectedStatusFilter = filter
            ),
            events = emptyFlow(),
            onCreateHangoutClick = {},
            onAction = {},
            mainShellPadding = PaddingValues()
        )
    }
}

@PreviewLightDark
@Composable
private fun HangoutsListScreenUpcomingPreview() =
    HangoutsListScreenPreview(HangoutStatusFilter.UPCOMING)

@PreviewLightDark
@Composable
private fun HangoutsListScreenOngoingPreview() =
    HangoutsListScreenPreview(HangoutStatusFilter.ONGOING)

@PreviewLightDark
@Composable
private fun HangoutsListScreenCompletedPreview() =
    HangoutsListScreenPreview(HangoutStatusFilter.COMPLETED)
