package com.eeseka.lynk.hangouts.presentation.hangout_detail.payments.components

import androidx.compose.runtime.Composable

@Composable
expect fun PaymentCheckout(
    url: String,
    onDismiss: () -> Unit
)
