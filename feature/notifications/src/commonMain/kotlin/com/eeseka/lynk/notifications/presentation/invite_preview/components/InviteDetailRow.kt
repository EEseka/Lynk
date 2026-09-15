package com.eeseka.lynk.notifications.presentation.invite_preview.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme

@Composable
fun InviteDetailRow(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            LynkText(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            supportingText?.takeIf { it.isNotBlank() }?.let {
                LynkText(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun InviteDetailRowPreview() {
    LynkTheme {
        InviteDetailRow(
            icon = Lucide.MapPin,
            text = "Nok by Alara",
            supportingText = "Restaurant • ₦₦ • Victoria Island, Lagos",
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(16.dp)
        )
    }
}

@PreviewLightDark
@Composable
private fun InviteDetailRowLongTextPreview() {
    LynkTheme {
        InviteDetailRow(
            icon = Lucide.MapPin,
            text = "The Yellow Chilli Restaurant and Lounge, Victoria Island Annexe",
            supportingText = "Restaurant • ₦₦₦ • 27 Oju Olobun Close, Victoria Island, Lagos",
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(16.dp)
        )
    }
}