package com.eeseka.lynk.core.data.payment.mappers

import com.eeseka.lynk.core.data.payment.dto.BankAccountDto
import com.eeseka.lynk.core.data.payment.dto.BankDto
import com.eeseka.lynk.core.data.payment.dto.PaymentInitializationDto
import com.eeseka.lynk.core.data.payment.dto.PayoutAccountDto
import com.eeseka.lynk.core.domain.payment.model.Bank
import com.eeseka.lynk.core.domain.payment.model.BankAccount
import com.eeseka.lynk.core.domain.payment.model.PaymentInitialization
import com.eeseka.lynk.core.domain.payment.model.PayoutAccount

fun PayoutAccountDto.toDomain(): PayoutAccount {
    return PayoutAccount(
        bankName = bankName,
        accountNumberLast4 = accountNumberLast4,
        accountHolderName = accountHolderName
    )
}

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
        accountHolderName = accountHolderName,
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