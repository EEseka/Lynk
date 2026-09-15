package com.eeseka.lynk.hangouts.presentation.hangout_detail.payments.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.hangouts.presentation.hangout_detail.components.DetailSection
import com.eeseka.lynk.hangouts.presentation.hangout_detail.payments.HangoutPaymentsAction
import com.eeseka.lynk.hangouts.presentation.hangout_detail.payments.HangoutPaymentsState
import com.eeseka.lynk.hangouts.presentation.model.BankUi
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.toggles_and_control.LynkSwitch
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.presentation.util.toDateLabel
import kotlinx.datetime.LocalDate
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.payment_collect_hint
import lynk.feature.hangouts.generated.resources.payment_collect_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun CollectPaymentsSetup(
    state: HangoutPaymentsState,
    onAction: (HangoutPaymentsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = rememberAppHaptic()

    DetailSection(
        title = stringResource(Res.string.payment_collect_title),
        modifier = modifier,
        trailing = {
            LynkSwitch(
                checked = state.isCollectPaymentsOn,
                onCheckedChange = { isOn ->
                    hapticFeedback(AppHaptic.Selection)
                    onAction(HangoutPaymentsAction.OnCollectPaymentsToggled(isOn))
                },
                // Switching off wipes the form, which must not happen while it is being submitted.
                enabled = !state.isEnablingPayments
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            LynkText(
                text = stringResource(Res.string.payment_collect_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AnimatedVisibility(visible = state.isCollectPaymentsOn) {
                EnablePaymentsForm(
                    totalCostState = state.totalCostState,
                    totalCostError = state.totalCostError?.asString(),
                    accountNumberState = state.accountNumberState,
                    deadlineLabel = state.paymentDeadlineDate?.toDateLabel(),
                    deadlineError = state.paymentDeadlineError?.asString(),
                    selectedBankName = state.selectedBank?.name,
                    bankError = state.bankError?.asString(),
                    resolvedAccountName = state.resolvedAccountName,
                    accountError = state.accountResolutionError?.asString() ?: state.accountNumberError?.asString(),
                    isResolvingAccount = state.isResolvingAccount,
                    isEnabling = state.isEnablingPayments,
                    canConfirm = state.canEnablePayments,
                    onDeadlineClick = { onAction(HangoutPaymentsAction.OnPaymentDeadlinePickerClick) },
                    onBankPickerClick = { onAction(HangoutPaymentsAction.OnBankPickerClick) },
                    onConfirm = { onAction(HangoutPaymentsAction.OnEnablePaymentsConfirmed) },
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun CollectPaymentsSetupOffPreview() {
    LynkTheme {
        CollectPaymentsSetup(
            state = HangoutPaymentsState(),
            onAction = {},
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        )
    }
}

@PreviewLightDark
@Composable
private fun CollectPaymentsSetupOnPreview() {
    LynkTheme {
        CollectPaymentsSetup(
            state = HangoutPaymentsState(
                isCollectPaymentsOn = true,
                paymentDeadlineDate = LocalDate(2026, 9, 12),
                selectedBank = BankUi("058", "Guaranty Trust Bank", null, "GT"),
                resolvedAccountName = "EMMANUEL ESEKA"
            ),
            onAction = {},
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        )
    }
}
