package com.eeseka.lynk.shared.design_system.components.layouts

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

enum class LynkCardStyle {
    FILLED,   // Subtle tinted background — standard social feed card
    OUTLINED, // Surface background with a thin crisp border
    ELEVATED  // Casts a shadow — use sparingly
}

@Composable
fun LynkCard(
    modifier: Modifier = Modifier,
    style: LynkCardStyle = LynkCardStyle.FILLED,
    shape: Shape = MaterialTheme.shapes.large,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    when (style) {
        LynkCardStyle.FILLED -> {
            if (onClick != null) {
                Card(
                    onClick = onClick,
                    modifier = modifier,
                    shape = shape,
                    content = content
                )
            } else {
                Card(
                    modifier = modifier,
                    shape = shape,
                    content = content
                )
            }
        }

        LynkCardStyle.OUTLINED -> {
            if (onClick != null) {
                OutlinedCard(
                    onClick = onClick,
                    modifier = modifier,
                    shape = shape,
                    content = content
                )
            } else {
                OutlinedCard(
                    modifier = modifier,
                    shape = shape,
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
                    content = content
                )
            } else {
                ElevatedCard(
                    modifier = modifier,
                    shape = shape,
                    content = content
                )
            }
        }
    }
}