package com.eeseka.lynk.discover.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.eeseka.lynk.discover.presentation.util.rememberGoogleImageRequest
import com.eeseka.lynk.shared.design_system.components.buttons.LynkTonalIconButton
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import com.github.panpf.zoomimage.rememberCoilZoomState
import lynk.feature.discover.generated.resources.Res
import lynk.feature.discover.generated.resources.close_image
import org.jetbrains.compose.resources.stringResource

@Composable
fun FullScreenImageViewer(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    val imageRequest = rememberGoogleImageRequest(url = imageUrl)
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (imageRequest != null) {
                CoilZoomAsyncImage(
                    model = imageRequest,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    zoomState = zoomState,
                    scrollBar = null,
                    modifier = Modifier.fillMaxSize()
                )
            }

            LynkTonalIconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            ) {
                Icon(
                    imageVector = Lucide.X,
                    contentDescription = stringResource(Res.string.close_image)
                )
            }
        }
    }
}