package com.eeseka.lynk.core.presentation.media

import androidx.compose.runtime.Composable

interface PhotoSaver {
    suspend fun saveToGallery(photo: CaptionedPhoto): Boolean
}

@Composable
expect fun rememberPhotoSaver(): PhotoSaver
