package com.eeseka.lynk.hangouts.presentation.hangout_detail.payments

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.eeseka.lynk.hangouts.presentation.hangout_detail.payments.model.DeadlineChangeIntent
import com.eeseka.lynk.shared.data.hangout.InMemoryHangoutDetailRepository
import com.eeseka.lynk.shared.domain.auth.model.AuthInfo
import com.eeseka.lynk.shared.domain.auth.model.AuthProvider
import com.eeseka.lynk.shared.domain.auth.model.User
import com.eeseka.lynk.shared.domain.hangout.model.HangoutVibe
import com.eeseka.lynk.shared.domain.payment.model.Bank
import com.eeseka.lynk.shared.domain.payment.model.DeadlineDecision
import com.eeseka.lynk.shared.domain.payment.model.PaymentStatus
import com.eeseka.lynk.testing.collectInBackground
import com.eeseka.lynk.testing.data.FakeHangoutService
import com.eeseka.lynk.testing.data.FakePaymentService
import com.eeseka.lynk.testing.data.FakeSessionStorage
import com.eeseka.lynk.testing.typeText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class HangoutPaymentsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var hangoutService: FakeHangoutService
    private lateinit var sessionStorage: FakeSessionStorage
    private lateinit var paymentService: FakePaymentService

    // The hangout is on 1 Jan 2100, so a deadline has to fall between today and then
    private val validDeadline = LocalDate(2099, 12, 1)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        hangoutService = FakeHangoutService()
        sessionStorage = FakeSessionStorage()
        paymentService = FakePaymentService()
        paymentService.banks = mutableListOf(
            Bank(name = "Access Bank", code = "044", logoUrl = null),
            Bank(name = "GTBank", code = "058", logoUrl = null)
        )
        paymentService.accountNamesByNumber = mutableMapOf(ACCOUNT_NUMBER to "ADA OBI")
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `turning payments on loads the banks`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(HangoutPaymentsAction.OnCollectPaymentsToggled(isOn = true))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertThat(state.isCollectPaymentsOn).isTrue()
        assertThat(state.allBanks.map { it.code }).containsExactly("044", "058")
        assertThat(state.isLoadingBanks).isFalse()
    }

    @Test
    fun `a failed bank load shows an error and opening the picker tries again`() = runTest {
        val viewModel = createViewModel()
        paymentService.shouldReturnError = true
        viewModel.onAction(HangoutPaymentsAction.OnCollectPaymentsToggled(isOn = true))
        advanceUntilIdle()
        assertThat(viewModel.state.value.bankLoadError).isNotNull()

        paymentService.shouldReturnError = false
        viewModel.onAction(HangoutPaymentsAction.OnBankPickerClick)
        advanceUntilIdle()

        assertThat(viewModel.state.value.bankLoadError).isNull()
        assertThat(viewModel.state.value.allBanks.size).isEqualTo(2)
        assertThat(viewModel.state.value.isBankPickerOpen).isTrue()
    }

    @Test
    fun `searching banks narrows the list after the debounce`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(HangoutPaymentsAction.OnCollectPaymentsToggled(isOn = true))
        viewModel.onAction(HangoutPaymentsAction.OnBankPickerClick)
        advanceUntilIdle()

        viewModel.state.value.bankSearchState.typeText("gt")
        advanceTimeBy(201.milliseconds)
        advanceUntilIdle()

        assertThat(viewModel.state.value.bankResults.map { it.code }).containsExactly("058")
    }

    @Test
    fun `a ten digit account number with a bank resolves the account name`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(HangoutPaymentsAction.OnCollectPaymentsToggled(isOn = true))
        advanceUntilIdle()

        viewModel.onAction(HangoutPaymentsAction.OnBankSelected("058"))
        viewModel.state.value.accountNumberState.typeText(ACCOUNT_NUMBER)
        advanceTimeBy(801.milliseconds)
        advanceUntilIdle()

        assertThat(viewModel.state.value.resolvedAccountName).isEqualTo("ADA OBI")
        assertThat(viewModel.state.value.isResolvingAccount).isFalse()
    }

    @Test
    fun `an account that cannot be found shows an error`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(HangoutPaymentsAction.OnCollectPaymentsToggled(isOn = true))
        advanceUntilIdle()

        viewModel.onAction(HangoutPaymentsAction.OnBankSelected("058"))
        viewModel.state.value.accountNumberState.typeText("9999999999")
        advanceTimeBy(801.milliseconds)
        advanceUntilIdle()

        assertThat(viewModel.state.value.resolvedAccountName).isNull()
        assertThat(viewModel.state.value.accountResolutionError).isNotNull()
    }

    @Test
    fun `changing the bank clears the name resolved for the old one`() = runTest {
        val viewModel = createViewModel()
        fillValidForm(viewModel)

        viewModel.onAction(HangoutPaymentsAction.OnBankSelected("044"))

        assertThat(viewModel.state.value.resolvedAccountName).isNull()
        assertThat(viewModel.state.value.selectedBank?.code).isEqualTo("044")
    }

    @Test
    fun `pressing turn on with an empty form shows every error and sends nothing`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(HangoutPaymentsAction.OnCollectPaymentsToggled(isOn = true))
        advanceUntilIdle()

        viewModel.onAction(HangoutPaymentsAction.OnEnablePaymentsConfirmed)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertThat(state.totalCostError).isNotNull()
        assertThat(state.paymentDeadlineError).isNotNull()
        assertThat(state.bankError).isNotNull()
        assertThat(state.accountNumberError).isNotNull()
        assertThat(paymentService.enabledPayments).isEmpty()
    }

    @Test
    fun `a share below the minimum per person is refused`() = runTest {
        val viewModel = createViewModel()
        fillValidForm(viewModel, totalCost = "0.50")

        viewModel.onAction(HangoutPaymentsAction.OnEnablePaymentsConfirmed)
        advanceUntilIdle()

        assertThat(viewModel.state.value.totalCostError).isNotNull()
        assertThat(paymentService.enabledPayments).isEmpty()
    }

    @Test
    fun `a deadline after the hangout is refused`() = runTest {
        val viewModel = createViewModel()
        fillValidForm(viewModel, deadline = LocalDate(2100, 2, 1))

        viewModel.onAction(HangoutPaymentsAction.OnEnablePaymentsConfirmed)
        advanceUntilIdle()

        assertThat(viewModel.state.value.paymentDeadlineError).isNotNull()
        assertThat(paymentService.enabledPayments).isEmpty()
    }

    @Test
    fun `a complete form turns payments on and resets the form`() = runTest {
        val viewModel = createViewModel()
        fillValidForm(viewModel, totalCost = "5,000.50")

        viewModel.events.test {
            viewModel.onAction(HangoutPaymentsAction.OnEnablePaymentsConfirmed)
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(HangoutPaymentsEvent.PaymentsEnabled)
            val sent = paymentService.enabledPayments.single()
            assertThat(sent.totalCostKobo).isEqualTo(500_050L)
            assertThat(sent.accountNumber).isEqualTo(ACCOUNT_NUMBER)
            assertThat(sent.bankCode).isEqualTo("058")

            val state = viewModel.state.value
            assertThat(state.isCollectPaymentsOn).isFalse()
            assertThat(state.selectedBank).isNull()
            assertThat(state.isEnablingPayments).isFalse()
        }
    }

    @Test
    fun `a failed enable sends an error and keeps the form`() = runTest {
        val viewModel = createViewModel()
        fillValidForm(viewModel)
        paymentService.shouldReturnError = true

        viewModel.events.test {
            viewModel.onAction(HangoutPaymentsAction.OnEnablePaymentsConfirmed)
            advanceUntilIdle()

            assertThat(awaitItem()).isInstanceOf(HangoutPaymentsEvent.Error::class)
            assertThat(viewModel.state.value.isCollectPaymentsOn).isTrue()
            assertThat(viewModel.state.value.isEnablingPayments).isFalse()
        }
    }

    @Test
    fun `changing the deadline sends the new date and confirms it`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAction(HangoutPaymentsAction.OnChangeDeadlineClick)
            assertThat(viewModel.state.value.pendingDeadlineChange).isEqualTo(DeadlineChangeIntent.CHANGE)

            viewModel.onAction(HangoutPaymentsAction.OnNewDeadlineSelected(validDeadline))
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(HangoutPaymentsEvent.DeadlineChanged)
            assertThat(paymentService.changedDeadlines.size).isEqualTo(1)
            assertThat(viewModel.state.value.pendingDeadlineChange).isNull()
            assertThat(viewModel.state.value.isChangingDeadline).isFalse()
        }
    }

    @Test
    fun `a new deadline in the past is refused without asking the server`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(HangoutPaymentsAction.OnChangeDeadlineClick)

        viewModel.events.test {
            viewModel.onAction(HangoutPaymentsAction.OnNewDeadlineSelected(LocalDate(2020, 1, 1)))
            advanceUntilIdle()

            assertThat(awaitItem()).isInstanceOf(HangoutPaymentsEvent.Error::class)
            assertThat(paymentService.changedDeadlines).isEmpty()
        }
    }

    @Test
    fun `extending at the deadline asks for a date and then sends it`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(HangoutPaymentsAction.OnDeadlineDecisionClick)

        viewModel.onAction(HangoutPaymentsAction.OnDeadlineDecisionSelected(DeadlineDecision.EXTEND))
        assertThat(viewModel.state.value.isDeadlineDecisionSheetOpen).isFalse()
        assertThat(viewModel.state.value.pendingDeadlineChange).isEqualTo(DeadlineChangeIntent.EXTEND)
        assertThat(paymentService.deadlineDecisions).isEmpty()

        viewModel.events.test {
            viewModel.onAction(HangoutPaymentsAction.OnNewDeadlineSelected(validDeadline))
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(HangoutPaymentsEvent.DeadlineChanged)
            val (decision, newDeadline) = paymentService.deadlineDecisions.single()
            assertThat(decision).isEqualTo(DeadlineDecision.EXTEND)
            assertThat(newDeadline).isNotNull()
            assertThat(viewModel.state.value.isDecidingAtDeadline).isFalse()
        }
    }

    @Test
    fun `any other deadline decision is sent straight away`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAction(HangoutPaymentsAction.OnDeadlineDecisionSelected(DeadlineDecision.REMOVE_NON_PAYERS))
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(HangoutPaymentsEvent.DecisionSaved)
            assertThat(paymentService.deadlineDecisions.single()).isEqualTo(DeadlineDecision.REMOVE_NON_PAYERS to null)
            assertThat(viewModel.state.value.isDecidingAtDeadline).isFalse()
        }
    }

    @Test
    fun `retrying the payout confirms it is queued`() = runTest {
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onAction(HangoutPaymentsAction.OnRetryPayoutClick)
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(HangoutPaymentsEvent.PayoutQueued)
            assertThat(paymentService.retriedPayoutHangoutIds.size).isEqualTo(1)
            assertThat(viewModel.state.value.isRetryingPayout).isFalse()
        }
    }

    @Test
    fun `paying shows the quote and confirming opens the checkout`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(HangoutPaymentsAction.OnPayClick)
        advanceUntilIdle()

        val quote = viewModel.state.value.paymentQuote
        assertThat(quote?.shareLabel).isEqualTo("₦5,000")
        assertThat(quote?.chargeLabel).isEqualTo("₦5,100")

        viewModel.onAction(HangoutPaymentsAction.OnConfirmPayment)

        val state = viewModel.state.value
        assertThat(state.paymentQuote).isNull()
        assertThat(state.paymentCheckoutUrl).isEqualTo("https://checkout.paystack.com/fake")
        assertThat(state.isAwaitingPaymentReturn).isTrue()
    }

    @Test
    fun `closing the checkout checks the payment and a success stops waiting`() = runTest {
        val viewModel = createViewModel()
        openCheckout(viewModel)

        viewModel.onAction(HangoutPaymentsAction.OnDismissPaymentCheckout)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertThat(state.paymentCheckoutUrl).isNull()
        assertThat(state.isAwaitingPaymentReturn).isFalse()
        assertThat(state.isVerifyingPayment).isFalse()
    }

    @Test
    fun `a payment still pending keeps waiting and says so`() = runTest {
        paymentService.paymentStatusToReturn = PaymentStatus.PENDING
        val viewModel = createViewModel()
        openCheckout(viewModel)

        viewModel.events.test {
            viewModel.onAction(HangoutPaymentsAction.OnDismissPaymentCheckout)
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(HangoutPaymentsEvent.PaymentPending)
            assertThat(viewModel.state.value.isAwaitingPaymentReturn).isTrue()
        }
    }

    @Test
    fun `an abandoned payment stops waiting and says it did not go through`() = runTest {
        paymentService.paymentStatusToReturn = PaymentStatus.ABANDONED
        val viewModel = createViewModel()
        openCheckout(viewModel)

        viewModel.events.test {
            viewModel.onAction(HangoutPaymentsAction.OnDismissPaymentCheckout)
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(HangoutPaymentsEvent.PaymentNotCompleted)
            assertThat(viewModel.state.value.isAwaitingPaymentReturn).isFalse()
        }
    }

    @Test
    fun `checking the payment by hand confirms it went through`() = runTest {
        val viewModel = createViewModel()
        openCheckout(viewModel)

        viewModel.onAction(HangoutPaymentsAction.OnCheckPaymentClick)
        advanceUntilIdle()

        assertThat(viewModel.state.value.isAwaitingPaymentReturn).isFalse()
        assertThat(viewModel.state.value.isVerifyingPayment).isFalse()
    }

    @Test
    fun `backing out of the pay confirmation drops the quote`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(HangoutPaymentsAction.OnPayClick)
        advanceUntilIdle()

        viewModel.onAction(HangoutPaymentsAction.OnDismissPayConfirmSheet)

        assertThat(viewModel.state.value.paymentQuote).isNull()
        assertThat(viewModel.state.value.isAwaitingPaymentReturn).isFalse()
    }

    @Test
    fun `the deadline pickers and sheets open and close`() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(HangoutPaymentsAction.OnPaymentDeadlinePickerClick)
        assertThat(viewModel.state.value.isPaymentDeadlinePickerOpen).isTrue()
        viewModel.onAction(HangoutPaymentsAction.OnDismissPaymentDeadlinePicker)
        assertThat(viewModel.state.value.isPaymentDeadlinePickerOpen).isFalse()

        viewModel.onAction(HangoutPaymentsAction.OnChangeDeadlineClick)
        viewModel.onAction(HangoutPaymentsAction.OnDismissDeadlinePicker)
        assertThat(viewModel.state.value.pendingDeadlineChange).isNull()

        viewModel.onAction(HangoutPaymentsAction.OnDeadlineDecisionClick)
        assertThat(viewModel.state.value.isDeadlineDecisionSheetOpen).isTrue()
        viewModel.onAction(HangoutPaymentsAction.OnDismissDeadlineDecisionSheet)
        assertThat(viewModel.state.value.isDeadlineDecisionSheetOpen).isFalse()
    }

    @Test
    fun `closing the bank picker clears what was typed in it`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAction(HangoutPaymentsAction.OnCollectPaymentsToggled(isOn = true))
        viewModel.onAction(HangoutPaymentsAction.OnBankPickerClick)
        advanceUntilIdle()
        viewModel.state.value.bankSearchState.typeText("gt")
        advanceTimeBy(201.milliseconds)
        advanceUntilIdle()

        viewModel.onAction(HangoutPaymentsAction.OnDismissBankPicker)
        advanceUntilIdle()

        // The filtered list refills on the next frame, which a test has none of, so only the field is checked here
        assertThat(viewModel.state.value.isBankPickerOpen).isFalse()
        assertThat(viewModel.state.value.bankSearchState.text.toString()).isEqualTo("")
    }

    @Test
    fun `selecting another hangout resets the form and any payment in progress`() = runTest {
        val otherId = createHangout()
        val viewModel = createViewModel()
        fillValidForm(viewModel)
        openCheckout(viewModel)

        viewModel.onAction(HangoutPaymentsAction.OnSelectHangout(otherId))

        val state = viewModel.state.value
        assertThat(state.isCollectPaymentsOn).isFalse()
        assertThat(state.selectedBank).isNull()
        assertThat(state.paymentCheckoutUrl).isNull()
        assertThat(state.isAwaitingPaymentReturn).isFalse()
    }

    // The real in-memory store fed by the fake service, holding the hangout being paid for
    private suspend fun TestScope.createViewModel(): HangoutPaymentsViewModel {
        val hangoutId = createHangout()
        val repository = InMemoryHangoutDetailRepository(hangoutService, sessionStorage, backgroundScope)
        runCurrent()
        val viewModel = HangoutPaymentsViewModel(repository, paymentService)
        collectInBackground(viewModel.state)
        viewModel.onAction(HangoutPaymentsAction.OnSelectHangout(hangoutId))
        repository.refreshHangout(hangoutId)
        advanceUntilIdle()
        return viewModel
    }

    private fun TestScope.fillValidForm(
        viewModel: HangoutPaymentsViewModel,
        totalCost: String = "5000",
        deadline: LocalDate = validDeadline
    ) {
        viewModel.onAction(HangoutPaymentsAction.OnCollectPaymentsToggled(isOn = true))
        advanceUntilIdle()
        viewModel.state.value.totalCostState.typeText(totalCost)
        viewModel.onAction(HangoutPaymentsAction.OnPaymentDeadlineSelected(deadline))
        viewModel.onAction(HangoutPaymentsAction.OnBankSelected("058"))
        viewModel.state.value.accountNumberState.typeText(ACCOUNT_NUMBER)
        advanceTimeBy(801.milliseconds)
        advanceUntilIdle()
    }

    private fun TestScope.openCheckout(viewModel: HangoutPaymentsViewModel) {
        viewModel.onAction(HangoutPaymentsAction.OnPayClick)
        advanceUntilIdle()
        viewModel.onAction(HangoutPaymentsAction.OnConfirmPayment)
    }

    private suspend fun createHangout(): String {
        sessionStorage.set(
            AuthInfo(
                accessToken = "access",
                refreshToken = "refresh",
                user = User.Authenticated(
                    id = HOST_ID,
                    provider = AuthProvider.GOOGLE,
                    email = "host@test.com",
                    displayName = "Ada",
                    username = "ada"
                )
            )
        )
        hangoutService.currentUserId = HOST_ID
        hangoutService.createHangout(
            name = "Night Out",
            description = null,
            vibe = HangoutVibe.CHILL,
            scheduledAt = Instant.fromEpochMilliseconds(4102444800000L),
            maxAttendees = null,
            spotId = null
        )
        return hangoutService.hangouts.last().id
    }

    private companion object {
        const val HOST_ID = "host_1"
        const val ACCOUNT_NUMBER = "0123456789"
    }
}
