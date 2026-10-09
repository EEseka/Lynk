package com.eeseka.lynk.hangouts.presentation.hangout_album

sealed interface HangoutAlbumAction {
    data class OnSelectHangout(val hangoutId: String, val initialPhotoId: String?) : HangoutAlbumAction
    data object LoadNextPage : HangoutAlbumAction
    data object OnRetryClick : HangoutAlbumAction
    data object OnNewPhotosClick : HangoutAlbumAction

    // Select mode
    data object OnSelectClick : HangoutAlbumAction
    data class OnPhotoLongClick(val photoId: String) : HangoutAlbumAction
    data class OnDragSelectStart(val photoId: String) : HangoutAlbumAction
    data class OnDragSelectOver(val photoId: String) : HangoutAlbumAction
    data object OnDragSelectEnd : HangoutAlbumAction
    data object OnCancelSelectionClick : HangoutAlbumAction
    data class OnSelectedPhotosSaved(val savedCount: Int, val photoCount: Int) : HangoutAlbumAction
    data class OnSelectedPhotosShared(val isShared: Boolean) : HangoutAlbumAction

    // Viewer
    // Opens the viewer, or picks the photo instead while selecting
    data class OnPhotoClick(val photoId: String) : HangoutAlbumAction
    data class OnViewerPageChanged(val index: Int) : HangoutAlbumAction
    data object OnDismissViewer : HangoutAlbumAction
    data object OnPhotoLoadFailed : HangoutAlbumAction
    data class OnPhotoSaved(val isSaved: Boolean) : HangoutAlbumAction
    data class OnPhotoShared(val isShared: Boolean) : HangoutAlbumAction

    // Caption (uploader)
    data class OnEditCaptionClick(val photoId: String) : HangoutAlbumAction
    data object OnDismissCaptionSheet : HangoutAlbumAction
    data object OnSaveCaptionClick : HangoutAlbumAction

    // Delete (uploader or host)
    data class OnDeleteClick(val photoId: String) : HangoutAlbumAction
    data object OnDismissDeleteDialog : HangoutAlbumAction
    data object OnDeleteConfirmed : HangoutAlbumAction
}
