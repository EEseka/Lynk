package com.eeseka.lynk.profile.presentation.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.UserRound
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButtonStyle
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import lynk.feature.profile.generated.resources.Res
import lynk.feature.profile.generated.resources.create_account
import lynk.feature.profile.generated.resources.guest_message
import lynk.feature.profile.generated.resources.guest_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun GuestProfileSection(
    isDeletingAccount: Boolean,
    onCreateAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = rememberAppHaptic()

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Lucide.UserRound,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        LynkText(
            text = stringResource(Res.string.guest_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        LynkText(
            text = stringResource(Res.string.guest_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        LynkButton(
            text = stringResource(Res.string.create_account),
            onClick = {
                hapticFeedback(AppHaptic.ImpactMedium)
                onCreateAccountClick()
            },
            style = LynkButtonStyle.PRIMARY,
            isLoading = isDeletingAccount,
            loadingText = "",
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )
    }
}

@PreviewLightDark
@Composable
private fun GuestProfileSectionPreview() {
    LynkTheme {
        GuestProfileSection(
            isDeletingAccount = false,
            onCreateAccountClick = {},
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp)
        )
    }
}

@PreviewLightDark
@Composable
private fun GuestProfileSectionLoadingPreview() {
    LynkTheme {
        GuestProfileSection(
            isDeletingAccount = true,
            onCreateAccountClick = {},
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp)
        )
    }
}