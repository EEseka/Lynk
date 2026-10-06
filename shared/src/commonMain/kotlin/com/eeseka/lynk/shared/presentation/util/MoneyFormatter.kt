package com.eeseka.lynk.shared.presentation.util

// Paystack settles in naira, so this is the currency of the money itself, not of the device.
const val NAIRA_SYMBOL = "₦"

private val CURRENCY_SYMBOLS = mapOf(
    "NGN" to NAIRA_SYMBOL,
    "GHS" to "GH₵",
    "USD" to "$",
    "GBP" to "£",
    "EUR" to "€",
    "KES" to "KSh",
    "ZAR" to "R",
    "JPY" to "¥",
    "CNY" to "CN¥",
    "INR" to "₹",
    "CAD" to "CA$",
    "AUD" to "A$"
)

fun Long.toNairaString(): String {
    val naira = this / 100
    val kobo = this % 100

    val groupedNaira = naira.toGroupedString()

    val koboPart = if (kobo == 0L) "" else ".${kobo.toString().padStart(2, '0')}"

    return "$NAIRA_SYMBOL$groupedNaira$koboPart"
}

fun Long.toCurrencyString(currencyCode: String): String {
    val groupedAmount = toGroupedString()
    val symbol = CURRENCY_SYMBOLS[currencyCode] ?: "$currencyCode "
    return "$symbol$groupedAmount"
}