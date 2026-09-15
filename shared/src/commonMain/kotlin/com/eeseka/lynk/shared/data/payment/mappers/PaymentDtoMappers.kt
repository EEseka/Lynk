package com.eeseka.lynk.shared.data.payment.mappers

import com.eeseka.lynk.shared.data.payment.dto.BankAccountDto
import com.eeseka.lynk.shared.data.payment.dto.BankDto
import com.eeseka.lynk.shared.data.payment.dto.PaymentInitializationDto
import com.eeseka.lynk.shared.domain.payment.model.Bank
import com.eeseka.lynk.shared.domain.payment.model.BankAccount
import com.eeseka.lynk.shared.domain.payment.model.PaymentInitialization

fun BankDto.toDomain(): Bank {
    return Bank(
        name = name,
        code = code,
        logoUrl = logoUrl
    )
}

fun BankAccountDto.toDomain(): BankAccount {
    return BankAccount(
        accountNumber = accountNumber,
        accountName = accountName,
        bankCode = bankCode
    )
}

fun PaymentInitializationDto.toDomain(): PaymentInitialization {
    return PaymentInitialization(
        authorizationUrl = authorizationUrl,
        reference = reference,
        amountKobo = amountKobo,
        netAmountKobo = netAmountKobo
    )
}