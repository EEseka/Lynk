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

data class LynkColors(
    // Brand
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,

    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,

    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,

    // Neutrals
    val background: Color,
    val onBackground: Color,

    val surface: Color,
    val onSurface: Color,

    val surfaceVariant: Color,
    val onSurfaceVariant: Color,

    // Elevation surfaces (cards, sheets, dialogs)
    val surfaceContainerLow: Color,   // cards, list items
    val surfaceContainerHigh: Color,  // bottom sheets, modals, dialogs

    // Utility
    val outline: Color,
    val outlineVariant: Color,
    val scrim: Color,

    // Error
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,

    // Theme flag
    // Consumed by components (e.g. LynkButton) that need to adapt content colors for dark theme without inspecting individual color luminance values.
    val isDark: Boolean
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
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,

    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,

    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,

    background = backgroundLight,
    onBackground = onBackgroundLight,

    surface = surfaceLight,
    onSurface = onSurfaceLight,

    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,

    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainerHigh = surfaceContainerHighLight,

    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,

    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,

    isDark = false
)

val LynkDarkColors = LynkColors(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,

    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,

    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,

    background = backgroundDark,
    onBackground = onBackgroundDark,

    surface = surfaceDark,
    onSurface = onSurfaceDark,

    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,

    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainerHigh = surfaceContainerHighDark,

    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,

    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,

    isDark = true
)

val LocalLynkColors = staticCompositionLocalOf<LynkColors> { error("No LynkColors provided") }
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

    object Typography {

        /** Largest display text — hero sections, splash screens */
        val displayLarge: TextStyle
            @Composable @ReadOnlyComposable
            get() = if (isIOS()) CupertinoTheme.typography.largeTitle
            else MaterialTheme.typography.displayLarge

        /** Primary screen headings */
        val headlineLarge: TextStyle
            @Composable @ReadOnlyComposable
            get() = if (isIOS()) CupertinoTheme.typography.title1
            else MaterialTheme.typography.headlineLarge

        /** Section headings */
        val headlineMedium: TextStyle
            @Composable @ReadOnlyComposable
            get() = if (isIOS()) CupertinoTheme.typography.title2
            else MaterialTheme.typography.headlineMedium

        /** Card / sheet titles */
        val titleLarge: TextStyle
            @Composable @ReadOnlyComposable
            get() = if (isIOS()) CupertinoTheme.typography.title3
            else MaterialTheme.typography.titleLarge

        /** List item primary text, emphasized UI labels */
        val titleMedium: TextStyle
            @Composable @ReadOnlyComposable
            get() = if (isIOS()) CupertinoTheme.typography.headline
            else MaterialTheme.typography.titleMedium

        /** Primary body copy */
        val bodyLarge: TextStyle
            @Composable @ReadOnlyComposable
            get() = if (isIOS()) CupertinoTheme.typography.body
            else MaterialTheme.typography.bodyLarge

        /** Secondary body copy, list item supporting text */
        val bodyMedium: TextStyle
            @Composable @ReadOnlyComposable
            get() = if (isIOS()) CupertinoTheme.typography.subhead
            else MaterialTheme.typography.bodyMedium

        /** Button labels, prominent tags */
        val labelLarge: TextStyle
            @Composable @ReadOnlyComposable
            get() = if (isIOS()) CupertinoTheme.typography.callout
            else MaterialTheme.typography.labelLarge

        /** Captions, metadata, secondary tags */
        val labelMedium: TextStyle
            @Composable @ReadOnlyComposable
            get() = if (isIOS()) CupertinoTheme.typography.footnote
            else MaterialTheme.typography.labelMedium

        /** Fine print, timestamps, badges */
        val labelSmall: TextStyle
            @Composable @ReadOnlyComposable
            get() = if (isIOS()) CupertinoTheme.typography.caption1
            else MaterialTheme.typography.labelSmall
    }
}