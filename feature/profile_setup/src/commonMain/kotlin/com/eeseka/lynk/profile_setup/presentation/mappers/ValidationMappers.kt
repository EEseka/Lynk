package com.eeseka.lynk.profile_setup.presentation.mappers

import com.eeseka.lynk.profile_setup.domain.UsernameValidationState
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.presentation.util.UiText
import lynk.feature.profile_setup.generated.resources.Res
import lynk.feature.profile_setup.generated.resources.error_username_blank
import lynk.feature.profile_setup.generated.resources.error_username_consecutive_underscores
import lynk.feature.profile_setup.generated.resources.error_username_edge_underscore
import lynk.feature.profile_setup.generated.resources.error_username_generic_verify
import lynk.feature.profile_setup.generated.resources.error_username_invalid_chars
import lynk.feature.profile_setup.generated.resources.error_username_needs_letter
import lynk.feature.profile_setup.generated.resources.error_username_network_drop
import lynk.feature.profile_setup.generated.resources.error_username_rate_limit
import lynk.feature.profile_setup.generated.resources.error_username_too_long
import lynk.feature.profile_setup.generated.resources.error_username_too_short

fun UsernameValidationState.toUiText(): UiText? = when (this) {
    UsernameValidationState.BLANK -> UiText.Resource(Res.string.error_username_blank)
    UsernameValidationState.TOO_SHORT -> UiText.Resource(Res.string.error_username_too_short)
    UsernameValidationState.TOO_LONG -> UiText.Resource(Res.string.error_username_too_long)
    UsernameValidationState.NEEDS_LETTER -> UiText.Resource(Res.string.error_username_needs_letter)
    UsernameValidationState.EDGE_UNDERSCORE -> UiText.Resource(Res.string.error_username_edge_underscore)
    UsernameValidationState.CONSECUTIVE_UNDERSCORES -> UiText.Resource(Res.string.error_username_consecutive_underscores)
    UsernameValidationState.INVALID_CHARACTERS -> UiText.Resource(Res.string.error_username_invalid_chars)
    UsernameValidationState.VALID -> null
}

fun DataError.Remote.toUsernameCheckUiText(): UiText = when (this) {
    DataError.Remote.NO_INTERNET -> UiText.Resource(Res.string.error_username_network_drop)
    DataError.Remote.TOO_MANY_REQUESTS -> UiText.Resource(Res.string.error_username_rate_limit)
    else -> UiText.Resource(Res.string.error_username_generic_verify)
}