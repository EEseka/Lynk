package com.eeseka.lynk.notifications.presentation.notifications.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.notifications.presentation.util.getIcon
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCard
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCardStyle
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.notification.model.NotificationType
import lynk.feature.notifications.generated.resources.Res
import lynk.feature.notifications.generated.resources.unread
import org.jetbrains.compose.resources.stringResource

@Composable
fun NotificationListItem(
    type: NotificationType,
    message: String,
    timeLabel: String,
    isRead: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LynkCard(
        style = if (isRead) LynkCardStyle.OUTLINED else LynkCardStyle.FILLED,
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = type.getIcon(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LynkText(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isRead) FontWeight.Normal else FontWeight.Medium,
                    color = if (isRead) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )

                LynkText(
                    text = timeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            if (!isRead) {
                Spacer(modifier = Modifier.width(8.dp))

                val unreadLabel = stringResource(Res.string.unread)

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clearAndSetSemantics { contentDescription = unreadLabel }
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun NotificationListItemUnreadPreview() {
    LynkTheme {
        NotificationListItem(
            type = NotificationType.PARTICIPANT_INVITED,
            message = "Tolu invited you to Sunday Jollof Run",
            timeLabel = "3h ago",
            isRead = false,
            onClick = {},
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        )
    }
}

@PreviewLightDark
@Composable
private fun NotificationListItemReadPreview() {
    LynkTheme {
        NotificationListItem(
            type = NotificationType.PAYOUT_SUCCEEDED,
            message = "₦24,000 from Sunday Jollof Run is on its way to your bank",
            timeLabel = "2d ago",
            isRead = true,
            onClick = {},
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        )
    }
}