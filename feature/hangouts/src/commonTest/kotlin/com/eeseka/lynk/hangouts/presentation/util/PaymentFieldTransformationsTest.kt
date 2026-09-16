package com.eeseka.lynk.hangouts.presentation.util

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldState
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class PaymentFieldTransformationsTest {

    @Test
    fun `account number keeps digits only`() {
        assertThat(type(accountNumberInput, before = "", typed = "01a2-3 4")).isEqualTo("01234")
    }

    @Test
    fun `account number stops at ten digits`() {
        assertThat(type(accountNumberInput, before = "0123456789", typed = "0")).isEqualTo("0123456789")
    }

    @Test
    fun `amount drops letters and symbols`() {
        assertThat(type(amountInput, before = "", typed = "₦1,5a00")).isEqualTo("1500")
    }

    @Test
    fun `amount keeps only the first decimal point`() {
        assertThat(type(amountInput, before = "", typed = "1.5.0")).isEqualTo("1.50")
    }

    @Test
    fun `amount keeps at most two kobo digits`() {
        assertThat(type(amountInput, before = "", typed = "1500.999")).isEqualTo("1500.99")
    }

    @Test
    fun `amount stops at twelve characters`() {
        assertThat(type(amountInput, before = "999999999999", typed = "9")).isEqualTo("999999999999")
    }

    @Test
    fun `amount shows whole naira grouped in threes`() {
        assertThat(display("1")).isEqualTo("1")
        assertThat(display("1500")).isEqualTo("1,500")
        assertThat(display("1234567")).isEqualTo("1,234,567")
        assertThat(display("999999999")).isEqualTo("999,999,999")
    }

    @Test
    fun `amount display leaves the kobo part alone`() {
        assertThat(display("1234.56")).isEqualTo("1,234.56")
        assertThat(display("100.5")).isEqualTo("100.5")
        assertThat(display(".5")).isEqualTo(".5")
    }

    // Runs the filter the way a text field does when the user types: on the edit, with the old text to fall back to
    private fun type(transformation: InputTransformation, before: String, typed: String): String {
        val state = TextFieldState(initialText = before)
        state.edit {
            append(typed)
            with(transformation) { transformInput() }
        }
        return state.text.toString()
    }

    private fun display(stored: String): String {
        val state = TextFieldState(initialText = stored)
        state.edit {
            with(amountOutput) { transformOutput() }
        }
        return state.text.toString()
    }
}
