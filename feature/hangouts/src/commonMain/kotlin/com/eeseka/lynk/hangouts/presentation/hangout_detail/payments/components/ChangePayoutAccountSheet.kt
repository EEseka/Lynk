package com.eeseka.lynk.hangouts.presentation.hangout_detail.payments.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.hangouts.presentation.model.PayoutAccountUi
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkAdaptiveSheet
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.presentation.util.clearFocusOnTap
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.payment_change_account
import lynk.feature.hangouts.generated.resources.payment_change_account_confirm
import lynk.feature.hangouts.generated.resources.payment_change_account_hint
import lynk.feature.hangouts.generated.resources.payment_changing_account
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChangePayoutAccountSheet(
    currentAccount: PayoutAccountUi?,
    accountNumberState: TextFieldState,
    selectedBankName: String?,
    bankError: String?,
    resolvedAccountHolderName: String?,
    accountError: String?,
    isResolvingAccount: Boolean,
    isChanging: Boolean,
    onBankPickerClick: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LynkAdaptiveSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        ChangePayoutAccountSheetContent(
            currentAccount = currentAccount,
            accountNumberState = accountNumberState,
            selectedBankName = selectedBankName,
            bankError = bankError,
            resolvedAccountHolderName = resolvedAccountHolderName,
            accountError = accountError,
            isResolvingAccount = isResolvingAccount,
            isChanging = isChanging,
            onBankPickerClick = onBankPickerClick,
            onConfirm = onConfirm
        )
    }
}

@Composable
private fun ChangePayoutAccountSheetContent(
    currentAccount: PayoutAccountUi?,
    accountNumberState: TextFieldState,
    selectedBankName: String?,
    bankError: String?,
    resolvedAccountHolderName: String?,
    accountError: String?,
    isResolvingAccount: Boolean,
    isChanging: Boolean,
    onBankPickerClick: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = rememberAppHaptic()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clearFocusOnTap()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            LynkText(
                text = stringResource(Res.string.payment_change_account),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            LynkText(
                text = stringResource(Res.string.payment_change_account_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        currentAccount?.let { account ->
            PayoutAccountRow(account = account)
        }

        PayoutAccountFields(
            accountNumberState = accountNumberState,
            selectedBankName = selectedBankName,
            bankError = bankError,
            resolvedAccountHolderName = resolvedAccountHolderName,
            accountError = accountError,
            isResolvingAccount = isResolvingAccount,
            isEnabled = !isChanging,
            onBankPickerClick = onBankPickerClick
        )

        LynkButton(
            text = stringResource(Res.string.payment_change_account_confirm),
            onClick = {
                hapticFeedback(AppHaptic.ImpactMedium)
                onConfirm()
            },
            enabled = !isResolvingAccount,
            isLoading = isChanging,
            loadingText = stringResource(Res.string.payment_changing_account),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@PreviewLightDark
@Composable
private fun ChangePayoutAccountSheetPreview() {
    LynkTheme {
        ChangePayoutAccountSheetContent(
            currentAccount = PayoutAccountUi("Access Bank", "4321", "EMMANUEL ESEKA"),
            accountNumberState = TextFieldState("0123456789"),
            selectedBankName = "Guaranty Trust Bank",
            bankError = null,
            resolvedAccountHolderName = "EMMANUEL ESEKA",
            accountError = null,
            isResolvingAccount = false,
            isChanging = false,
            onBankPickerClick = {},
            onConfirm = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}
