package com.eeseka.lynk.auth.presentation

import com.eeseka.lynk.core.domain.auth.model.User
import com.eeseka.lynk.core.presentation.util.UiText

sealed interface AuthEvent {
    data class Success(val user: User) : AuthEvent
    data class Error(val error: UiText) : AuthEvent
}