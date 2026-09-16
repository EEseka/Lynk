package com.eeseka.lynk.shared.data.lobby.lifecycle

import com.eeseka.lynk.shared.domain.lifecycle.AppLifecycleObserver
import kotlinx.coroutines.flow.Flow

expect class NativeAppLifecycleObserver : AppLifecycleObserver {
    override val isInForeground: Flow<Boolean>
}
