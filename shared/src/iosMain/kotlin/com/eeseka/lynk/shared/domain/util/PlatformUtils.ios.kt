package com.eeseka.lynk.shared.domain.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import platform.UIKit.UIDevice
import platform.posix.uname
import platform.posix.utsname

actual object PlatformUtils {
    actual fun isIOS(): Boolean = true

    actual fun osVersion(): String = "${UIDevice.currentDevice.systemName} ${UIDevice.currentDevice.systemVersion}"

    @OptIn(ExperimentalForeignApi::class)
    actual fun deviceModel(): String = memScoped {
        val systemInfo = alloc<utsname>()
        uname(systemInfo.ptr)
        systemInfo.machine.toKString()
    }
}
