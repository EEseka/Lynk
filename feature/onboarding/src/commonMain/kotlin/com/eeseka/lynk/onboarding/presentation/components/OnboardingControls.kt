package com.eeseka.lynk.onboarding.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import lynk.feature.onboarding.generated.resources.Res
import lynk.feature.onboarding.generated.resources.get_started
import lynk.feature.onboarding.generated.resources.next
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingControls(
    currentPage: Int,
    pageSize: Int,
    onOnboardingButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OnboardingPageIndicator(
            pageSize = pageSize,
            currentPage = currentPage,
        )

        Spacer(modifier = Modifier.height(32.dp))

        LynkButton(
            onClick = onOnboardingButtonClick,
            text = if (currentPage == pageSize - 1) stringResource(Res.string.get_started)
            else stringResource(Res.string.next),
            modifier = Modifier.height(56.dp)
        )
    }
}

@Preview
@Composable
private fun OnboardingControlsPreview() {
    LynkTheme {
        OnboardingControls(
            currentPage = 2,
            pageSize = 3,
            onOnboardingButtonClick = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        )
    }
}