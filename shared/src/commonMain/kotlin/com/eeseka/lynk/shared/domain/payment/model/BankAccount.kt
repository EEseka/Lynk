package com.eeseka.lynk.shared.domain.payment.model

data class BankAccount(
    val accountNumber: String,
    val accountName: String,
    val bankCode: String
)