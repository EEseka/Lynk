package com.eeseka.lynk.hangouts.presentation.hangout_detail.voting.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.then
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.UsersRound
import com.eeseka.lynk.hangouts.presentation.hangout_detail.voting.HangoutVotingState
import com.eeseka.lynk.hangouts.presentation.hangout_detail.voting.model.SearchTab
import com.eeseka.lynk.hangouts.presentation.mappers.getIcon
import com.eeseka.lynk.hangouts.presentation.mappers.getTitle
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkTonalIconButton
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkAdaptiveSheet
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.textfields.LynkSearchField
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.textfields.LynkTextField
import com.eeseka.lynk.shared.design_system.components.textfields.SingleLineInputTransformation
import com.eeseka.lynk.shared.design_system.components.toggles_and_control.LynkSegmentedControl
import com.eeseka.lynk.shared.design_system.components.toggles_and_control.LynkSegmentedItem
import com.eeseka.lynk.shared.design_system.components.toggles_and_control.LynkSegmentedStyle
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.hangout.HangoutConstants.MAX_SPOT_NOTE_LENGTH
import com.eeseka.lynk.shared.domain.location.LocationCoordinates
import com.eeseka.lynk.shared.presentation.preview.previewSpots
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import com.eeseka.lynk.shared.presentation.util.PaginationScrollListener
import com.eeseka.lynk.shared.presentation.util.clearFocusOnTap
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.propose_back
import lynk.feature.hangouts.generated.resources.propose_center_caption
import lynk.feature.hangouts.generated.resources.propose_empty_favorite_message
import lynk.feature.hangouts.generated.resources.propose_empty_message
import lynk.feature.hangouts.generated.resources.propose_favorite_search_hint
import lynk.feature.hangouts.generated.resources.propose_note_counter
import lynk.feature.hangouts.generated.resources.propose_note_hint
import lynk.feature.hangouts.generated.resources.propose_note_label
import lynk.feature.hangouts.generated.resources.propose_note_title
import lynk.feature.hangouts.generated.resources.propose_suggest_action
import lynk.feature.hangouts.generated.resources.propose_suggesting
import lynk.feature.hangouts.generated.resources.propose_search_hint
import lynk.feature.hangouts.generated.resources.propose_sheet_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProposeSpotSheet(
    state: HangoutVotingState,
    onTabSelected: (SearchTab) -> Unit,
    onSuggestSpotClick: (SpotUi) -> Unit,
    onBackToSpotSearch: () -> Unit,
    onPropose: () -> Unit,
    onLoadNextSpotPage: () -> Unit,
    onLoadNextFavoritePage: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LynkAdaptiveSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        AnimatedContent(
            targetState = state.spotToSuggest,
            contentKey = { it == null },
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
            label = "propose_spot_step"
        ) { spotToSuggest ->
            if (spotToSuggest == null) {
                ProposeSpotSheetContent(
                    state = state,
                    onTabSelected = onTabSelected,
                    onSuggestSpotClick = onSuggestSpotClick,
                    onLoadNextSpotPage = onLoadNextSpotPage,
                    onLoadNextFavoritePage = onLoadNextFavoritePage
                )
            } else {
                SpotNoteContent(
                    spot = spotToSuggest,
                    noteState = state.spotNoteState,
                    isProposing = spotToSuggest.id in state.proposingSpotIds,
                    onBack = onBackToSpotSearch,
                    onPropose = onPropose
                )
            }
        }
    }
}

@Composable
private fun ProposeSpotSheetContent(
    state: HangoutVotingState,
    onTabSelected: (SearchTab) -> Unit,
    onSuggestSpotClick: (SpotUi) -> Unit,
    onLoadNextSpotPage: () -> Unit,
    onLoadNextFavoritePage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = rememberAppHaptic()
    val allSpotsListState = rememberLazyListState()
    val favoritesListState = rememberLazyListState()

    val originLat = state.center?.latitude ?: state.myLocation?.latitude
    val originLng = state.center?.longitude ?: state.myLocation?.longitude

    val candidateSpotIds = remember(state.candidates) {
        state.candidates.mapTo(mutableSetOf()) { it.spot.id }.toImmutableSet()
    }

    val isSearchActive = state.proposeSpotSheetSearchTextState.text.toString().isNotBlank()

    PaginationScrollListener(
        lazyListState = allSpotsListState,
        itemCount = if (isSearchActive) state.spotSearchResults.size else state.trendingSpots.size,
        isPaginationLoading = if (isSearchActive) state.isSpotSearchLoading else state.isTrendingLoading,
        isEndReached = if (isSearchActive) state.spotSearchEndReached else true,
        onNearBottom = { if (isSearchActive) onLoadNextSpotPage() },
        resetKey = state.spotSearchResetEpoch
    )

    PaginationScrollListener(
        lazyListState = favoritesListState,
        itemCount = state.favoriteSpotSearchResults.size,
        isPaginationLoading = state.isFavoriteSpotSearchLoading,
        isEndReached = state.favoriteSpotSearchEndReached,
        onNearBottom = onLoadNextFavoritePage,
        resetKey = state.favoriteSearchResetEpoch
    )

    val currentError =
        if (state.activeProposeSpotSheetSearchTab == SearchTab.ALL_SPOTS) state.spotSearchError
        else state.favoriteSpotSearchError

    val tabs = SearchTab.entries

    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            LynkText(
                text = stringResource(Res.string.propose_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Lucide.UsersRound,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(12.dp)
                )
                LynkText(
                    text = stringResource(Res.string.propose_center_caption),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(modifier = Modifier.weight(1f)) {
            when (state.activeProposeSpotSheetSearchTab) {
                SearchTab.ALL_SPOTS -> {
                    val spotsToShow =
                        if (isSearchActive) state.spotSearchResults else state.trendingSpots
                    val isLoading =
                        if (isSearchActive) state.isSpotSearchLoading else state.isTrendingLoading
                    val isEndReached = if (isSearchActive) state.spotSearchEndReached else true
                    val showEmptyState =
                        isSearchActive && spotsToShow.isEmpty() && !isLoading && isEndReached

                    SuggestSpotList(
                        listState = allSpotsListState,
                        spots = spotsToShow,
                        originLat = originLat,
                        originLng = originLng,
                        isLoading = isLoading,
                        showEmptyState = showEmptyState,
                        emptyStateMessage = stringResource(Res.string.propose_empty_message),
                        proposingSpotIds = state.proposingSpotIds,
                        candidateSpotIds = candidateSpotIds,
                        onSuggest = onSuggestSpotClick
                    )
                }

                SearchTab.FAVORITES -> {
                    val spotsToShow = state.favoriteSpotSearchResults
                    val showEmptyState =
                        spotsToShow.isEmpty() && !state.isFavoriteSpotSearchLoading && state.favoriteSpotSearchEndReached

                    SuggestSpotList(
                        listState = favoritesListState,
                        spots = spotsToShow,
                        originLat = originLat,
                        originLng = originLng,
                        isLoading = state.isFavoriteSpotSearchLoading,
                        showEmptyState = showEmptyState,
                        emptyStateMessage = stringResource(Res.string.propose_empty_favorite_message),
                        proposingSpotIds = state.proposingSpotIds,
                        candidateSpotIds = candidateSpotIds,
                        onSuggest = onSuggestSpotClick
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(top = 4.dp)) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    LynkSearchField(
                        state = state.proposeSpotSheetSearchTextState,
                        placeholder = when (state.activeProposeSpotSheetSearchTab) {
                            SearchTab.ALL_SPOTS -> stringResource(Res.string.propose_search_hint)
                            SearchTab.FAVORITES -> stringResource(Res.string.propose_favorite_search_hint)
                        }
                    )

                    AnimatedVisibility(
                        visible = currentError != null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        LynkText(
                            text = currentError?.asString() ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp, start = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LynkSegmentedControl(
                    items = tabs.map {
                        LynkSegmentedItem(
                            title = it.getTitle(),
                            icon = it.getIcon()
                        )
                    }.toImmutableList(),
                    selectedIndex = tabs.indexOf(state.activeProposeSpotSheetSearchTab),
                    onItemSelected = { index ->
                        hapticFeedback(AppHaptic.Selection)
                        onTabSelected(tabs[index])
                    },
                    style = LynkSegmentedStyle.SCROLLABLE_CHIPS,
                    contentPadding = PaddingValues(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun SuggestSpotList(
    listState: LazyListState,
    spots: ImmutableList<SpotUi>,
    originLat: Double?,
    originLng: Double?,
    isLoading: Boolean,
    showEmptyState: Boolean,
    emptyStateMessage: String,
    proposingSpotIds: ImmutableSet<String>,
    candidateSpotIds: ImmutableSet<String>,
    onSuggest: (SpotUi) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 120.dp,
            bottom = 48.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showEmptyState) {
            item { EmptySearchState(emptyStateMessage) }
        } else {
            items(spots, key = { it.id }) { spot ->
                Box(modifier = Modifier.animateItem()) {
                    SuggestSpotListItem(
                        spot = spot,
                        originLat = originLat,
                        originLng = originLng,
                        isProposing = spot.id in proposingSpotIds,
                        alreadyAdded = spot.id in candidateSpotIds,
                        onSuggest = { onSuggest(spot) }
                    )
                }
            }
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
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
}

@Composable
private fun SpotNoteContent(
    spot: SpotUi,
    noteState: TextFieldState,
    isProposing: Boolean,
    onBack: () -> Unit,
    onPropose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = rememberAppHaptic()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clearFocusOnTap()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LynkTonalIconButton(
                onClick = {
                    hapticFeedback(AppHaptic.ImpactLight)
                    onBack()
                }
            ) {
                Icon(
                    imageVector = Lucide.ArrowLeft,
                    contentDescription = stringResource(Res.string.propose_back)
                )
            }
            LynkText(
                text = stringResource(Res.string.propose_note_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LynkText(
                text = spot.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            spot.shortAddress?.let {
                LynkText(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LynkTextField(
            state = noteState,
            label = stringResource(Res.string.propose_note_label),
            placeholder = stringResource(Res.string.propose_note_hint),
            singleLine = false,
            maxLines = 4,
            helperText = stringResource(Res.string.propose_note_counter, noteState.text.length, MAX_SPOT_NOTE_LENGTH),
            inputTransformation = SingleLineInputTransformation.then(InputTransformation.maxLength(MAX_SPOT_NOTE_LENGTH)),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            )
        )

        LynkButton(
            text = stringResource(Res.string.propose_suggest_action),
            onClick = {
                hapticFeedback(AppHaptic.ImpactMedium)
                onPropose()
            },
            isLoading = isProposing,
            loadingText = stringResource(Res.string.propose_suggesting),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun EmptySearchState(message: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Lucide.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            LynkText(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ProposeSpotSheetTrendingPreview() = ProposeSpotSheetPreview(
    state = HangoutVotingState(
        center = LocationCoordinates(latitude = 6.44, longitude = 3.42),
        trendingSpots = previewSpots
    )
)

@PreviewLightDark
@Composable
private fun ProposeSpotSheetEmptyPreview() = ProposeSpotSheetPreview(
    state = HangoutVotingState(
        center = LocationCoordinates(latitude = 6.44, longitude = 3.42),
        proposeSpotSheetSearchTextState = TextFieldState("xyzzy"),
        spotSearchResults = persistentListOf(),
        spotSearchEndReached = true
    )
)

@PreviewLightDark
@Composable
private fun ProposeSpotSheetLoadingPreview() = ProposeSpotSheetPreview(
    state = HangoutVotingState(
        center = LocationCoordinates(latitude = 6.44, longitude = 3.42),
        proposeSpotSheetSearchTextState = TextFieldState("beach"),
        isSpotSearchLoading = true
    )
)

@PreviewLightDark
@Composable
private fun SpotNoteContentPreview() {
    LynkTheme {
        SpotNoteContent(
            spot = previewSpots.first(),
            noteState = TextFieldState("Live band on Fridays and the jollof is unreal"),
            isProposing = false,
            onBack = {},
            onPropose = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        )
    }
}

@Composable
private fun ProposeSpotSheetPreview(state: HangoutVotingState) {
    LynkTheme {
        ProposeSpotSheetContent(
            state = state,
            onTabSelected = {},
            onSuggestSpotClick = {},
            onLoadNextSpotPage = {},
            onLoadNextFavoritePage = {},
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 20.dp)
        )
    }
}
