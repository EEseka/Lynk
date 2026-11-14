package com.eeseka.lynk.core.domain.payment.model

data class BankAccount(
    val accountNumber: String,
    val accountHolderName: String,
    val bankCode: String
)