package com.eeseka.lynk.hangouts.presentation.hangout_album.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.hangouts.presentation.util.rememberHangoutPhotoRequest
import com.eeseka.lynk.shared.design_system.components.images.LynkAsyncImage
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.album_select
import lynk.feature.hangouts.generated.resources.memories_photo
import org.jetbrains.compose.resources.stringResource

@Composable
fun HangoutPhotoThumbnail(
    photoId: String,
    photoThumbnailUrl: String,
    photoCaption: String?,
    onClick: () -> Unit,
    onLoadFailed: () -> Unit,
    modifier: Modifier = Modifier,
    isSelecting: Boolean = false,
    isSelected: Boolean = false,
    isClickable: Boolean = true,
    isDimmed: Boolean = false,
    onLongClick: (() -> Unit)? = null
) {
    val hapticFeedback = rememberAppHaptic()
    val imageRequest = rememberHangoutPhotoRequest(
        url = photoThumbnailUrl,
        cacheKey = "photo:${photoId}:thumb"
    )
    val selectLabel = stringResource(Res.string.album_select)
    val alpha by animateFloatAsState(if (isDimmed) 0.4f else 1f)

    Box(
        modifier = modifier
            .graphicsLayer { this.alpha = alpha }
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .semantics {
                if (isSelecting) selected = isSelected
                onLongClick?.let { longClick ->
                    onLongClick(label = selectLabel) {
                        longClick()
                        true
                    }
                }
            }
            .clickable(enabled = isClickable && !isDimmed) {
                hapticFeedback(if (isSelecting) AppHaptic.Selection else AppHaptic.ImpactLight)
                onClick()
            }
    ) {
        LynkAsyncImage(
            model = imageRequest,
            contentDescription = photoCaption ?: stringResource(Res.string.memories_photo),
            contentScale = ContentScale.Crop,
            onLoadFailed = onLoadFailed,
            modifier = Modifier.fillMaxSize()
        )

        if (isSelecting) {
            SelectionBadge(
                isSelected = isSelected,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            )
        }
    }
}

@Composable
private fun SelectionBadge(
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.2f))
            .border(width = 2.dp, color = Color.White, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Lucide.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun SelectionBadgePreview() {
    LynkTheme {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            SelectionBadge(isSelected = false)
            SelectionBadge(isSelected = true, modifier = Modifier.padding(start = 16.dp))
        }
    }
}
