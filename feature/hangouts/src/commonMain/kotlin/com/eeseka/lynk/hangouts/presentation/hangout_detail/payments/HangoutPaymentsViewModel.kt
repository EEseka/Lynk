package com.eeseka.lynk.hangouts.presentation.hangout_detail.payments

import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eeseka.lynk.hangouts.presentation.hangout_detail.payments.model.DeadlineChangeIntent
import com.eeseka.lynk.hangouts.presentation.hangout_detail.payments.model.PaymentQuoteUi
import com.eeseka.lynk.hangouts.presentation.mappers.toAccountResolutionUiText
import com.eeseka.lynk.hangouts.presentation.mappers.toBankUi
import com.eeseka.lynk.shared.domain.hangout.HangoutConstants.MIN_COST_PER_PERSON_KOBO
import com.eeseka.lynk.shared.domain.hangout.HangoutDetailRepository
import com.eeseka.lynk.shared.domain.hangout.model.RsvpStatus
import com.eeseka.lynk.shared.domain.payment.PaymentConstants.NUBAN_LENGTH
import com.eeseka.lynk.shared.domain.payment.PaymentService
import com.eeseka.lynk.shared.domain.payment.model.DeadlineDecision
import com.eeseka.lynk.shared.domain.payment.model.PaymentStatus
import com.eeseka.lynk.shared.domain.util.onFailure
import com.eeseka.lynk.shared.domain.util.onSuccess
import com.eeseka.lynk.shared.presentation.util.UiText
import com.eeseka.lynk.shared.presentation.util.toNairaString
import com.eeseka.lynk.shared.presentation.util.toUiText
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.payment_account_required
import lynk.feature.hangouts.generated.resources.payment_bank_required
import lynk.feature.hangouts.generated.resources.payment_deadline_after_hangout
import lynk.feature.hangouts.generated.resources.payment_deadline_in_past
import lynk.feature.hangouts.generated.resources.payment_deadline_required
import lynk.feature.hangouts.generated.resources.payment_share_too_low
import lynk.feature.hangouts.generated.resources.payment_total_cost_required
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class HangoutPaymentsViewModel(
    private val hangoutDetailRepository: HangoutDetailRepository,
    private val paymentService: PaymentService
) : ViewModel() {
    private val eventChannel = Channel<HangoutPaymentsEvent>()
    val events = eventChannel.receiveAsFlow()

    private val _state = MutableStateFlow(HangoutPaymentsState())
    private var hasLoadedInitialData = false

    private val _hangoutId = MutableStateFlow<String?>(null)

    private val hangout = _hangoutId
        .flatMapLatest { hangoutId ->
            hangoutId?.let(hangoutDetailRepository::observeHangout) ?: flowOf(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val state = _state
        .onStart {
            if (!hasLoadedInitialData) {
                observeBankSearch()
                observeAccountResolution()
                observeCanEnablePayments()
                observeEnablePaymentsFormValidation()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = HangoutPaymentsState()
        )

    private val isBusyFlow = state.map {
        it.isEnablingPayments || it.isResolvingAccount
    }.distinctUntilChanged()

    // Errors stay quiet until the first press of Turn on payments, then follow every change.
    private var hasAttemptedSubmit = false

    fun onAction(action: HangoutPaymentsAction) {
        when (action) {
            is HangoutPaymentsAction.OnSelectHangout -> selectHangout(action.hangoutId)
            is HangoutPaymentsAction.OnCollectPaymentsToggled -> toggleCollectPayments(action.isOn)
            HangoutPaymentsAction.OnPaymentDeadlinePickerClick -> _state.update { it.copy(isPaymentDeadlinePickerOpen = true) }
            HangoutPaymentsAction.OnDismissPaymentDeadlinePicker -> _state.update { it.copy(isPaymentDeadlinePickerOpen = false)}
            is HangoutPaymentsAction.OnPaymentDeadlineSelected -> selectPaymentDeadline(action.date)
            HangoutPaymentsAction.OnEnablePaymentsConfirmed -> enablePayments()
            HangoutPaymentsAction.OnChangeDeadlineClick -> _state.update { it.copy(pendingDeadlineChange = DeadlineChangeIntent.CHANGE) }
            HangoutPaymentsAction.OnDismissDeadlinePicker -> _state.update { it.copy(pendingDeadlineChange = null) }
            is HangoutPaymentsAction.OnNewDeadlineSelected -> submitNewDeadline(action.date)
            HangoutPaymentsAction.OnDeadlineDecisionClick -> _state.update { it.copy(isDeadlineDecisionSheetOpen = true) }
            HangoutPaymentsAction.OnDismissDeadlineDecisionSheet -> _state.update { it.copy(isDeadlineDecisionSheetOpen = false) }
            is HangoutPaymentsAction.OnDeadlineDecisionSelected -> selectDeadlineDecision(action.decision)
            HangoutPaymentsAction.OnRetryPayoutClick -> retryPayout()
            HangoutPaymentsAction.OnPayClick -> initializePayment()
            HangoutPaymentsAction.OnDismissPayConfirmSheet -> dismissPayConfirmSheet()
            HangoutPaymentsAction.OnConfirmPayment -> openPaymentPage()
            HangoutPaymentsAction.OnCheckPaymentClick -> verifyPayment()
            HangoutPaymentsAction.OnDismissPaymentCheckout -> dismissPaymentCheckout()
            HangoutPaymentsAction.OnBankPickerClick -> openBankPicker()
            HangoutPaymentsAction.OnDismissBankPicker -> dismissBankPicker()
            is HangoutPaymentsAction.OnBankSelected -> selectBank(action.bankCode)
        }
    }

    private fun selectHangout(hangoutId: String?) {
        if (hangoutId == _hangoutId.value) return
        _hangoutId.update { hangoutId }

        resetCollectPaymentsForm()
        _state.update {
            it.copy(
                isEnablingPayments = false,
                isResolvingAccount = false,
                pendingDeadlineChange = null,
                isChangingDeadline = false,
                isDeadlineDecisionSheetOpen = false,
                isDecidingAtDeadline = false,
                isRetryingPayout = false,
                isInitializingPayment = false,
                paymentQuote = null,
                paymentCheckoutUrl = null,
                isAwaitingPaymentReturn = false,
                isVerifyingPayment = false
            )
        }
    }

    private fun toggleCollectPayments(isOn: Boolean) {
        if (!isOn) {
            resetCollectPaymentsForm()
            return
        }
        _state.update { it.copy(isCollectPaymentsOn = true) }
        // 262 banks in one unpaginated call, so it is fetched once when the form first opens.
        if (state.value.allBanks.isEmpty()) loadBanks()
    }

    private fun selectPaymentDeadline(date: LocalDate) {
        _state.update {
            it.copy(
                paymentDeadlineDate = date,
                isPaymentDeadlinePickerOpen = false
            )
        }
    }

    private fun submitNewDeadline(date: LocalDate) {
        val hangoutId = _hangoutId.value ?: return
        val intent = state.value.pendingDeadlineChange ?: return
        val scheduledAt = hangout.value?.scheduledAt ?: return

        date.deadlineError()?.let { error ->
            viewModelScope.launch {
                eventChannel.send(HangoutPaymentsEvent.Error(error))
            }
            return
        }

        val deadline = date.toDeadlineInstant(scheduledAt)
        _state.update { it.copy(pendingDeadlineChange = null) }

        viewModelScope.launch {
            // A change spins on Change date. An extension is an answer to the deadline decision,
            // so it spins on the Decide button instead.
            _state.update {
                when (intent) {
                    DeadlineChangeIntent.CHANGE -> it.copy(isChangingDeadline = true)
                    DeadlineChangeIntent.EXTEND -> it.copy(isDecidingAtDeadline = true)
                }
            }
            val result = when (intent) {
                DeadlineChangeIntent.CHANGE -> paymentService.changeDeadline(hangoutId, deadline)
                DeadlineChangeIntent.EXTEND -> paymentService.decideAtDeadline(
                    hangoutId = hangoutId,
                    decision = DeadlineDecision.EXTEND,
                    newDeadline = deadline
                )
            }
            result
                .onSuccess {
                    hangoutDetailRepository.refreshHangout(hangoutId)
                    eventChannel.send(HangoutPaymentsEvent.DeadlineChanged)
                }
                .onFailure { error ->
                    eventChannel.send(HangoutPaymentsEvent.Error(error.toUiText()))
                }
            _state.update { it.copy(isChangingDeadline = false, isDecidingAtDeadline = false) }
        }
    }

    private fun selectDeadlineDecision(decision: DeadlineDecision) {
        if (decision == DeadlineDecision.EXTEND) {
            _state.update {
                it.copy(
                    isDeadlineDecisionSheetOpen = false,
                    pendingDeadlineChange = DeadlineChangeIntent.EXTEND
                )
            }
            return
        }

        val hangoutId = _hangoutId.value ?: return
        viewModelScope.launch {
            _state.update { it.copy(isDecidingAtDeadline = true) }
            paymentService
                .decideAtDeadline(hangoutId = hangoutId, decision = decision)
                .onSuccess {
                    hangoutDetailRepository.refreshHangout(hangoutId)
                    eventChannel.send(HangoutPaymentsEvent.DecisionSaved)
                }
                .onFailure { error ->
                    eventChannel.send(HangoutPaymentsEvent.Error(error.toUiText()))
                }
            _state.update { it.copy(isDecidingAtDeadline = false) }
        }
    }

    private fun openBankPicker() {
        _state.update { it.copy(isBankPickerOpen = true) }
        // The banks load once, when payments are switched on. If that failed there is nothing to pick from, so opening the picker again is the retry.
        if (state.value.allBanks.isEmpty() && !state.value.isLoadingBanks) loadBanks()
    }

    private fun dismissBankPicker() {
        _state.value.bankSearchState.clearText()
        _state.update { it.copy(isBankPickerOpen = false) }
    }

    private fun selectBank(bankCode: String) {
        val bank = state.value.allBanks.firstOrNull { it.code == bankCode } ?: return
        _state.value.bankSearchState.clearText()
        _state.update {
            it.copy(
                selectedBank = bank,
                isBankPickerOpen = false,
                // The old name belonged to the old bank, so it cannot stand while the new one resolves.
                resolvedAccountName = null,
                accountResolutionError = null
            )
        }
    }

    private fun loadBanks() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingBanks = true, bankLoadError = null) }
            paymentService
                .getBanks()
                .onSuccess { banks ->
                    val bankUis = banks.map { bank -> bank.toBankUi() }.toImmutableList()
                    _state.update {
                        it.copy(
                            allBanks = bankUis,
                            bankResults = bankUis,
                            isLoadingBanks = false
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(bankLoadError = error.toUiText(), isLoadingBanks = false)
                    }
                }
        }
    }

    private fun observeBankSearch() {
        snapshotFlow { _state.value.bankSearchState.text.toString() }
            .debounce { query -> if (query.isBlank()) 0.milliseconds else 200.milliseconds }
            .onEach { query ->
                val trimmed = query.trim()
                _state.update { state ->
                    state.copy(
                        bankResults = if (trimmed.isBlank()) {
                            state.allBanks
                        } else {
                            state.allBanks.filter { it.name.contains(trimmed, ignoreCase = true) }.toImmutableList()
                        }
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeAccountResolution() {
        combine(
            snapshotFlow { _state.value.accountNumberState.text.toString() },
            state.map { it.selectedBank?.code }.distinctUntilChanged()
        ) { accountNumber, bankCode ->
            accountNumber.trim() to bankCode
        }
        .distinctUntilChanged()
        .debounce(800.milliseconds)
        .mapLatest { (accountNumber, bankCode) ->
            if (bankCode == null || accountNumber.length != NUBAN_LENGTH) {
                _state.update {
                    it.copy(
                        resolvedAccountName = null,
                        accountResolutionError = null,
                        isResolvingAccount = false
                    )
                }
                return@mapLatest
            }

            _state.update {
                it.copy(
                    isResolvingAccount = true,
                    resolvedAccountName = null,
                    accountResolutionError = null
                )
            }
            paymentService
                .resolveBankAccount(accountNumber = accountNumber, bankCode = bankCode)
                .onSuccess { account ->
                    _state.update {
                        it.copy(
                            resolvedAccountName = account.accountName,
                            isResolvingAccount = false
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            accountResolutionError = error.toAccountResolutionUiText(),
                            isResolvingAccount = false
                        )
                    }
                }
        }
        .launchIn(viewModelScope)
    }

    private fun observeCanEnablePayments() {
        isBusyFlow
            .onEach { isBusy -> _state.update { it.copy(canEnablePayments = !isBusy) } }
            .launchIn(viewModelScope)
    }

    private fun observeEnablePaymentsFormValidation() {
        val totalCostFlow = snapshotFlow { _state.value.totalCostState.text.toString() }
        val accountNumberFlow = snapshotFlow { _state.value.accountNumberState.text.toString() }

        val pickedValuesFlow = state
            .map { Triple(it.paymentDeadlineDate, it.selectedBank, it.resolvedAccountName) }
            .distinctUntilChanged()

        // The share check reads the headcount, and the deadline check reads the hangout's date.
        val hangoutFlow = hangout
            .map { currentHangout ->
                currentHangout?.participants?.count { it.rsvpStatus == RsvpStatus.ATTENDING } to currentHangout?.scheduledAt
            }
            .distinctUntilChanged()

        combine(
            totalCostFlow,
            accountNumberFlow,
            pickedValuesFlow,
            hangoutFlow
        ) { _, _, _, _ -> }
            .onEach { if (hasAttemptedSubmit) validateFormInputs() }
            .launchIn(viewModelScope)
    }

    private fun enablePayments() {
        val currentState = state.value

        hasAttemptedSubmit = true

        if (currentState.isEnablingPayments || currentState.isResolvingAccount || !validateFormInputs()) return

        val hangoutId = _hangoutId.value ?: return
        val scheduledAt = hangout.value?.scheduledAt ?: return
        val totalCostKobo = _state.value.totalCostState.text.toKoboOrNull() ?: return
        val deadline = currentState.paymentDeadlineDate?.toDeadlineInstant(scheduledAt) ?: return
        val bankCode = currentState.selectedBank?.code ?: return
        val accountNumber = _state.value.accountNumberState.text.toString().trim()

        viewModelScope.launch {
            _state.update { it.copy(isEnablingPayments = true) }
            paymentService
                .enablePayments(
                    hangoutId = hangoutId,
                    totalCostKobo = totalCostKobo,
                    paymentDeadline = deadline,
                    accountNumber = accountNumber,
                    bankCode = bankCode
                )
                .onSuccess {
                    hangoutDetailRepository.refreshHangout(hangoutId)
                    resetCollectPaymentsForm()
                    eventChannel.send(HangoutPaymentsEvent.PaymentsEnabled)
                }
                .onFailure { error ->
                    eventChannel.send(HangoutPaymentsEvent.Error(error.toUiText()))
                }
            _state.update { it.copy(isEnablingPayments = false) }
        }
    }

    private fun resetCollectPaymentsForm() {
        hasAttemptedSubmit = false
        _state.value.totalCostState.clearText()
        _state.value.accountNumberState.clearText()
        _state.value.bankSearchState.clearText()
        _state.update {
            it.copy(
                isCollectPaymentsOn = false,
                isPaymentDeadlinePickerOpen = false,
                isBankPickerOpen = false,
                totalCostError = null,
                paymentDeadlineDate = null,
                paymentDeadlineError = null,
                selectedBank = null,
                bankError = null,
                resolvedAccountName = null,
                accountResolutionError = null,
                accountNumberError = null
            )
        }
    }

    private fun validateFormInputs(): Boolean {
        val currentState = state.value
        val totalCostKobo = _state.value.totalCostState.text.toKoboOrNull()
        val attendingCount = hangout.value?.participants?.count { it.rsvpStatus == RsvpStatus.ATTENDING } ?: 0
        val isShareTooLow = totalCostKobo != null && attendingCount > 0 && totalCostKobo / attendingCount < MIN_COST_PER_PERSON_KOBO
        val deadline = currentState.paymentDeadlineDate
        val deadlineError = deadline?.deadlineError()
        val isAccountConfirmed = currentState.resolvedAccountName != null

        _state.update {
            it.copy(
                totalCostError = when {
                    totalCostKobo == null -> UiText.Resource(Res.string.payment_total_cost_required)
                    isShareTooLow -> UiText.Resource(Res.string.payment_share_too_low)
                    else -> null
                },
                paymentDeadlineError = if (deadline == null) {
                    UiText.Resource(Res.string.payment_deadline_required)
                } else deadlineError,
                bankError = if (currentState.selectedBank == null) {
                    UiText.Resource(Res.string.payment_bank_required)
                } else null,
                accountNumberError = if (!isAccountConfirmed) {
                    UiText.Resource(Res.string.payment_account_required)
                } else null
            )
        }

        return totalCostKobo != null &&
                !isShareTooLow &&
                deadline != null &&
                deadlineError == null &&
                currentState.selectedBank != null &&
                isAccountConfirmed
    }

    private fun retryPayout() {
        val hangoutId = _hangoutId.value ?: return
        viewModelScope.launch {
            _state.update { it.copy(isRetryingPayout = true) }
            paymentService
                .retryPayout(hangoutId)
                .onSuccess {
                    hangoutDetailRepository.refreshHangout(hangoutId)
                    eventChannel.send(HangoutPaymentsEvent.PayoutQueued)
                }
                .onFailure { error ->
                    eventChannel.send(HangoutPaymentsEvent.Error(error.toUiText()))
                }
            _state.update { it.copy(isRetryingPayout = false) }
        }
    }

    private fun initializePayment() {
        val hangoutId = _hangoutId.value ?: return
        viewModelScope.launch {
            _state.update { it.copy(isInitializingPayment = true) }
            paymentService
                .initializePayment(hangoutId)
                .onSuccess { initialization ->
                    _state.update {
                        it.copy(
                            paymentQuote = PaymentQuoteUi(
                                shareLabel = initialization.netAmountKobo.toNairaString(),
                                chargeLabel = initialization.amountKobo.toNairaString(),
                                authorizationUrl = initialization.authorizationUrl
                            )
                        )
                    }
                }
                .onFailure { error ->
                    eventChannel.send(HangoutPaymentsEvent.Error(error.toUiText()))
                }
            _state.update { it.copy(isInitializingPayment = false) }
        }
    }

    private fun openPaymentPage() {
        val url = state.value.paymentQuote?.authorizationUrl ?: return
        dismissPayConfirmSheet()
        _state.update { it.copy(paymentCheckoutUrl = url, isAwaitingPaymentReturn = true) }
    }

    private fun dismissPaymentCheckout() {
        _state.update { it.copy(paymentCheckoutUrl = null) }
        verifyPayment()
    }

    // The quote belongs to one attempt at paying, so it leaves with the sheet.
    private fun dismissPayConfirmSheet() {
        _state.update { it.copy(paymentQuote = null) }
    }

    private fun verifyPayment() {
        val hangoutId = _hangoutId.value ?: return
        viewModelScope.launch {
            _state.update { it.copy(isVerifyingPayment = true) }
            paymentService
                .verifyPayment(hangoutId)
                .onSuccess { status ->
                    when (status) {
                        PaymentStatus.SUCCESS -> {
                            hangoutDetailRepository.refreshHangout(hangoutId)
                            _state.update { it.copy(isAwaitingPaymentReturn = false) }
                        }
                        PaymentStatus.PENDING -> {
                            eventChannel.send(HangoutPaymentsEvent.PaymentPending)
                        }
                        PaymentStatus.FAILED, PaymentStatus.ABANDONED -> {
                            _state.update { it.copy(isAwaitingPaymentReturn = false) }
                            eventChannel.send(HangoutPaymentsEvent.PaymentNotCompleted)
                        }
                    }
                }
                .onFailure { error ->
                    eventChannel.send(HangoutPaymentsEvent.Error(error.toUiText()))
                }
            _state.update { it.copy(isVerifyingPayment = false) }
        }
    }

    private fun CharSequence.toKoboOrNull(): Long? {
        val cleaned = filter { it.isDigit() || it == '.' }.toString()
        // Two points is a typo, and there is no honest amount to guess from it.
        if (cleaned.count { it == '.' } > 1) return null

        val naira = cleaned.substringBefore('.').ifEmpty { "0" }.toLongOrNull() ?: return null
        val kobo =
            cleaned.substringAfter('.', "").take(2).padEnd(2, '0').toLongOrNull() ?: return null

        if (naira > (Long.MAX_VALUE - kobo) / 100) return null

        val totalKobo = naira * 100 + kobo
        return if (totalKobo <= 0L) null else totalKobo
    }

    private fun LocalDate.toDeadlineInstant(scheduledAt: Instant): Instant {
        val endOfDay = LocalDateTime(this, LocalTime(23, 59))
            .toInstant(TimeZone.currentSystemDefault())
        return if (endOfDay > scheduledAt) scheduledAt else endOfDay
    }

    private fun LocalDate.deadlineError(): UiText? {
        val scheduledAt = hangout.value?.scheduledAt ?: return null
        val timeZone = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(timeZone).date
        val lastAllowedDate = scheduledAt.toLocalDateTime(timeZone).date

        return when {
            this < today -> UiText.Resource(Res.string.payment_deadline_in_past)
            this > lastAllowedDate -> UiText.Resource(Res.string.payment_deadline_after_hangout)
            else -> null
        }
    }
}
