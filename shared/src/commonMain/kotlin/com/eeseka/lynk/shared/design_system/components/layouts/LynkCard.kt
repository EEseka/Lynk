package com.eeseka.lynk.shared.design_system.components.layouts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.util.PlatformUtils.isIOS

enum class LynkCardStyle {
    FILLED,   // Subtle tinted background — standard social feed card
    OUTLINED, // Surface background with a thin crisp border
    ELEVATED  // Casts a shadow — use sparingly
}

@Composable
fun LynkCard(
    modifier: Modifier = Modifier,
    style: LynkCardStyle = LynkCardStyle.FILLED,
    shape: Shape = LynkTheme.shapes.large,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val containerColor = when (style) {
        LynkCardStyle.FILLED -> LynkTheme.colors.surfaceVariant.copy(alpha = 0.5f)
        LynkCardStyle.OUTLINED, LynkCardStyle.ELEVATED -> LynkTheme.colors.surface
    }

    val borderStroke = if (style == LynkCardStyle.OUTLINED) {
        BorderStroke(1.dp, LynkTheme.colors.outlineVariant)
    } else null

    val elevation = if (style == LynkCardStyle.ELEVATED) 4.dp else 0.dp

    if (isIOS()) {
        val baseModifier = modifier
            .let { if (elevation > 0.dp) it.shadow(elevation, shape) else it }
            .clip(shape)
            .background(containerColor)
            .let { if (borderStroke != null) it.border(borderStroke, shape) else it }
            .let {
                if (onClick != null) {
                    it.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                } else it
            }

        Column(modifier = baseModifier, content = content)

    } else {
        when (style) {
            LynkCardStyle.FILLED -> {
                if (onClick != null) {
                    Card(
                        onClick = onClick,
                        modifier = modifier,
                        shape = shape,
                        colors = CardDefaults.cardColors(containerColor = containerColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        content = content
                    )
                } else {
                    Card(
                        modifier = modifier,
                        shape = shape,
                        colors = CardDefaults.cardColors(containerColor = containerColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        content = content
                    )
                }
            }

            LynkCardStyle.OUTLINED -> {
                val border = borderStroke ?: BorderStroke(1.dp, LynkTheme.colors.outlineVariant)
                if (onClick != null) {
                    OutlinedCard(
                        onClick = onClick,
                        modifier = modifier,
                        shape = shape,
                        colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
                        border = border,
                        content = content
                    )
                } else {
                    OutlinedCard(
                        modifier = modifier,
                        shape = shape,
                        colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
                        border = border,
                        content = content
                    )
                }
            }

            LynkCardStyle.ELEVATED -> {
                if (onClick != null) {
                    ElevatedCard(
                        onClick = onClick,
                        modifier = modifier,
                        shape = shape,
                        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation),
                        content = content
                    )
                } else {
                    ElevatedCard(
                        modifier = modifier,
                        shape = shape,
                        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation),
                        content = content
                    )
                }
            }
        }
    }
}