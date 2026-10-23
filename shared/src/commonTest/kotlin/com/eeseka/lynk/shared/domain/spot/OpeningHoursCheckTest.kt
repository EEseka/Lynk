package com.eeseka.lynk.shared.domain.spot

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.eeseka.lynk.shared.domain.spot.model.SpotOpeningHours
import com.eeseka.lynk.shared.domain.spot.model.SpotOpeningPeriod
import kotlinx.datetime.DayOfWeek
import kotlin.test.Test
import kotlin.time.Instant

class OpeningHoursCheckTest {

    private companion object {
        const val LAGOS_OFFSET_MINUTES = 60
    }

    @Test
    fun `is open during opening hours and closed outside them`() {
        val hours = hoursOf(SpotOpeningPeriod(DayOfWeek.FRIDAY, 9 * 60, DayOfWeek.FRIDAY, 17 * 60))

        // 2026-10-16 is a Friday; 13:00 UTC is 2 PM in Lagos
        assertThat(OpeningHoursCheck.isOpenAt(hours, Instant.parse("2026-10-16T13:00:00Z"))).isEqualTo(true)
        assertThat(OpeningHoursCheck.isOpenAt(hours, Instant.parse("2026-10-16T17:00:00Z"))).isEqualTo(false)
    }

    @Test
    fun `reads the time in the place time zone not UTC`() {
        val hours = hoursOf(SpotOpeningPeriod(DayOfWeek.FRIDAY, 9 * 60, DayOfWeek.FRIDAY, 17 * 60))

        // 8:30 UTC is 9:30 in Lagos: already open there, though not yet 9 in UTC
        assertThat(OpeningHoursCheck.isOpenAt(hours, Instant.parse("2026-10-16T08:30:00Z"))).isEqualTo(true)
    }

    @Test
    fun `a late night stays open past midnight`() {
        val hours = hoursOf(SpotOpeningPeriod(DayOfWeek.FRIDAY, 22 * 60, DayOfWeek.SATURDAY, 2 * 60))

        // Saturday 1 AM in Lagos
        assertThat(OpeningHoursCheck.isOpenAt(hours, Instant.parse("2026-10-17T00:00:00Z"))).isEqualTo(true)
        // Saturday 3 AM in Lagos
        assertThat(OpeningHoursCheck.isOpenAt(hours, Instant.parse("2026-10-17T02:00:00Z"))).isEqualTo(false)
    }

    @Test
    fun `a Saturday night that ends on Sunday wraps past the end of the week`() {
        val hours = hoursOf(SpotOpeningPeriod(DayOfWeek.SATURDAY, 22 * 60, DayOfWeek.SUNDAY, 2 * 60))

        // Sunday 1 AM in Lagos
        assertThat(OpeningHoursCheck.isOpenAt(hours, Instant.parse("2026-10-18T00:00:00Z"))).isEqualTo(true)
        // Saturday 11 PM in Lagos
        assertThat(OpeningHoursCheck.isOpenAt(hours, Instant.parse("2026-10-17T22:00:00Z"))).isEqualTo(true)
    }

    @Test
    fun `follows a clock change in the place instead of the offset from when it was fetched`() {
        // Fetched in summer, when London was an hour ahead of UTC
        val hours = hoursOf(SpotOpeningPeriod(DayOfWeek.MONDAY, 9 * 60, DayOfWeek.MONDAY, 17 * 60))
            .copy(utcOffsetMinutes = 60, timeZoneId = "Europe/London")

        // Monday 2 November, after the clocks went back: 16:30 UTC is 4:30 PM in London, still open.
        // The summer offset alone would have said 5:30 PM and closed
        assertThat(OpeningHoursCheck.isOpenAt(hours, Instant.parse("2026-11-02T16:30:00Z"))).isEqualTo(true)
    }

    @Test
    fun `a night cut off at the end of the Google week still runs into the next morning`() {
        val hours = hoursOf(
            SpotOpeningPeriod(DayOfWeek.TUESDAY, 0, DayOfWeek.TUESDAY, 2 * 60),
            SpotOpeningPeriod(DayOfWeek.MONDAY, 22 * 60, DayOfWeek.MONDAY, 23 * 60 + 59)
        )

        // Monday 11:59 PM in Lagos, the minute Google's cut-off would leave closed
        assertThat(OpeningHoursCheck.isOpenAt(hours, Instant.parse("2026-10-19T22:59:30Z"))).isEqualTo(true)
    }

    @Test
    fun `a place that never closes is always open`() {
        val hours = hoursOf(SpotOpeningPeriod(DayOfWeek.SUNDAY, 0, null, null))

        assertThat(OpeningHoursCheck.isOpenAt(hours, Instant.parse("2026-10-14T03:00:00Z"))).isEqualTo(true)
    }

    @Test
    fun `says nothing when Google gave no hours or no clock`() {
        val noPeriods = hoursOf()
        val noClock = hoursOf(SpotOpeningPeriod(DayOfWeek.FRIDAY, 9 * 60, DayOfWeek.FRIDAY, 17 * 60)).copy(utcOffsetMinutes = null)

        assertThat(OpeningHoursCheck.isOpenAt(noPeriods, Instant.parse("2026-10-16T13:00:00Z"))).isNull()
        assertThat(OpeningHoursCheck.isOpenAt(noClock, Instant.parse("2026-10-16T13:00:00Z"))).isNull()
    }

    @Test
    fun `an open place next changes when it closes`() {
        val hours = hoursOf(SpotOpeningPeriod(DayOfWeek.FRIDAY, 9 * 60, DayOfWeek.FRIDAY, 17 * 60))

        // Friday 2 PM in Lagos closes at 5 PM, 16:00 UTC
        assertThat(OpeningHoursCheck.nextChangeAt(hours, Instant.parse("2026-10-16T13:00:00Z")))
            .isEqualTo(Instant.parse("2026-10-16T16:00:00Z"))
    }

    @Test
    fun `a closed place next changes when it opens even days ahead`() {
        val hours = hoursOf(SpotOpeningPeriod(DayOfWeek.MONDAY, 9 * 60, DayOfWeek.MONDAY, 17 * 60))

        // Friday 2 PM in Lagos opens Monday 9 AM, 08:00 UTC
        assertThat(OpeningHoursCheck.nextChangeAt(hours, Instant.parse("2026-10-16T13:00:00Z")))
            .isEqualTo(Instant.parse("2026-10-19T08:00:00Z"))
    }

    @Test
    fun `a night cut off at the end of the Google week closes the next morning instead of at midnight`() {
        val hours = hoursOf(
            SpotOpeningPeriod(DayOfWeek.TUESDAY, 0, DayOfWeek.TUESDAY, 2 * 60),
            SpotOpeningPeriod(DayOfWeek.MONDAY, 22 * 60, DayOfWeek.MONDAY, 23 * 60 + 59)
        )

        // Monday 11 PM in Lagos closes Tuesday 2 AM, 01:00 UTC
        assertThat(OpeningHoursCheck.nextChangeAt(hours, Instant.parse("2026-10-19T22:00:00Z")))
            .isEqualTo(Instant.parse("2026-10-20T01:00:00Z"))
    }

    @Test
    fun `a place open until the end of the day with nothing the next morning closes at midnight`() {
        val hours = hoursOf(SpotOpeningPeriod(DayOfWeek.MONDAY, 18 * 60, DayOfWeek.MONDAY, 23 * 60 + 59))

        // Monday 8 PM in Lagos closes at midnight, 23:00 UTC
        assertThat(OpeningHoursCheck.nextChangeAt(hours, Instant.parse("2026-10-19T19:00:00Z")))
            .isEqualTo(Instant.parse("2026-10-19T23:00:00Z"))
    }

    @Test
    fun `a place that never closes never changes`() {
        val hours = hoursOf(SpotOpeningPeriod(DayOfWeek.SUNDAY, 0, null, null))
        val now = Instant.parse("2026-10-14T03:00:00Z")

        assertThat(OpeningHoursCheck.isOpenAt(hours, now)).isEqualTo(true)
        assertThat(OpeningHoursCheck.nextChangeAt(hours, now)).isNull()
    }

    @Test
    fun `an unknown time zone falls back to the offset`() {
        val hours = hoursOf(SpotOpeningPeriod(DayOfWeek.FRIDAY, 9 * 60, DayOfWeek.FRIDAY, 17 * 60))
            .copy(timeZoneId = "Not/AZone")

        // 9:30 in Lagos by the offset
        assertThat(OpeningHoursCheck.isOpenAt(hours, Instant.parse("2026-10-16T08:30:00Z"))).isEqualTo(true)
    }

    private fun hoursOf(vararg periods: SpotOpeningPeriod) = SpotOpeningHours(
        weekdayDescriptions = emptyList(),
        utcOffsetMinutes = LAGOS_OFFSET_MINUTES,
        timeZoneId = null,
        periods = periods.toList()
    )
}
