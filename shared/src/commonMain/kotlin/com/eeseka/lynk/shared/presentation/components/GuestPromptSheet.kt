package com.eeseka.lynk.shared.presentation.components

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
import com.composables.icons.lucide.LogIn
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButtonStyle
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkAdaptiveSheet
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.guest_prompt_body
import lynk.shared.generated.resources.guest_prompt_not_now
import lynk.shared.generated.resources.guest_prompt_sign_in
import lynk.shared.generated.resources.guest_prompt_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun GuestPromptSheet(
    actionStr: String,
    onSignInClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    LynkAdaptiveSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        GuestPromptSheetContent(
            actionStr = actionStr,
            onSignInClick = onSignInClick,
            onDismissRequest = onDismissRequest
        )
    }
}

@Composable
private fun GuestPromptSheetContent(
    actionStr: String,
    onSignInClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Lucide.LogIn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        LynkText(
            text = stringResource(Res.string.guest_prompt_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        LynkText(
            text = stringResource(Res.string.guest_prompt_body, actionStr),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        LynkButton(
            text = stringResource(Res.string.guest_prompt_sign_in),
            onClick = onSignInClick
        )

        Spacer(modifier = Modifier.height(8.dp))

        LynkButton(
            text = stringResource(Res.string.guest_prompt_not_now),
            onClick = onDismissRequest,
            style = LynkButtonStyle.SECONDARY
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@PreviewLightDark
@Composable
private fun GuestPromptSheetPreview() {
    LynkTheme {
        GuestPromptSheetContent(
            actionStr = "save this spot",
            onSignInClick = {},
            onDismissRequest = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        )
    }
}