package com.eeseka.lynk.main_shell.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import lynk.feature.main_shell.generated.resources.Res
import lynk.feature.main_shell.generated.resources.unread_notifications
import org.jetbrains.compose.resources.stringResource

@Composable
fun NavigationItemIcon(
    icon: ImageVector,
    contentDescription: String,
    hasUnread: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription
        )

        if (hasUnread) {
            val unreadLabel = stringResource(Res.string.unread_notifications)

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error)
                    .clearAndSetSemantics { this.contentDescription = unreadLabel }
            )
        }
    }
}