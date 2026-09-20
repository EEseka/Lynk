package com.eeseka.lynk.shared.design_system.components.images

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.composables.icons.lucide.ImageOff
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.shared.design_system.components.util.shimmerEffect
import com.eeseka.lynk.shared.design_system.theme.LynkTheme

@Composable
fun LynkAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale
) {
    var isLoading by remember(model) { mutableStateOf(true) }
    var hasFailed by remember(model) { mutableStateOf(false) }

    Box(modifier = modifier) {
        if (isLoading) {
            Box(modifier = Modifier.matchParentSize().shimmerEffect())
        }

        if (hasFailed) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Lucide.ImageOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            contentScale = contentScale,
            onState = { state ->
                isLoading = state is AsyncImagePainter.State.Loading
                hasFailed = state is AsyncImagePainter.State.Error
            },
            modifier = Modifier.matchParentSize()
        )
    }
}


@PreviewLightDark
@Composable
private fun LynkAsyncImagePhotoLoadingPreview() {
    LynkTheme {
        Box(
            modifier = Modifier
                .size(width = 280.dp, height = 200.dp)
                .clip(MaterialTheme.shapes.medium)
                .shimmerEffect()
        )
    }
}

@PreviewLightDark
@Composable
private fun LynkAsyncImagePhotoFailedPreview() {
    LynkTheme {
        Box(
            modifier = Modifier
                .size(width = 280.dp, height = 200.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Lucide.ImageOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun LynkAsyncImageAvatarLoadingPreview() {
    LynkTheme {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .shimmerEffect()
        )
    }
}
