package com.eeseka.lynk.shared.presentation.util

private const val HEX_DIGITS = "0123456789ABCDEF"

fun buildMailtoUri(
    email: String,
    subject: String,
    body: String
): String = "mailto:$email" + "?subject=${subject.percentEncoded()}" + "&body=${body.percentEncoded()}"

// Letters, digits and - . _ ~ pass through; every other UTF-8 byte becomes %XX.
// A space must be %20, not +, because mail apps show a + literally
private fun String.percentEncoded(): String = buildString {
    this@percentEncoded.encodeToByteArray().forEach { byte ->
        val code = byte.toInt() and 0xFF
        val char = code.toChar()
        if (char in 'A'..'Z' || char in 'a'..'z' || char in '0'..'9' || char in "-._~") {
            append(char)
        } else {
            append('%')
            append(HEX_DIGITS[code shr 4])
            append(HEX_DIGITS[code and 0x0F])
        }
    }
}
