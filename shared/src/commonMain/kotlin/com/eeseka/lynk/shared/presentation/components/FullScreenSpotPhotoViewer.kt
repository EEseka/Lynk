package com.eeseka.lynk.shared.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.eeseka.lynk.shared.design_system.components.buttons.LynkTonalIconButton
import com.eeseka.lynk.shared.presentation.spot.util.SpotPhotoUrlBuilder
import com.eeseka.lynk.shared.presentation.spot.util.rememberGoogleImageRequest
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.close_image
import org.jetbrains.compose.resources.stringResource

/**
 * Swipeable full-screen viewer for Google Places photos.
 *
 * [rawPhotoNames] are Places resource names ("places/ChIJ.../photos/Aaw..."), not URLs — each one
 * goes through [SpotPhotoUrlBuilder] and is fetched with the Places API headers. Anything that is
 * already a real URL belongs in [FullScreenAvatarViewer] instead.
 */
@Composable
fun FullScreenSpotPhotoViewer(
    rawPhotoNames: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { rawPhotoNames.size }
    )

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
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                key = { page -> rawPhotoNames[page] }
            ) { page ->
                val fullUrl = remember(rawPhotoNames[page]) {
                    SpotPhotoUrlBuilder.build(rawPhotoNames[page])
                }
                val imageRequest = rememberGoogleImageRequest(url = fullUrl ?: "")

                ZoomableImagePage(model = imageRequest)
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