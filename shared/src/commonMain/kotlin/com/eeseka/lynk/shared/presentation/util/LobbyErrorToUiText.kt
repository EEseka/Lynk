package com.eeseka.lynk.shared.presentation.util

import com.eeseka.lynk.shared.domain.lobby.model.LobbyEvent
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.error_unknown
import lynk.shared.generated.resources.lobby_error_candidates_full
import lynk.shared.generated.resources.lobby_error_invalid_choice
import lynk.shared.generated.resources.lobby_error_lock_failed
import lynk.shared.generated.resources.lobby_error_no_votes
import lynk.shared.generated.resources.lobby_error_not_a_candidate
import lynk.shared.generated.resources.lobby_error_not_attending
import lynk.shared.generated.resources.lobby_error_not_host
import lynk.shared.generated.resources.lobby_error_spot_not_found
import lynk.shared.generated.resources.lobby_error_voting_closed

fun LobbyEvent.LobbyError.toUiText(): UiText {
    val resource = when (code) {
        "NOT_ATTENDING" -> Res.string.lobby_error_not_attending
        "VOTING_CLOSED" -> Res.string.lobby_error_voting_closed
        "SPOT_NOT_FOUND" -> Res.string.lobby_error_spot_not_found
        "CANDIDATES_FULL" -> Res.string.lobby_error_candidates_full
        "NOT_HOST" -> Res.string.lobby_error_not_host
        "NOT_A_CANDIDATE" -> Res.string.lobby_error_not_a_candidate
        "INVALID_CHOICE" -> Res.string.lobby_error_invalid_choice
        "NO_VOTES" -> Res.string.lobby_error_no_votes
        "LOCK_FAILED" -> Res.string.lobby_error_lock_failed
        else -> Res.string.error_unknown // INVALID_JSON, SERVER_ERROR, and any code a newer backend adds
    }
    return UiText.Resource(resource)
}
