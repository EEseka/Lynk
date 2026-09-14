package com.eeseka.lynk.shared.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.TriangleAlert
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButtonStyle
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.retry
import org.jetbrains.compose.resources.stringResource

@Composable
fun LynkErrorState(
    title: String,
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .widthIn(max = 320.dp)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Lucide.TriangleAlert,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(36.dp)
        )
        LynkText(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        LynkText(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))
        LynkButton(
            text = stringResource(Res.string.retry),
            onClick = onRetry,
            style = LynkButtonStyle.SECONDARY
        )
    }
}

@PreviewLightDark
@Composable
private fun LynkErrorStateDefaultPreview() = LynkErrorStatePreview(
    title = "Couldn't load this hangout",
    message = "Couldn't reach the server. Check your connection and try again."
)

@PreviewLightDark
@Composable
private fun LynkErrorStateShortPreview() = LynkErrorStatePreview(
    title = "Couldn't load notifications",
    message = "Something went wrong."
)

@Composable
private fun LynkErrorStatePreview(title: String, message: String) {
    LynkTheme {
        LynkErrorState(
            title = title,
            message = message,
            onRetry = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        )
    }
}
