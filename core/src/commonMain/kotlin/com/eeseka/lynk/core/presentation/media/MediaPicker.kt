package com.eeseka.lynk.core.presentation.media

import androidx.compose.runtime.Composable
import com.eeseka.lynk.core.domain.media.model.PickedImage

interface MediaPicker {
    suspend fun pickImage(): PickedImage?
    suspend fun pickImages(maxCount: Int): List<PickedImage>
    suspend fun captureImage(): PickedImage?
}

@Composable
expect fun rememberMediaPicker(): MediaPicker