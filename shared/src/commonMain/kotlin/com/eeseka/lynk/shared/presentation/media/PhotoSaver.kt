package com.eeseka.lynk.shared.presentation.media

import androidx.compose.runtime.Composable

interface PhotoSaver {
    suspend fun saveToGallery(imageBytes: ByteArray, caption: String?): Boolean
}

@Composable
expect fun rememberPhotoSaver(): PhotoSaver
