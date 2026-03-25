package com.eeseka.lynk.shared.design_system.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.jetbrains_mono_medium
import lynk.shared.generated.resources.plus_jakarta_sans_bold
import lynk.shared.generated.resources.plus_jakarta_sans_medium
import lynk.shared.generated.resources.plus_jakarta_sans_regular
import lynk.shared.generated.resources.space_grotesk_bold
import lynk.shared.generated.resources.space_grotesk_medium
import lynk.shared.generated.resources.space_grotesk_regular
import org.jetbrains.compose.resources.Font

@Composable
fun getAppTypography(): Typography {
    val spaceGrotesk = FontFamily(
        Font(Res.font.space_grotesk_regular, FontWeight.Normal),
        Font(Res.font.space_grotesk_medium, FontWeight.Medium),
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

    val baseline = Typography()

    return Typography(
        displayLarge = baseline.displayLarge.copy(
            fontFamily = spaceGrotesk,
            fontWeight = FontWeight.Normal
        ),
        displayMedium = baseline.displayMedium.copy(
            fontFamily = spaceGrotesk,
            fontWeight = FontWeight.Normal
        ),
        displaySmall = baseline.displaySmall.copy(
            fontFamily = spaceGrotesk,
            fontWeight = FontWeight.Normal
        ),
        headlineLarge = baseline.headlineLarge.copy(
            fontFamily = spaceGrotesk,
            fontWeight = FontWeight.Normal
        ),
        headlineMedium = baseline.headlineMedium.copy(
            fontFamily = spaceGrotesk,
            fontWeight = FontWeight.Normal
        ),
        headlineSmall = baseline.headlineSmall.copy(
            fontFamily = spaceGrotesk,
            fontWeight = FontWeight.Normal
        ),
        titleLarge = baseline.titleLarge.copy(
            fontFamily = plusJakartaSans,
            fontWeight = FontWeight.Bold
        ),
        titleMedium = baseline.titleMedium.copy(
            fontFamily = plusJakartaSans,
            fontWeight = FontWeight.Bold
        ),
        titleSmall = baseline.titleSmall.copy(
            fontFamily = plusJakartaSans,
            fontWeight = FontWeight.Bold
        ),
        bodyLarge = baseline.bodyLarge.copy(
            fontFamily = plusJakartaSans,
            fontWeight = FontWeight.Normal
        ),
        bodyMedium = baseline.bodyMedium.copy(
            fontFamily = plusJakartaSans,
            fontWeight = FontWeight.Normal
        ),
        bodySmall = baseline.bodySmall.copy(
            fontFamily = plusJakartaSans,
            fontWeight = FontWeight.Normal
        ),
        labelLarge = baseline.labelLarge.copy(
            fontFamily = plusJakartaSans,
            fontWeight = FontWeight.Medium
        ),
        labelMedium = baseline.labelMedium.copy(
            fontFamily = jetBrainsMono,
            fontWeight = FontWeight.Medium
        ),
        labelSmall = baseline.labelSmall.copy(
            fontFamily = jetBrainsMono,
            fontWeight = FontWeight.Medium
        )
    )
}