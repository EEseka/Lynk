package com.eeseka.lynk.assistant.presentation.mappers

import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.presentation.util.UiText
import com.eeseka.lynk.shared.presentation.util.toUiText
import lynk.feature.assistant.generated.resources.Res
import lynk.feature.assistant.generated.resources.error_hangout_busy
import lynk.feature.assistant.generated.resources.error_not_attending
import lynk.feature.assistant.generated.resources.error_still_answering
import lynk.feature.assistant.generated.resources.error_unavailable

fun DataError.Remote.toAssistantUiText(isHangoutChat: Boolean): UiText {
    return when (this) {
        DataError.Remote.FORBIDDEN -> {
            if (isHangoutChat) UiText.Resource(Res.string.error_not_attending) else toUiText()
        }

        DataError.Remote.CONFLICT -> UiText.Resource(
            if (isHangoutChat) Res.string.error_hangout_busy else Res.string.error_still_answering
        )
        DataError.Remote.SERVICE_UNAVAILABLE -> UiText.Resource(Res.string.error_unavailable)
        else -> toUiText()
    }
}
