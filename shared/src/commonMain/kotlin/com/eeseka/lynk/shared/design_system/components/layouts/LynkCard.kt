package com.eeseka.lynk.shared.design_system.components.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme

enum class LynkCardStyle {
    FILLED,
    OUTLINED,
    ELEVATED
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

@PreviewLightDark
@Composable
private fun LynkCardPreview() {
    LynkTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            LynkCardStyle.entries.forEach { style ->
                LynkCard(style = style) {
                    LynkText(
                        text = "Hello World",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(4.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    LynkText(
                        text = "This is a ${style.name} card",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}