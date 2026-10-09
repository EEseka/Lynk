package com.eeseka.lynk.discover.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Trophy
import com.composables.icons.lucide.X
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import lynk.feature.discover.generated.resources.Res
import lynk.feature.discover.generated.resources.leave_top_spots
import lynk.feature.discover.generated.resources.top_spots_in_area
import org.jetbrains.compose.resources.stringResource

@Composable
fun TopSpotsChip(
    areaName: String,
    isActive: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val background = if (isActive) scheme.primaryContainer else scheme.surfaceContainerHigh
    val foreground = if (isActive) scheme.onPrimaryContainer else scheme.onSurface

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(background)
            .clickable(
                onClickLabel = if (isActive) stringResource(Res.string.leave_top_spots) else null,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isLoading) {
            LynkProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = Lucide.Trophy,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = foreground
            )
        }
        LynkText(
            text = stringResource(Res.string.top_spots_in_area, areaName),
            style = MaterialTheme.typography.labelLarge,
            color = foreground
        )
        if (isActive) {
            Icon(
                imageVector = Lucide.X,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = foreground
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun TopSpotsChipPreview() {
    LynkTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            TopSpotsChip(areaName = "Lagos", isActive = false, isLoading = false, onClick = {})
            TopSpotsChip(areaName = "Lagos", isActive = true, isLoading = true, onClick = {})
            TopSpotsChip(areaName = "Lagos", isActive = true, isLoading = false, onClick = {})
        }
    }
}
