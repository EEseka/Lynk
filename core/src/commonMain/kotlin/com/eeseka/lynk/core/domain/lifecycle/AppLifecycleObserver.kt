package com.eeseka.lynk.core.domain.lifecycle

import kotlinx.coroutines.flow.Flow

interface AppLifecycleObserver {
    val isInForeground: Flow<Boolean>
}
