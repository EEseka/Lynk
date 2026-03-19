package com.eeseka.lynk.shared.domain.util

import platform.UIKit.UIDevice

actual object PlatformUtils {
    actual fun isIOS26(): Boolean {
        val version = UIDevice.currentDevice.systemVersion.toDoubleOrNull() ?: 0.0
        return version >= 26.0
    }

    actual fun isIOS(): Boolean = true
}