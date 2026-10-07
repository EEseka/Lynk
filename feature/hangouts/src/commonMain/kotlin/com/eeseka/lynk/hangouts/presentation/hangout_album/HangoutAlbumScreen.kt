package com.eeseka.lynk.hangouts.presentation.hangout_album

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.LocalPlatformContext
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.hangouts.presentation.hangout_album.components.EditCaptionSheet
import com.eeseka.lynk.hangouts.presentation.hangout_album.components.HangoutAlbumEmptyState
import com.eeseka.lynk.hangouts.presentation.hangout_album.components.HangoutPhotoThumbnail
import com.eeseka.lynk.hangouts.presentation.hangout_album.components.HangoutPhotoViewer
import com.eeseka.lynk.hangouts.presentation.preview.previewHangoutPhotos
import com.eeseka.lynk.hangouts.presentation.util.loadHangoutPhotoBytes
import com.eeseka.lynk.shared.design_system.components.buttons.LynkIconButton
import com.eeseka.lynk.shared.design_system.components.layouts.LynkScaffold
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDialog
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFlashType
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.showFlashMessage
import com.eeseka.lynk.shared.design_system.components.navigation.LynkIosBarButtonItem
import com.eeseka.lynk.shared.design_system.components.navigation.LynkTopAppBar
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.presentation.components.LynkErrorState
import com.eeseka.lynk.shared.presentation.media.rememberPhotoSaver
import com.eeseka.lynk.shared.presentation.media.rememberPhotoSharer
import com.eeseka.lynk.shared.presentation.preview.PREVIEW_HOST_ID
import com.eeseka.lynk.shared.presentation.util.ObserveAsEvents
import com.eeseka.lynk.shared.presentation.util.PaginationScrollListener
import com.eeseka.lynk.shared.presentation.util.currentDeviceConfiguration
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.album_back
import lynk.feature.hangouts.generated.resources.album_caption_updated
import lynk.feature.hangouts.generated.resources.album_delete_confirm_action
import lynk.feature.hangouts.generated.resources.album_delete_confirm_message
import lynk.feature.hangouts.generated.resources.album_delete_confirm_title
import lynk.feature.hangouts.generated.resources.album_load_error_title
import lynk.feature.hangouts.generated.resources.album_new_photos
import lynk.feature.hangouts.generated.resources.album_photo_deleted
import lynk.feature.hangouts.generated.resources.album_photo_saved
import lynk.feature.hangouts.generated.resources.album_show_new_photos
import lynk.feature.hangouts.generated.resources.detail_dialog_dismiss
import lynk.feature.hangouts.generated.resources.memories_title
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HangoutAlbumRoot(
    hangoutId: String,
    initialPhotoId: String?,
    navigateBack: () -> Unit,
    viewModel: HangoutAlbumViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(hangoutId) {
        viewModel.onAction(HangoutAlbumAction.OnSelectHangout(hangoutId, initialPhotoId))
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is HangoutAlbumEvent.Error -> snackbarHostState.showFlashMessage(
                message = event.message.asStringAsync(),
                type = LynkFlashType.Error
            )

            HangoutAlbumEvent.PhotoSaved -> snackbarHostState.showFlashMessage(
                message = getString(Res.string.album_photo_saved),
                type = LynkFlashType.Info
            )

            HangoutAlbumEvent.CaptionUpdated -> snackbarHostState.showFlashMessage(
                message = getString(Res.string.album_caption_updated),
                type = LynkFlashType.Success
            )

            HangoutAlbumEvent.PhotoDeleted -> snackbarHostState.showFlashMessage(
                message = getString(Res.string.album_photo_deleted),
                type = LynkFlashType.Info
            )

            HangoutAlbumEvent.NewPhotosAdded -> {
                val flashResult = snackbarHostState.showFlashMessage(
                    message = getString(Res.string.album_new_photos),
                    type = LynkFlashType.Info,
                    duration = SnackbarDuration.Long,
                    actionLabel = getString(Res.string.album_show_new_photos)
                )
                if (flashResult == SnackbarResult.ActionPerformed) {
                    viewModel.onAction(HangoutAlbumAction.OnNewPhotosClick)
                }
            }
        }
    }

    HangoutAlbumScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
        navigateBack = navigateBack
    )
}

@Composable
fun HangoutAlbumScreen(
    state: HangoutAlbumState,
    onAction: (HangoutAlbumAction) -> Unit,
    snackbarHostState: SnackbarHostState,
    navigateBack: () -> Unit
) {
    val hapticFeedback = rememberAppHaptic()
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val photoSaver = rememberPhotoSaver()
    val photoSharer = rememberPhotoSharer()

    // True while a photo is being read for saving or sharing, so a second tap waits
    var isExporting by remember { mutableStateOf(false) }

    PaginationScrollListener(
        lazyGridState = gridState,
        itemCount = state.photos.size,
        isPaginationLoading = state.isLoading,
        isEndReached = state.isEndReached,
        onNearBottom = { onAction(HangoutAlbumAction.LoadNextPage) },
        resetKey = state.photosResetEpoch
    )

    LynkScaffold(
        snackbarHostState = snackbarHostState.takeIf { state.viewerIndex == null },
        topBar = {
            val backLabel = stringResource(Res.string.album_back)

            LynkTopAppBar(
                title = state.hangoutName ?: stringResource(Res.string.memories_title),
                navigationIcon = {
                    LynkIconButton(
                        onClick = {
                            hapticFeedback(AppHaptic.ImpactLight)
                            navigateBack()
                        }
                    ) {
                        Icon(
                            imageVector = Lucide.ChevronLeft,
                            contentDescription = backLabel
                        )
                    }
                },
                iosLeadingItems = persistentListOf(
                    LynkIosBarButtonItem(
                        sfSymbol = "chevron.left",
                        onClick = {
                            hapticFeedback(AppHaptic.ImpactLight)
                            navigateBack()
                        }
                    )
                )
            )
        }
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            val configuration = currentDeviceConfiguration()
            val gridMaxWidth = if (configuration.isMobile) Dp.Unspecified else 640.dp

            val showEmptyAlbum = state.photos.isEmpty() && !state.isLoading && state.isEndReached
            val showLoadError = state.photos.isEmpty() && state.loadError != null && !state.isLoading

            if (showLoadError) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LynkErrorState(
                        title = stringResource(Res.string.album_load_error_title),
                        message = state.loadError.asString(),
                        onRetry = {
                            hapticFeedback(AppHaptic.ImpactLight)
                            onAction(HangoutAlbumAction.OnRetryClick)
                        }
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 96.dp),
                    state = gridState,
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = scaffoldPadding.calculateTopPadding() + 16.dp,
                        bottom = scaffoldPadding.calculateBottomPadding() + 16.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.widthIn(max = gridMaxWidth).fillMaxSize()
                ) {
                    if (showEmptyAlbum) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            HangoutAlbumEmptyState()
                        }
                    } else {
                        items(state.photos, key = { it.id }) { photo ->
                            HangoutPhotoThumbnail(
                                photoId = photo.id,
                                photoThumbnailUrl = photo.thumbnailUrl,
                                photoCaption = photo.caption,
                                onClick = { onAction(HangoutAlbumAction.OnPhotoClick(photo.id)) },
                                onLoadFailed = { onAction(HangoutAlbumAction.OnPhotoLoadFailed) },
                                modifier = Modifier.aspectRatio(1f).animateItem()
                            )
                        }

                        if (state.isLoading) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(64.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    LynkProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.viewerIndex != null) {
        val platformContext = LocalPlatformContext.current

        HangoutPhotoViewer(
            photos = state.photos,
            initialIndex = state.viewerIndex,
            currentUserId = state.currentUserId,
            hostId = state.hostId,
            isExporting = isExporting,
            deletingPhotoId = state.deletingPhotoId,
            isEndReached = state.isEndReached,
            snackbarHostState = snackbarHostState,
            onPageChanged = { onAction(HangoutAlbumAction.OnViewerPageChanged(it)) },
            onLoadNextPage = { onAction(HangoutAlbumAction.LoadNextPage) },
            onPhotoLoadFailed = { onAction(HangoutAlbumAction.OnPhotoLoadFailed) },
            onSaveClick = { photo ->
                coroutineScope.launch {
                    isExporting = true
                    val imageBytes = loadHangoutPhotoBytes(
                        context = platformContext,
                        url = photo.fullUrl,
                        cacheKey = "photo:${photo.id}:full"
                    )
                    val isSaved = imageBytes != null && photoSaver.saveToGallery(imageBytes, photo.caption)
                    isExporting = false
                    onAction(HangoutAlbumAction.OnPhotoSaved(isSaved))
                }
            },
            onShareClick = { photo ->
                coroutineScope.launch {
                    isExporting = true
                    val imageBytes = loadHangoutPhotoBytes(
                        context = platformContext,
                        url = photo.fullUrl,
                        cacheKey = "photo:${photo.id}:full"
                    )
                    val isShared = imageBytes != null && photoSharer.share(imageBytes, photo.caption)
                    isExporting = false
                    onAction(HangoutAlbumAction.OnPhotoShared(isShared))
                }
            },
            onEditCaptionClick = { onAction(HangoutAlbumAction.OnEditCaptionClick(it)) },
            onDeleteClick = { onAction(HangoutAlbumAction.OnDeleteClick(it)) },
            onDismiss = { onAction(HangoutAlbumAction.OnDismissViewer) }
        )
    }

    if (state.captionEditPhotoId != null) {
        EditCaptionSheet(
            captionState = state.captionState,
            canSave = state.canSaveCaption,
            isSaving = state.isSavingCaption,
            onSave = { onAction(HangoutAlbumAction.OnSaveCaptionClick) },
            onDismiss = { onAction(HangoutAlbumAction.OnDismissCaptionSheet) }
        )
    }

    if (state.pendingDeletePhotoId != null) {
        LynkDialog(
            onDismissRequest = { onAction(HangoutAlbumAction.OnDismissDeleteDialog) },
            title = stringResource(Res.string.album_delete_confirm_title),
            message = stringResource(Res.string.album_delete_confirm_message),
            confirmText = stringResource(Res.string.album_delete_confirm_action),
            dismissText = stringResource(Res.string.detail_dialog_dismiss),
            isDestructive = true,
            onConfirm = { onAction(HangoutAlbumAction.OnDeleteConfirmed) }
        )
    }
}

@PreviewLightDark
@Composable
private fun HangoutAlbumScreenPreview() {
    LynkTheme {
        HangoutAlbumScreen(
            state = HangoutAlbumState(
                hangoutName = "Friday Night Out",
                currentUserId = PREVIEW_HOST_ID,
                hostId = PREVIEW_HOST_ID,
                photos = previewHangoutPhotos,
                isEndReached = true
            ),
            onAction = {},
            snackbarHostState = SnackbarHostState(),
            navigateBack = {}
        )
    }
}
