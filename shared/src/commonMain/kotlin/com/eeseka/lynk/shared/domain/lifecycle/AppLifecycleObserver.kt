package com.eeseka.lynk.shared.domain.lifecycle

import kotlinx.coroutines.flow.Flow

interface AppLifecycleObserver {
    val isInForeground: Flow<Boolean>
}
