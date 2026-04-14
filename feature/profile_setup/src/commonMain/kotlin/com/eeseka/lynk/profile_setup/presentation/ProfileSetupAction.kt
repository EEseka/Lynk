package com.eeseka.lynk.profile_setup.presentation


sealed interface ProfileSetupAction {
    data class OnImagePicked(val rawPath: String, val mimeType: String) : ProfileSetupAction
    data object OnRemoveImageClick : ProfileSetupAction
    data object OnSubmitClick : ProfileSetupAction
}