package com.eeseka.lynk.hangouts.presentation.hangout_detail.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.eeseka.lynk.shared.design_system.components.buttons.LynkTonalIconButton
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.domain.location.LocationCoordinates
import com.eeseka.lynk.shared.domain.settings.AppTheme
import com.eeseka.lynk.shared.domain.util.onSuccess
import com.eeseka.lynk.shared.presentation.components.SpotDetailSheet
import com.eeseka.lynk.shared.presentation.location.rememberLocationController
import com.eeseka.lynk.shared.presentation.map.components.MapAttributionMenu
import com.eeseka.lynk.shared.presentation.map.components.SelectedSpotPinOverlay
import com.eeseka.lynk.shared.presentation.map.components.SpotLocationMapMarker
import com.eeseka.lynk.shared.presentation.map.components.UserLocationMapMarker
import com.eeseka.lynk.shared.presentation.map.components.rememberSpotMapInteractions
import com.eeseka.lynk.shared.presentation.map.util.isMapDark
import com.eeseka.lynk.shared.presentation.map.util.mapStyleUri
import com.eeseka.lynk.shared.presentation.map.util.toBoundingBox
import com.eeseka.lynk.shared.presentation.permissions.Permission
import com.eeseka.lynk.shared.presentation.permissions.PermissionState
import com.eeseka.lynk.shared.presentation.permissions.rememberPermissionController
import com.eeseka.lynk.shared.presentation.spot.model.SpotUi
import kotlinx.collections.immutable.ImmutableList
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.map_close
import org.jetbrains.compose.resources.stringResource
import org.maplibre.compose.camera.CameraPosition
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

private const val SINGLE_SPOT_ZOOM = 15.0

// Covers the whole screen like LynkFullScreenDialog, but follows the app theme instead of staying dark
@Composable
fun HangoutMapDialog(
    spots: ImmutableList<SpotUi>,
    centerLatitude: Double?,
    centerLongitude: Double?,
    mapTheme: AppTheme,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = hangoutMapDialogProperties()
    ) {
        ThemedSystemBarIcons(isDark = isMapDark(mapTheme))
        HangoutMapContent(
            spots = spots,
            centerLatitude = centerLatitude,
            centerLongitude = centerLongitude,
            mapTheme = mapTheme,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun HangoutMapContent(
    spots: ImmutableList<SpotUi>,
    centerLatitude: Double?,
    centerLongitude: Double?,
    mapTheme: AppTheme,
    onDismiss: () -> Unit
) {
    val hapticFeedback = rememberAppHaptic()
    val permissionController = rememberPermissionController()
    val locationController = rememberLocationController()

    var selectedSpotId by remember { mutableStateOf<String?>(null) }
    var userLocation by remember { mutableStateOf<LocationCoordinates?>(null) }

    val centerPosition = if (centerLatitude != null && centerLongitude != null) {
        Position(longitude = centerLongitude, latitude = centerLatitude)
    } else null
    val openingTarget = centerPosition
        ?: spots.firstOrNull()?.let { Position(longitude = it.longitude, latitude = it.latitude) }
        ?: Position(longitude = 0.0, latitude = 0.0)

    val mapState = rememberMapState(
        baseStyle = BaseStyle.Uri(mapStyleUri(mapTheme)),
        initialCameraPosition = CameraPosition(target = openingTarget, zoom = SINGLE_SPOT_ZOOM),
        content = {
            SpotLocationMapMarker(
                spots = spots,
                selectedSpotId = selectedSpotId,
                isRanked = false
            )
        }
    )

    val selectedSpot = spots.find { it.id == selectedSpotId }

    val spotMapInteractions = rememberSpotMapInteractions(mapState) { spotId ->
        hapticFeedback(AppHaptic.ImpactLight)
        selectedSpotId = spotId
    }

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
        val pointCount = spots.size + if (centerPosition != null) 1 else 0
        if (pointCount < 2) return@LaunchedEffect

        val pointsBounds = spots.toBoundingBox(including = centerPosition) ?: return@LaunchedEffect
        mapState.animateCameraToBounds(
            boundingBox = pointsBounds,
            fitPadding = DpPadding(left = 48.dp, top = 64.dp, right = 48.dp, bottom = 48.dp)
        )
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
            interactions = spotMapInteractions,
            viewportInsets = WindowInsets(top = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding() + 64.dp)
                .union(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .asPaddingValues()
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

            SelectedSpotPinOverlay(spot = selectedSpot)
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

        MapAttributionMenu(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp)
        )
    }

    selectedSpot?.let { spot ->
        SpotDetailSheet(
            spot = spot,
            userLat = centerLatitude ?: userLocation?.latitude,
            userLng = centerLongitude ?: userLocation?.longitude,
            onDismissRequest = { selectedSpotId = null }
        )
    }
}

internal expect fun hangoutMapDialogProperties(): DialogProperties

@Composable
internal expect fun ThemedSystemBarIcons(isDark: Boolean)
