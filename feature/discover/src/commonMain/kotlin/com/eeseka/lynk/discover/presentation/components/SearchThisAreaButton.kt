package com.eeseka.lynk.discover.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.RefreshCw
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.presentation.spot.util.DistanceCalculator
import lynk.feature.discover.generated.resources.Res
import lynk.feature.discover.generated.resources.search_this_area
import org.jetbrains.compose.resources.stringResource
import org.maplibre.compose.camera.CameraMoveReason
import org.maplibre.compose.map.MapState

private const val MIN_DISTANCE_METERS = 2_000
private const val MIN_ZOOM = 11.0

@Composable
fun SearchThisAreaButton(
    mapState: MapState,
    trendingLatitude: Double?,
    trendingLongitude: Double?,
    isLoading: Boolean,
    onClick: (latitude: Double, longitude: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    // Reads the position only once the camera rests, so a pan doesn't recompose the screen every frame
    val isCameraMoving = mapState.isCameraMoving
    val isVisible = if (isCameraMoving || trendingLatitude == null || trendingLongitude == null) {
        false
    } else {
        val camera = mapState.cameraPosition
        val wasMovedByUser = mapState.cameraMoveReason == CameraMoveReason.GESTURE
        val distanceMeters = DistanceCalculator.calculateDistanceInMeters(
            userLat = trendingLatitude,
            userLng = trendingLongitude,
            spotLat = camera.target.latitude,
            spotLng = camera.target.longitude
        )
        wasMovedByUser && camera.zoom >= MIN_ZOOM && distanceMeters > MIN_DISTANCE_METERS
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically { height -> -height / 2 },
        exit = fadeOut() + slideOutVertically { height -> -height / 2 },
        modifier = modifier
    ) {
        SearchThisAreaPill(
            isLoading = isLoading,
            onClick = {
                val target = mapState.cameraPosition.target
                onClick(target.latitude, target.longitude)
            }
        )
    }
}

@Composable
private fun SearchThisAreaPill(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(
                enabled = !isLoading,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isLoading) {
            LynkProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = Lucide.RefreshCw,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        LynkText(
            text = stringResource(Res.string.search_this_area),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@PreviewLightDark
@Composable
private fun SearchThisAreaPillPreview() {
    LynkTheme {
        SearchThisAreaPill(isLoading = false, onClick = {})
    }
}

@PreviewLightDark
@Composable
private fun SearchThisAreaPillLoadingPreview() {
    LynkTheme {
        SearchThisAreaPill(isLoading = true, onClick = {})
    }
}
