package com.eeseka.lynk.hangouts.presentation.hangout_album

import com.eeseka.lynk.core.presentation.util.UiText

sealed interface HangoutAlbumEvent {
    data class Error(val message: UiText) : HangoutAlbumEvent
    data object PhotoSaved : HangoutAlbumEvent
    data object CaptionUpdated : HangoutAlbumEvent
    data object PhotoDeleted : HangoutAlbumEvent
    data object NewPhotosAdded : HangoutAlbumEvent
}
