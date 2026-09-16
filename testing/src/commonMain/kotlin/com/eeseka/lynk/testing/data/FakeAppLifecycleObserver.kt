package com.eeseka.lynk.testing.data

import com.eeseka.lynk.shared.domain.lifecycle.AppLifecycleObserver
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAppLifecycleObserver : AppLifecycleObserver {
    // Set it to move the app between the foreground and the background
    override val isInForeground = MutableStateFlow(false)
}
