package com.eeseka.lynk.hangouts.presentation.mappers

import com.eeseka.lynk.hangouts.presentation.model.BankUi
import com.eeseka.lynk.shared.domain.payment.model.Bank

fun Bank.toBankUi() = BankUi(
    code = code,
    name = name,
    logoUrl = logoUrl,
    initials = name.toBankInitials()
)

// Left out of the initials so "First Bank of Nigeria" reads FN, not FO.
private val wordsSkippedInInitials = setOf("bank", "of", "for", "and", "the", "&")

private fun String.toBankInitials(): String {
    val words = trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() && it.lowercase() !in wordsSkippedInInitials }

    return when {
        words.isEmpty() -> take(2).uppercase()
        words.size == 1 -> words.first().take(2).uppercase()
        else -> "${words[0].first()}${words[1].first()}".uppercase()
    }
}