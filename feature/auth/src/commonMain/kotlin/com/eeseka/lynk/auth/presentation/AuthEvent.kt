package com.eeseka.lynk.auth.presentation

import com.eeseka.lynk.shared.domain.auth.model.User

sealed interface AuthEvent {
    data class Success(val user: User) : AuthEvent
    data class Error(val message: String) : AuthEvent
}