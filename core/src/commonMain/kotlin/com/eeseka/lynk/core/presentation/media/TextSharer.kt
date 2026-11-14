package com.eeseka.lynk.core.presentation.media

import androidx.compose.runtime.Composable

interface TextSharer {
    fun share(text: String): Boolean
}

@Composable
expect fun rememberTextSharer(): TextSharer
