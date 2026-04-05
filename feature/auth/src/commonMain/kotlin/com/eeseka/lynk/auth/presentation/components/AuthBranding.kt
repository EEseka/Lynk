package com.eeseka.lynk.auth.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import lynk.feature.auth.generated.resources.Res
import lynk.feature.auth.generated.resources.auth_tagline
import lynk.feature.auth.generated.resources.lynk
import org.jetbrains.compose.resources.stringResource

private const val ANIMATION_LYNK_LOGO = "lynk.json"

@Composable
fun AuthBranding(
    iconSize: Dp,
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/${ANIMATION_LYNK_LOGO}").decodeToString()
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = rememberLottiePainter(
                composition = composition,
                iterations = Compottie.IterateForever
            ),
            contentDescription = null,
            modifier = Modifier.size(iconSize)
        )

        LynkText(
            text = stringResource(Res.string.lynk),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )

        LynkText(
            text = stringResource(Res.string.auth_tagline),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}

@PreviewLightDark
@Composable
private fun AuthBrandingPreview() {
    LynkTheme {
        AuthBranding(
            iconSize = 250.dp,
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        )
    }
}