package com.eeseka.lynk.hangouts.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
data class PayoutAccountUi(
    val bankName: String?,
    val accountNumberLast4: String,
    val accountHolderName: String
)
