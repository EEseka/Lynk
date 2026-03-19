package com.eeseka.lynk.shared.design_system.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.domain.util.PlatformUtils.isIOS
import com.slapps.cupertino.theme.CupertinoTheme

// All the colors I will realistically need for custom layouts
data class LynkColors(
    val primary: Color,
    val primaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color,
    val tertiary: Color,
    val tertiaryContainer: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val textMain: Color,
    val textMuted: Color,
    val outline: Color,
    val outlineVariant: Color,
    val error: Color,
    val errorContainer: Color
)

data class LynkShapes(
    val extraSmall: Shape = RoundedCornerShape(4.dp),
    val small: Shape = RoundedCornerShape(8.dp),
    val medium: Shape = RoundedCornerShape(12.dp),
    val large: Shape = RoundedCornerShape(16.dp),
    val extraLarge: Shape = RoundedCornerShape(24.dp),
    val pill: Shape = RoundedCornerShape(50)
)

val LynkLightColors = LynkColors(
    primary = primaryLight,
    primaryContainer = primaryContainerLight,
    secondary = secondaryLight,
    secondaryContainer = secondaryContainerLight,
    tertiary = tertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    background = backgroundLight,
    surface = surfaceLight,
    surfaceVariant = surfaceVariantLight,
    textMain = onBackgroundLight,
    textMuted = outlineLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    error = errorLight,
    errorContainer = errorContainerLight
)

val LynkDarkColors = LynkColors(
    primary = primaryDark,
    primaryContainer = primaryContainerDark,
    secondary = secondaryDark,
    secondaryContainer = secondaryContainerDark,
    tertiary = tertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    background = backgroundDark,
    surface = surfaceDark,
    surfaceVariant = surfaceVariantDark,
    textMain = onBackgroundDark,
    textMuted = outlineDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    error = errorDark,
    errorContainer = errorContainerDark
)

val LocalLynkColors = staticCompositionLocalOf<LynkColors> { error("No colors provided") }
val LocalLynkShapes = staticCompositionLocalOf { LynkShapes() }

object LynkTheme {
    val colors: LynkColors
        @Composable
        @ReadOnlyComposable
        get() = LocalLynkColors.current

    val shapes: LynkShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalLynkShapes.current

    object LynkTypography {
        val displayLarge: TextStyle
            @Composable @ReadOnlyComposable
            get() = if (isIOS()) CupertinoTheme.typography.largeTitle else MaterialTheme.typography.displayLarge

        val headlineLarge: TextStyle
            @Composable @ReadOnlyComposable
            get() = if (isIOS()) CupertinoTheme.typography.title1 else MaterialTheme.typography.headlineLarge

        val headlineMedium: TextStyle
            @Composable @ReadOnlyComposable
            get() = if (isIOS()) CupertinoTheme.typography.title2 else MaterialTheme.typography.headlineMedium

        val titleLarge: TextStyle
            @Composable @ReadOnlyComposable
            get() = if (isIOS()) CupertinoTheme.typography.title3 else MaterialTheme.typography.titleLarge

        val titleMedium: TextStyle
            @Composable @ReadOnlyComposable
            get() = if (isIOS()) CupertinoTheme.typography.headline else MaterialTheme.typography.titleMedium

        val bodyLarge: TextStyle
            @Composable @ReadOnlyComposable
            get() = if (isIOS()) CupertinoTheme.typography.body else MaterialTheme.typography.bodyLarge

        val bodyMedium: TextStyle
            @Composable @ReadOnlyComposable
            get() = if (isIOS()) CupertinoTheme.typography.subhead else MaterialTheme.typography.bodyMedium

        val labelLarge: TextStyle
            @Composable @ReadOnlyComposable
            get() = if (isIOS()) CupertinoTheme.typography.callout else MaterialTheme.typography.labelLarge

        val labelMedium: TextStyle
            @Composable @ReadOnlyComposable
            get() = if (isIOS()) CupertinoTheme.typography.footnote else MaterialTheme.typography.labelMedium

        val labelSmall: TextStyle
            @Composable @ReadOnlyComposable
            get() = if (isIOS()) CupertinoTheme.typography.caption1 else MaterialTheme.typography.labelSmall
    }
}