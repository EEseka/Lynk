package com.eeseka.lynk.shared.design_system.components.buttons

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.util.PlatformUtils.isIOS
import com.slapps.cupertino.CupertinoButton
import com.slapps.cupertino.CupertinoButtonDefaults
import com.slapps.cupertino.ExperimentalCupertinoApi

enum class LynkButtonStyle {
    PRIMARY,
    DESTRUCTIVE_PRIMARY,
    SECONDARY,
    DESTRUCTIVE_SECONDARY,
    TEXT
}

@OptIn(ExperimentalCupertinoApi::class)
@Composable
fun LynkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: LynkButtonStyle = LynkButtonStyle.PRIMARY,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    loadingText: String? = null,
    icon: @Composable (() -> Unit)? = null
) {
    val isEffectivelyEnabled = enabled && !isLoading
    val m3Modifier = modifier.fillMaxWidth()

    if (isIOS()) {
        val shape = LynkTheme.shapes.pill

        val buttonColors = when (style) {
            LynkButtonStyle.PRIMARY -> CupertinoButtonDefaults.filledButtonColors(
                containerColor = LynkTheme.colors.primary,
                contentColor = Color.White
            )

            LynkButtonStyle.DESTRUCTIVE_PRIMARY -> CupertinoButtonDefaults.filledButtonColors(
                containerColor = LynkTheme.colors.error,
                contentColor = Color.White
            )

            LynkButtonStyle.SECONDARY, LynkButtonStyle.TEXT -> {
                val containerColor =
                    if (!MaterialTheme.colorScheme.primary.isLight()) {
                        LynkTheme.colors.primary.copy(alpha = 0.08f)
                    } else {
                        Color.Transparent
                    }
                CupertinoButtonDefaults.plainButtonColors(
                    containerColor = containerColor,
                    contentColor = LynkTheme.colors.primary
                )
            }

            LynkButtonStyle.DESTRUCTIVE_SECONDARY -> {
                val containerColor =
                    if (!MaterialTheme.colorScheme.primary.isLight()) {
                        LynkTheme.colors.error.copy(alpha = 0.08f)
                    } else {
                        Color.Transparent
                    }
                CupertinoButtonDefaults.plainButtonColors(
                    containerColor = containerColor,
                    contentColor = LynkTheme.colors.error
                )
            }
        }

        val cupertinoModifier =
            if (style == LynkButtonStyle.SECONDARY || style == LynkButtonStyle.DESTRUCTIVE_SECONDARY) {
                val borderColor =
                    if (style == LynkButtonStyle.SECONDARY) LynkTheme.colors.primary else LynkTheme.colors.error
                m3Modifier.border(
                    width = 1.dp,
                    color = if (isEffectivelyEnabled) borderColor else LynkTheme.colors.outlineVariant,
                    shape = shape
                )
            } else {
                m3Modifier
            }

        CupertinoButton(
            onClick = onClick,
            enabled = isEffectivelyEnabled,
            modifier = cupertinoModifier,
            shape = shape,
            colors = buttonColors,
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            LynkButtonContent(text, isLoading, loadingText, icon)
        }
    } else {
        val shape = CircleShape
        val elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)

        when (style) {
            LynkButtonStyle.PRIMARY -> {
                Button(
                    onClick = onClick,
                    enabled = isEffectivelyEnabled,
                    modifier = m3Modifier,
                    shape = shape,
                    elevation = elevation,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                ) {
                    LynkButtonContent(text, isLoading, loadingText, icon)
                }
            }

            LynkButtonStyle.DESTRUCTIVE_PRIMARY -> {
                Button(
                    onClick = onClick,
                    enabled = isEffectivelyEnabled,
                    modifier = m3Modifier,
                    shape = shape,
                    elevation = elevation,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                ) {
                    LynkButtonContent(text, isLoading, loadingText, icon)
                }
            }

            LynkButtonStyle.SECONDARY -> {
                val isDark = !MaterialTheme.colorScheme.primary.isLight()
                OutlinedButton(
                    onClick = onClick,
                    enabled = isEffectivelyEnabled,
                    modifier = m3Modifier,
                    shape = shape,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isEffectivelyEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.12f
                        )
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isDark) MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f) else Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.secondary,
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                ) {
                    LynkButtonContent(text, isLoading, loadingText, icon)
                }
            }

            LynkButtonStyle.DESTRUCTIVE_SECONDARY -> {
                val isDark = !MaterialTheme.colorScheme.primary.isLight()
                OutlinedButton(
                    onClick = onClick,
                    enabled = isEffectivelyEnabled,
                    modifier = m3Modifier,
                    shape = shape,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isEffectivelyEnabled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.12f
                        )
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isDark) MaterialTheme.colorScheme.error.copy(alpha = 0.08f) else Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.error,
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                ) {
                    LynkButtonContent(text, isLoading, loadingText, icon)
                }
            }

            LynkButtonStyle.TEXT -> {
                val isDark = !MaterialTheme.colorScheme.primary.isLight()
                TextButton(
                    onClick = onClick,
                    enabled = isEffectivelyEnabled,
                    modifier = m3Modifier,
                    shape = shape,
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (isDark) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    ),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    LynkButtonContent(text, isLoading, loadingText, icon)
                }
            }
        }
    }
}

private fun Color.isLight(): Boolean {
    val luminance = 0.2126f * red + 0.7152f * green + 0.0722f * blue
    return luminance > 0.5f
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

        val displayText = if (isLoading && loadingText != null) loadingText else text

        LynkText(
            text = displayText,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = LynkTheme.LynkTypography.titleMedium
        )
    }
}