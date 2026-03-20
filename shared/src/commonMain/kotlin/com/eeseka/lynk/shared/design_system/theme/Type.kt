package com.eeseka.lynk.shared.design_system.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.jetbrains_mono_medium
import lynk.shared.generated.resources.plus_jakarta_sans_bold
import lynk.shared.generated.resources.plus_jakarta_sans_medium
import lynk.shared.generated.resources.plus_jakarta_sans_regular
import lynk.shared.generated.resources.space_grotesk_bold
import org.jetbrains.compose.resources.Font
import androidx.compose.material3.Typography as MaterialTypography
import com.slapps.cupertino.theme.Typography as CupertinoTypography

@Composable
fun getMaterialTypography(): MaterialTypography {
    val spaceGrotesk = FontFamily(
        Font(Res.font.space_grotesk_bold, FontWeight.Bold)
    )
    val plusJakartaSans = FontFamily(
        Font(Res.font.plus_jakarta_sans_regular, FontWeight.Normal),
        Font(Res.font.plus_jakarta_sans_medium, FontWeight.Medium),
        Font(Res.font.plus_jakarta_sans_bold, FontWeight.Bold)
    )
    val jetBrainsMono = FontFamily(
        Font(Res.font.jetbrains_mono_medium, FontWeight.Medium)
    )

    val baseline = MaterialTypography()

    return MaterialTypography(
        // Display & Headline
        displayLarge = baseline.displayLarge.copy(fontFamily = spaceGrotesk),
        displayMedium = baseline.displayMedium.copy(fontFamily = spaceGrotesk),
        displaySmall = baseline.displaySmall.copy(fontFamily = spaceGrotesk),
        headlineLarge = baseline.headlineLarge.copy(fontFamily = spaceGrotesk),
        headlineMedium = baseline.headlineMedium.copy(fontFamily = spaceGrotesk),
        headlineSmall = baseline.headlineSmall.copy(fontFamily = spaceGrotesk),

        // Title
        titleLarge = baseline.titleLarge.copy(fontFamily = plusJakartaSans, fontWeight = FontWeight.Bold),
        titleMedium = baseline.titleMedium.copy(fontFamily = plusJakartaSans, fontWeight = FontWeight.Bold),
        titleSmall = baseline.titleSmall.copy(fontFamily = plusJakartaSans, fontWeight = FontWeight.Bold),

        // Body
        bodyLarge = baseline.bodyLarge.copy(fontFamily = plusJakartaSans, fontWeight = FontWeight.Normal),
        bodyMedium = baseline.bodyMedium.copy(fontFamily = plusJakartaSans, fontWeight = FontWeight.Medium),
        bodySmall = baseline.bodySmall.copy(fontFamily = plusJakartaSans, fontWeight = FontWeight.Normal),

        // Label
        labelLarge = baseline.labelLarge.copy(fontFamily = plusJakartaSans, fontWeight = FontWeight.Medium),
        labelMedium = baseline.labelMedium.copy(fontFamily = jetBrainsMono, fontWeight = FontWeight.Medium),
        labelSmall = baseline.labelSmall.copy(fontFamily = jetBrainsMono, fontWeight = FontWeight.Medium)
    )
}

@Composable
fun getCupertinoTypography(): CupertinoTypography {
    val spaceGrotesk = FontFamily(
        Font(Res.font.space_grotesk_bold, FontWeight.Bold)
    )
    val plusJakartaSans = FontFamily(
        Font(Res.font.plus_jakarta_sans_regular, FontWeight.Normal),
        Font(Res.font.plus_jakarta_sans_medium, FontWeight.Medium),
        Font(Res.font.plus_jakarta_sans_bold, FontWeight.Bold)
    )
    val jetBrainsMono = FontFamily(
        Font(Res.font.jetbrains_mono_medium, FontWeight.Medium)
    )

    val baseline = CupertinoTypography()

    return CupertinoTypography(
        // iOS Large Titles & Headlines
        largeTitle = baseline.largeTitle.copy(fontFamily = spaceGrotesk),
        title1 = baseline.title1.copy(fontFamily = spaceGrotesk),
        title2 = baseline.title2.copy(fontFamily = spaceGrotesk),
        title3 = baseline.title3.copy(fontFamily = plusJakartaSans, fontWeight = FontWeight.Bold),

        // iOS Body & UI text
        headline = baseline.headline.copy(fontFamily = plusJakartaSans, fontWeight = FontWeight.Bold),
        body = baseline.body.copy(fontFamily = plusJakartaSans, fontWeight = FontWeight.Normal),
        callout = baseline.callout.copy(fontFamily = plusJakartaSans, fontWeight = FontWeight.Medium),
        subhead = baseline.subhead.copy(fontFamily = plusJakartaSans, fontWeight = FontWeight.Medium),

        // iOS Small accents
        footnote = baseline.footnote.copy(fontFamily = jetBrainsMono, fontWeight = FontWeight.Medium),
        caption1 = baseline.caption1.copy(fontFamily = jetBrainsMono, fontWeight = FontWeight.Medium),
        caption2 = baseline.caption2.copy(fontFamily = jetBrainsMono, fontWeight = FontWeight.Medium)
    )
}