package com.eeseka.lynk.hangouts.presentation.hangout_detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.eeseka.lynk.hangouts.presentation.hangout_detail.voting.components.CandidateMapCard
import com.eeseka.lynk.hangouts.presentation.hangout_detail.voting.components.CandidateMapPin
import com.eeseka.lynk.shared.design_system.components.buttons.LynkTonalIconButton
import com.eeseka.lynk.shared.design_system.components.layouts.LynkScaffold
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.domain.location.LocationCoordinates
import com.eeseka.lynk.shared.domain.settings.AppTheme
import com.eeseka.lynk.shared.domain.util.onSuccess
import com.eeseka.lynk.shared.presentation.components.SpotDetailSheet
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutParticipantUi
import com.eeseka.lynk.shared.presentation.location.rememberLocationController
import com.eeseka.lynk.shared.presentation.map.components.MapAttributionMenu
import com.eeseka.lynk.shared.presentation.map.components.UserLocationMapMarker
import com.eeseka.lynk.shared.presentation.map.util.isMapDark
import com.eeseka.lynk.shared.presentation.map.util.mapStyleUri
import com.eeseka.lynk.shared.presentation.map.util.toBoundingBox
import com.eeseka.lynk.shared.presentation.permissions.Permission
import com.eeseka.lynk.shared.presentation.permissions.PermissionState
import com.eeseka.lynk.shared.presentation.permissions.rememberPermissionController
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import com.eeseka.lynk.shared.presentation.spot.util.rememberSpotDistanceLabel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.map_close
import org.jetbrains.compose.resources.stringResource
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraUpdate
import org.maplibre.compose.map.CameraConstraints
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.overlay.CompassButtonStyle
import org.maplibre.compose.overlay.DisappearingCompassButton
import org.maplibre.compose.overlay.DisappearingScaleBar
import org.maplibre.compose.overlay.LocalViewportInsets
import org.maplibre.compose.overlay.MapOverlay
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.DpPadding
import org.maplibre.spatialk.geojson.Position

// Covers the whole screen like LynkFullScreenDialog, but follows the app theme instead of staying dark
@Composable
fun HangoutMapDialog(
    candidates: ImmutableList<SpotUi>,
    votes: ImmutableMap<String, String>,
    participants: ImmutableList<HangoutParticipantUi>,
    currentUserId: String?,
    isHost: Boolean,
    tiedSpotIds: ImmutableList<String>,
    centerLatitude: Double?,
    centerLongitude: Double?,
    mapTheme: AppTheme,
    snackbarHostState: SnackbarHostState,
    onCastVote: (String) -> Unit,
    onBreakTie: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = hangoutMapDialogProperties()
    ) {
        ThemedSystemBarIcons(isDark = isMapDark(mapTheme))
        LynkScaffold(
            snackbarHostState = snackbarHostState,
            applyHorizontalInsets = false
        ) {
            HangoutMapContent(
                candidates = candidates,
                votes = votes,
                participants = participants,
                currentUserId = currentUserId,
                isHost = isHost,
                tiedSpotIds = tiedSpotIds,
                centerLatitude = centerLatitude,
                centerLongitude = centerLongitude,
                mapTheme = mapTheme,
                onCastVote = onCastVote,
                onBreakTie = onBreakTie,
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
private fun HangoutMapContent(
    candidates: ImmutableList<SpotUi>,
    votes: ImmutableMap<String, String>,
    participants: ImmutableList<HangoutParticipantUi>,
    currentUserId: String?,
    isHost: Boolean,
    tiedSpotIds: ImmutableList<String>,
    centerLatitude: Double?,
    centerLongitude: Double?,
    mapTheme: AppTheme,
    onCastVote: (String) -> Unit,
    onBreakTie: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val hapticFeedback = rememberAppHaptic()
    val scope = rememberCoroutineScope()
    val permissionController = rememberPermissionController()
    val locationController = rememberLocationController()

    var sheetSpotId by remember { mutableStateOf<String?>(null) }
    var userLocation by remember { mutableStateOf<LocationCoordinates?>(null) }

    // Same tally as VotingSection
    val counts = remember(votes) { votes.values.groupingBy { it }.eachCount() }
    val maxCount = counts.values.maxOrNull() ?: 0
    val myVote = currentUserId?.let { votes[it] }
    val tie = tiedSpotIds.isNotEmpty()

    val pagerState = rememberPagerState { candidates.size }
    val currentCandidates by rememberUpdatedState(candidates)
    val selectedSpotId = candidates.getOrNull(pagerState.currentPage)?.id

    val centerPosition = if (centerLatitude != null && centerLongitude != null) {
        Position(longitude = centerLongitude, latitude = centerLatitude)
    } else null
    // Opens on the group center, else the first spot; 0,0 never shows since the map only opens with a spot
    val openingTarget = centerPosition
        ?: candidates.firstOrNull()?.let { Position(longitude = it.longitude, latitude = it.latitude) }
        ?: Position(longitude = 0.0, latitude = 0.0)

    val mapState = rememberMapState(
        baseStyle = BaseStyle.Uri(mapStyleUri(mapTheme)),
        initialCameraPosition = CameraPosition(target = openingTarget, zoom = 15.0)
    )

    // Your own dot only when location is already allowed; this map never asks for it
    LaunchedEffect(Unit) {
        val permissionState = permissionController.getPermissionState(Permission.LOCATION)
        if (permissionState != PermissionState.GRANTED) return@LaunchedEffect

        locationController.observeCurrentLocation().collect { locationResult ->
            locationResult.onSuccess { coordinate -> userLocation = coordinate }
        }
    }

    // Frames the center and every spot once; one point alone keeps the opening zoom
    LaunchedEffect(Unit) {
        val pointCount = candidates.size + if (centerPosition != null) 1 else 0
        if (pointCount < 2) return@LaunchedEffect

        val pointsBounds = candidates.toBoundingBox(including = centerPosition) ?: return@LaunchedEffect
        mapState.animateCameraToBounds(
            boundingBox = pointsBounds,
            fitPadding = DpPadding(left = 48.dp, top = 64.dp, right = 48.dp, bottom = 48.dp)
        )
    }

    // Swiping to a card glides the camera to its pin; the first page is skipped so the opening fit stays
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .drop(1)
            .collect { page ->
                val spot = currentCandidates.getOrNull(page) ?: return@collect
                mapState.animateCamera(
                    CameraUpdate(
                        target = Position(
                            longitude = spot.longitude,
                            latitude = spot.latitude
                        )
                    )
                )
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            state = mapState,
            cameraConstraints = CameraConstraints(
                minZoom = 2.0,
                maxZoom = 20.0,
                minPitch = 0.0,
                maxPitch = 60.0
            ),
            viewportInsets = WindowInsets(
                top = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding() + 64.dp,
                bottom = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding() + 216.dp
            ).union(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)).asPaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(LocalViewportInsets.current)
                    .padding(horizontal = MapOverlay.Spacing)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .height(48.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    DisappearingScaleBar(
                        metersPerDp = { mapState.viewport?.metersPerDpAtTarget ?: 0.0 },
                        zoom = { mapState.cameraPosition.zoom },
                        color = MaterialTheme.colorScheme.onSurface,
                        haloColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                        textStyle = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
                DisappearingCompassButton(
                    style = CompassButtonStyle(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ),
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }

            centerPosition?.let { groupCenter ->
                GroupCenterMapMarker(
                    centerLatitude = groupCenter.latitude,
                    centerLongitude = groupCenter.longitude
                )
            }

            userLocation?.let { location ->
                UserLocationMapMarker(
                    userLatitude = location.latitude,
                    userLongitude = location.longitude,
                    pulseKey = 0
                )
            }

            // Pins drawn later cover earlier ones, so the selected pin goes last and stays visible where pins overlap
            candidates.sortedBy { it.id == selectedSpotId }.forEach { spot ->
                key(spot.id) {
                    val voteCount = counts[spot.id] ?: 0
                    CandidateMapPin(
                        spotName = spot.name,
                        latitude = spot.latitude,
                        longitude = spot.longitude,
                        photoUrls = spot.photoUrls,
                        voteCount = voteCount,
                        isMyVote = myVote == spot.id,
                        isLeading = voteCount > 0 && voteCount == maxCount,
                        isTiebreakTarget = tie && isHost && spot.id in tiedSpotIds,
                        isSelected = spot.id == selectedSpotId,
                        onClick = {
                            hapticFeedback(AppHaptic.ImpactLight)
                            val page = candidates.indexOfFirst { it.id == spot.id }
                            scope.launch { pagerState.animateScrollToPage(page) }
                        }
                    )
                }
            }
        }

        LynkTonalIconButton(
            onClick = {
                hapticFeedback(AppHaptic.ImpactLight)
                onDismiss()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Lucide.X,
                contentDescription = stringResource(Res.string.map_close)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MapAttributionMenu(
                modifier = Modifier
                    .align(Alignment.Start)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Start))
                    .padding(start = 16.dp)
            )

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val horizontalSafePadding = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal).asPaddingValues()
                val layoutDirection = LocalLayoutDirection.current
                val safeStart = horizontalSafePadding.calculateStartPadding(layoutDirection)
                val safeEnd = horizontalSafePadding.calculateEndPadding(layoutDirection)
                val usableWidth = maxWidth - safeStart - safeEnd
                // A card of at most 416dp, centred; on narrow screens the 32dp minimum keeps the next card peeking in
                val sidePadding = maxOf(32.dp, (usableWidth - 416.dp) / 2)

                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(
                        start = safeStart + sidePadding,
                        end = safeEnd + sidePadding
                    ),
                    pageSpacing = 12.dp,
                    key = { page -> candidates[page].id },
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    val spot = candidates[page]
                    val voters = remember(votes, participants, spot.id) {
                        participants
                            .filter { votes[it.user.userId] == spot.id }
                            .map { it.user }
                            .toImmutableList()
                    }
                    val voteCount = counts[spot.id] ?: 0
                    val isTiebreakTarget = tie && isHost && spot.id in tiedSpotIds

                    CandidateMapCard(
                        spotName = spot.name,
                        photoUrls = spot.photoUrls,
                        distanceFromMiddle = rememberSpotDistanceLabel(
                            userLatitude = centerLatitude,
                            userLongitude = centerLongitude,
                            spotLatitude = spot.latitude,
                            spotLongitude = spot.longitude
                        ),
                        voters = voters,
                        isMyVote = myVote == spot.id,
                        isLeading = voteCount > 0 && voteCount == maxCount,
                        isTiebreakTarget = isTiebreakTarget,
                        isTieLocked = tie && !isTiebreakTarget,
                        onClick = { sheetSpotId = spot.id },
                        onVoteClick = {
                            if (isTiebreakTarget) onBreakTie(spot.id) else onCastVote(spot.id)
                        }
                    )
                }
            }
        }
    }

    candidates.find { it.id == sheetSpotId }?.let { spot ->
        SpotDetailSheet(
            spot = spot,
            userLat = centerLatitude ?: userLocation?.latitude,
            userLng = centerLongitude ?: userLocation?.longitude,
            onDismissRequest = { sheetSpotId = null }
        )
    }
}

internal expect fun hangoutMapDialogProperties(): DialogProperties

@Composable
internal expect fun ThemedSystemBarIcons(isDark: Boolean)
