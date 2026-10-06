package com.eeseka.lynk.shared.presentation.spot.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.design_system.theme.extended
import com.eeseka.lynk.shared.presentation.preview.previewWeekHours
import com.eeseka.lynk.shared.presentation.spot.model.SpotDayHoursUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.closed
import lynk.shared.generated.resources.open_now
import lynk.shared.generated.resources.spot_show_week_hours
import org.jetbrains.compose.resources.stringResource

@Composable
fun SpotOpenStatus(
    isOpenNow: Boolean,
    opensOrClosesLabel: String?,
    weekHours: ImmutableList<SpotDayHoursUi>,
    modifier: Modifier = Modifier
) {
    var isWeekShown by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(if (isWeekShown) 180f else 0f)
    val canExpand = weekHours.isNotEmpty()
    val statusColor = if (isOpenNow) MaterialTheme.colorScheme.extended.success else MaterialTheme.colorScheme.error

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = canExpand) { isWeekShown = !isWeekShown }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            LynkText(
                text = stringResource(if (isOpenNow) Res.string.open_now else Res.string.closed),
                style = MaterialTheme.typography.bodyMedium,
                color = statusColor
            )
            opensOrClosesLabel?.let { label ->
                LynkText(
                    text = "·  $label",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (canExpand) {
                Icon(
                    imageVector = Lucide.ChevronDown,
                    contentDescription = stringResource(Res.string.spot_show_week_hours),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer { rotationZ = chevronRotation }
                )
            }
        }

        AnimatedVisibility(
            visible = isWeekShown,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier.padding(start = 32.dp, end = 16.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                weekHours.forEach { day ->
                    val weight = if (day.isToday) FontWeight.SemiBold else FontWeight.Normal
                    val color = if (day.isToday) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    Row(modifier = Modifier.fillMaxWidth()) {
                        LynkText(
                            text = day.day,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = weight,
                            color = color,
                            modifier = Modifier.weight(1f)
                        )
                        LynkText(
                            text = day.hours,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = weight,
                            color = color
                        )
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun SpotOpenStatusOpenPreview() {
    LynkTheme {
        SpotOpenStatus(
            isOpenNow = true,
            opensOrClosesLabel = "Closes 1:00 AM",
            weekHours = previewWeekHours,
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        )
    }
}

@PreviewLightDark
@Composable
private fun SpotOpenStatusClosedPreview() {
    LynkTheme {
        SpotOpenStatus(
            isOpenNow = false,
            opensOrClosesLabel = "Opens 4:00 PM",
            weekHours = persistentListOf(),
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        )
    }
}
