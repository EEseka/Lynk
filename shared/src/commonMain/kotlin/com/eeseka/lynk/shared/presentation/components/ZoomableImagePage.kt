package com.eeseka.lynk.shared.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ImageOff
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.components.util.shimmerEffect
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
        var isLoading by remember(model) { mutableStateOf(true) }
        var hasFailed by remember(model) { mutableStateOf(false) }

        Box(modifier = modifier.fillMaxSize()) {
            CoilZoomAsyncImage(
                model = model,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                zoomState = zoomState,
                scrollBar = null,
                onLoading = {
                    isLoading = true
                    hasFailed = false
                },
                onSuccess = {
                    isLoading = false
                    hasFailed = false
                },
                onError = {
                    isLoading = false
                    hasFailed = true
                },
                modifier = Modifier.fillMaxSize()
            )

            val placeholderModifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .aspectRatio(4f / 3f)

            if (isLoading) {
                Box(modifier = placeholderModifier.shimmerEffect())
            }

            if (hasFailed) {
                Box(
                    modifier = placeholderModifier
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
    }
}