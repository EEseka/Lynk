package com.eeseka.lynk.onboarding.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.onboarding.presentation.model.OnboardingPageUi
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.presentation.util.DeviceConfiguration
import com.eeseka.lynk.shared.presentation.util.currentDeviceConfiguration
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import lynk.feature.onboarding.generated.resources.Res

@Composable
fun OnboardingPageContent(
    page: OnboardingPageUi,
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/${page.animationFileName}").decodeToString()
        )
    }

    val animationSize = when (currentDeviceConfiguration()) {
        DeviceConfiguration.MOBILE_LANDSCAPE -> 220.dp
        DeviceConfiguration.TABLET_PORTRAIT -> 400.dp
        else -> 320.dp
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = rememberLottiePainter(
                composition = composition,
                iterations = Compottie.IterateForever
            ),
            contentDescription = null,
            modifier = Modifier.size(animationSize)
        )

        Spacer(modifier = Modifier.height(24.dp))

        LynkText(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground

        )

        Spacer(modifier = Modifier.height(16.dp))

        LynkText(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@PreviewLightDark
@Composable
private fun OnboardingPageContentPreview() {
    LynkTheme {
        OnboardingPageContent(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            page = OnboardingPageUi(
                title = "Stop wasting time",
                description = "Stop wasting time on manual stuff. Let Lynk handle your stuff automatically.",
                animationFileName = ""
            )
        )
    }
}