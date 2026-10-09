package com.eeseka.lynk.shared.presentation.map.util

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.eeseka.lynk.AppConfig
import com.eeseka.lynk.shared.domain.settings.AppTheme

private val MAP_TILER_API_KEY = AppConfig.MAP_TILER_API_KEY
private val MAP_STYLE_URI_DARK = "https://api.maptiler.com/maps/streets-v4-dark/style.json?key=$MAP_TILER_API_KEY"
private val MAP_STYLE_URI_LIGHT = "https://api.maptiler.com/maps/streets-v4/style.json?key=$MAP_TILER_API_KEY"

@Composable
fun mapStyleUri(theme: AppTheme) = if (isMapDark(theme)) MAP_STYLE_URI_DARK else MAP_STYLE_URI_LIGHT

@Composable
fun isMapDark(theme: AppTheme): Boolean {
    return when (theme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }
}
