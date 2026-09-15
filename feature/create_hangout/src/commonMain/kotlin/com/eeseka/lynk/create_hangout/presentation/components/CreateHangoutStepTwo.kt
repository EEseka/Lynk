package com.eeseka.lynk.create_hangout.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Users
import com.composables.icons.lucide.X
import com.eeseka.lynk.create_hangout.presentation.mappers.getIcon
import com.eeseka.lynk.create_hangout.presentation.mappers.getTitle
import com.eeseka.lynk.create_hangout.presentation.model.SearchTab
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.textfields.LynkSearchField
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.toggles_and_control.LynkSegmentedControl
import com.eeseka.lynk.shared.design_system.components.toggles_and_control.LynkSegmentedItem
import com.eeseka.lynk.shared.design_system.components.toggles_and_control.LynkSegmentedStyle
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.presentation.preview.previewSpots
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import com.eeseka.lynk.shared.presentation.util.PaginationScrollListener
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import lynk.feature.create_hangout.generated.resources.Res
import lynk.feature.create_hangout.generated.resources.change_location
import lynk.feature.create_hangout.generated.resources.group_vote
import lynk.feature.create_hangout.generated.resources.let_the_group_decide
import lynk.feature.create_hangout.generated.resources.let_the_group_decide_description
import lynk.feature.create_hangout.generated.resources.pick_a_spot
import lynk.feature.create_hangout.generated.resources.search_favorite_spots_hint
import lynk.feature.create_hangout.generated.resources.search_spots_hint
import lynk.feature.create_hangout.generated.resources.selected
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateHangoutStepTwo(
    isVotingMode: Boolean,
    activeSearchTab: SearchTab,
    searchState: TextFieldState,
    selectedSpot: SpotUi?,
    userLatitude: Double?,
    userLongitude: Double?,
    spots: ImmutableList<SpotUi>,
    isLoading: Boolean,
    isEndReached: Boolean,
    showEmptyState: Boolean,
    emptyStateMessage: String,
    errorMessage: String?,
    resetKey: Int,
    onVotingModeChanged: (Boolean) -> Unit,
    onTabSelected: (SearchTab) -> Unit,
    onSpotSelected: (SpotUi?) -> Unit,
    onLoadNextPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = rememberAppHaptic()

    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Spacer(modifier = Modifier.height(8.dp))
            LynkSegmentedControl(
                items = persistentListOf(
                    LynkSegmentedItem(stringResource(Res.string.pick_a_spot), Lucide.MapPin),
                    LynkSegmentedItem(stringResource(Res.string.group_vote), Lucide.Users)
                ),
                selectedIndex = if (isVotingMode) 1 else 0,
                onItemSelected = { index ->
                    hapticFeedback(AppHaptic.Selection)
                    onVotingModeChanged(index == 1)
                },
                style = LynkSegmentedStyle.FIXED_BAR,
                contentPadding = PaddingValues(0.dp)
            )
        }

        AnimatedContent(
            targetState = isVotingMode,
            transitionSpec = {
                fadeIn() togetherWith fadeOut() using SizeTransform(clip = false)
            },
            modifier = Modifier.weight(1f),
            label = "LocationModeTransition"
        ) { votingMode ->
            if (votingMode) {
                VotingModeUI()
            } else {
                SpotPickerUI(
                    activeSearchTab = activeSearchTab,
                    searchState = searchState,
                    selectedSpot = selectedSpot,
                    userLatitude = userLatitude,
                    userLongitude = userLongitude,
                    spots = spots,
                    isLoading = isLoading,
                    isEndReached = isEndReached,
                    showEmptyState = showEmptyState,
                    emptyStateMessage = emptyStateMessage,
                    errorMessage = errorMessage,
                    resetKey = resetKey,
                    onTabSelected = onTabSelected,
                    onSpotSelected = onSpotSelected,
                    onLoadNextPage = onLoadNextPage
                )
            }
        }
    }
}

@Composable
private fun VotingModeUI() {
    val scrollState = rememberScrollState()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(32.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Lucide.Users,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        LynkText(
            text = stringResource(Res.string.let_the_group_decide),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        LynkText(
            text = stringResource(Res.string.let_the_group_decide_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SpotPickerUI(
    activeSearchTab: SearchTab,
    searchState: TextFieldState,
    selectedSpot: SpotUi?,
    userLatitude: Double?,
    userLongitude: Double?,
    spots: ImmutableList<SpotUi>,
    isLoading: Boolean,
    isEndReached: Boolean,
    showEmptyState: Boolean,
    emptyStateMessage: String,
    errorMessage: String?,
    resetKey: Int,
    onTabSelected: (SearchTab) -> Unit,
    onSpotSelected: (SpotUi?) -> Unit,
    onLoadNextPage: () -> Unit
) {
    val hapticFeedback = rememberAppHaptic()

    val allSpotsListState = rememberLazyListState()
    val favoritesListState = rememberLazyListState()

    AnimatedContent(
        targetState = selectedSpot != null,
        transitionSpec = {
            fadeIn() togetherWith fadeOut() using SizeTransform(clip = false)
        },
        modifier = Modifier.fillMaxSize(),
        label = "SpotSelectionTransition"
    ) { hasSelectedSpot ->
        if (hasSelectedSpot && selectedSpot != null) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Lucide.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    LynkText(
                        text = stringResource(Res.string.selected),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    SpotPickerListItem(
                        spotName = selectedSpot.name,
                        spotPhotos = selectedSpot.photoUrls,
                        spotLatitude = selectedSpot.latitude,
                        spotLongitude = selectedSpot.longitude,
                        spotCategory = selectedSpot.category,
                        spotPriceLevel = selectedSpot.priceLevel,
                        userLat = userLatitude,
                        userLng = userLongitude,
                        onClick = {}
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .clickable(role = Role.Button) {
                                hapticFeedback(AppHaptic.ImpactLight)
                                onSpotSelected(null)
                            }
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Lucide.X,
                            contentDescription = stringResource(Res.string.change_location),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        } else {
            val listState = when (activeSearchTab) {
                SearchTab.ALL_SPOTS -> allSpotsListState
                SearchTab.FAVORITES -> favoritesListState
            }

            PaginationScrollListener(
                lazyListState = listState,
                itemCount = spots.size,
                isPaginationLoading = isLoading,
                isEndReached = isEndReached,
                onNearBottom = onLoadNextPage,
                resetKey = resetKey
            )

            val tabs = SearchTab.entries

            Box(modifier = Modifier.fillMaxSize()) {
                SpotPickerList(
                    listState = listState,
                    spots = spots,
                    isLoading = isLoading,
                    showEmptyState = showEmptyState,
                    emptyStateMessage = emptyStateMessage,
                    onSpotSelected = { spot ->
                        hapticFeedback(AppHaptic.Selection)
                        onSpotSelected(spot)
                    },
                    userLat = userLatitude,
                    userLng = userLongitude
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        LynkSearchField(
                            state = searchState,
                            placeholder = when (activeSearchTab) {
                                SearchTab.ALL_SPOTS -> stringResource(Res.string.search_spots_hint)
                                SearchTab.FAVORITES -> stringResource(Res.string.search_favorite_spots_hint)
                            }
                        )

                        AnimatedVisibility(
                            visible = errorMessage != null,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            LynkText(
                                text = errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 4.dp, start = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LynkSegmentedControl(
                        items = tabs.map {
                            LynkSegmentedItem(title = it.getTitle(), icon = it.getIcon())
                        }.toImmutableList(),
                        selectedIndex = tabs.indexOf(activeSearchTab),
                        onItemSelected = { index ->
                            hapticFeedback(AppHaptic.Selection)
                            onTabSelected(tabs[index])
                        },
                        style = LynkSegmentedStyle.SCROLLABLE_CHIPS,
                        contentPadding = PaddingValues(horizontal = 24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SpotPickerList(
    listState: LazyListState,
    spots: ImmutableList<SpotUi>,
    isLoading: Boolean,
    showEmptyState: Boolean,
    emptyStateMessage: String,
    onSpotSelected: (SpotUi) -> Unit,
    userLat: Double?,
    userLng: Double?
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 24.dp,
            end = 24.dp,
            top = 140.dp,
            bottom = 48.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showEmptyState) {
            item { EmptySearchState(emptyStateMessage) }
        } else {
            items(spots, key = { it.id }) { spot ->
                SpotPickerListItem(
                    spotName = spot.name,
                    spotPhotos = spot.photoUrls,
                    spotLatitude = spot.latitude,
                    spotLongitude = spot.longitude,
                    spotCategory = spot.category,
                    spotPriceLevel = spot.priceLevel,
                    userLat = userLat,
                    userLng = userLng,
                    onClick = { onSpotSelected(spot) },
                    modifier = Modifier.animateItem()
                )
            }
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
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
private fun EmptySearchState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
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
private fun CreateHangoutStepTwoVotingPreview() = CreateHangoutStepTwoPreview(isVotingMode = true)

@PreviewLightDark
@Composable
private fun CreateHangoutStepTwoSelectedPreview() = CreateHangoutStepTwoPreview(
    selectedSpot = previewSpots.first()
)

@Composable
private fun CreateHangoutStepTwoPreview(
    isVotingMode: Boolean = false,
    selectedSpot: SpotUi? = null
) {
    LynkTheme {
        CreateHangoutStepTwo(
            isVotingMode = isVotingMode,
            activeSearchTab = SearchTab.ALL_SPOTS,
            searchState = TextFieldState(),
            selectedSpot = selectedSpot,
            userLatitude = 6.443,
            userLongitude = 3.455,
            spots = previewSpots,
            isLoading = false,
            isEndReached = true,
            showEmptyState = false,
            emptyStateMessage = "No spots found.",
            errorMessage = null,
            resetKey = 0,
            onVotingModeChanged = {},
            onTabSelected = {},
            onSpotSelected = {},
            onLoadNextPage = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}
