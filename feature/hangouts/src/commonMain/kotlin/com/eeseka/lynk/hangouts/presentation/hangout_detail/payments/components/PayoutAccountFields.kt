package com.eeseka.lynk.hangouts.presentation.hangout_detail.payments.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Building2
import com.composables.icons.lucide.CircleCheck
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.hangouts.presentation.util.accountNumberInput
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.textfields.LynkTextField
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.design_system.theme.extended
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.payment_account_number_label
import lynk.feature.hangouts.generated.resources.payment_account_number_placeholder
import lynk.feature.hangouts.generated.resources.payment_bank_label
import lynk.feature.hangouts.generated.resources.payment_bank_placeholder
import org.jetbrains.compose.resources.stringResource

@Composable
fun PayoutAccountFields(
    accountNumberState: TextFieldState,
    selectedBankName: String?,
    bankError: String?,
    resolvedAccountHolderName: String?,
    accountError: String?,
    isResolvingAccount: Boolean,
    isEnabled: Boolean,
    onBankPickerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        PickerRow(
            label = stringResource(Res.string.payment_bank_label),
            value = selectedBankName,
            placeholder = stringResource(Res.string.payment_bank_placeholder),
            errorMessage = bankError,
            leadingIcon = Lucide.Building2,
            enabled = isEnabled,
            onClick = onBankPickerClick
        )

        LynkTextField(
            state = accountNumberState,
            label = stringResource(Res.string.payment_account_number_label),
            placeholder = stringResource(Res.string.payment_account_number_placeholder),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            inputTransformation = accountNumberInput,
            errorMessage = accountError,
            enabled = isEnabled && selectedBankName != null,
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

        resolvedAccountHolderName?.let { name ->
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
    }
}

@PreviewLightDark
@Composable
private fun PayoutAccountFieldsPreview() {
    LynkTheme {
        PayoutAccountFields(
            accountNumberState = TextFieldState("0123456789"),
            selectedBankName = "Guaranty Trust Bank",
            bankError = null,
            resolvedAccountHolderName = "EMMANUEL ESEKA",
            accountError = null,
            isResolvingAccount = false,
            isEnabled = true,
            onBankPickerClick = {},
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        )
    }
}
