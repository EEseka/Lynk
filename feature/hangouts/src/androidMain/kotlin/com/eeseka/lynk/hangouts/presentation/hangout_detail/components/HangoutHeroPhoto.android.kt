package com.eeseka.lynk.hangouts.presentation.hangout_detail.components

import android.os.Build

internal actual fun canBlurHeroBackdrop(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
