package com.eeseka.lynk.core.presentation.util

import com.eeseka.lynk.core.domain.lobby.model.LobbyEvent
import lynk.core.generated.resources.Res
import lynk.core.generated.resources.error_unknown
import lynk.core.generated.resources.lobby_error_candidates_full
import lynk.core.generated.resources.lobby_error_invalid_choice
import lynk.core.generated.resources.lobby_error_lock_failed
import lynk.core.generated.resources.lobby_error_no_votes
import lynk.core.generated.resources.lobby_error_not_a_candidate
import lynk.core.generated.resources.lobby_error_not_attending
import lynk.core.generated.resources.lobby_error_not_host
import lynk.core.generated.resources.lobby_error_not_suggester
import lynk.core.generated.resources.lobby_error_note_not_one_line
import lynk.core.generated.resources.lobby_error_note_too_long
import lynk.core.generated.resources.lobby_error_spot_not_found
import lynk.core.generated.resources.lobby_error_voting_closed

fun LobbyEvent.LobbyError.toUiText(): UiText {
    val resource = when (code) {
        "NOT_ATTENDING" -> Res.string.lobby_error_not_attending
        "VOTING_CLOSED" -> Res.string.lobby_error_voting_closed
        "SPOT_NOT_FOUND" -> Res.string.lobby_error_spot_not_found
        "CANDIDATES_FULL" -> Res.string.lobby_error_candidates_full
        "NOT_HOST" -> Res.string.lobby_error_not_host
        "NOT_SUGGESTER" -> Res.string.lobby_error_not_suggester
        "NOTE_TOO_LONG" -> Res.string.lobby_error_note_too_long
        "NOTE_NOT_ONE_LINE" -> Res.string.lobby_error_note_not_one_line
        "NOT_A_CANDIDATE" -> Res.string.lobby_error_not_a_candidate
        "INVALID_CHOICE" -> Res.string.lobby_error_invalid_choice
        "NO_VOTES" -> Res.string.lobby_error_no_votes
        "LOCK_FAILED" -> Res.string.lobby_error_lock_failed
        else -> Res.string.error_unknown // INVALID_JSON, SERVER_ERROR, and any code a newer backend adds
    }
    return UiText.Resource(resource)
}
