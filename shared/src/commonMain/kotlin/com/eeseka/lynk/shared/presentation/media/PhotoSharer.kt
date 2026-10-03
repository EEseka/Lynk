package com.eeseka.lynk.shared.presentation.media

import androidx.compose.runtime.Composable

interface PhotoSharer {
    suspend fun share(imageBytes: ByteArray, caption: String?): Boolean
}

@Composable
expect fun rememberPhotoSharer(): PhotoSharer
