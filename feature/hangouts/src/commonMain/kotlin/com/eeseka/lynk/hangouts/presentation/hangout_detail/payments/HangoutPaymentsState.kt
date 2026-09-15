package com.eeseka.lynk.hangouts.presentation.hangout_detail.payments

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Stable
import com.eeseka.lynk.hangouts.presentation.hangout_detail.payments.model.DeadlineChangeIntent
import com.eeseka.lynk.hangouts.presentation.hangout_detail.payments.model.PaymentQuoteUi
import com.eeseka.lynk.hangouts.presentation.model.BankUi
import com.eeseka.lynk.shared.presentation.util.UiText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate

@Stable
data class HangoutPaymentsState(
    val isCollectPaymentsOn: Boolean = false,
    val totalCostState: TextFieldState = TextFieldState(),
    val totalCostError: UiText? = null,
    val paymentDeadlineDate: LocalDate? = null,
    val paymentDeadlineError: UiText? = null,
    val isPaymentDeadlinePickerOpen: Boolean = false,
    val isEnablingPayments: Boolean = false,

    // Bank picker
    val isBankPickerOpen: Boolean = false,
    val bankSearchState: TextFieldState = TextFieldState(),
    val allBanks: ImmutableList<BankUi> = persistentListOf(),
    val bankResults: ImmutableList<BankUi> = persistentListOf(),
    val isLoadingBanks: Boolean = false,
    val bankLoadError: UiText? = null,
    val selectedBank: BankUi? = null,
    val bankError: UiText? = null,

    // Account resolution
    val accountNumberState: TextFieldState = TextFieldState(),
    val resolvedAccountName: String? = null,
    val isResolvingAccount: Boolean = false,
    val accountResolutionError: UiText? = null,
    val accountNumberError: UiText? = null,

    val canEnablePayments: Boolean = true,

    val pendingDeadlineChange: DeadlineChangeIntent? = null,
    val isChangingDeadline: Boolean = false,

    // The four-way answer the server waits for once a deadline lapses with people unpaid.
    val isDeadlineDecisionSheetOpen: Boolean = false,
    val isDecidingAtDeadline: Boolean = false,

    val isRetryingPayout: Boolean = false,

    val isInitializingPayment: Boolean = false,
    val paymentQuote: PaymentQuoteUi? = null,

    // Paystack's checkout page.
    val paymentCheckoutUrl: String? = null,

    val isAwaitingPaymentReturn: Boolean = false,
    val isVerifyingPayment: Boolean = false
)
