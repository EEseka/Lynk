package com.eeseka.lynk.profile_setup.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import lynk.feature.profile_setup.generated.resources.Res
import lynk.feature.profile_setup.generated.resources.setup_profile_subtitle
import lynk.feature.profile_setup.generated.resources.setup_profile_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileSetupHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LynkText(
            text = stringResource(Res.string.setup_profile_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        LynkText(
            text = stringResource(Res.string.setup_profile_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@PreviewLightDark
@Composable
private fun ProfileSetupHeaderPreview() {
    LynkTheme {
        ProfileSetupHeader(modifier = Modifier.background(MaterialTheme.colorScheme.background))
    }
}