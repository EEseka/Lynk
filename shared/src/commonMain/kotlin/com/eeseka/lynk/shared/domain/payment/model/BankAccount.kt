package com.eeseka.lynk.shared.domain.payment.model

data class BankAccount(
    val accountNumber: String,
    val accountHolderName: String,
    val bankCode: String
)