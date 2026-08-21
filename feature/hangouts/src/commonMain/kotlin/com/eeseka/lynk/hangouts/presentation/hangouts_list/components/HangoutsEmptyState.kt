package com.eeseka.lynk.hangouts.presentation.hangouts_list.components

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
import com.eeseka.lynk.hangouts.presentation.model.HangoutStatusFilter
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.empty_hangouts_cancelled_message
import lynk.feature.hangouts.generated.resources.empty_hangouts_cancelled_title
import lynk.feature.hangouts.generated.resources.empty_hangouts_completed_message
import lynk.feature.hangouts.generated.resources.empty_hangouts_completed_title
import lynk.feature.hangouts.generated.resources.empty_hangouts_ongoing_message
import lynk.feature.hangouts.generated.resources.empty_hangouts_ongoing_title
import lynk.feature.hangouts.generated.resources.empty_hangouts_upcoming_message
import lynk.feature.hangouts.generated.resources.empty_hangouts_upcoming_title
import org.jetbrains.compose.resources.stringResource

private const val ANIMATION_BORED_MAN = "bored_man.json"

@Composable
fun HangoutsEmptyState(
    currentFilter: HangoutStatusFilter,
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/$ANIMATION_BORED_MAN").decodeToString()
        )
    }

    val titleRes = when (currentFilter) {
        HangoutStatusFilter.UPCOMING -> Res.string.empty_hangouts_upcoming_title
        HangoutStatusFilter.ONGOING -> Res.string.empty_hangouts_ongoing_title
        HangoutStatusFilter.COMPLETED -> Res.string.empty_hangouts_completed_title
        HangoutStatusFilter.CANCELLED -> Res.string.empty_hangouts_cancelled_title
    }

    val messageRes = when (currentFilter) {
        HangoutStatusFilter.UPCOMING -> Res.string.empty_hangouts_upcoming_message
        HangoutStatusFilter.ONGOING -> Res.string.empty_hangouts_ongoing_message
        HangoutStatusFilter.COMPLETED -> Res.string.empty_hangouts_completed_message
        HangoutStatusFilter.CANCELLED -> Res.string.empty_hangouts_cancelled_message
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
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            LynkText(
                text = stringResource(messageRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
private fun HangoutsEmptyStatePreview(currentFilter: HangoutStatusFilter) {
    LynkTheme {
        HangoutsEmptyState(
            currentFilter = currentFilter,
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        )
    }
}

@PreviewLightDark
@Composable
private fun HangoutsEmptyStateUpcomingPreview() =
    HangoutsEmptyStatePreview(HangoutStatusFilter.UPCOMING)

@PreviewLightDark
@Composable
private fun HangoutsEmptyStateOngoingPreview() =
    HangoutsEmptyStatePreview(HangoutStatusFilter.ONGOING)

@PreviewLightDark
@Composable
private fun HangoutsEmptyStateCompletedPreview() =
    HangoutsEmptyStatePreview(HangoutStatusFilter.COMPLETED)

@PreviewLightDark
@Composable
private fun HangoutsEmptyStateCancelledPreview() =
    HangoutsEmptyStatePreview(HangoutStatusFilter.CANCELLED)
