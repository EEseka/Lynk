package com.eeseka.lynk.shared.presentation.hangout.components

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
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutUserUi
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.detail_online
import lynk.shared.generated.resources.detail_participants_overflow
import org.jetbrains.compose.resources.stringResource

@Composable
fun ParticipantStack(
    users: List<HangoutUserUi>,
    presentUserIds: Set<String> = emptySet(),
    maxVisible: Int = 5,
    avatarSize: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val overlap = avatarSize / 3
    val visible = users.take(maxVisible)
    val overflow = users.size - visible.size

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(-overlap),
        verticalAlignment = Alignment.CenterVertically
    ) {
        visible.forEachIndexed { index, user ->
            ParticipantAvatar(
                displayName = user.displayName,
                initials = user.initials,
                profilePictureUrl = user.profilePictureUrl,
                isOnline = user.userId in presentUserIds,
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
            users = List(7) { index ->
                HangoutUserUi(
                    userId = "$index",
                    username = "user$index",
                    displayName = "User $index",
                    initials = "U$index",
                    profilePictureUrl = null
                )
            },
            presentUserIds = setOf("0", "2", "3")
        )
    }
}