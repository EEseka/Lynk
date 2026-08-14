package com.eeseka.lynk.shared.data.payment.mappers

import com.eeseka.lynk.shared.data.payment.dto.BankAccountDto
import com.eeseka.lynk.shared.data.payment.dto.BankDto
import com.eeseka.lynk.shared.data.payment.dto.PaymentInitializationDto
import com.eeseka.lynk.shared.data.payment.dto.PaymentSettingsDto
import com.eeseka.lynk.shared.domain.payment.model.Bank
import com.eeseka.lynk.shared.domain.payment.model.BankAccount
import com.eeseka.lynk.shared.domain.payment.model.PaymentInitialization
import com.eeseka.lynk.shared.domain.payment.model.PaymentSettings

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

fun PaymentSettingsDto.toDomain(): PaymentSettings {
    return PaymentSettings(
        hangoutId = hangoutId,
        totalCostKobo = totalCostKobo,
        costPerPersonKobo = costPerPersonKobo,
        paymentDeadline = paymentDeadline,
        bankName = bankName,
        accountNumberLast4 = accountNumberLast4,
        accountHolderName = accountHolderName
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
