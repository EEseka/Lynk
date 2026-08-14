package com.eeseka.lynk.create_hangout.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme

@Composable
fun LynkStepIndicator(
    numberOfSteps: Int,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = currentStep.toFloat(),
                    range = 1f..numberOfSteps.toFloat()
                )
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        for (step in 1..numberOfSteps) {
            val isDone = step < currentStep
            val isCurrent = step == currentStep

            // Animate background color
            val dotBgColor by animateColorAsState(
                targetValue = when {
                    isDone -> MaterialTheme.colorScheme.primary
                    isCurrent -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                animationSpec = tween(durationMillis = 300),
                label = "dotBackgroundColor_$step"
            )

            // Animate text color
            val dotTextColor by animateColorAsState(
                targetValue = when {
                    isDone -> MaterialTheme.colorScheme.onPrimary
                    isCurrent -> MaterialTheme.colorScheme.onSecondary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                animationSpec = tween(durationMillis = 300),
                label = "dotTextColor_$step"
            )

            // Step Dot
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .drawBehind { drawCircle(color = dotBgColor) },
                contentAlignment = Alignment.Center
            ) {
                LynkText(
                    text = step.toString(),
                    color = dotTextColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Connecting Line
            if (step < numberOfSteps) {
                val isLineDone = step < currentStep

                val lineBgColor by animateColorAsState(
                    targetValue = if (isLineDone) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    animationSpec = tween(durationMillis = 300),
                    label = "lineBackgroundColor_$step"
                )

                Spacer(modifier = Modifier.weight(1f).height(2.dp).drawBehind { drawRect(color = lineBgColor) })
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun Step1IndicatorPreview() {
    LynkTheme {
        LynkStepIndicator(
            numberOfSteps = 3,
            currentStep = 1,
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        )
    }
}

@PreviewLightDark
@Composable
private fun Step2IndicatorPreview() {
    LynkTheme {
        LynkStepIndicator(
            numberOfSteps = 3,
            currentStep = 2,
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        )
    }
}

@PreviewLightDark
@Composable
private fun Step3IndicatorPreview() {
    LynkTheme {
        LynkStepIndicator(
            numberOfSteps = 3,
            currentStep = 3,
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        )
    }
}