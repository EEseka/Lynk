package com.eeseka.lynk.core.domain.util

expect object PlatformUtils {
    fun isIOS(): Boolean
    fun osVersion(): String
    fun deviceModel(): String
}
