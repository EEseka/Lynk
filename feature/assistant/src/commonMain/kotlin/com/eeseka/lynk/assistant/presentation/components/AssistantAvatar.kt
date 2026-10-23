package com.eeseka.lynk.assistant.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Sparkles
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.design_system.theme.extended

@Composable
fun AssistantAvatar(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.extended.violetContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Lucide.Sparkles,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.extended.onVioletContainer,
            modifier = Modifier.size(36.dp)
        )
    }
}

@PreviewLightDark
@Composable
private fun AssistantAvatarPreview() {
    LynkTheme {
        AssistantAvatar(modifier = Modifier.background(MaterialTheme.colorScheme.background))
    }
}
