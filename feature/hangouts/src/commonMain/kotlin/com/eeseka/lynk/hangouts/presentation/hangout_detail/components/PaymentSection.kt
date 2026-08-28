package com.eeseka.lynk.hangouts.presentation.hangout_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.CalendarClock
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.hangouts.presentation.util.toDeadlineDisplayDate
import com.eeseka.lynk.shared.presentation.util.toNairaString
import com.eeseka.lynk.hangouts.presentation.util.toUiText
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButtonStyle
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCard
import com.eeseka.lynk.shared.design_system.components.layouts.LynkCardStyle
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.design_system.theme.extended
import com.eeseka.lynk.shared.domain.hangout.model.PaymentState
import com.eeseka.lynk.shared.presentation.hangout.model.HangoutPaymentUi
import kotlin.time.Instant
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.payment_change_deadline
import lynk.feature.hangouts.generated.resources.payment_check_action
import lynk.feature.hangouts.generated.resources.payment_checking
import lynk.feature.hangouts.generated.resources.payment_decide_action
import lynk.feature.hangouts.generated.resources.payment_pay_action
import lynk.feature.hangouts.generated.resources.payment_pay_by
import lynk.feature.hangouts.generated.resources.payment_payout_on
import lynk.feature.hangouts.generated.resources.payment_preparing
import lynk.feature.hangouts.generated.resources.payment_retry_payout
import lynk.feature.hangouts.generated.resources.payment_retrying_payout
import lynk.feature.hangouts.generated.resources.payment_section_title
import lynk.feature.hangouts.generated.resources.payment_total
import lynk.feature.hangouts.generated.resources.payment_you_paid
import lynk.feature.hangouts.generated.resources.payment_your_share
import org.jetbrains.compose.resources.stringResource

@Composable
fun PaymentSection(
    payment: HangoutPaymentUi,
    isHost: Boolean,
    hasUnpaidGuests: Boolean = false,
    hasCurrentUserPaid: Boolean = false,
    canPay: Boolean = false,
    isInitializingPayment: Boolean = false,
    isAwaitingPaymentReturn: Boolean = false,
    isVerifyingPayment: Boolean = false,
    canChangeDeadline: Boolean = false,
    needsDeadlineDecision: Boolean = false,
    canRetryPayout: Boolean = false,
    isRetryingPayout: Boolean = false,
    onPayClick: () -> Unit = {},
    onCheckPaymentClick: () -> Unit = {},
    onChangeDeadlineClick: () -> Unit = {},
    onDecideClick: () -> Unit = {},
    onRetryPayoutClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val hapticFeedback = rememberAppHaptic()

    DetailSection(
        title = stringResource(Res.string.payment_section_title),
        trailing = if (canChangeDeadline) {
            {
                Row(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .clickable {
                            hapticFeedback(AppHaptic.ImpactLight)
                            onChangeDeadlineClick()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LynkText(
                        text = stringResource(Res.string.payment_change_deadline),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Icon(
                        imageVector = Lucide.CalendarClock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else null,
        modifier = modifier
    ) {
        LynkCard(
            style = LynkCardStyle.OUTLINED,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isHost) {
                    PaymentRow(
                        label = stringResource(Res.string.payment_total),
                        value = payment.totalCostKobo.toNairaString()
                    )
                } else {
                    PaymentRow(
                        label = stringResource(Res.string.payment_your_share),
                        value = payment.costPerPersonKobo.toNairaString()
                    )
                }

                val awaitingPayout = !hasUnpaidGuests &&
                        (payment.state == PaymentState.COLLECTING ||
                                payment.state == PaymentState.AWAITING_HOST_DECISION)
                val statusState = if (awaitingPayout) PaymentState.READY_FOR_PAYOUT else payment.state

                if (payment.state == PaymentState.COLLECTING) {
                    PaymentRow(
                        label = if (awaitingPayout) {
                            stringResource(Res.string.payment_payout_on)
                        } else {
                            stringResource(Res.string.payment_pay_by)
                        },
                        value = payment.deadline.toDeadlineDisplayDate()
                    )
                }

                if (hasCurrentUserPaid && !isHost) {
                    YouHavePaidRow()
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                LynkText(
                    text = statusState.toUiText(isHost).asString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (statusState == PaymentState.PAYOUT_FAILED && isHost) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                if (needsDeadlineDecision) {
                    LynkButton(
                        text = stringResource(Res.string.payment_decide_action),
                        onClick = onDecideClick,
                        style = LynkButtonStyle.SECONDARY,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (canRetryPayout) {
                    LynkButton(
                        text = stringResource(Res.string.payment_retry_payout),
                        onClick = onRetryPayoutClick,
                        style = LynkButtonStyle.SECONDARY,
                        isLoading = isRetryingPayout,
                        loadingText = stringResource(Res.string.payment_retrying_payout),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (isAwaitingPaymentReturn) {
                    LynkButton(
                        text = stringResource(Res.string.payment_check_action),
                        onClick = onCheckPaymentClick,
                        style = LynkButtonStyle.SECONDARY,
                        isLoading = isVerifyingPayment,
                        loadingText = stringResource(Res.string.payment_checking),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (canPay) {
                    LynkButton(
                        text = stringResource(Res.string.payment_pay_action),
                        onClick = onPayClick,
                        style = LynkButtonStyle.PRIMARY,
                        isLoading = isInitializingPayment,
                        loadingText = stringResource(Res.string.payment_preparing),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun YouHavePaidRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Lucide.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.extended.success,
            modifier = Modifier.size(16.dp)
        )
        LynkText(
            text = stringResource(Res.string.payment_you_paid),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.extended.success
        )
    }
}

@Composable
private fun PaymentRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LynkText(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LynkText(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PaymentSectionPreview(
    paymentState: PaymentState = PaymentState.COLLECTING,
    isHost: Boolean = true,
    hasUnpaidGuests: Boolean = true,
    hasCurrentUserPaid: Boolean = false,
    canPay: Boolean = false,
    isAwaitingPaymentReturn: Boolean = false,
    canChangeDeadline: Boolean = false,
    needsDeadlineDecision: Boolean = false,
    canRetryPayout: Boolean = false
) {
    LynkTheme {
        PaymentSection(
            payment = HangoutPaymentUi(
                totalCostKobo = 2_400_000L,
                costPerPersonKobo = 300_000L,
                splitHeadcount = 8,
                deadline = Instant.fromEpochSeconds(1_799_000_000L),
                state = paymentState
            ),
            isHost = isHost,
            hasUnpaidGuests = hasUnpaidGuests,
            hasCurrentUserPaid = hasCurrentUserPaid,
            canPay = canPay,
            isAwaitingPaymentReturn = isAwaitingPaymentReturn,
            canChangeDeadline = canChangeDeadline,
            needsDeadlineDecision = needsDeadlineDecision,
            canRetryPayout = canRetryPayout,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        )
    }
}

@PreviewLightDark
@Composable
private fun PaymentSectionHostCollectingPreview() = PaymentSectionPreview(
    canChangeDeadline = true
)

@PreviewLightDark
@Composable
private fun PaymentSectionGuestCanPayPreview() = PaymentSectionPreview(
    isHost = false,
    canPay = true
)

@PreviewLightDark
@Composable
private fun PaymentSectionGuestConfirmingPreview() = PaymentSectionPreview(
    isHost = false,
    isAwaitingPaymentReturn = true
)

@PreviewLightDark
@Composable
private fun PaymentSectionAwaitingDecisionPreview() = PaymentSectionPreview(
    paymentState = PaymentState.AWAITING_HOST_DECISION,
    needsDeadlineDecision = true
)

@PreviewLightDark
@Composable
private fun PaymentSectionPayoutFailedPreview() = PaymentSectionPreview(
    paymentState = PaymentState.PAYOUT_FAILED,
    canRetryPayout = true
)

@PreviewLightDark
@Composable
private fun PaymentSectionPaidOutPreview() = PaymentSectionPreview(
    paymentState = PaymentState.PAID_OUT
)

@PreviewLightDark
@Composable
private fun PaymentSectionHostAllPaidPreview() = PaymentSectionPreview(
    hasUnpaidGuests = false,
    canChangeDeadline = true
)

@PreviewLightDark
@Composable
private fun PaymentSectionGuestHasPaidPreview() = PaymentSectionPreview(
    isHost = false,
    hasCurrentUserPaid = true
)

@PreviewLightDark
@Composable
private fun PaymentSectionGuestAllPaidPreview() = PaymentSectionPreview(
    isHost = false,
    hasUnpaidGuests = false,
    hasCurrentUserPaid = true
)
