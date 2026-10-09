package com.eeseka.lynk.hangouts.presentation.hangout_detail.voting.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.eeseka.lynk.shared.design_system.components.images.LynkAsyncImage
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.extended
import com.eeseka.lynk.shared.presentation.spot.util.SpotPhotoUrlBuilder
import com.eeseka.lynk.shared.presentation.spot.util.rememberGoogleImageRequest
import kotlinx.collections.immutable.ImmutableList
import org.maplibre.compose.overlay.MapOverlayScope
import org.maplibre.spatialk.geojson.Position

private val PHOTO_SIZE = 48.dp
private val RING_WIDTH = 4.dp
private val BADGE_SIZE = 24.dp
private const val SELECTED_SCALE = 1.25f

// A candidate as its own photo, ringed like CandidateCard: gold leads, yours is primary, tied is tertiary
@Composable
fun MapOverlayScope.CandidateMapPin(
    spotName: String,
    latitude: Double,
    longitude: Double,
    photoUrls: ImmutableList<String>,
    voteCount: Int,
    isMyVote: Boolean,
    isLeading: Boolean,
    isTiebreakTarget: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    val ringColor by animateColorAsState(
        when {
            isTiebreakTarget -> scheme.tertiary
            isLeading -> scheme.extended.gold
            isMyVote -> scheme.primary
            else -> scheme.surface
        }
    )
    val scale by animateFloatAsState(if (isSelected) SELECTED_SCALE else 1f)

    val primaryPhotoUrl = remember(photoUrls) {
        SpotPhotoUrlBuilder.getPrimaryPhotoUrl(photoUrls)
    }
    val imageRequest = rememberGoogleImageRequest(url = primaryPhotoUrl ?: "")

    Box(
        modifier = Modifier
            .placedAt(
                position = Position(longitude = longitude, latitude = latitude),
                alignment = Alignment.Center
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Box(
            modifier = Modifier
                .padding(top = BADGE_SIZE / 2, end = BADGE_SIZE / 2)
                .size(PHOTO_SIZE)
                .clip(CircleShape)
                .background(scheme.surfaceVariant)
                .border(RING_WIDTH, ringColor, CircleShape)
                .clickable(role = Role.Button, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (imageRequest != null) {
                LynkAsyncImage(
                    model = imageRequest,
                    contentDescription = spotName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(PHOTO_SIZE - RING_WIDTH * 2)
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Lucide.MapPin,
                    contentDescription = spotName,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (voteCount > 0) {
            VoteCountBadge(
                voteCount = voteCount,
                isMyVote = isMyVote,
                isLeading = isLeading,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}

@Composable
private fun VoteCountBadge(
    voteCount: Int,
    isMyVote: Boolean,
    isLeading: Boolean,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val background = when {
        isLeading -> scheme.extended.gold
        isMyVote -> scheme.primary
        else -> scheme.surface
    }
    val content = when {
        isLeading -> scheme.extended.onGold
        isMyVote -> scheme.onPrimary
        else -> scheme.onSurface
    }

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = BADGE_SIZE, minHeight = BADGE_SIZE)
            .clip(CircleShape)
            .background(background)
            .border(2.dp, scheme.surface, CircleShape)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        LynkText(
            text = voteCount.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = content
        )
    }
}
