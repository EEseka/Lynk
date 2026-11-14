package com.eeseka.lynk.core.data.lobby.lifecycle

import com.eeseka.lynk.core.domain.lifecycle.AppLifecycleObserver
import kotlinx.coroutines.flow.Flow

expect class NativeAppLifecycleObserver : AppLifecycleObserver {
    override val isInForeground: Flow<Boolean>
}
