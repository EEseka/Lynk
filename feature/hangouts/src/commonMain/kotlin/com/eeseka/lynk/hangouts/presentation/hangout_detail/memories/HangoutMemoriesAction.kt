package com.eeseka.lynk.hangouts.presentation.hangout_detail.memories

sealed interface HangoutMemoriesAction {
    data class OnSelectHangout(val hangoutId: String?) : HangoutMemoriesAction
    data class OnPhotosPicked(val imagePaths: List<String>) : HangoutMemoriesAction
    data object OnPhotoLoadFailed : HangoutMemoriesAction
}
