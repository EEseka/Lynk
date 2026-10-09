package com.eeseka.lynk.hangouts.presentation.hangout_album

import androidx.compose.foundation.text.input.TextFieldState
import com.eeseka.lynk.hangouts.presentation.model.HangoutPhotoUi
import com.eeseka.lynk.shared.presentation.util.UiText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

data class HangoutAlbumState(
    val hangoutId: String? = null,
    val hangoutName: String? = null,
    val currentUserId: String? = null,
    val hostId: String? = null,
    val photos: ImmutableList<HangoutPhotoUi> = persistentListOf(),
    val isLoading: Boolean = false,
    val isEndReached: Boolean = false,
    val photosResetEpoch: Int = 0,
    val loadError: UiText? = null,

    // The photo open in the full-screen viewer
    val viewerIndex: Int? = null,

    val isSelecting: Boolean = false,
    val selectedPhotoIds: ImmutableSet<String> = persistentSetOf(),

    val captionEditPhotoId: String? = null,
    val captionState: TextFieldState = TextFieldState(),
    val canSaveCaption: Boolean = false,
    val isSavingCaption: Boolean = false,

    val pendingDeletePhotoId: String? = null,
    val deletingPhotoId: String? = null
)
