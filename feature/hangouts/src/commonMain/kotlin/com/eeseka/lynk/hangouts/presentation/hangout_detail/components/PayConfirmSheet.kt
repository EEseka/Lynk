package com.eeseka.lynk.hangouts.presentation.hangout_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButton
import com.eeseka.lynk.shared.design_system.components.buttons.LynkButtonStyle
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkAdaptiveSheet
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.hangouts.presentation.util.toNairaString
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.payment_confirm_action
import lynk.feature.hangouts.generated.resources.payment_confirm_charge
import lynk.feature.hangouts.generated.resources.payment_confirm_note
import lynk.feature.hangouts.generated.resources.payment_confirm_title
import lynk.feature.hangouts.generated.resources.payment_your_share
import org.jetbrains.compose.resources.stringResource

@Composable
fun PayConfirmSheet(
    shareLabel: String,
    chargeLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LynkAdaptiveSheet(onDismissRequest = onDismiss) {
        PayConfirmSheetContent(
            shareLabel = shareLabel,
            chargeLabel = chargeLabel,
            onConfirm = onConfirm,
            modifier = modifier
        )
    }
}

@Composable
private fun PayConfirmSheetContent(
    shareLabel: String,
    chargeLabel: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        LynkText(
            text = stringResource(Res.string.payment_confirm_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AmountRow(
                label = stringResource(Res.string.payment_your_share),
                value = shareLabel,
                isEmphasised = false
            )
            AmountRow(
                label = stringResource(Res.string.payment_confirm_charge),
                value = chargeLabel,
                isEmphasised = true
            )
        }

        LynkText(
            text = stringResource(Res.string.payment_confirm_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LynkButton(
            text = stringResource(Res.string.payment_confirm_action),
            onClick = onConfirm,
            style = LynkButtonStyle.PRIMARY,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        )
    }
}

@Composable
private fun AmountRow(
    label: String,
    value: String,
    isEmphasised: Boolean,
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
            style = if (isEmphasised) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@PreviewLightDark
@Composable
private fun PayConfirmSheetPreview() {
    LynkTheme {
        PayConfirmSheetContent(
            shareLabel = 300_000L.toNairaString(),
            chargeLabel = 314_721L.toNairaString(),
            onConfirm = {},
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(vertical = 20.dp)
        )
    }
}
