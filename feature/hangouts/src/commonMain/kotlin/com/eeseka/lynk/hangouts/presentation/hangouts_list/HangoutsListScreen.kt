package com.eeseka.lynk.hangouts.presentation.hangouts_list

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.SlidersHorizontal
import com.composables.icons.lucide.UserRoundSearch
import com.eeseka.lynk.hangouts.presentation.hangouts_list.components.HangoutSummaryCard
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
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
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
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.any_vibe
import lynk.feature.hangouts.generated.resources.create_a_hangout
import lynk.feature.hangouts.generated.resources.create_hangout
import lynk.feature.hangouts.generated.resources.empty_hangouts_cancelled_message
import lynk.feature.hangouts.generated.resources.empty_hangouts_cancelled_title
import lynk.feature.hangouts.generated.resources.empty_hangouts_completed_message
import lynk.feature.hangouts.generated.resources.empty_hangouts_completed_title
import lynk.feature.hangouts.generated.resources.empty_hangouts_ongoing_message
import lynk.feature.hangouts.generated.resources.empty_hangouts_ongoing_title
import lynk.feature.hangouts.generated.resources.empty_hangouts_upcoming_message
import lynk.feature.hangouts.generated.resources.empty_hangouts_upcoming_title
import lynk.feature.hangouts.generated.resources.empty_search_message
import lynk.feature.hangouts.generated.resources.empty_search_title
import lynk.feature.hangouts.generated.resources.filter_vibe
import lynk.feature.hangouts.generated.resources.hangouts
import lynk.feature.hangouts.generated.resources.search_hangouts_hint
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

private const val ANIMATION_BORED_MAN = "bored_man.json"

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


            Column(modifier = Modifier.align(Alignment.TopCenter)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = scaffoldPadding.calculateTopPadding(), bottom = 4.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LynkSearchField(
                        state = state.searchTextState,
                        placeholder = stringResource(Res.string.search_hangouts_hint),
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

@Composable
private fun HangoutsEmptyState(
    currentFilter: HangoutStatusFilter,
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/$ANIMATION_BORED_MAN").decodeToString()
        )
    }

    val titleRes = when (currentFilter) {
        HangoutStatusFilter.UPCOMING -> Res.string.empty_hangouts_upcoming_title
        HangoutStatusFilter.ONGOING -> Res.string.empty_hangouts_ongoing_title
        HangoutStatusFilter.COMPLETED -> Res.string.empty_hangouts_completed_title
        HangoutStatusFilter.CANCELLED -> Res.string.empty_hangouts_cancelled_title
    }

    val messageRes = when (currentFilter) {
        HangoutStatusFilter.UPCOMING -> Res.string.empty_hangouts_upcoming_message
        HangoutStatusFilter.ONGOING -> Res.string.empty_hangouts_ongoing_message
        HangoutStatusFilter.COMPLETED -> Res.string.empty_hangouts_completed_message
        HangoutStatusFilter.CANCELLED -> Res.string.empty_hangouts_cancelled_message
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = rememberLottiePainter(
                    composition = composition,
                    iterations = Compottie.IterateForever
                ),
                contentDescription = null,
                modifier = Modifier.size(300.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LynkText(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            LynkText(
                text = stringResource(messageRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
private fun HangoutsSearchEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Lucide.UserRoundSearch,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            LynkText(
                text = stringResource(Res.string.empty_search_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            LynkText(
                text = stringResource(Res.string.empty_search_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

internal val previewHangouts = listOf(
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
).mapIndexed { index, (name, vibe) ->
    HangoutSummaryUi(
        id = "preview-$index",
        hostId = "host-1",
        name = name,
        vibe = vibe,
        status = HangoutStatus.entries[index % HangoutStatus.entries.size],
        scheduledAt = Instant.fromEpochSeconds(1_800_000_000L + index * 86_400L),
        maxAttendees = if (index % 3 == 0) null else 4 + index % 8,
        participantCount = 1 + index % 5,
        createdAt = Instant.fromEpochSeconds(1_790_000_000L + index * 86_400L)
    )
}

@PreviewLightDark
@Composable
private fun HangoutsListScreenFilledPreview() {
    LynkTheme {
        HangoutsListScreen(
            state = HangoutsListState(hangouts = previewHangouts),
            events = emptyFlow(),
            onCreateHangoutClick = {},
            onAction = {},
            mainShellPadding = PaddingValues()
        )
    }
}

@PreviewLightDark
@Composable
private fun HangoutsEmptyStatePreview() {
    LynkTheme {
        HangoutsEmptyState(
            currentFilter = HangoutStatusFilter.UPCOMING,
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        )
    }
}

@PreviewLightDark
@Composable
private fun HangoutsSearchEmptyStatePreview() {
    LynkTheme {
        HangoutsSearchEmptyState(modifier = Modifier.background(MaterialTheme.colorScheme.background))
    }
}