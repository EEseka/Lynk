package com.eeseka.lynk

sealed interface MainEvent {
    data object OnSessionExpired: MainEvent
}