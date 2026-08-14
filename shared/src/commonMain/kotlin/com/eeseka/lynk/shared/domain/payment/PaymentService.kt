package com.eeseka.lynk.shared.domain.payment

import com.eeseka.lynk.shared.domain.payment.model.Bank
import com.eeseka.lynk.shared.domain.payment.model.BankAccount
import com.eeseka.lynk.shared.domain.payment.model.DeadlineDecision
import com.eeseka.lynk.shared.domain.payment.model.PaymentInitialization
import com.eeseka.lynk.shared.domain.payment.model.PaymentSettings
import com.eeseka.lynk.shared.domain.payment.model.PaymentStatus
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result
import kotlin.time.Instant

interface PaymentService {
    suspend fun getBanks(): Result<List<Bank>, DataError.Remote>

    suspend fun resolveBankAccount(
        accountNumber: String,
        bankCode: String
    ): Result<BankAccount, DataError.Remote>

    suspend fun enablePayments(
        hangoutId: String,
        totalCostKobo: Long,
        paymentDeadline: Instant,
        accountNumber: String,
        bankCode: String
    ): Result<PaymentSettings, DataError.Remote>

    suspend fun initializePayment(
        hangoutId: String
    ): Result<PaymentInitialization, DataError.Remote>

    suspend fun verifyPayment(
        hangoutId: String
    ): Result<PaymentStatus, DataError.Remote>

    suspend fun changeDeadline(
        hangoutId: String,
        newDeadline: Instant
    ): EmptyResult<DataError.Remote>

    suspend fun decideAtDeadline(
        hangoutId: String,
        decision: DeadlineDecision,
        newDeadline: Instant? = null
    ): EmptyResult<DataError.Remote>

    suspend fun retryPayout(hangoutId: String): EmptyResult<DataError.Remote>
}