package com.eeseka.lynk.profile_setup.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.composables.icons.lucide.Camera
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.User
import com.composables.icons.lucide.X
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import lynk.feature.profile_setup.generated.resources.Res
import lynk.feature.profile_setup.generated.resources.choose
import lynk.feature.profile_setup.generated.resources.profile_picture
import lynk.feature.profile_setup.generated.resources.remove
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileAvatarSection(
    currentImagePayload: Any?,
    imageError: String?,
    isCompressingImage: Boolean,
    isUploadingImage: Boolean,
    onImageClick: () -> Unit,
    onRemoveImage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isBusy = isCompressingImage || isUploadingImage
    val enabled = !isBusy

    val haptic = rememberAppHaptic()

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            // Main Avatar Circle
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(
                        enabled = enabled,
                        onClick = {
                            haptic(AppHaptic.Selection)
                            onImageClick()
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (currentImagePayload != null) {
                    AsyncImage(
                        model = currentImagePayload,
                        contentDescription = stringResource(Res.string.profile_picture),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Lucide.User,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Spinner Overlay if compressing or uploading
                if (isBusy) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        LynkProgressIndicator()
                    }
                }
            }

            if (currentImagePayload != null && enabled) {
                // The "X" Remove Button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        .clickable(
                            onClick = {
                                haptic(AppHaptic.Selection)
                                onRemoveImage()
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Lucide.X,
                        contentDescription = stringResource(Res.string.remove),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else if (currentImagePayload == null && enabled) {
                // A subtle camera indicator so they know the circle is interactive
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(
                            onClick = {
                                haptic(AppHaptic.Selection)
                                onImageClick()
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Lucide.Camera,
                        contentDescription = stringResource(Res.string.choose),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Show Image Errors inline below the avatar
        if (imageError != null) {
            Spacer(modifier = Modifier.height(12.dp))
            LynkText(
                text = imageError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ProfileAvatarSectionPreview() {
    LynkTheme {
        ProfileAvatarSection(
            currentImagePayload = null,
            imageError = null,
            isCompressingImage = false,
            isUploadingImage = false,
            onImageClick = {},
            onRemoveImage = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)
        )
    }
}

@PreviewLightDark
@Composable
private fun ProfileAvatarSectionPreviewLoading() {
    LynkTheme {
        ProfileAvatarSection(
            currentImagePayload = null,
            imageError = null,
            isCompressingImage = true,
            isUploadingImage = true,
            onImageClick = {},
            onRemoveImage = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)
        )
    }
}

@PreviewLightDark
@Composable
private fun ProfileAvatarSectionPreviewError() {
    LynkTheme {
        ProfileAvatarSection(
            currentImagePayload = null,
            imageError = "Invalid file type",
            isCompressingImage = false,
            isUploadingImage = false,
            onImageClick = {},
            onRemoveImage = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)
        )
    }
}