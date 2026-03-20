package com.eeseka.lynk.shared.design_system.components.toggles_and_control

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme

data class LynkSegmentedItem(
    val title: String,
    val icon: ImageVector? = null
)

enum class LynkSegmentedStyle {
    /** Horizontally scrolling chips — for feed filters, tags, categories */
    SCROLLABLE_CHIPS,

    /** Full-width pill with animated thumb — for settings, binary/ternary mode switches */
    FIXED_BAR
}

@Composable
fun LynkSegmentedControl(
    items: List<LynkSegmentedItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    style: LynkSegmentedStyle = LynkSegmentedStyle.SCROLLABLE_CHIPS,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp)
) {
    if (style == LynkSegmentedStyle.SCROLLABLE_CHIPS) {
        LazyRow(
            modifier = modifier,
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(items) { index, item ->
                val isSelected = selectedIndex == index
                val containerColor = if (isSelected) LynkTheme.colors.secondary
                else LynkTheme.colors.surfaceVariant.copy(alpha = 0.5f)
                val contentColor = if (isSelected) LynkTheme.colors.onSecondary
                else LynkTheme.colors.onSurfaceVariant

                Row(
                    modifier = Modifier
                        .clip(LynkTheme.shapes.pill)
                        .background(containerColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onItemSelected(index) }
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (item.icon != null) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = contentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    LynkText(
                        text = item.title,
                        style = LynkTheme.Typography.labelLarge,
                        color = contentColor
                    )
                }
            }
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(LynkTheme.shapes.pill)
                .background(LynkTheme.colors.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp)
        ) {
            BoxWithConstraints(modifier = Modifier.matchParentSize()) {
                val segmentWidth = maxWidth / items.size

                val thumbOffset by animateDpAsState(
                    targetValue = segmentWidth * selectedIndex,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "thumbOffset"
                )

                Box(
                    modifier = Modifier
                        .offset(x = thumbOffset)
                        .width(segmentWidth)
                        .fillMaxHeight()
                        .border(
                            1.dp,
                            LynkTheme.colors.outlineVariant.copy(alpha = 0.3f),
                            LynkTheme.shapes.pill
                        )
                        .background(LynkTheme.colors.surface, LynkTheme.shapes.pill)
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                items.forEachIndexed { index, item ->
                    val isSelected = selectedIndex == index
                    val contentColor = if (isSelected) LynkTheme.colors.onSurface
                    else LynkTheme.colors.onSurfaceVariant

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onItemSelected(index) }
                            )
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (item.icon != null) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = contentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        LynkText(
                            text = item.title,
                            style = LynkTheme.Typography.labelMedium,
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}