package com.eeseka.lynk.profile.presentation.saved_spots.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import lynk.feature.profile.generated.resources.Res
import lynk.feature.profile.generated.resources.saved_spots_empty_message
import lynk.feature.profile.generated.resources.saved_spots_empty_title
import org.jetbrains.compose.resources.stringResource

private const val ANIMATION_EMPTY_BOOKMARKS = "empty_bookmarks.json"

@Composable
fun SavedSpotsEmptyState(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/$ANIMATION_EMPTY_BOOKMARKS").decodeToString()
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = rememberLottiePainter(
                    composition = composition,
                    iterations = Compottie.IterateForever
                ),
                contentDescription = null,
                modifier = Modifier.size(300.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LynkText(
                text = stringResource(Res.string.saved_spots_empty_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            LynkText(
                text = stringResource(Res.string.saved_spots_empty_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun SavedSpotsEmptyStatePreview() {
    LynkTheme {
        SavedSpotsEmptyState(modifier = Modifier.background(MaterialTheme.colorScheme.background))
    }
}