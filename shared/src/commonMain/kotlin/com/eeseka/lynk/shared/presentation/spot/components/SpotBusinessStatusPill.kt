package com.eeseka.lynk.shared.presentation.spot.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.design_system.theme.extended
import com.eeseka.lynk.shared.domain.spot.model.BusinessStatus
import com.eeseka.lynk.shared.presentation.spot.mappers.getIcon
import com.eeseka.lynk.shared.presentation.spot.mappers.getTitle

@Composable
fun SpotBusinessStatusPill(
    status: BusinessStatus,
    modifier: Modifier = Modifier
) {
    val title = status.getTitle() ?: return // An operating spot has no label, so nothing to draw
    val scheme = MaterialTheme.colorScheme
    val (background, foreground) = when (status) {
        BusinessStatus.CLOSED_PERMANENTLY -> scheme.errorContainer to scheme.onErrorContainer
        BusinessStatus.CLOSED_TEMPORARILY -> scheme.extended.warningContainer to scheme.extended.onWarningContainer
        BusinessStatus.FUTURE_OPENING, BusinessStatus.OPERATIONAL -> scheme.tertiaryContainer to scheme.onTertiaryContainer
    }

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(background)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = status.getIcon(),
            contentDescription = null,
            tint = foreground,
            modifier = Modifier.size(12.dp)
        )
        LynkText(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = foreground
        )
    }
}

@PreviewLightDark
@Composable
private fun SpotBusinessStatusPillPreview() {
    LynkTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            SpotBusinessStatusPill(status = BusinessStatus.FUTURE_OPENING)
            SpotBusinessStatusPill(status = BusinessStatus.CLOSED_TEMPORARILY)
            SpotBusinessStatusPill(status = BusinessStatus.CLOSED_PERMANENTLY)
        }
    }
}
