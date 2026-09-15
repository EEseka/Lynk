package com.eeseka.lynk.notifications.presentation.invite_preview

import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutPreviewUi

data class InvitePreviewState(
    val hangoutPreview: HangoutPreviewUi? = null,
    val isLoading: Boolean = false,
    val respondingTo: RsvpStatus? = null
)