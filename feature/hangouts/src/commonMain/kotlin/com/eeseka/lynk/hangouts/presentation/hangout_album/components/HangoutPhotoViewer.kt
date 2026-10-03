package com.eeseka.lynk.hangouts.presentation.hangout_album.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Download
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Share2
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.X
import com.eeseka.lynk.hangouts.presentation.model.HangoutPhotoUi
import com.eeseka.lynk.hangouts.presentation.preview.previewHangoutPhotos
import com.eeseka.lynk.hangouts.presentation.util.rememberHangoutPhotoRequest
import com.eeseka.lynk.shared.design_system.components.buttons.LynkTonalIconButton
import com.eeseka.lynk.shared.design_system.components.layouts.LynkScaffold
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFullScreenDialog
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.presentation.components.ZoomableImagePage
import com.eeseka.lynk.shared.presentation.hangout.components.ParticipantAvatar
import com.eeseka.lynk.shared.presentation.preview.PREVIEW_GUEST_ID
import com.eeseka.lynk.shared.presentation.preview.PREVIEW_HOST_ID
import com.eeseka.lynk.shared.presentation.util.toDateTimeLabel
import kotlinx.collections.immutable.ImmutableList
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.album_close_viewer
import lynk.feature.hangouts.generated.resources.album_delete_photo
import lynk.feature.hangouts.generated.resources.album_edit_caption
import lynk.feature.hangouts.generated.resources.album_photo_position
import lynk.feature.hangouts.generated.resources.album_save_photo
import lynk.feature.hangouts.generated.resources.album_share_photo
import org.jetbrains.compose.resources.stringResource

@Composable
fun HangoutPhotoViewer(
    photos: ImmutableList<HangoutPhotoUi>,
    initialIndex: Int,
    currentUserId: String?,
    hostId: String?,
    isExporting: Boolean,
    deletingPhotoId: String?,
    isEndReached: Boolean,
    snackbarHostState: SnackbarHostState,
    onPageChanged: (Int) -> Unit,
    onLoadNextPage: () -> Unit,
    onPhotoLoadFailed: () -> Unit,
    onSaveClick: (HangoutPhotoUi) -> Unit,
    onShareClick: (HangoutPhotoUi) -> Unit,
    onEditCaptionClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    LynkFullScreenDialog(onDismissRequest = onDismiss) {
        val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { photos.size })
        val lastIndex by rememberUpdatedState(photos.lastIndex)
        val isEndReached by rememberUpdatedState(isEndReached)

        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }.collect { page ->
                onPageChanged(page)
                // Asks for the next page while there are still a few photos to swipe through
                if (page >= lastIndex - 2 && !isEndReached) onLoadNextPage()
            }
        }

        // Photos read best on black, so the viewer is dark whatever the app theme
        LynkTheme(darkTheme = true) {
            // The dialog covers the screen's own scaffold, so this one only hosts the flashes.
            LynkScaffold(
                snackbarHostState = snackbarHostState,
                applyHorizontalInsets = false
            ) {
                HangoutPhotoViewerContent(
                    photos = photos,
                    pagerState = pagerState,
                    currentUserId = currentUserId,
                    hostId = hostId,
                    isExporting = isExporting,
                    deletingPhotoId = deletingPhotoId,
                    onPhotoLoadFailed = onPhotoLoadFailed,
                    onSaveClick = onSaveClick,
                    onShareClick = onShareClick,
                    onEditCaptionClick = onEditCaptionClick,
                    onDeleteClick = onDeleteClick,
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
private fun HangoutPhotoViewerContent(
    photos: ImmutableList<HangoutPhotoUi>,
    pagerState: PagerState,
    currentUserId: String?,
    hostId: String?,
    isExporting: Boolean,
    deletingPhotoId: String?,
    onPhotoLoadFailed: () -> Unit,
    onSaveClick: (HangoutPhotoUi) -> Unit,
    onShareClick: (HangoutPhotoUi) -> Unit,
    onEditCaptionClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val hapticFeedback = rememberAppHaptic()
    val photo = photos.getOrNull(pagerState.currentPage)
    var areBarsVisible by remember { mutableStateOf(true) }
    val scrimColor = MaterialTheme.colorScheme.background.copy(alpha = 0.72f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { page -> photos[page].id }
        ) { page ->
            val pagePhoto = photos[page]
            val imageRequest = rememberHangoutPhotoRequest(
                url = pagePhoto.fullUrl,
                cacheKey = "photo:${pagePhoto.id}:full"
            )

            ZoomableImagePage(
                model = imageRequest,
                onLoadFailed = onPhotoLoadFailed,
                onTap = { areBarsVisible = !areBarsVisible }
            )
        }

        AnimatedVisibility(
            visible = areBarsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(0.6f to scrimColor, 1f to Color.Transparent))
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (photo != null) {
                    ParticipantAvatar(
                        displayName = photo.uploader.displayName,
                        initials = photo.uploader.initials,
                        profilePictureUrl = photo.uploader.profilePictureUrl,
                        size = 32.dp
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        LynkText(
                            text = photo.uploader.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        LynkText(
                            text = photo.createdAt.toDateTimeLabel(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                LynkTonalIconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Lucide.X,
                        contentDescription = stringResource(Res.string.album_close_viewer)
                    )
                }
            }
        }

        if (photo != null) {
            val isUploader = photo.uploader.userId == currentUserId
            val canDelete = isUploader || hostId == currentUserId

            AnimatedVisibility(
                visible = areBarsVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(0f to Color.Transparent, 0.4f to scrimColor))
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
                        .padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    photo.caption?.let { caption ->
                        key(photo.id) {
                            LynkText(
                                text = caption,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier
                                    .heightIn(max = 96.dp)
                                    .verticalScroll(rememberScrollState())
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LynkTonalIconButton(
                            enabled = !isExporting,
                            onClick = {
                                hapticFeedback(AppHaptic.ImpactLight)
                                onSaveClick(photo)
                            }
                        ) {
                            Icon(
                                imageVector = Lucide.Download,
                                contentDescription = stringResource(Res.string.album_save_photo)
                            )
                        }
                        LynkTonalIconButton(
                            enabled = !isExporting,
                            onClick = {
                                hapticFeedback(AppHaptic.ImpactLight)
                                onShareClick(photo)
                            }
                        ) {
                            Icon(
                                imageVector = Lucide.Share2,
                                contentDescription = stringResource(Res.string.album_share_photo)
                            )
                        }
                        if (isUploader) {
                            LynkTonalIconButton(
                                onClick = {
                                    hapticFeedback(AppHaptic.ImpactLight)
                                    onEditCaptionClick(photo.id)
                                }
                            ) {
                                Icon(
                                    imageVector = Lucide.Pencil,
                                    contentDescription = stringResource(Res.string.album_edit_caption)
                                )
                            }
                        }
                        if (canDelete) {
                            LynkTonalIconButton(
                                enabled = deletingPhotoId == null,
                                onClick = {
                                    hapticFeedback(AppHaptic.ImpactLight)
                                    onDeleteClick(photo.id)
                                },
                                contentColor = MaterialTheme.colorScheme.error
                            ) {
                                if (photo.id == deletingPhotoId) {
                                    LynkProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.error,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Lucide.Trash2,
                                        contentDescription = stringResource(Res.string.album_delete_photo)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        LynkText(
                            text = stringResource(Res.string.album_photo_position, pagerState.currentPage + 1, photos.size),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun HangoutPhotoViewerOwnPhotoPreview() = HangoutPhotoViewerPreview(currentUserId = PREVIEW_HOST_ID)

@Preview
@Composable
private fun HangoutPhotoViewerOthersPhotoPreview() = HangoutPhotoViewerPreview(currentUserId = PREVIEW_GUEST_ID)

// The viewer is always dark, so there is no light preview to show
@Composable
private fun HangoutPhotoViewerPreview(currentUserId: String) {
    LynkTheme(darkTheme = true) {
        HangoutPhotoViewerContent(
            photos = previewHangoutPhotos,
            pagerState = rememberPagerState(pageCount = { previewHangoutPhotos.size }),
            currentUserId = currentUserId,
            hostId = PREVIEW_HOST_ID,
            isExporting = false,
            deletingPhotoId = null,
            onPhotoLoadFailed = {},
            onSaveClick = {},
            onShareClick = {},
            onEditCaptionClick = {},
            onDeleteClick = {},
            onDismiss = {}
        )
    }
}
