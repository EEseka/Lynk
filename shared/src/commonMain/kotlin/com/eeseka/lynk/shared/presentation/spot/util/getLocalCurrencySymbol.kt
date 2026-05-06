package com.eeseka.lynk.shared.presentation.spot.util

expect fun getLocalCurrencySymbol(): String

/** Utility to generate "$", "$$", "₦₦₦", etc. */
fun getPriceLevelSymbol(tier: Int): String {
    if (tier <= 0) return ""
    return getLocalCurrencySymbol().repeat(tier)
}