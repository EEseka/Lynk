package com.eeseka.lynk.shared.design_system.components.toggles_and_control

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.mohamedrejeb.calf.ui.gesture.adaptiveClickable

@Immutable
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
    val scheme = MaterialTheme.colorScheme

    when (style) {
        LynkSegmentedStyle.SCROLLABLE_CHIPS -> {
            LazyRow(
                modifier = modifier,
                contentPadding = contentPadding,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(items) { index, item ->
                    val isSelected = selectedIndex == index
                    val containerColor = if (isSelected) scheme.secondaryContainer
                    else scheme.surfaceContainerLow
                    val contentColor = if (isSelected) scheme.onSecondaryContainer
                    else scheme.onSurfaceVariant

                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(containerColor)
                            .adaptiveClickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                role = Role.Tab,
                                shape = CircleShape,
                                onClick = { onItemSelected(index) }
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        LynkText(
                            text = item.title,
                            style = MaterialTheme.typography.labelLarge,
                            color = contentColor
                        )
                    }
                }
            }
        }

        LynkSegmentedStyle.FIXED_BAR -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(scheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp)
            ) {
                BoxWithConstraints(modifier = Modifier.matchParentSize()) {
                    val segmentWidth = maxWidth / items.size
                    val animatedOffset by animateFloatAsState(
                        targetValue = selectedIndex.toFloat(),
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "thumbOffset"
                    )
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                translationX = segmentWidth.toPx() * animatedOffset
                            }
                            .width(segmentWidth)
                            .fillMaxHeight()
                            .border(
                                1.dp,
                                scheme.outlineVariant.copy(alpha = 0.3f),
                                CircleShape
                            )
                            .background(scheme.surface, CircleShape)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    items.forEachIndexed { index, item ->
                        val isSelected = selectedIndex == index
                        val contentColor = if (isSelected) scheme.onSurface
                        else scheme.onSurfaceVariant

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .adaptiveClickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    role = Role.Tab,
                                    onClick = { onItemSelected(index) }
                                )
                                .padding(vertical = 8.dp),
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
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            LynkText(
                                text = item.title,
                                style = MaterialTheme.typography.labelMedium,
                                color = contentColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun LynkSegmentedControlPreview() {
    LynkTheme {
        LynkSegmentedControl(
            items = previewItems,
            selectedIndex = 0,
            onItemSelected = {},
            style = LynkSegmentedStyle.SCROLLABLE_CHIPS
        )
    }
}

@Preview
@Composable
private fun LynkSegmentedControlPreviewDark() {
    LynkTheme(true) {
        LynkSegmentedControl(
            items = previewItems,
            selectedIndex = 0,
            onItemSelected = {},
            style = LynkSegmentedStyle.SCROLLABLE_CHIPS
        )
    }
}

@Preview
@Composable
private fun LynkSegmentedControlFixedPreview() {
    LynkTheme {
        LynkSegmentedControl(
            items = previewItems,
            selectedIndex = 1,
            onItemSelected = {},
            style = LynkSegmentedStyle.FIXED_BAR
        )
    }
}

@Preview
@Composable
private fun LynkSegmentedControlFixedPreviewDark() {
    LynkTheme(true) {
        LynkSegmentedControl(
            items = previewItems,
            selectedIndex = 1,
            onItemSelected = {},
            style = LynkSegmentedStyle.FIXED_BAR
        )
    }
}

private val previewItems = listOf(
    LynkSegmentedItem("All"),
    LynkSegmentedItem("Unread"),
    LynkSegmentedItem("Archived")
)