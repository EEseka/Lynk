package com.eeseka.lynk.shared.domain.auth.model

data class AuthInfo(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)