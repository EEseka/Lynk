package com.eeseka.lynk.hangouts.presentation.hangouts_list.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.shared.design_system.components.buttons.LynkIconButton
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.notifications
import lynk.feature.hangouts.generated.resources.notifications_with_unread
import lynk.feature.hangouts.generated.resources.unread_count_overflow
import org.jetbrains.compose.resources.stringResource

private const val MAX_SHOWN_UNREAD_COUNT = 9

@Composable
fun NotificationBell(
    unreadCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasUnread = unreadCount > 0
    val label = if (hasUnread) stringResource(Res.string.notifications_with_unread, unreadCount)
    else stringResource(Res.string.notifications)

    Box(modifier = modifier) {
        LynkIconButton(onClick = onClick) {
            Icon(
                imageVector = Lucide.Bell,
                contentDescription = label
            )
        }

        AnimatedContent(
            targetState = unreadCount,
            contentKey = { count -> count > 0 },
            transitionSpec = { (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut()) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 4.dp)
        ) { badgeCount ->
            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 16.dp, minHeight = 16.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = badgeCount,
                        transitionSpec = {
                            val direction = if (targetState > initialState) 1 else -1
                            (slideInVertically { height -> height * direction } + fadeIn()) togetherWith (slideOutVertically { height -> -height * direction } + fadeOut())
                        }
                    ) { count ->
                        LynkText(
                            text = if (count > MAX_SHOWN_UNREAD_COUNT) stringResource(Res.string.unread_count_overflow)
                            else count.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun NotificationBellPreview() {
    LynkTheme {
        Row(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            NotificationBell(unreadCount = 3, onClick = {})
            NotificationBell(unreadCount = 42, onClick = {})
        }
    }
}