package com.eeseka.lynk.shared.presentation.spot.util

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class SpotUtilTest {

    @Test
    fun `same point is zero metres away`() {
        val meters = DistanceCalculator.calculateDistanceInMeters(6.5244, 3.3792, 6.5244, 3.3792)

        assertThat(meters).isEqualTo(0)
    }

    @Test
    fun `one degree of latitude is about 111 kilometres`() {
        val meters = DistanceCalculator.calculateDistanceInMeters(0.0, 0.0, 1.0, 0.0)

        assertThat(meters).isEqualTo(111_195)
    }

    @Test
    fun `a quarter of the way round the equator is about 10000 kilometres`() {
        val meters = DistanceCalculator.calculateDistanceInMeters(0.0, 0.0, 0.0, 90.0)

        assertThat(meters).isEqualTo(10_007_543)
    }

    @Test
    fun `distance is the same in both directions`() {
        val there = DistanceCalculator.calculateDistanceInMeters(6.4281, 3.4219, 6.6018, 3.3515)
        val back = DistanceCalculator.calculateDistanceInMeters(6.6018, 3.3515, 6.4281, 3.4219)

        assertThat(there).isEqualTo(back)
    }

    @Test
    fun `no price tier shows no symbol`() {
        assertThat(getPriceLevelSymbol(0)).isEqualTo("")
        assertThat(getPriceLevelSymbol(-1)).isEqualTo("")
    }

    @Test
    fun `price tier repeats the currency symbol`() {
        val symbol = getLocalCurrencySymbol()

        assertThat(getPriceLevelSymbol(1)).isEqualTo(symbol)
        assertThat(getPriceLevelSymbol(3)).isEqualTo(symbol + symbol + symbol)
    }
}
