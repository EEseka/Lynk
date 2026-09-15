package com.eeseka.lynk.create_hangout.presentation.mappers

import com.eeseka.lynk.create_hangout.domain.validation.HangoutDateValidationState
import com.eeseka.lynk.create_hangout.domain.validation.HangoutDescriptionValidationState
import com.eeseka.lynk.create_hangout.domain.validation.HangoutNameValidationState
import com.eeseka.lynk.create_hangout.domain.validation.HangoutTimeValidationState
import com.eeseka.lynk.shared.presentation.util.UiText
import lynk.feature.create_hangout.generated.resources.Res
import lynk.feature.create_hangout.generated.resources.error_hangout_date_missing
import lynk.feature.create_hangout.generated.resources.error_hangout_date_past
import lynk.feature.create_hangout.generated.resources.error_hangout_description_too_long
import lynk.feature.create_hangout.generated.resources.error_hangout_name_blank
import lynk.feature.create_hangout.generated.resources.error_hangout_name_too_long
import lynk.feature.create_hangout.generated.resources.error_hangout_time_missing
import lynk.feature.create_hangout.generated.resources.error_hangout_time_past

fun HangoutNameValidationState.toUiText(): UiText? = when (this) {
    HangoutNameValidationState.BLANK -> UiText.Resource(Res.string.error_hangout_name_blank)
    HangoutNameValidationState.TOO_LONG -> UiText.Resource(Res.string.error_hangout_name_too_long)
    HangoutNameValidationState.VALID -> null
}

fun HangoutDescriptionValidationState.toUiText(): UiText? = when (this) {
    HangoutDescriptionValidationState.TOO_LONG -> UiText.Resource(Res.string.error_hangout_description_too_long)
    HangoutDescriptionValidationState.VALID -> null
}

fun HangoutDateValidationState.toUiText(): UiText? = when (this) {
    HangoutDateValidationState.MISSING -> UiText.Resource(Res.string.error_hangout_date_missing)
    HangoutDateValidationState.PAST_DATE -> UiText.Resource(Res.string.error_hangout_date_past)
    HangoutDateValidationState.VALID -> null
}

fun HangoutTimeValidationState.toUiText(): UiText? = when (this) {
    HangoutTimeValidationState.MISSING -> UiText.Resource(Res.string.error_hangout_time_missing)
    HangoutTimeValidationState.PAST_TIME -> UiText.Resource(Res.string.error_hangout_time_past)
    HangoutTimeValidationState.VALID -> null
}
