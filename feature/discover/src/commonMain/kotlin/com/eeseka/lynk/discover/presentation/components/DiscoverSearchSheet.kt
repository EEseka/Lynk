package com.eeseka.lynk.discover.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.LayoutGrid
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.SlidersHorizontal
import com.composables.icons.lucide.Wallet
import com.eeseka.lynk.discover.presentation.DiscoverState
import com.eeseka.lynk.shared.design_system.components.buttons.LynkTonalIconButton
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkAdaptiveSheet
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDropDownItem
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDropDownMenu
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.textfields.LynkSearchField
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.toggles_and_control.LynkSegmentedControl
import com.eeseka.lynk.shared.design_system.components.toggles_and_control.LynkSegmentedItem
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.spot.model.PriceLevel
import com.eeseka.lynk.shared.domain.spot.model.SpotCategory
import com.eeseka.lynk.shared.presentation.spot.mappers.getIcon
import com.eeseka.lynk.shared.presentation.spot.mappers.getTitle
import com.eeseka.lynk.shared.presentation.spot.util.getPriceLevelSymbol
import com.eeseka.lynk.shared.presentation.util.PaginationScrollListener
import com.eeseka.lynk.shared.presentation.util.clearFocusOnTap
import kotlinx.coroutines.delay
import lynk.feature.discover.generated.resources.Res
import lynk.feature.discover.generated.resources.all
import lynk.feature.discover.generated.resources.any_price
import lynk.feature.discover.generated.resources.empty_search_message
import lynk.feature.discover.generated.resources.empty_search_title
import lynk.feature.discover.generated.resources.filter_price
import lynk.feature.discover.generated.resources.search_spots_hint
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun DiscoverSearchSheet(
    state: DiscoverState,
    onLoadNextSearchPage: () -> Unit,
    onSelectPriceLevel: (PriceLevel?) -> Unit,
    onSelectCategory: (SpotCategory?) -> Unit,
    onSpotClick: (String?) -> Unit,
    onDismissRequest: () -> Unit
) {
    var showPriceMenu by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val hapticFeedback = rememberAppHaptic()
    val gridState = rememberLazyGridState()

    LaunchedEffect(Unit) {
        delay(100.milliseconds)
        focusRequester.requestFocus()
    }

    PaginationScrollListener(
        lazyGridState = gridState,
        itemCount = state.searchResults.size,
        isPaginationLoading = state.isSearchLoading,
        isEndReached = state.searchEndReached,
        onNearBottom = onLoadNextSearchPage,
        resetKey = state.searchResetEpoch
    )

    val isSearchActive = state.searchTextFieldState.text.toString().isNotBlank() ||
            state.selectedCategory != null ||
            state.selectedPriceLevel != null

    val spotsToShow = if (isSearchActive) state.searchResults else state.trendingSpots
    val showEmptyState =
        isSearchActive && spotsToShow.isEmpty() && !state.isSearchLoading && state.searchEndReached

    LynkAdaptiveSheet(
        onDismissRequest = onDismissRequest
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clearFocusOnTap()
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 160.dp,
                    bottom = 48.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (showEmptyState) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        DiscoverEmptyState(
                            modifier = Modifier.padding(top = 64.dp)
                        )
                    }
                } else {
                    items(spotsToShow, key = { it.id }) { spot ->
                        Box(modifier = Modifier.animateItem()) {
                            SpotDiscoverCard(
                                spotName = spot.name,
                                spotPhotos = spot.photoUrls,
                                spotLatitude = spot.latitude,
                                spotLongitude = spot.longitude,
                                spotCategory = spot.category,
                                spotPriceLevel = spot.priceLevel,
                                spotRating = spot.rating,
                                userLat = state.userLatitude,
                                userLng = state.userLongitude,
                                onClick = {
                                    hapticFeedback(AppHaptic.ImpactMedium)
                                    focusRequester.freeFocus()
                                    onSpotClick(spot.id)
                                }
                            )
                        }
                    }
                }

                if (state.isSearchLoading) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
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
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LynkSearchField(
                        state = state.searchTextFieldState,
                        placeholder = stringResource(Res.string.search_spots_hint),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    LynkDropDownMenu(
                        expanded = showPriceMenu,
                        onDismissRequest = { showPriceMenu = false },
                        items = listOf(
                            LynkDropDownItem(
                                title = stringResource(Res.string.any_price),
                                icon = if (state.selectedPriceLevel == null) Lucide.Check else Lucide.Wallet,
                                sfSymbol = if (state.selectedPriceLevel == null) "checkmark" else "creditcard",
                                onClick = {
                                    hapticFeedback(AppHaptic.Selection)
                                    onSelectPriceLevel(null)
                                    showPriceMenu = false
                                }
                            )
                        ) + PriceLevel.entries.map { level ->
                            val isSelected = state.selectedPriceLevel == level
                            LynkDropDownItem(
                                title = "${level.getTitle()} (${getPriceLevelSymbol(level.tier)})",
                                icon = if (isSelected) Lucide.Check else null,
                                sfSymbol = if (isSelected) "checkmark" else null,
                                onClick = {
                                    hapticFeedback(AppHaptic.Selection)
                                    onSelectPriceLevel(level)
                                    showPriceMenu = false
                                }
                            )
                        },
                        anchor = {
                            val isActive = state.selectedPriceLevel != null
                            val containerColor =
                                if (isActive) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            val contentColor =
                                if (isActive) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

                            LynkTonalIconButton(
                                onClick = {
                                    hapticFeedback(AppHaptic.ImpactLight)
                                    showPriceMenu = true
                                },
                                containerColor = containerColor,
                                contentColor = contentColor
                            ) {
                                Icon(
                                    imageVector = Lucide.SlidersHorizontal,
                                    contentDescription = stringResource(Res.string.filter_price)
                                )
                            }
                        }
                    )
                }

                AnimatedVisibility(
                    visible = state.searchError != null,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    LynkText(
                        text = state.searchError?.asString() ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 2.dp)
                    )
                }

                LynkSegmentedControl(
                    items = listOf(
                        LynkSegmentedItem(
                            title = stringResource(Res.string.all),
                            icon = Lucide.LayoutGrid
                        )
                    ) + SpotCategory.entries.map { category ->
                        LynkSegmentedItem(
                            title = category.getTitle(),
                            icon = category.getIcon()
                        )
                    },
                    selectedIndex = state.selectedCategory?.let { it.ordinal + 1 } ?: 0,
                    onItemSelected = { index ->
                        hapticFeedback(AppHaptic.Selection)
                        val category = if (index == 0) null else SpotCategory.entries[index - 1]
                        onSelectCategory(category)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun DiscoverEmptyState(modifier: Modifier = Modifier) {
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
                    imageVector = Lucide.Search,
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

@PreviewLightDark
@Composable
private fun DiscoverEmptyStatePreview() {
    LynkTheme {
        DiscoverEmptyState(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh))
    }
}