package com.eeseka.lynk.shared.domain.hangout.model

enum class HangoutStatus {
    VOTING,      // Spot is undecided. Lobby shows voting UI.
    SCHEDULED,   // Spot is locked in. Waiting for the date.
    ONGOING,     // It is happening right now!
    COMPLETED,   // In the past.
    CANCELLED
}
