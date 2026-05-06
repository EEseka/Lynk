package com.eeseka.lynk.shared.presentation.spot.util

import platform.Foundation.NSLocale
import platform.Foundation.currencySymbol
import platform.Foundation.currentLocale

actual fun getLocalCurrencySymbol(): String {
    return NSLocale.currentLocale.currencySymbol
}