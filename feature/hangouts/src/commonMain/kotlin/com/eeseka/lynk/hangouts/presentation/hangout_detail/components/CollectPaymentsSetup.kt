package com.eeseka.lynk.hangouts.presentation.hangout_detail.components

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
import com.eeseka.lynk.hangouts.presentation.hangout_detail.HangoutDetailAction
import com.eeseka.lynk.hangouts.presentation.hangout_detail.HangoutDetailState
import com.eeseka.lynk.hangouts.presentation.model.BankUi
import com.eeseka.lynk.hangouts.presentation.util.toDeadlineLabel
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.toggles_and_control.LynkSwitch
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import kotlinx.datetime.LocalDate
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.payment_collect_hint
import lynk.feature.hangouts.generated.resources.payment_collect_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun CollectPaymentsSetup(
    state: HangoutDetailState,
    onAction: (HangoutDetailAction) -> Unit,
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
                    onAction(HangoutDetailAction.OnCollectPaymentsToggled(isOn))
                }
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
                    deadlineLabel = state.paymentDeadlineDate?.toDeadlineLabel(),
                    deadlineError = state.paymentDeadlineError?.asString(),
                    selectedBankName = state.selectedBank?.name,
                    resolvedAccountName = state.resolvedAccountName,
                    accountError = state.accountResolutionError?.asString(),
                    isResolvingAccount = state.isResolvingAccount,
                    isEnabling = state.isEnablingPayments,
                    canConfirm = state.canEnablePayments,
                    onDeadlineClick = {
                        onAction(HangoutDetailAction.OnPaymentDeadlinePickerClick)
                    },
                    onBankPickerClick = { onAction(HangoutDetailAction.OnBankPickerClick) },
                    onConfirm = { onAction(HangoutDetailAction.OnEnablePaymentsConfirmed) },
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
            state = HangoutDetailState(),
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
            state = HangoutDetailState(
                isCollectPaymentsOn = true,
                paymentDeadlineDate = LocalDate(2026, 9, 12),
                selectedBank = BankUi("058", "Guaranty Trust Bank", null, "GT"),
                resolvedAccountName = "EMMANUEL ESEKA",
                canEnablePayments = true
            ),
            onAction = {},
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        )
    }
}
