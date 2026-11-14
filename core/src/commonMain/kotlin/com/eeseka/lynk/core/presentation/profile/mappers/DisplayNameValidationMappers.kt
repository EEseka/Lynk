package com.eeseka.lynk.core.presentation.profile.mappers

import com.eeseka.lynk.core.domain.profile.validation.DisplayNameValidationState
import com.eeseka.lynk.core.presentation.util.UiText
import lynk.core.generated.resources.Res
import lynk.core.generated.resources.error_display_name_blank
import lynk.core.generated.resources.error_display_name_too_long

fun DisplayNameValidationState.toUiText(): UiText? = when (this) {
    DisplayNameValidationState.BLANK -> UiText.Resource(Res.string.error_display_name_blank)
    DisplayNameValidationState.TOO_LONG -> UiText.Resource(Res.string.error_display_name_too_long)
    DisplayNameValidationState.VALID -> null
}
