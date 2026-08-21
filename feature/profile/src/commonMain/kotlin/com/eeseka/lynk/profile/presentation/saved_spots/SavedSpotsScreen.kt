package com.eeseka.lynk.profile.presentation.saved_spots

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.profile.presentation.saved_spots.components.SavedSpotListItem
import com.eeseka.lynk.profile.presentation.saved_spots.components.SavedSpotsEmptyState
import com.eeseka.lynk.profile.presentation.saved_spots.components.SavedSpotsSearchEmptyState
import com.eeseka.lynk.shared.design_system.components.buttons.LynkIconButton
import com.eeseka.lynk.shared.design_system.components.layouts.LynkScaffold
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFlashType
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.showFlashMessage
import com.eeseka.lynk.shared.design_system.components.navigation.LynkIosBarButtonItem
import com.eeseka.lynk.shared.design_system.components.navigation.LynkTopAppBar
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.textfields.LynkSearchField
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.presentation.components.SpotDetailSheet
import com.eeseka.lynk.shared.presentation.util.ObserveAsEvents
import com.eeseka.lynk.shared.presentation.util.PaginationScrollListener
import com.eeseka.lynk.shared.presentation.util.clearFocusOnTap
import com.eeseka.lynk.shared.presentation.util.currentDeviceConfiguration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import lynk.feature.profile.generated.resources.Res
import lynk.feature.profile.generated.resources.back
import lynk.feature.profile.generated.resources.saved_spots
import lynk.feature.profile.generated.resources.saved_spots_search_placeholder
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedSpotsScreen(
    state: SavedSpotsState,
    events: Flow<SavedSpotsEvent>,
    onAction: (SavedSpotsAction) -> Unit,
    onNavigateBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val hapticFeedback = rememberAppHaptic()
    val listState = rememberLazyListState()

    ObserveAsEvents(events) { event ->
        when (event) {
            is SavedSpotsEvent.Error -> {
                hapticFeedback(AppHaptic.Error)
                snackbarHostState.showFlashMessage(
                    message = event.message.asStringAsync(),
                    type = LynkFlashType.Error
                )
            }
        }
    }

    PaginationScrollListener(
        lazyListState = listState,
        itemCount = state.spots.size,
        isPaginationLoading = state.isLoading,
        isEndReached = state.isEndReached,
        onNearBottom = { onAction(SavedSpotsAction.LoadNextPage) },
        resetKey = state.searchResetEpoch
    )

    LynkScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            val backLabel = stringResource(Res.string.back)

            LynkTopAppBar(
                title = stringResource(Res.string.saved_spots),
                navigationIcon = {
                    LynkIconButton(
                        onClick = {
                            hapticFeedback(AppHaptic.ImpactLight)
                            onNavigateBack()
                        }
                    ) {
                        Icon(
                            imageVector = Lucide.ChevronLeft,
                            contentDescription = backLabel
                        )
                    }
                },
                iosLeadingItems = listOf(
                    LynkIosBarButtonItem(
                        sfSymbol = "chevron.left",
                        onClick = {
                            hapticFeedback(AppHaptic.ImpactLight)
                            onNavigateBack()
                        }
                    )
                )
            )
        }
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clearFocusOnTap(),
            contentAlignment = Alignment.TopCenter
        ) {
            val configuration = currentDeviceConfiguration()
            val listMaxWidth = if (configuration.isMobile) Dp.Unspecified else 640.dp

            val isSearchActive = state.searchTextState.text.toString().isNotBlank()
            val showEmptyList =
                !isSearchActive && state.spots.isEmpty() && !state.isLoading && state.isEndReached
            val showEmptySearch =
                isSearchActive && state.spots.isEmpty() && !state.isLoading && state.isEndReached

            LazyColumn(
                state = listState,
                modifier = Modifier.widthIn(max = listMaxWidth).fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = scaffoldPadding.calculateTopPadding() + 72.dp,
                    bottom = scaffoldPadding.calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (showEmptyList) {
                    item {
                        SavedSpotsEmptyState()
                    }
                } else if (showEmptySearch) {
                    item {
                        SavedSpotsSearchEmptyState(
                            modifier = Modifier.padding(top = 64.dp)
                        )
                    }
                } else {
                    items(state.spots, key = { it.id }) { spot ->
                        Box(modifier = Modifier.animateItem()) {
                            SavedSpotListItem(
                                spotName = spot.name,
                                spotPhotos = spot.photoUrls,
                                spotAddress = spot.shortAddress,
                                spotCategory = spot.category,
                                spotPriceLevel = spot.priceLevel,
                                spotRating = spot.rating,
                                isSaved = spot.isSaved,
                                onClick = { onAction(SavedSpotsAction.OnSpotSelected(spot.id)) },
                                onToggleSave = {
                                    hapticFeedback(AppHaptic.Selection)
                                    onAction(
                                        SavedSpotsAction.OnToggleSaveSpot(spot.id, spot.isSaved)
                                    )
                                }
                            )
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

            LynkSearchField(
                state = state.searchTextState,
                placeholder = stringResource(Res.string.saved_spots_search_placeholder),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .padding(top = scaffoldPadding.calculateTopPadding())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }

    if (state.selectedSpotId != null) {
        state.spots.find { it.id == state.selectedSpotId }?.let { selectedSpot ->
            SpotDetailSheet(
                spot = selectedSpot,
                userLat = null,
                userLng = null,
                onDismissRequest = { onAction(SavedSpotsAction.OnDismissSpotDetail) },
                onToggleSave = { spotId, isCurrentlySaved ->
                    onAction(SavedSpotsAction.OnToggleSaveSpot(spotId, isCurrentlySaved))
                }
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun SavedSpotsScreenEmptyPreview() {
    LynkTheme {
        SavedSpotsScreen(
            state = SavedSpotsState(),
            events = flowOf(),
            onAction = {},
            onNavigateBack = {}
        )
    }
}