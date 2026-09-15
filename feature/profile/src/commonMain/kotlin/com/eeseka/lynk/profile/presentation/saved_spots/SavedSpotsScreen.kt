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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.eeseka.lynk.shared.presentation.components.LynkErrorState
import com.eeseka.lynk.shared.presentation.components.SpotDetailSheet
import com.eeseka.lynk.shared.presentation.preview.previewSpots
import com.eeseka.lynk.shared.presentation.util.ObserveAsEvents
import com.eeseka.lynk.shared.presentation.util.PaginationScrollListener
import com.eeseka.lynk.shared.presentation.util.UiText
import com.eeseka.lynk.shared.presentation.util.clearFocusOnTap
import com.eeseka.lynk.shared.presentation.util.currentDeviceConfiguration
import kotlinx.collections.immutable.persistentListOf
import lynk.feature.profile.generated.resources.Res
import lynk.feature.profile.generated.resources.back
import lynk.feature.profile.generated.resources.saved_spots
import lynk.feature.profile.generated.resources.saved_spots_load_error_title
import lynk.feature.profile.generated.resources.saved_spots_search_placeholder
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SavedSpotsRoot(
    navigateBack: () -> Unit,
    viewModel: SavedSpotsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is SavedSpotsEvent.Error -> {
                snackbarHostState.showFlashMessage(
                    message = event.message.asStringAsync(),
                    type = LynkFlashType.Error
                )
            }
        }
    }

    SavedSpotsScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
        navigateBack = navigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedSpotsScreen(
    state: SavedSpotsState,
    onAction: (SavedSpotsAction) -> Unit,
    snackbarHostState: SnackbarHostState,
    navigateBack: () -> Unit
) {
    val hapticFeedback = rememberAppHaptic()
    val listState = rememberLazyListState()

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
                            navigateBack()
                        }
                    ) {
                        Icon(
                            imageVector = Lucide.ChevronLeft,
                            contentDescription = backLabel
                        )
                    }
                },
                iosLeadingItems = persistentListOf(
                    LynkIosBarButtonItem(
                        sfSymbol = "chevron.left",
                        onClick = {
                            hapticFeedback(AppHaptic.ImpactLight)
                            navigateBack()
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
            val showLoadError =
                state.spots.isEmpty() && state.loadError != null && !state.isLoading

            if (showLoadError) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LynkErrorState(
                        title = stringResource(Res.string.saved_spots_load_error_title),
                        message = state.loadError.asString(),
                        onRetry = {
                            hapticFeedback(AppHaptic.ImpactLight)
                            onAction(SavedSpotsAction.OnRetryClick)
                        }
                    )
                }
            } else {
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
                                    onClick = {
                                        hapticFeedback(AppHaptic.ImpactLight)
                                        onAction(SavedSpotsAction.OnSpotSelected(spot.id))
                                    },
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

@PreviewLightDark
@Preview(name = "Tablet landscape", widthDp = 1280, heightDp = 800)
@Composable
private fun SavedSpotsScreenFilledPreview() = SavedSpotsScreenPreview(
    SavedSpotsState(spots = previewSpots, isEndReached = true)
)

@PreviewLightDark
@Composable
private fun SavedSpotsScreenEmptyPreview() = SavedSpotsScreenPreview(
    SavedSpotsState(isEndReached = true)
)

@PreviewLightDark
@Composable
private fun SavedSpotsScreenErrorPreview() = SavedSpotsScreenPreview(
    SavedSpotsState(
        loadError = UiText.DynamicString(
            "Couldn't reach the server. Check your connection and try again."
        )
    )
)

@Composable
private fun SavedSpotsScreenPreview(state: SavedSpotsState) {
    LynkTheme {
        SavedSpotsScreen(
            state = state,
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() },
            navigateBack = {}
        )
    }
}