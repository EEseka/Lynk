package com.eeseka.lynk.create_hangout.presentation

sealed interface CreateHangoutEvent {
    data class Success(val hangoutId: String) : CreateHangoutEvent
}