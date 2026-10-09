package com.eeseka.lynk.shared.domain.util

import android.os.Build

actual object PlatformUtils {
    actual fun isIOS(): Boolean = false

    actual fun osVersion(): String = "Android ${Build.VERSION.RELEASE}"

    actual fun deviceModel(): String = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"
}
