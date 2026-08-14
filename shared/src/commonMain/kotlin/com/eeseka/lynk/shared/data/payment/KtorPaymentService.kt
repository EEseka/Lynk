package com.eeseka.lynk.shared.data.payment

import com.eeseka.lynk.shared.data.networking.get
import com.eeseka.lynk.shared.data.networking.patch
import com.eeseka.lynk.shared.data.networking.post
import com.eeseka.lynk.shared.data.payment.dto.BankAccountDto
import com.eeseka.lynk.shared.data.payment.dto.BankDto
import com.eeseka.lynk.shared.data.payment.dto.PaymentInitializationDto
import com.eeseka.lynk.shared.data.payment.dto.PaymentSettingsDto
import com.eeseka.lynk.shared.data.payment.dto.PaymentVerificationDto
import com.eeseka.lynk.shared.data.payment.dto.requests.ChangeDeadlineRequest
import com.eeseka.lynk.shared.data.payment.dto.requests.DeadlineDecisionRequest
import com.eeseka.lynk.shared.data.payment.dto.requests.EnablePaymentsRequest
import com.eeseka.lynk.shared.data.payment.mappers.toDomain
import com.eeseka.lynk.shared.domain.payment.PaymentService
import com.eeseka.lynk.shared.domain.payment.model.Bank
import com.eeseka.lynk.shared.domain.payment.model.BankAccount
import com.eeseka.lynk.shared.domain.payment.model.DeadlineDecision
import com.eeseka.lynk.shared.domain.payment.model.PaymentInitialization
import com.eeseka.lynk.shared.domain.payment.model.PaymentSettings
import com.eeseka.lynk.shared.domain.payment.model.PaymentStatus
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result
import com.eeseka.lynk.shared.domain.util.map
import io.ktor.client.HttpClient
import kotlin.time.Instant

class KtorPaymentService(
    private val httpClient: HttpClient
) : PaymentService {

    override suspend fun getBanks(): Result<List<Bank>, DataError.Remote> {
        return httpClient.get<List<BankDto>>(
            route = "/payments/banks"
        ).map { banks -> banks.map { it.toDomain() } }
    }

    override suspend fun resolveBankAccount(
        accountNumber: String,
        bankCode: String
    ): Result<BankAccount, DataError.Remote> {
        return httpClient.get<BankAccountDto>(
            route = "/payments/bank-account",
            queryParams = mapOf(
                "accountNumber" to accountNumber,
                "bankCode" to bankCode
            )
        ).map { it.toDomain() }
    }

    override suspend fun enablePayments(
        hangoutId: String,
        totalCostKobo: Long,
        paymentDeadline: Instant,
        accountNumber: String,
        bankCode: String
    ): Result<PaymentSettings, DataError.Remote> {
        return httpClient.patch<EnablePaymentsRequest, PaymentSettingsDto>(
            route = "/payments/hangouts/$hangoutId",
            body = EnablePaymentsRequest(
                totalCostKobo = totalCostKobo,
                paymentDeadline = paymentDeadline,
                accountNumber = accountNumber,
                bankCode = bankCode
            )
        ).map { it.toDomain() }
    }

    override suspend fun initializePayment(
        hangoutId: String
    ): Result<PaymentInitialization, DataError.Remote> {
        return httpClient.post<PaymentInitializationDto>(
            route = "/payments/hangouts/$hangoutId/initialize"
        ).map { it.toDomain() }
    }

    override suspend fun verifyPayment(
        hangoutId: String
    ): Result<PaymentStatus, DataError.Remote> {
        return httpClient.post<PaymentVerificationDto>(
            route = "/payments/hangouts/$hangoutId/verify"
        ).map { it.status }
    }

    override suspend fun changeDeadline(
        hangoutId: String,
        newDeadline: Instant
    ): EmptyResult<DataError.Remote> {
        return httpClient.patch<ChangeDeadlineRequest, Unit>(
            route = "/payments/hangouts/$hangoutId/deadline",
            body = ChangeDeadlineRequest(newDeadline = newDeadline)
        )
    }

    override suspend fun decideAtDeadline(
        hangoutId: String,
        decision: DeadlineDecision,
        newDeadline: Instant?
    ): EmptyResult<DataError.Remote> {
        return httpClient.post<DeadlineDecisionRequest, Unit>(
            route = "/payments/hangouts/$hangoutId/deadline-decision",
            body = DeadlineDecisionRequest(
                decision = decision,
                newDeadline = newDeadline
            )
        )
    }

    override suspend fun retryPayout(hangoutId: String): EmptyResult<DataError.Remote> {
        return httpClient.post<Unit>(
            route = "/payments/hangouts/$hangoutId/retry-payout"
        )
    }
}
