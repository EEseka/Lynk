package com.eeseka.lynk.core.presentation.media

import androidx.compose.runtime.Composable

interface PhotoSharer {
    suspend fun share(photos: List<CaptionedPhoto>): Boolean
}

@Composable
expect fun rememberPhotoSharer(): PhotoSharer
