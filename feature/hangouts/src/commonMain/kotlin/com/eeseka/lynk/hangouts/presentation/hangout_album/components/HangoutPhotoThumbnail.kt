package com.eeseka.lynk.hangouts.presentation.hangout_album.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import com.eeseka.lynk.hangouts.presentation.util.rememberHangoutPhotoRequest
import com.eeseka.lynk.shared.design_system.components.images.LynkAsyncImage
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.memories_photo
import org.jetbrains.compose.resources.stringResource

@Composable
fun HangoutPhotoThumbnail(
    photoId: String,
    photoThumbnailUrl: String,
    photoCaption: String?,
    onClick: () -> Unit,
    onLoadFailed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = rememberAppHaptic()
    val imageRequest = rememberHangoutPhotoRequest(
        url = photoThumbnailUrl,
        cacheKey = "photo:${photoId}:thumb"
    )

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable {
                hapticFeedback(AppHaptic.ImpactLight)
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
    }
}
