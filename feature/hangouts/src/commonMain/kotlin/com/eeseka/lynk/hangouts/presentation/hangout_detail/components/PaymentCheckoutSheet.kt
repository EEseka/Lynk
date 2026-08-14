package com.eeseka.lynk.hangouts.presentation.hangout_detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.eeseka.lynk.shared.design_system.components.buttons.LynkTonalIconButton
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkAdaptiveSheet
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.mohamedrejeb.calf.ui.web.WebView
import com.mohamedrejeb.calf.ui.web.rememberWebViewState
import lynk.feature.hangouts.generated.resources.Res
import lynk.feature.hangouts.generated.resources.payment_checkout_close
import lynk.feature.hangouts.generated.resources.payment_checkout_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun PaymentCheckoutSheet(
    url: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val webViewState = rememberWebViewState(url = url)
    webViewState.settings.javaScriptEnabled = true

    LynkAdaptiveSheet(
        onDismissRequest = onDismiss,
        isDismissibleByGesture = false,
        modifier = modifier
    ) {
        PaymentCheckoutSheetContent(
            isLoading = webViewState.isLoading,
            onClose = onDismiss
        ) {
            WebView(
                state = webViewState,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun PaymentCheckoutSheetContent(
    isLoading: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    webView: @Composable () -> Unit
) {
    val hapticFeedback = rememberAppHaptic()

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LynkTonalIconButton(
                onClick = {
                    hapticFeedback(AppHaptic.ImpactLight)
                    onClose()
                }
            ) {
                Icon(
                    imageVector = Lucide.X,
                    contentDescription = stringResource(Res.string.payment_checkout_close)
                )
            }

            LynkText(
                text = stringResource(Res.string.payment_checkout_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            webView()

            if (isLoading) {
                LynkProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PaymentCheckoutSheetPreview(isLoading: Boolean) {
    LynkTheme {
        PaymentCheckoutSheetContent(
            isLoading = isLoading,
            onClose = {},
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .height(320.dp),
            webView = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        )
    }
}

@PreviewLightDark
@Composable
private fun PaymentCheckoutSheetLoadingPreview() = PaymentCheckoutSheetPreview(isLoading = true)

@PreviewLightDark
@Composable
private fun PaymentCheckoutSheetLoadedPreview() = PaymentCheckoutSheetPreview(isLoading = false)
