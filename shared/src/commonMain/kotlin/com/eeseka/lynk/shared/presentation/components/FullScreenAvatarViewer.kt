package com.eeseka.lynk.shared.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.eeseka.lynk.shared.design_system.components.buttons.LynkTonalIconButton
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFullScreenDialog
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.close_image
import org.jetbrains.compose.resources.stringResource

@Composable
fun FullScreenAvatarViewer(
    model: String,
    onDismiss: () -> Unit
) {
    LynkFullScreenDialog(onDismissRequest = onDismiss) {
        // Photos read best on black, so the viewer is dark whatever the app theme
        LynkTheme(darkTheme = true) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                ZoomableImagePage(model = model)

                LynkTonalIconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Lucide.X,
                        contentDescription = stringResource(Res.string.close_image)
                    )
                }
            }
        }
    }
}
