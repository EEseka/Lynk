package com.eeseka.lynk.hangouts.presentation.hangout_detail.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Scale
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCard
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCardStyle
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme

@Composable
fun PlaceholderCard(
    icon: ImageVector,
    title: String,
    message: String?,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    LynkCard(style = LynkCardStyle.OUTLINED, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LynkText(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )
                message?.let {
                    LynkText(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceholderCardPreview(
    icon: ImageVector = Lucide.MapPin,
    title: String = "No spot yet",
    message: String? = "The group hasn't locked in a place.",
    iconTint: Color? = null
) {
    LynkTheme {
        PlaceholderCard(
            icon = icon,
            title = title,
            message = message,
            iconTint = iconTint ?: MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(4.dp)
        )
    }
}

@PreviewLightDark
@Composable
private fun PlaceholderCardDefaultPreview() = PlaceholderCardPreview()

@PreviewLightDark
@Composable
private fun PlaceholderCardTitleOnlyPreview() = PlaceholderCardPreview(
    icon = Lucide.Scale,
    title = "It's a tie — pick the winner",
    message = null,
    iconTint = MaterialTheme.colorScheme.tertiary
)