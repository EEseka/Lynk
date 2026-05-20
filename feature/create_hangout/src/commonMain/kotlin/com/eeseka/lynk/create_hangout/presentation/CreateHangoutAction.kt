package com.eeseka.lynk.create_hangout.presentation

import com.eeseka.lynk.create_hangout.domain.SearchTab
import com.eeseka.lynk.shared.domain.hangout.model.Hangout
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.domain.spot.model.Spot
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

sealed interface CreateHangoutAction {
    data class InitCreateMode(val spot: Spot? = null) : CreateHangoutAction
    data class InitEditMode(val hangout: Hangout) : CreateHangoutAction
    data class OnLocationFetched(val latitude: Double, val longitude: Double) : CreateHangoutAction
    data class OnSearchTabSelected(val tab: SearchTab) : CreateHangoutAction
    data class OnVibeSelected(val vibe: HangoutVibe) : CreateHangoutAction
    data class OnDateSelected(val date: LocalDate) : CreateHangoutAction
    data class OnTimeSelected(val time: LocalTime) : CreateHangoutAction
    data object IncrementAttendees : CreateHangoutAction
    data object DecrementAttendees : CreateHangoutAction
    data class OnLocationModeChanged(val isVotingMode: Boolean) : CreateHangoutAction
    data class OnSpotSelected(val spot: Spot?) : CreateHangoutAction
    data object LoadNextSpotSearchPage : CreateHangoutAction
    data object LoadNextFavoriteSpotSearchPage : CreateHangoutAction
    data object OnSearchQueryCleared : CreateHangoutAction
    data object OnNextStep : CreateHangoutAction
    data object OnPreviousStep : CreateHangoutAction
    data object OnSubmitClick : CreateHangoutAction
}