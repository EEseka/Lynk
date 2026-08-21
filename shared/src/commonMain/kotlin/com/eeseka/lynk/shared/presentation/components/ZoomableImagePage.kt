package com.eeseka.lynk.shared.presentation.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import com.github.panpf.zoomimage.rememberCoilZoomState

@Composable
fun ZoomableImagePage(
    model: Any?,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = rememberAppHaptic()
    val zoomState = rememberCoilZoomState()

    var hasHitMax by remember { mutableStateOf(false) }
    var hasHitMin by remember { mutableStateOf(false) }

    LaunchedEffect(zoomState) {
        snapshotFlow { zoomState.zoomable.transform.scaleX }
            .collect { currentScale ->
                val maxScale = zoomState.zoomable.maxScale + 0.01f
                val minScale = zoomState.zoomable.minScale - 0.01f

                if (currentScale > maxScale) {
                    if (!hasHitMax) {
                        hapticFeedback(AppHaptic.ImpactLight)
                        hasHitMax = true
                    }
                } else {
                    hasHitMax = false
                }

                if (currentScale < minScale) {
                    if (!hasHitMin) {
                        hapticFeedback(AppHaptic.ImpactLight)
                        hasHitMin = true
                    }
                } else {
                    hasHitMin = false
                }
            }
    }

    if (model != null) {
        CoilZoomAsyncImage(
            model = model,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            zoomState = zoomState,
            scrollBar = null,
            modifier = modifier.fillMaxSize()
        )
    }
}