package com.eeseka.lynk.hangouts.presentation.hangout_detail.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.composables.icons.lucide.Locate
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.eeseka.lynk.hangouts.presentation.hangout_detail.live_location.LiveLocationShareEffect
import com.eeseka.lynk.hangouts.presentation.hangout_detail.live_location.components.LiveParticipantsSheet
import com.eeseka.lynk.hangouts.presentation.hangout_detail.live_location.components.ParticipantMapPin
import com.eeseka.lynk.hangouts.presentation.hangout_detail.live_location.components.ShareLocationCard
import com.eeseka.lynk.hangouts.presentation.hangout_detail.live_location.model.LiveLocationUi
import com.eeseka.lynk.hangouts.presentation.hangout_detail.voting.components.CandidateMapCard
import com.eeseka.lynk.hangouts.presentation.hangout_detail.voting.components.CandidateMapPin
import com.eeseka.lynk.hangouts.presentation.mappers.toLiveParticipants
import com.eeseka.lynk.hangouts.presentation.util.toLiveParticipantStacks
import com.eeseka.lynk.shared.design_system.components.buttons.LynkTonalIconButton
import com.eeseka.lynk.shared.design_system.components.layouts.LynkScaffold
import com.eeseka.lynk.shared.design_system.components.layouts.safeArea
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFlashType
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.showFlashMessage
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.domain.location.LocationCoordinates
import com.eeseka.lynk.shared.domain.settings.AppTheme
import com.eeseka.lynk.shared.domain.util.onSuccess
import com.eeseka.lynk.shared.presentation.components.SpotDetailSheet
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutParticipantUi
import com.eeseka.lynk.shared.presentation.location.rememberLocationController
import com.eeseka.lynk.shared.presentation.map.components.MapAttributionMenu
import com.eeseka.lynk.shared.presentation.map.util.boundingBoxOf
import com.eeseka.lynk.shared.presentation.map.util.flightDurationTo
import com.eeseka.lynk.shared.presentation.map.util.isMapDark
import com.eeseka.lynk.shared.presentation.map.util.mapStyleUri
import com.eeseka.lynk.shared.presentation.permissions.Permission
import com.eeseka.lynk.shared.presentation.permissions.PermissionState
import com.eeseka.lynk.shared.presentation.permissions.rememberPermissionController
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import com.eeseka.lynk.shared.presentation.spot.util.rememberSpotDistanceLabel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.map_close
import lynk.feature.hangouts.generated.resources.map_locate_me
import lynk.feature.hangouts.generated.resources.map_share_location_denied
import lynk.feature.hangouts.generated.resources.map_share_location_off
import lynk.feature.hangouts.generated.resources.map_shared_minutes_short
import org.jetbrains.compose.resources.stringResource
import org.maplibre.compose.camera.CameraAnimation
import org.maplibre.compose.camera.CameraFit
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
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

private val STALE_LABEL_REFRESH_INTERVAL = 30.seconds
private const val STACK_RADIUS_DP = 40.0
private const val MAP_ZOOM = 15.0

// Covers the whole screen like LynkFullScreenDialog, but follows the app theme instead of staying dark
@Composable
fun HangoutMapDialog(
    candidates: ImmutableList<SpotUi>,
    chosenSpot: SpotUi?,
    votes: ImmutableMap<String, String>,
    participants: ImmutableList<HangoutParticipantUi>,
    currentUserId: String?,
    hostId: String,
    isHost: Boolean,
    tiedSpotIds: ImmutableList<String>,
    liveLocations: ImmutableMap<String, LiveLocationUi>,
    isTrackingLive: Boolean,
    canShareLocation: Boolean,
    isSharingLocation: Boolean,
    centerLatitude: Double?,
    centerLongitude: Double?,
    mapTheme: AppTheme,
    snackbarHostState: SnackbarHostState,
    onCastVote: (String) -> Unit,
    onBreakTie: (String) -> Unit,
    onToggleSaveSpot: (String, Boolean) -> Unit,
    onSharingChange: (Boolean) -> Unit,
    onLocationChange: (Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    val permissionController = rememberPermissionController()
    val locationController = rememberLocationController()
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    var userLocation by remember { mutableStateOf<LocationCoordinates?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }

    val sharingDeniedMessage = stringResource(Res.string.map_share_location_denied)
    val sharingLocationOffMessage = stringResource(Res.string.map_share_location_off)

    LaunchedEffect(Unit) {
        hasLocationPermission = permissionController.getPermissionState(Permission.LOCATION) == PermissionState.GRANTED
    }

    LaunchedEffect(lifecycleOwner, hasLocationPermission, isTrackingLive, isSharingLocation) {
        if (!hasLocationPermission) return@LaunchedEffect

        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            if (isTrackingLive) {
                locationController.observeLocationUpdates().collect { coordinate -> userLocation = coordinate }
            } else {
                locationController.observeCurrentLocation().collect { locationResult ->
                    locationResult.onSuccess { coordinate -> userLocation = coordinate }
                }
            }
        }
    }

    LiveLocationShareEffect(
        isSharing = isSharingLocation,
        canShare = canShareLocation,
        latitude = userLocation?.latitude,
        longitude = userLocation?.longitude,
        isLocationTurnedOn = locationController::isLocationTurnedOn,
        onLocationTurnedOff = {
            onSharingChange(false)
            scope.launch {
                snackbarHostState.showFlashMessage(
                    message = sharingLocationOffMessage,
                    type = LynkFlashType.Error
                )
            }
        },
        onSharingChange = onSharingChange,
        onLocationChange = onLocationChange
    )

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
                chosenSpot = chosenSpot,
                votes = votes,
                participants = participants,
                currentUserId = currentUserId,
                hostId = hostId,
                isHost = isHost,
                tiedSpotIds = tiedSpotIds,
                liveLocations = liveLocations,
                canShareLocation = canShareLocation,
                isSharingLocation = isSharingLocation,
                centerLatitude = centerLatitude,
                centerLongitude = centerLongitude,
                userLatitude = userLocation?.latitude,
                userLongitude = userLocation?.longitude,
                mapTheme = mapTheme,
                onCastVote = onCastVote,
                onBreakTie = onBreakTie,
                onToggleSaveSpot = onToggleSaveSpot,
                onShareLocationToggle = { isSharing ->
                    scope.launch {
                        var permissionState = permissionController.getPermissionState(Permission.LOCATION)
                        if (isSharing && permissionState != PermissionState.GRANTED) {
                            permissionState = permissionController.requestPermission(Permission.LOCATION)
                        }
                        if (isSharing && permissionState != PermissionState.GRANTED) {
                            snackbarHostState.showFlashMessage(
                                message = sharingDeniedMessage,
                                type = LynkFlashType.Error
                            )
                            return@launch
                        }
                        hasLocationPermission = permissionState == PermissionState.GRANTED
                        if (isSharing && !locationController.isLocationTurnedOn()) {
                            snackbarHostState.showFlashMessage(
                                message = sharingLocationOffMessage,
                                type = LynkFlashType.Error
                            )
                            return@launch
                        }
                        onSharingChange(isSharing)
                    }
                },
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
private fun HangoutMapContent(
    candidates: ImmutableList<SpotUi>,
    chosenSpot: SpotUi?,
    votes: ImmutableMap<String, String>,
    participants: ImmutableList<HangoutParticipantUi>,
    currentUserId: String?,
    hostId: String,
    isHost: Boolean,
    tiedSpotIds: ImmutableList<String>,
    liveLocations: ImmutableMap<String, LiveLocationUi>,
    canShareLocation: Boolean,
    isSharingLocation: Boolean,
    centerLatitude: Double?,
    centerLongitude: Double?,
    userLatitude: Double?,
    userLongitude: Double?,
    mapTheme: AppTheme,
    onCastVote: (String) -> Unit,
    onBreakTie: (String) -> Unit,
    onToggleSaveSpot: (String, Boolean) -> Unit,
    onShareLocationToggle: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val hapticFeedback = rememberAppHaptic()
    val scope = rememberCoroutineScope()

    var sheetSpotId by remember { mutableStateOf<String?>(null) }
    var sheetUserIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Keeps "2 min ago" honest while nothing else on the map changes
    val now by produceState(Clock.System.now()) {
        while (true) {
            delay(STALE_LABEL_REFRESH_INTERVAL)
            value = Clock.System.now()
        }
    }

    val currentUser = participants.find { it.user.userId == currentUserId }?.user
    val liveParticipants = remember(participants, liveLocations, currentUserId, now) {
        participants.toLiveParticipants(liveLocations, currentUserId, now).toImmutableList()
    }

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
    val openingCenter = centerPosition
        ?: (chosenSpot ?: candidates.firstOrNull())?.let { Position(longitude = it.longitude, latitude = it.latitude) }
        ?: Position(longitude = 0.0, latitude = 0.0)

    val mapState = rememberMapState(
        baseStyle = BaseStyle.Uri(mapStyleUri(mapTheme)),
        initialCameraPosition = CameraPosition(center = openingCenter, zoom = MAP_ZOOM)
    )

    // Voting frames the center and every spot once, a chosen spot frames it with you and whoever is sharing,
    // again when your fix lands; one point alone keeps the opening zoom
    LaunchedEffect(chosenSpot?.id, chosenSpot != null && userLatitude != null) {
        val positions = if (chosenSpot != null) {
            buildList {
                add(Position(longitude = chosenSpot.longitude, latitude = chosenSpot.latitude))
                if (userLatitude != null && userLongitude != null) {
                    add(Position(longitude = userLongitude, latitude = userLatitude))
                }
                liveParticipants.forEach { add(Position(longitude = it.longitude, latitude = it.latitude)) }
            }
        } else {
            candidates.map { Position(longitude = it.longitude, latitude = it.latitude) } + listOfNotNull(centerPosition)
        }
        if (positions.size < 2) return@LaunchedEffect

        val pointsBounds = boundingBoxOf(positions) ?: return@LaunchedEffect
        mapState.animateCameraToBounds(
            boundingBox = pointsBounds,
            fit = CameraFit(fitPadding = DpPadding(left = 48.dp, top = 64.dp, right = 48.dp, bottom = 48.dp))
        )
    }

    // Avatars regroup as you zoom, only when somebody actually joins or leaves a stack
    val latestLiveParticipants by rememberUpdatedState(liveParticipants)
    val liveParticipantStacks by remember {
        derivedStateOf(structuralEqualityPolicy()) {
            val metersPerDp = mapState.viewport?.metersPerDpAtCenter ?: 0.0
            latestLiveParticipants.toLiveParticipantStacks(radiusMeters = metersPerDp * STACK_RADIUS_DP)
        }
    }

    // Swiping to a card glides the camera to its pin; the first page is skipped so the opening fit stays
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .drop(1)
            .collect { page ->
                val spot = currentCandidates.getOrNull(page) ?: return@collect
                mapState.animateCamera(
                    CameraUpdate(
                        center = Position(
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
            cameraConstraints = CameraConstraints {
                minZoom = 2.0
                maxZoom = 20.0
                minPitch = 0.0
                maxPitch = 60.0
            },
            viewportInsets = WindowInsets(
                top = WindowInsets.safeArea.asPaddingValues().calculateTopPadding() + 64.dp,
                bottom = WindowInsets.safeArea.asPaddingValues().calculateBottomPadding() + if (chosenSpot != null) 120.dp else 216.dp
            ).union(WindowInsets.safeArea.only(WindowInsetsSides.Horizontal)).asPaddingValues()
        ) {
            centerPosition?.let { groupCenter ->
                GroupCenterMapMarker(
                    centerLatitude = groupCenter.latitude,
                    centerLongitude = groupCenter.longitude
                )
            }

            chosenSpot?.let { spot ->
                CandidateMapPin(
                    spotName = spot.name,
                    latitude = spot.latitude,
                    longitude = spot.longitude,
                    photoUrls = spot.photoUrls,
                    voteCount = 0,
                    isMyVote = false,
                    isLeading = false,
                    isTiebreakTarget = false,
                    isSelected = false,
                    onClick = {
                        hapticFeedback(AppHaptic.ImpactLight)
                        sheetSpotId = spot.id
                    }
                )
            }

            liveParticipantStacks.forEach { stack ->
                val person = stack.first()
                key(person.participant.user.userId) {
                    ParticipantMapPin(
                        user = person.participant.user,
                        latitude = person.latitude,
                        longitude = person.longitude,
                        ringColor = MaterialTheme.colorScheme.surface,
                        stackSize = stack.size,
                        lastSharedLabel = person.minutesSinceShared
                            .takeIf { stack.size == 1 && it >= 1 }
                            ?.let { stringResource(Res.string.map_shared_minutes_short, it) },
                        onClick = {
                            hapticFeedback(AppHaptic.ImpactLight)
                            sheetUserIds = stack.map { it.participant.user.userId }.toSet()
                        }
                    )
                }
            }

            if (currentUser != null && userLatitude != null && userLongitude != null) {
                ParticipantMapPin(
                    user = currentUser,
                    latitude = userLatitude,
                    longitude = userLongitude,
                    ringColor = MaterialTheme.colorScheme.primary,
                    onClick = null
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
                        metersPerDp = { mapState.viewport?.metersPerDpAtCenter ?: 0.0 },
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
        }

        LynkTonalIconButton(
            onClick = {
                hapticFeedback(AppHaptic.ImpactLight)
                onDismiss()
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .windowInsetsPadding(WindowInsets.safeArea)
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
                .windowInsetsPadding(WindowInsets.safeArea.only(WindowInsetsSides.Bottom))
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeArea.only(WindowInsetsSides.Horizontal))
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MapAttributionMenu()

                AnimatedVisibility(visible = userLatitude != null && userLongitude != null) {
                    LynkTonalIconButton(
                        onClick = {
                            hapticFeedback(AppHaptic.ImpactLight)
                            if (userLatitude != null && userLongitude != null) {
                                val currentPosition = mapState.cameraPosition
                                val userPosition = currentPosition.copy(
                                    center = Position(longitude = userLongitude, latitude = userLatitude),
                                    zoom = MAP_ZOOM
                                )
                                scope.launch {
                                    mapState.animateCamera(
                                        update = CameraUpdate(center = userPosition.center, zoom = userPosition.zoom),
                                        animation = CameraAnimation.Fly {
                                            duration = currentPosition.flightDurationTo(userPosition)
                                        }
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Lucide.Locate,
                            contentDescription = stringResource(Res.string.map_locate_me)
                        )
                    }
                }
            }

            if (canShareLocation) {
                ShareLocationCard(
                    isSharing = isSharingLocation,
                    onToggle = { isSharing ->
                        hapticFeedback(AppHaptic.ImpactLight)
                        onShareLocationToggle(isSharing)
                    },
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.safeArea.only(WindowInsetsSides.Horizontal))
                        .padding(horizontal = 16.dp)
                        .widthIn(max = 416.dp)
                )
            }

            if (candidates.isNotEmpty()) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val horizontalSafePadding = WindowInsets.safeArea.only(WindowInsetsSides.Horizontal).asPaddingValues()
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
                        key = { page -> candidates.getOrNull(page)?.id ?: page },
                        modifier = Modifier.fillMaxWidth()
                    ) { page ->
                        val spot = candidates.getOrNull(page) ?: return@HorizontalPager
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
    }

    candidates.find { it.id == sheetSpotId }?.let { spot ->
        SpotDetailSheet(
            spot = spot,
            userLat = centerLatitude ?: userLatitude,
            userLng = centerLongitude ?: userLongitude,
            isDistanceFromMiddle = centerPosition != null,
            onDismissRequest = { sheetSpotId = null }
        )
    }

    chosenSpot?.takeIf { it.id == sheetSpotId }?.let { spot ->
        SpotDetailSheet(
            spot = spot,
            userLat = userLatitude,
            userLng = userLongitude,
            onDismissRequest = { sheetSpotId = null },
            onToggleSave = onToggleSaveSpot
        )
    }

    val sheetPeople = liveParticipants.filter { it.participant.user.userId in sheetUserIds }.toImmutableList()
    if (sheetPeople.isNotEmpty()) {
        LiveParticipantsSheet(
            people = sheetPeople,
            hostId = hostId,
            onDismiss = { sheetUserIds = emptySet() }
        )
    }
}

internal expect fun hangoutMapDialogProperties(): DialogProperties

@Composable
internal expect fun ThemedSystemBarIcons(isDark: Boolean)
