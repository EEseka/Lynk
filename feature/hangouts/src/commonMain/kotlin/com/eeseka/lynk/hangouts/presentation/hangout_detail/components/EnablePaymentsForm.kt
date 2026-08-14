package com.eeseka.lynk.hangouts.presentation.hangout_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Building2
import com.composables.icons.lucide.CalendarDays
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.CircleCheck
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.hangouts.presentation.util.NAIRA_SYMBOL
import com.eeseka.lynk.hangouts.presentation.util.accountNumberInput
import com.eeseka.lynk.hangouts.presentation.util.amountInput
import com.eeseka.lynk.hangouts.presentation.util.amountOutput
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButtonStyle
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.textfields.LynkTextField
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.design_system.theme.extended
import com.eeseka.lynk.shared.presentation.util.clearFocusOnTap
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.payment_account_number_label
import lynk.feature.hangouts.generated.resources.payment_account_number_placeholder
import lynk.feature.hangouts.generated.resources.payment_bank_label
import lynk.feature.hangouts.generated.resources.payment_bank_placeholder
import lynk.feature.hangouts.generated.resources.payment_deadline_label
import lynk.feature.hangouts.generated.resources.payment_deadline_placeholder
import lynk.feature.hangouts.generated.resources.payment_enable_confirm
import lynk.feature.hangouts.generated.resources.payment_enable_warning
import lynk.feature.hangouts.generated.resources.payment_enabling
import lynk.feature.hangouts.generated.resources.payment_total_cost_helper
import lynk.feature.hangouts.generated.resources.payment_total_cost_label
import lynk.feature.hangouts.generated.resources.payment_total_cost_placeholder
import org.jetbrains.compose.resources.stringResource

@Composable
fun EnablePaymentsForm(
    totalCostState: TextFieldState,
    totalCostError: String?,
    accountNumberState: TextFieldState,
    deadlineLabel: String?,
    deadlineError: String?,
    selectedBankName: String?,
    resolvedAccountName: String?,
    accountError: String?,
    isResolvingAccount: Boolean,
    isEnabling: Boolean,
    canConfirm: Boolean,
    onDeadlineClick: () -> Unit,
    onBankPickerClick: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().clearFocusOnTap(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        LynkTextField(
            state = totalCostState,
            label = stringResource(Res.string.payment_total_cost_label),
            placeholder = stringResource(Res.string.payment_total_cost_placeholder),
            helperText = stringResource(Res.string.payment_total_cost_helper),
            errorMessage = totalCostError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            inputTransformation = amountInput,
            outputTransformation = amountOutput,
            enabled = !isEnabling,
            leadingIcon = {
                LynkText(
                    text = NAIRA_SYMBOL,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        PickerRow(
            label = stringResource(Res.string.payment_deadline_label),
            value = deadlineLabel,
            placeholder = stringResource(Res.string.payment_deadline_placeholder),
            errorMessage = deadlineError,
            leadingIcon = Lucide.CalendarDays,
            enabled = !isEnabling,
            onClick = onDeadlineClick
        )

        PickerRow(
            label = stringResource(Res.string.payment_bank_label),
            value = selectedBankName,
            placeholder = stringResource(Res.string.payment_bank_placeholder),
            errorMessage = null,
            leadingIcon = Lucide.Building2,
            enabled = !isEnabling,
            onClick = onBankPickerClick
        )

        LynkTextField(
            state = accountNumberState,
            label = stringResource(Res.string.payment_account_number_label),
            placeholder = stringResource(Res.string.payment_account_number_placeholder),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            inputTransformation = accountNumberInput,
            errorMessage = accountError,
            enabled = !isEnabling && selectedBankName != null,
            trailingIcon = {
                if (isResolvingAccount) {
                    LynkProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        resolvedAccountName?.let { name ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Lucide.CircleCheck,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.extended.success,
                    modifier = Modifier.size(16.dp)
                )
                LynkText(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        LynkText(
            text = stringResource(Res.string.payment_enable_warning),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LynkButton(
            text = stringResource(Res.string.payment_enable_confirm),
            onClick = onConfirm,
            style = LynkButtonStyle.PRIMARY,
            enabled = canConfirm,
            isLoading = isEnabling,
            loadingText = stringResource(Res.string.payment_enabling),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        )
    }
}

@Composable
private fun PickerRow(
    label: String,
    value: String?,
    placeholder: String,
    errorMessage: String?,
    leadingIcon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = rememberAppHaptic()
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LynkText(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = scheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(scheme.surfaceContainerHigh)
                .clickable(enabled = enabled) {
                    hapticFeedback(AppHaptic.ImpactLight)
                    onClick()
                }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            LynkText(
                text = value ?: placeholder,
                style = MaterialTheme.typography.bodyLarge,
                color = if (value == null) scheme.onSurfaceVariant else scheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Lucide.ChevronRight,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
        errorMessage?.let {
            LynkText(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.error
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun EnablePaymentsFormPreview() {
    LynkTheme {
        EnablePaymentsForm(
            totalCostState = TextFieldState("24000"),
            totalCostError = null,
            accountNumberState = TextFieldState("0123456789"),
            deadlineLabel = "12 Sep 2026",
            deadlineError = null,
            selectedBankName = "Guaranty Trust Bank",
            resolvedAccountName = "EMMANUEL ESEKA",
            accountError = null,
            isResolvingAccount = false,
            isEnabling = false,
            canConfirm = true,
            onDeadlineClick = {},
            onBankPickerClick = {},
            onConfirm = {},
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        )
    }
}
