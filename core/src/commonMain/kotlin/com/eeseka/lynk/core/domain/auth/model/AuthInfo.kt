package com.eeseka.lynk.core.domain.auth.model

data class AuthInfo(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)