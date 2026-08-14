package com.eeseka.lynk.hangouts.presentation.model

import androidx.compose.runtime.Stable

@Stable
data class BankUi(
    val code: String,
    val name: String,
    val logoUrl: String?,
    val initials: String
)