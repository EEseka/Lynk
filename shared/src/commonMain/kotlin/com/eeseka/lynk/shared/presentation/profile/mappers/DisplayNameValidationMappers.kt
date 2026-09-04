package com.eeseka.lynk.shared.presentation.profile.mappers

import com.eeseka.lynk.shared.domain.profile.validation.DisplayNameValidationState
import com.eeseka.lynk.shared.presentation.util.UiText
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.error_display_name_blank
import lynk.shared.generated.resources.error_display_name_too_long

fun DisplayNameValidationState.toUiText(): UiText? = when (this) {
    DisplayNameValidationState.BLANK -> UiText.Resource(Res.string.error_display_name_blank)
    DisplayNameValidationState.TOO_LONG -> UiText.Resource(Res.string.error_display_name_too_long)
    DisplayNameValidationState.VALID -> null
}
