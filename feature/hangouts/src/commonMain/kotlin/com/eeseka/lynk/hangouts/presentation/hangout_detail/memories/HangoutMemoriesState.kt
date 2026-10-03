package com.eeseka.lynk.hangouts.presentation.hangout_detail.memories

import com.eeseka.lynk.hangouts.presentation.model.HangoutPhotoUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class HangoutMemoriesState(
    val photoCount: Int = 0,
    val remainingPhotoSlots: Int = 0,
    val photos: ImmutableList<HangoutPhotoUi> = persistentListOf(),
    val isLoadingPhotos: Boolean = false,
    val isUploadingPhotos: Boolean = false
)
