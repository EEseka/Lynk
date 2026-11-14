package com.eeseka.lynk.core.presentation.spot.util

import java.util.Currency
import java.util.Locale

actual fun getLocalCurrencySymbol(): String {
    return try {
        Currency.getInstance(Locale.getDefault()).symbol
    } catch (_: Exception) {
        "$"
    }
}