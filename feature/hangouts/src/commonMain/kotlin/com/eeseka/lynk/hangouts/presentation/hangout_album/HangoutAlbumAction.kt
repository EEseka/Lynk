package com.eeseka.lynk.hangouts.presentation.hangout_album

sealed interface HangoutAlbumAction {
    data class OnSelectHangout(val hangoutId: String, val initialPhotoId: String?) : HangoutAlbumAction
    data object LoadNextPage : HangoutAlbumAction
    data object OnRetryClick : HangoutAlbumAction
    data object OnNewPhotosClick : HangoutAlbumAction

    // Viewer
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
