package com.eeseka.lynk.hangouts.presentation.hangout_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.design_system.theme.extended
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutParticipantUi
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.detail_online
import lynk.feature.hangouts.generated.resources.detail_participants_overflow
import org.jetbrains.compose.resources.stringResource

@Composable
fun ParticipantStack(
    participants: List<HangoutParticipantUi>,
    presentUserIds: Set<String> = emptySet(),
    maxVisible: Int = 5,
    avatarSize: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val overlap = avatarSize / 3
    val visible = participants.take(maxVisible)
    val overflow = participants.size - visible.size

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(-overlap),
        verticalAlignment = Alignment.CenterVertically
    ) {
        visible.forEachIndexed { index, participant ->
            ParticipantAvatar(
                displayName = participant.displayName,
                initials = participant.initials,
                profilePictureUrl = participant.profilePictureUrl,
                isOnline = participant.userId in presentUserIds,
                size = avatarSize,
                modifier = Modifier.zIndex((visible.size - index).toFloat())
            )
        }

        if (overflow > 0) {
            AvatarBubble(
                size = avatarSize,
                background = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                LynkText(
                    text = stringResource(Res.string.detail_participants_overflow, overflow),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }
        }
    }
}

@Composable
fun ParticipantAvatar(
    displayName: String,
    initials: String,
    profilePictureUrl: String?,
    isOnline: Boolean = false,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val onlineLabel = stringResource(Res.string.detail_online)
    val description = if (isOnline) "$displayName · $onlineLabel" else displayName

    Box(modifier = modifier.clearAndSetSemantics { contentDescription = description }) {
        AvatarBubble(
            size = size,
            background = MaterialTheme.colorScheme.surfaceVariant
        ) {
            // Initials sit underneath as the fallback; the photo covers them once it loads.
            LynkText(
                text = initials,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
            if (!profilePictureUrl.isNullOrBlank()) {
                AsyncImage(
                    model = profilePictureUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(size).clip(CircleShape)
                )
            }
        }

        if (isOnline) {
            val dotSize = (size.value * 0.28f).dp
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(1.5.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.extended.success)
            )
        }
    }
}

@Composable
private fun AvatarBubble(
    size: Dp,
    background: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@PreviewLightDark
@Composable
private fun ParticipantStackPreview() {
    LynkTheme {
        ParticipantStack(
            participants = List(7) { index ->
                HangoutParticipantUi(
                    userId = "$index",
                    username = "user$index",
                    displayName = "User $index",
                    initials = "U$index",
                    profilePictureUrl = null,
                    rsvpStatus = RsvpStatus.ATTENDING,
                    hasPaid = false
                )
            },
            presentUserIds = setOf("0", "2", "3")
        )
    }
}