package com.eeseka.lynk.testing.data

import com.eeseka.lynk.shared.domain.payment.PaymentService
import com.eeseka.lynk.shared.domain.payment.model.Bank
import com.eeseka.lynk.shared.domain.payment.model.BankAccount
import com.eeseka.lynk.shared.domain.payment.model.DeadlineDecision
import com.eeseka.lynk.shared.domain.payment.model.PaymentInitialization
import com.eeseka.lynk.shared.domain.payment.model.PaymentStatus
import com.eeseka.lynk.shared.domain.util.DataError
import com.eeseka.lynk.shared.domain.util.EmptyResult
import com.eeseka.lynk.shared.domain.util.Result
import kotlin.time.Instant

class FakePaymentService : PaymentService {
    var shouldReturnError = false
    var errorToReturn = DataError.Remote.SERVER_ERROR
    var banks = mutableListOf<Bank>()
    var accountNamesByNumber = mutableMapOf<String, String>()
    var paymentInitializationToReturn = PaymentInitialization(
        authorizationUrl = "https://checkout.paystack.com/fake",
        reference = "ref_1",
        amountKobo = 510_000L,
        netAmountKobo = 500_000L
    )
    var paymentStatusToReturn = PaymentStatus.SUCCESS
    var enabledPayments = mutableListOf<EnabledPayment>()
    var changedDeadlines = mutableListOf<Instant>()
    var deadlineDecisions = mutableListOf<Pair<DeadlineDecision, Instant?>>()
    var retriedPayoutHangoutIds = mutableListOf<String>()

    data class EnabledPayment(
        val hangoutId: String,
        val totalCostKobo: Long,
        val paymentDeadline: Instant,
        val accountNumber: String,
        val bankCode: String
    )

    override suspend fun getBanks(): Result<List<Bank>, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        return Result.Success(banks.toList())
    }

    // Like Paystack, an account it cannot find comes back as a bad request
    override suspend fun resolveBankAccount(
        accountNumber: String,
        bankCode: String
    ): Result<BankAccount, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)

        val accountName = accountNamesByNumber[accountNumber]
            ?: return Result.Failure(DataError.Remote.BAD_REQUEST)
        return Result.Success(
            BankAccount(accountNumber = accountNumber, accountName = accountName, bankCode = bankCode)
        )
    }

    override suspend fun enablePayments(
        hangoutId: String,
        totalCostKobo: Long,
        paymentDeadline: Instant,
        accountNumber: String,
        bankCode: String
    ): EmptyResult<DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        enabledPayments.add(EnabledPayment(hangoutId, totalCostKobo, paymentDeadline, accountNumber, bankCode))
        return Result.Success(Unit)
    }

    override suspend fun initializePayment(hangoutId: String): Result<PaymentInitialization, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        return Result.Success(paymentInitializationToReturn)
    }

    override suspend fun verifyPayment(hangoutId: String): Result<PaymentStatus, DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        return Result.Success(paymentStatusToReturn)
    }

    override suspend fun changeDeadline(hangoutId: String, newDeadline: Instant): EmptyResult<DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        changedDeadlines.add(newDeadline)
        return Result.Success(Unit)
    }

    override suspend fun decideAtDeadline(
        hangoutId: String,
        decision: DeadlineDecision,
        newDeadline: Instant?
    ): EmptyResult<DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        deadlineDecisions.add(decision to newDeadline)
        return Result.Success(Unit)
    }

    override suspend fun retryPayout(hangoutId: String): EmptyResult<DataError.Remote> {
        if (shouldReturnError) return Result.Failure(errorToReturn)
        retriedPayoutHangoutIds.add(hangoutId)
        return Result.Success(Unit)
    }
}
