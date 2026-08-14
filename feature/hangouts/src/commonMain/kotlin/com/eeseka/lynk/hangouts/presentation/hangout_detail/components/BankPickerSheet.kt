package com.eeseka.lynk.hangouts.presentation.hangout_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.eeseka.lynk.hangouts.presentation.model.BankUi
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkAdaptiveSheet
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.textfields.LynkSearchField
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.presentation.util.clearFocusOnTap
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.payment_bank_empty
import lynk.feature.hangouts.generated.resources.payment_bank_picker_title
import lynk.feature.hangouts.generated.resources.payment_bank_search_hint
import org.jetbrains.compose.resources.stringResource

@Composable
fun BankPickerSheet(
    banks: List<BankUi>,
    searchState: TextFieldState,
    isLoading: Boolean,
    errorMessage: String?,
    onBankSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LynkAdaptiveSheet(
        onDismissRequest = onDismiss,
        skipBottomSheetPartiallyExpanded = false
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clearFocusOnTap()
                .padding(horizontal = 24.dp)
        ) {
            LynkText(
                text = stringResource(Res.string.payment_bank_picker_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            LynkSearchField(
                state = searchState,
                placeholder = stringResource(Res.string.payment_bank_search_hint),
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp)
            )

            when {
                isLoading -> {
                    BankPickerMessage {
                        LynkProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                errorMessage != null -> {
                    BankPickerMessage {
                        LynkText(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                banks.isEmpty() -> {
                    BankPickerMessage {
                        LynkText(
                            text = stringResource(Res.string.payment_bank_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(items = banks, key = { it.code }) { bank ->
                            BankRow(
                                bank = bank,
                                onClick = { onBankSelected(bank.code) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BankPickerMessage(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        contentAlignment = Alignment.Center,
        content = { content() }
    )
}

@Composable
private fun BankRow(
    bank: BankUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = rememberAppHaptic()
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable {
                hapticFeedback(AppHaptic.ImpactLight)
                onClick()
            }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (bank.logoUrl != null) Color.White else scheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            if (bank.logoUrl != null) {
                AsyncImage(
                    model = bank.logoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LynkText(
                    text = bank.initials,
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant
                )
            }
        }
        LynkText(
            text = bank.name,
            style = MaterialTheme.typography.bodyLarge,
            color = scheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private val previewBanks = listOf(
    BankUi("058", "Guaranty Trust Bank", null, "GT"),
    BankUi("044", "Access Bank", null, "AC"),
    BankUi("033", "United Bank for Africa", null, "UF"),
    BankUi("057", "Zenith Bank", null, "ZE")
)

@PreviewLightDark
@Composable
private fun BankPickerListPreview() {
    LynkTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            previewBanks.forEach { bank ->
                BankRow(bank = bank, onClick = {}, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
