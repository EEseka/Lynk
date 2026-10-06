package com.eeseka.lynk.shared.presentation.util

import assertk.assertThat
import assertk.assertions.doesNotContain
import assertk.assertions.endsWith
import assertk.assertions.isEqualTo
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Instant

class DateFormatterTest {

    private val currentYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year

    @Test
    fun `a date in another year shows the year`() {
        assertThat(LocalDate(2020, 9, 5).toDateLabel()).isEqualTo("Sat 5 Sep 2020")
    }

    @Test
    fun `a date in the current year leaves the year out`() {
        val label = LocalDate(currentYear, 9, 5).toDateLabel()

        assertThat(label).endsWith("5 Sep")
        assertThat(label).doesNotContain(currentYear.toString())
    }

    @Test
    fun `picker millis are read as a UTC day`() {
        val midnightUtcOnNewYear2100 = 4102444800000L

        assertThat(midnightUtcOnNewYear2100.toPickerDate()).isEqualTo(LocalDate(2100, 1, 1))
    }

    @Test
    fun `a date survives a round trip through picker millis`() {
        val date = LocalDate(2030, 12, 31)

        assertThat(date.toPickerMillis().toPickerDate()).isEqualTo(date)
    }

    // A café in Kenya (UTC+3) that opens at 8:00 AM, as Google sent it on 2026-10-06
    @Test
    fun `a place's time reads on its own clock`() {
        val nextOpenTime = Instant.parse("2026-10-07T05:00:00Z")

        assertThat(nextOpenTime.toLocalDateTime(placeTimeZone(180)).time).isEqualTo(LocalTime(8, 0))
    }

    @Test
    fun `a place without an offset reads on the phone's clock`() {
        assertThat(placeTimeZone(null)).isEqualTo(TimeZone.currentSystemDefault())
    }
}
