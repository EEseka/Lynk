package com.eeseka.lynk.assistant.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCard
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCardStyle
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun AssistantEmptyState(
    title: String,
    message: String,
    starterPrompts: ImmutableList<String>,
    onStarterPromptClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AssistantAvatar()

        Spacer(modifier = Modifier.height(16.dp))

        LynkText(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        LynkText(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (starterPrompts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                starterPrompts.forEach { prompt ->
                    LynkCard(
                        style = LynkCardStyle.OUTLINED,
                        onClick = { onStarterPromptClick(prompt) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LynkText(
                            text = prompt,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun AssistantEmptyStatePreview() {
    LynkTheme {
        AssistantEmptyState(
            title = "Find Your Next Spot",
            message = "Tell Lynk AI what you're in the mood for, and it will find places around Lekki.",
            starterPrompts = persistentListOf(
                "Somewhere chill for drinks tonight",
                "A good spot for a birthday dinner",
                "Cheap eats with a nice vibe"
            ),
            onStarterPromptClick = {},
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(vertical = 16.dp)
        )
    }
}
