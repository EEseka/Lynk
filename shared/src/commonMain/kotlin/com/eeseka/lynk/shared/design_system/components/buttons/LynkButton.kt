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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    val isDark = LynkTheme.colors.isDark

    if (isIOS()) {
        val shape = LynkTheme.shapes.pill

        val buttonColors = when (style) {
            LynkButtonStyle.PRIMARY -> CupertinoButtonDefaults.filledButtonColors(
                containerColor = LynkTheme.colors.primary,
                contentColor = LynkTheme.colors.onPrimary
            )

            LynkButtonStyle.DESTRUCTIVE_PRIMARY -> CupertinoButtonDefaults.filledButtonColors(
                containerColor = LynkTheme.colors.error,
                contentColor = LynkTheme.colors.onError
            )

            LynkButtonStyle.SECONDARY, LynkButtonStyle.TEXT -> {
                CupertinoButtonDefaults.plainButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = if (isDark) LynkTheme.colors.onSurface else LynkTheme.colors.primary
                )
            }

            LynkButtonStyle.DESTRUCTIVE_SECONDARY -> {
                CupertinoButtonDefaults.plainButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = LynkTheme.colors.error
                )
            }
        }

        val cupertinoModifier =
            if (style == LynkButtonStyle.SECONDARY || style == LynkButtonStyle.DESTRUCTIVE_SECONDARY) {
                val borderColor = if (style == LynkButtonStyle.SECONDARY) LynkTheme.colors.primary
                else LynkTheme.colors.error
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
        val disabledContainer = LynkTheme.colors.onSurface.copy(alpha = 0.12f)
        val disabledContent = LynkTheme.colors.onSurface.copy(alpha = 0.38f)

        when (style) {
            LynkButtonStyle.PRIMARY -> {
                Button(
                    onClick = onClick,
                    enabled = isEffectivelyEnabled,
                    modifier = m3Modifier,
                    shape = shape,
                    elevation = elevation,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LynkTheme.colors.primary,
                        contentColor = LynkTheme.colors.onPrimary,
                        disabledContainerColor = disabledContainer,
                        disabledContentColor = disabledContent
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
                        containerColor = LynkTheme.colors.error,
                        contentColor = LynkTheme.colors.onError,
                        disabledContainerColor = disabledContainer,
                        disabledContentColor = disabledContent
                    )
                ) {
                    LynkButtonContent(text, isLoading, loadingText, icon)
                }
            }

            LynkButtonStyle.SECONDARY -> {
                OutlinedButton(
                    onClick = onClick,
                    enabled = isEffectivelyEnabled,
                    modifier = m3Modifier,
                    shape = shape,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isEffectivelyEnabled) LynkTheme.colors.primary
                        else LynkTheme.colors.onSurface.copy(alpha = 0.12f)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = if (isDark) LynkTheme.colors.onSurface else LynkTheme.colors.primary,
                        disabledContentColor = disabledContent
                    )
                ) {
                    LynkButtonContent(text, isLoading, loadingText, icon)
                }
            }

            LynkButtonStyle.DESTRUCTIVE_SECONDARY -> {
                OutlinedButton(
                    onClick = onClick,
                    enabled = isEffectivelyEnabled,
                    modifier = m3Modifier,
                    shape = shape,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isEffectivelyEnabled) LynkTheme.colors.error
                        else LynkTheme.colors.onSurface.copy(alpha = 0.12f)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = LynkTheme.colors.error,
                        disabledContentColor = disabledContent
                    )
                ) {
                    LynkButtonContent(text, isLoading, loadingText, icon)
                }
            }

            LynkButtonStyle.TEXT -> {
                TextButton(
                    onClick = onClick,
                    enabled = isEffectivelyEnabled,
                    modifier = m3Modifier,
                    shape = shape,
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = if (isDark) LynkTheme.colors.onSurface else LynkTheme.colors.primary,
                        disabledContentColor = disabledContent
                    ),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    LynkButtonContent(text, isLoading, loadingText, icon)
                }
            }
        }
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
                    val color =
                        if (isIOS()) com.slapps.cupertino.LocalContentColor.current else LocalContentColor.current
                    LynkProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = color
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
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = LynkTheme.Typography.titleMedium
        )
    }
}