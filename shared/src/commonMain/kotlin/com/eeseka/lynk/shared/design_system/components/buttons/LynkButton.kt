package com.eeseka.lynk.shared.design_system.components.buttons

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.design_system.theme.extended
import com.mohamedrejeb.calf.ui.button.AdaptiveButton
import com.mohamedrejeb.calf.ui.button.LiquidGlassButtonColors

enum class LynkButtonStyle {
    PRIMARY,
    DESTRUCTIVE_PRIMARY,
    SECONDARY,
    DESTRUCTIVE_SECONDARY,
    TEXT
}

@Composable
fun LynkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: LynkButtonStyle = LynkButtonStyle.PRIMARY,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    loadingText: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    val isEffectivelyEnabled = enabled && !isLoading
    val shape = CircleShape
    val scheme = MaterialTheme.colorScheme

    val disabledContainer = scheme.onSurface.copy(alpha = 0.1f)
    val disabledContent = scheme.onSurfaceVariant.copy(alpha = 0.38f)

    val colors = when (style) {
        LynkButtonStyle.PRIMARY -> ButtonDefaults.buttonColors(
            containerColor = scheme.primaryContainer,
            contentColor = scheme.onPrimaryContainer,
            disabledContainerColor = disabledContainer,
            disabledContentColor = disabledContent
        )

        LynkButtonStyle.DESTRUCTIVE_PRIMARY -> ButtonDefaults.buttonColors(
            containerColor = scheme.errorContainer,
            contentColor = scheme.onErrorContainer,
            disabledContainerColor = disabledContainer,
            disabledContentColor = disabledContent
        )

        LynkButtonStyle.SECONDARY -> ButtonDefaults.buttonColors(
            containerColor = scheme.extended.neutralSurface,
            contentColor = scheme.onSurface,
            disabledContainerColor = disabledContainer,
            disabledContentColor = disabledContent
        )

        LynkButtonStyle.DESTRUCTIVE_SECONDARY -> ButtonDefaults.buttonColors(
            containerColor = scheme.extended.neutralSurface,
            contentColor = scheme.error,
            disabledContainerColor = disabledContainer,
            disabledContentColor = disabledContent
        )

        LynkButtonStyle.TEXT -> ButtonDefaults.buttonColors(
            containerColor = scheme.extended.neutralSurface,
            contentColor = scheme.tertiary,
            disabledContainerColor = disabledContainer,
            disabledContentColor = disabledContent
        )
    }

    val liquidGlassColors = when (style) {
        LynkButtonStyle.PRIMARY -> LiquidGlassButtonColors(
            tintColor = scheme.primaryContainer,
            surfaceColor = scheme.primaryContainer,
            contentColor = scheme.onPrimaryContainer,
            disabledContentColor = disabledContent
        )

        LynkButtonStyle.DESTRUCTIVE_PRIMARY -> LiquidGlassButtonColors(
            tintColor = scheme.errorContainer,
            surfaceColor = scheme.errorContainer,
            contentColor = scheme.onErrorContainer,
            disabledContentColor = disabledContent
        )

        LynkButtonStyle.SECONDARY -> LiquidGlassButtonColors(
            tintColor = scheme.extended.neutralSurface,
            surfaceColor = scheme.extended.neutralSurface,
            contentColor = scheme.onSurface,
            disabledContentColor = disabledContent
        )

        LynkButtonStyle.DESTRUCTIVE_SECONDARY -> LiquidGlassButtonColors(
            tintColor = scheme.extended.neutralSurface,
            surfaceColor = scheme.extended.neutralSurface,
            contentColor = scheme.error,
            disabledContentColor = disabledContent
        )

        LynkButtonStyle.TEXT -> LiquidGlassButtonColors(
            tintColor = scheme.extended.neutralSurface,
            surfaceColor = scheme.extended.neutralSurface,
            contentColor = scheme.tertiary,
            disabledContentColor = disabledContent
        )

    }

    AdaptiveButton(
        onClick = onClick,
        enabled = isEffectivelyEnabled,
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        colors = colors,
        liquidGlassColors = liquidGlassColors,
        contentPadding = PaddingValues(vertical = 16.dp),
    ) {
        LynkButtonContent(text, isLoading, loadingText, leadingIcon)
    }
}

@Composable
private fun LynkButtonContent(
    text: String,
    isLoading: Boolean,
    loadingText: String?,
    icon: @Composable (() -> Unit)?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (isLoading || icon != null) {
            AnimatedContent(
                targetState = isLoading,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut() using SizeTransform(clip = false)
                },
                label = "LynkButtonIconTransition"
            ) { loading ->
                if (loading) {
                    LynkProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = LocalContentColor.current
                    )
                } else {
                    icon?.invoke()
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        LynkText(
            text = if (isLoading && loadingText != null) loadingText else text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Preview
@Composable
private fun LynkPrimaryButtonPreview() {
    LynkTheme {
        LynkButton(
            text = "Click me!",
            onClick = {},
            style = LynkButtonStyle.PRIMARY
        )
    }
}

@Preview
@Composable
private fun LynkSecondaryButtonPreview() {
    LynkTheme {
        LynkButton(
            text = "Click me!",
            onClick = {},
            style = LynkButtonStyle.SECONDARY
        )
    }
}


@Preview
@Composable
private fun LynkSecondaryButtonPreviewDark() {
    LynkTheme(true) {
        LynkButton(
            text = "Click me!",
            onClick = {},
            style = LynkButtonStyle.SECONDARY
        )
    }
}


@Preview
@Composable
private fun LynkTextButtonPreview() {
    LynkTheme {
        LynkButton(
            text = "Click me!",
            onClick = {},
            style = LynkButtonStyle.TEXT
        )
    }
}

@Preview
@Composable
private fun LynkTextButtonPreviewDark() {
    LynkTheme(true) {
        LynkButton(
            text = "Click me!",
            onClick = {},
            style = LynkButtonStyle.TEXT
        )
    }
}

@Preview
@Composable
private fun LynkDestructivePrimaryButtonPreview() {
    LynkTheme {
        LynkButton(
            text = "Click me!",
            onClick = {},
            style = LynkButtonStyle.DESTRUCTIVE_PRIMARY
        )
    }
}

@Preview
@Composable
private fun LynkDestructiveSecondaryButtonPreview() {
    LynkTheme {
        LynkButton(
            text = "Click me!",
            onClick = {},
            style = LynkButtonStyle.DESTRUCTIVE_SECONDARY
        )
    }
}

@Preview
@Composable
private fun LynkDestructiveSecondaryButtonPreviewDark() {
    LynkTheme(true) {
        LynkButton(
            text = "Click me!",
            onClick = {},
            style = LynkButtonStyle.DESTRUCTIVE_SECONDARY
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LynkDisabledButtonPreview() {
    LynkTheme {
        LynkButton(
            text = "Click me!",
            onClick = {},
            enabled = false
        )
    }
}

@Preview
@Composable
private fun LynkDisabledButtonPreviewDark() {
    LynkTheme(true) {
        LynkButton(
            text = "Click me!",
            onClick = {},
            enabled = false
        )
    }
}