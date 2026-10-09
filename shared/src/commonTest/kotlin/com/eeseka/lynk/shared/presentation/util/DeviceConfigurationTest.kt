package com.eeseka.lynk.shared.presentation.util

import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.computeWindowSizeClass
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class DeviceConfigurationTest {

    @Test
    fun `phones in portrait are mobile portrait`() {
        assertThat(configurationFor(widthDp = 360f, heightDp = 780f)).isEqualTo(DeviceConfiguration.MOBILE_PORTRAIT)
        assertThat(configurationFor(widthDp = 412f, heightDp = 915f)).isEqualTo(DeviceConfiguration.MOBILE_PORTRAIT)
    }

    @Test
    fun `phones in landscape are mobile landscape even when the long side is under 840`() {
        assertThat(configurationFor(widthDp = 780f, heightDp = 360f)).isEqualTo(DeviceConfiguration.MOBILE_LANDSCAPE)
        assertThat(configurationFor(widthDp = 667f, heightDp = 375f)).isEqualTo(DeviceConfiguration.MOBILE_LANDSCAPE)
        assertThat(configurationFor(widthDp = 915f, heightDp = 412f)).isEqualTo(DeviceConfiguration.MOBILE_LANDSCAPE)
    }

    @Test
    fun `unfolded foldables and small tablets in portrait are tablet portrait`() {
        assertThat(configurationFor(widthDp = 673f, heightDp = 841f)).isEqualTo(DeviceConfiguration.TABLET_PORTRAIT)
        assertThat(configurationFor(widthDp = 800f, heightDp = 1280f)).isEqualTo(DeviceConfiguration.TABLET_PORTRAIT)
    }

    @Test
    fun `tablets in landscape are tablet landscape`() {
        assertThat(configurationFor(widthDp = 1280f, heightDp = 800f)).isEqualTo(DeviceConfiguration.TABLET_LANDSCAPE)
        assertThat(configurationFor(widthDp = 841f, heightDp = 673f)).isEqualTo(DeviceConfiguration.TABLET_LANDSCAPE)
    }

    @Test
    fun `large wide and tall windows are desktop`() {
        assertThat(configurationFor(widthDp = 1366f, heightDp = 1024f)).isEqualTo(DeviceConfiguration.DESKTOP)
    }

    private fun configurationFor(widthDp: Float, heightDp: Float): DeviceConfiguration {
        val windowSizeClass = WindowSizeClass.BREAKPOINTS_V1.computeWindowSizeClass(widthDp = widthDp, heightDp = heightDp)
        return DeviceConfiguration.fromWindowSizeClass(windowSizeClass)
    }
}
