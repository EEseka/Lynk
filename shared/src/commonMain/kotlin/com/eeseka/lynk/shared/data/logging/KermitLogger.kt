package com.eeseka.lynk.shared.data.logging

import co.touchlab.kermit.Logger
import com.eeseka.lynk.shared.domain.logging.LynkLogger

object KermitLogger : LynkLogger {

    override fun debug(message: String) {
        Logger.d(message)
    }

    override fun info(message: String) {
        Logger.i(message)
    }

    override fun warn(message: String) {
        Logger.w(message)
    }

    override fun error(message: String, throwable: Throwable?) {
        Logger.e(message, throwable)
    }
}