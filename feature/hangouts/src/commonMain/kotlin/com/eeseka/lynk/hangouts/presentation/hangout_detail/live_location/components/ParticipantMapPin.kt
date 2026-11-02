package com.eeseka.lynk.hangouts.presentation.hangout_detail.live_location.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.presentation.hangout.components.ParticipantAvatar
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUserUi
import org.maplibre.compose.overlay.MapOverlayScope
import org.maplibre.spatialk.geojson.Position

private val AVATAR_SIZE = 40.dp
private val RING_WIDTH = 4.dp
private val BADGE_SIZE = 24.dp

// One person, or the first of a stack with a count; your own wears primary and does nothing when tapped
@Composable
fun MapOverlayScope.ParticipantMapPin(
    user: HangoutUserUi,
    latitude: Double,
    longitude: Double,
    ringColor: Color,
    onClick: (() -> Unit)?,
    stackSize: Int = 1,
    lastSharedLabel: String? = null
) {
    val scheme = MaterialTheme.colorScheme
    val tapModifier = if (onClick != null) {
        Modifier.clickable(role = Role.Button, onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = Modifier.placedAt(
            position = Position(longitude = longitude, latitude = latitude),
            alignment = Alignment.Center
        )
    ) {
        Box(
            modifier = Modifier
                .size(AVATAR_SIZE + RING_WIDTH * 2)
                .clip(CircleShape)
                .background(ringColor)
                .then(tapModifier),
            contentAlignment = Alignment.Center
        ) {
            ParticipantAvatar(
                displayName = user.displayName,
                initials = user.initials,
                profilePictureUrl = user.profilePictureUrl,
                size = AVATAR_SIZE
            )
        }

        if (stackSize > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .defaultMinSize(minWidth = BADGE_SIZE, minHeight = BADGE_SIZE)
                    .clip(CircleShape)
                    .background(scheme.primary)
                    .border(2.dp, scheme.surface, CircleShape)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                LynkText(
                    text = stackSize.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onPrimary
                )
            }
        }

        lastSharedLabel?.let { label ->
            LynkText(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = scheme.onSurface,
                maxLines = 1,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 12.dp)
                    .wrapContentWidth(unbounded = true)
                    .clip(CircleShape)
                    .background(scheme.surface)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
