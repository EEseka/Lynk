package com.eeseka.lynk.hangouts.presentation.mappers

import com.eeseka.lynk.shared.domain.payment.model.Bank
import com.eeseka.lynk.hangouts.presentation.model.BankUi

fun Bank.toBankUi() = BankUi(
    code = code,
    name = name,
    logoUrl = logoUrl,
    initials = name.toBankInitials()
)

private fun String.toBankInitials(): String {
    val words = trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() && !it.equals("bank", ignoreCase = true) }

    return when {
        words.isEmpty() -> take(2).uppercase()
        words.size == 1 -> words.first().take(2).uppercase()
        else -> "${words[0].first()}${words[1].first()}".uppercase()
    }
}