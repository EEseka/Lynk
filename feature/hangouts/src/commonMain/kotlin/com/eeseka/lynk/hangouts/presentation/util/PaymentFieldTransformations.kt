package com.eeseka.lynk.hangouts.presentation.util

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.foundation.text.input.then
import com.eeseka.lynk.shared.domain.payment.PaymentConstants.NUBAN_LENGTH

// 999999999.99 — far past any hangout bill, and short enough that the amount can never overflow.
private const val MAX_AMOUNT_LENGTH = 12

val accountNumberInput = InputTransformation { keepDigitsOnly() }
    .then(InputTransformation.maxLength(NUBAN_LENGTH))

val amountInput = InputTransformation { keepAmountCharactersOnly() }
    .then(InputTransformation.maxLength(MAX_AMOUNT_LENGTH))

val amountOutput = OutputTransformation { groupWholeNaira() }

private fun TextFieldBuffer.keepDigitsOnly() {
    val original = asCharSequence().toString()
    val digitsOnly = original.filter { it.isDigit() }
    if (digitsOnly != original) replace(0, length, digitsOnly)
}

private fun TextFieldBuffer.keepAmountCharactersOnly() {
    val original = asCharSequence().toString()
    val cleaned = buildString {
        var hasDecimalPoint = false
        original.forEach { character ->
            when {
                character.isDigit() -> append(character)
                character == '.' && !hasDecimalPoint -> {
                    hasDecimalPoint = true
                    append(character)
                }
            }
        }
    }
    if (cleaned != original) replace(0, length, cleaned)
}

private fun TextFieldBuffer.groupWholeNaira() {
    val decimalPointIndex = asCharSequence().indexOf('.')
    val wholeNairaLength = if (decimalPointIndex == -1) length else decimalPointIndex

    // Walk right to left so every comma lands before the ones already added. A zero-length
    // replace is an insert, and it leaves the digits either side mapped for the cursor.
    var insertAt = wholeNairaLength - 3
    while (insertAt > 0) {
        replace(insertAt, insertAt, ",")
        insertAt -= 3
    }
}
