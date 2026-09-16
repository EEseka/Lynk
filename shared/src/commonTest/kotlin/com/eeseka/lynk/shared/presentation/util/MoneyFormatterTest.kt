package com.eeseka.lynk.shared.presentation.util

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class MoneyFormatterTest {

    @Test
    fun `zero kobo shows as zero naira`() {
        assertThat(0L.toNairaString()).isEqualTo("₦0")
    }

    @Test
    fun `whole naira drop the kobo part`() {
        assertThat(100L.toNairaString()).isEqualTo("₦1")
        assertThat(150_000L.toNairaString()).isEqualTo("₦1,500")
    }

    @Test
    fun `naira are grouped in threes`() {
        assertThat(100_000_000L.toNairaString()).isEqualTo("₦1,000,000")
    }

    @Test
    fun `kobo show as two digits after the point`() {
        assertThat(123_456_789L.toNairaString()).isEqualTo("₦1,234,567.89")
        assertThat(150_005L.toNairaString()).isEqualTo("₦1,500.05")
    }

    @Test
    fun `less than one naira keeps a leading zero`() {
        assertThat(5L.toNairaString()).isEqualTo("₦0.05")
    }
}
