package com.eeseka.lynk.hangouts.presentation.hangout_detail.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.eeseka.lynk.shared.design_system.components.images.LynkAsyncImage
import com.eeseka.lynk.shared.presentation.spot.util.rememberGoogleImageRequest

// How far the page scrolls before its text reaches the top bar, which then needs its own background
private val BAR_SOLID_AFTER_SCROLL = 240.dp

private const val PARALLAX_FACTOR = 0.4f
private val PHOTO_TOP_BLEND = 40.dp
private val PAGE_CORNER_RADIUS = 24.dp

@Composable
fun rememberIsDetailBarOverPhoto(hasHeroPhoto: Boolean, scrollState: ScrollState): Boolean {
    val solidAfterPx = with(LocalDensity.current) { BAR_SOLID_AFTER_SCROLL.toPx() }
    val isBeforeSolid by remember(scrollState, solidAfterPx) {
        derivedStateOf { scrollState.value < solidAfterPx }
    }
    return hasHeroPhoto && isBeforeSolid
}

internal expect fun canBlurHeroBackdrop(): Boolean

// The photo's own fade scrolls away with the page, so the status bar keeps a fixed one over the photo
@Composable
fun HangoutStatusBarProtection(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val pageColor = MaterialTheme.colorScheme.background
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val protectionAlpha by animateFloatAsState(if (isVisible) 1f else 0f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(statusBarHeight * 2)
            .graphicsLayer { alpha = protectionAlpha }
            .background(
                Brush.verticalGradient(
                    0f to pageColor.copy(alpha = 0.8f),
                    0.5f to pageColor.copy(alpha = 0.8f),
                    1f to Color.Transparent
                )
            )
    )
}

@Composable
fun HangoutHeroPhoto(
    photoUrl: String?,
    topInset: Dp,
    photoHeight: Dp,
    scrollOffset: () -> Int,
    modifier: Modifier = Modifier
) {
    val pageColor = MaterialTheme.colorScheme.background
    val height = topInset + photoHeight + PAGE_CORNER_RADIUS
    val barFadeEnd = (topInset + 24.dp) / height

    AnimatedContent(
        targetState = photoUrl,
        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) using null },
        label = "hangout_hero_photo",
        modifier = modifier.fillMaxWidth()
    ) { targetPhotoUrl ->
        val imageRequest = rememberGoogleImageRequest(url = targetPhotoUrl ?: "")
        var isPhotoLoaded by remember { mutableStateOf(false) }

        if (imageRequest != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clipToBounds()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { translationY = scrollOffset() * PARALLAX_FACTOR }
                ) {
                    if (isPhotoLoaded && canBlurHeroBackdrop()) {
                        AsyncImage(
                            model = imageRequest,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .blur(24.dp)
                        )
                    }

                    LynkAsyncImage(
                        model = imageRequest,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        onLoadSucceeded = { isPhotoLoaded = true },
                        modifier = Modifier
                            .padding(top = topInset)
                            .fillMaxWidth()
                            .height(photoHeight)
                            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                            .drawWithCache {
                                val topBlend = Brush.verticalGradient(
                                    0f to Color.Transparent,
                                    (PHOTO_TOP_BLEND.toPx() / size.height) to Color.Black
                                )
                                onDrawWithContent {
                                    drawContent()
                                    drawRect(brush = topBlend, blendMode = BlendMode.DstIn)
                                }
                            }
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0f to pageColor.copy(alpha = 0.8f),
                                barFadeEnd * 0.5f to pageColor.copy(alpha = 0.48f),
                                barFadeEnd * 0.8f to pageColor.copy(alpha = 0.16f),
                                barFadeEnd to Color.Transparent
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(PAGE_CORNER_RADIUS)
                        .background(
                            color = pageColor,
                            shape = RoundedCornerShape(
                                topStart = PAGE_CORNER_RADIUS,
                                topEnd = PAGE_CORNER_RADIUS
                            )
                        )
                )
            }
        }
    }
}
