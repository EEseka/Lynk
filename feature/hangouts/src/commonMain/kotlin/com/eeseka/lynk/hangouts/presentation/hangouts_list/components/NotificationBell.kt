package com.eeseka.lynk.hangouts.presentation.hangouts_list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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

        if (hasUnread) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 4.dp)
                    .defaultMinSize(minWidth = 16.dp, minHeight = 16.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                LynkText(
                    text = if (unreadCount > MAX_SHOWN_UNREAD_COUNT) stringResource(Res.string.unread_count_overflow)
                    else unreadCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onError
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun NotificationBellPreview() {
    LynkTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            NotificationBell(unreadCount = 3, onClick = {})
        }
    }
}

@PreviewLightDark
@Composable
private fun NotificationBellOverflowPreview() {
    LynkTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            NotificationBell(unreadCount = 42, onClick = {})
        }
    }
}