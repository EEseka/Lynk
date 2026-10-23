package com.eeseka.lynk.assistant.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Hourglass
import com.composables.icons.lucide.Lock
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCard
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCardStyle
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.design_system.theme.extended

@Composable
fun AssistantClosedNotice(
    title: String?,
    message: String,
    isOutOfMessages: Boolean,
    modifier: Modifier = Modifier
) {
    LynkCard(style = LynkCardStyle.OUTLINED, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isOutOfMessages) Lucide.Hourglass else Lucide.Lock,
                contentDescription = null,
                tint = if (isOutOfMessages) {
                    MaterialTheme.colorScheme.extended.violet
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(24.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                title?.let {
                    LynkText(
                        text = it,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                LynkText(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun AssistantClosedNoticePreview() {
    LynkTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            AssistantClosedNotice(
                title = "You've used today's messages",
                message = "Lynk can take more questions at 3:40 PM.",
                isOutOfMessages = true
            )
            AssistantClosedNotice(
                title = null,
                message = "This chat only takes messages while voting is open.",
                isOutOfMessages = false
            )
        }
    }
}
