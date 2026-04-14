package com.eeseka.lynk.shared.presentation.media

import androidx.compose.runtime.Composable
import com.eeseka.lynk.shared.domain.media.model.PickedImage

interface MediaPicker {
    suspend fun pickImage(): PickedImage?
    suspend fun captureImage(): PickedImage?
}

@Composable
expect fun rememberMediaPicker(): MediaPicker