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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.onboarding.presentation.model.OnboardingPageUi
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import lynk.feature.onboarding.generated.resources.Res

@Composable
fun OnboardingPageContent(
    page: OnboardingPageUi,
    isLandscape: Boolean,
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/${page.animationFileName}").decodeToString()
        )
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val imageSize = if (isLandscape) 220.dp else 320.dp

        Image(
            painter = rememberLottiePainter(
                composition = composition,
                iterations = Compottie.IterateForever
            ),
            contentDescription = null,
            modifier = Modifier.size(imageSize)
        )

        Spacer(modifier = Modifier.height(if (isLandscape) 24.dp else 40.dp))

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


@Preview
@Composable
private fun OnboardingPageContentPreview() {
    LynkTheme {
        OnboardingPageContent(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            page = OnboardingPageUi(
                title = "Stop wasting time",
                description = "Stop wasting time on manual stuff. Let Lynk handle your stuff automatically.",
                animationFileName = ""
            ),
            isLandscape = false
        )
    }
}