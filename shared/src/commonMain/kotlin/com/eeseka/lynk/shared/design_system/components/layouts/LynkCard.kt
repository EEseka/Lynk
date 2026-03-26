package com.eeseka.lynk.shared.design_system.components.layouts

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
import androidx.compose.ui.tooling.preview.Preview
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

@Preview
@Composable
private fun LynkFilledCardPreview() {
    LynkTheme {
        LynkCard(
            style = LynkCardStyle.FILLED
        ) {
            LynkText(
                text = "Hello Word",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(4.dp)
            )
            Spacer(Modifier.height(16.dp))
            LynkText(
                text = "This is a Filled Card Style",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

@Preview
@Composable
private fun LynkFilledCardPreviewDark() {
    LynkTheme(true) {
        LynkCard(
            style = LynkCardStyle.FILLED
        ) {
            LynkText(
                text = "Hello Word",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(4.dp)
            )
            Spacer(Modifier.height(16.dp))
            LynkText(
                text = "This is a Filled Card Style",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}


@Preview
@Composable
private fun LynkOutlinedCardPreview() {
    LynkTheme {
        LynkCard(
            style = LynkCardStyle.OUTLINED
        ) {
            LynkText(
                text = "Hello Word",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(4.dp)
            )
            Spacer(Modifier.height(16.dp))
            LynkText(
                text = "This is an Outlined Card Style",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

@Preview
@Composable
private fun LynkOutlinedCardPreviewDark() {
    LynkTheme(true) {
        LynkCard(
            style = LynkCardStyle.OUTLINED
        ) {
            LynkText(
                text = "Hello Word",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(4.dp)
            )
            Spacer(Modifier.height(16.dp))
            LynkText(
                text = "This is an Outlined Card Style",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}


@Preview
@Composable
private fun LynkElevatedCardPreview() {
    LynkTheme {
        LynkCard(
            style = LynkCardStyle.ELEVATED
        ) {
            LynkText(
                text = "Hello Word",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(4.dp)
            )
            Spacer(Modifier.height(16.dp))
            LynkText(
                text = "This is an Elevated Card Style",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

@Preview
@Composable
private fun LynkElevatedCardPreviewDark() {
    LynkTheme(true) {
        LynkCard(
            style = LynkCardStyle.ELEVATED
        ) {
            LynkText(
                text = "Hello Word",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(4.dp)
            )
            Spacer(Modifier.height(16.dp))
            LynkText(
                text = "This is an Elevated Card Style",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}