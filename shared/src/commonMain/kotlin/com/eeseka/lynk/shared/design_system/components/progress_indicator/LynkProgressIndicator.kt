package com.eeseka.lynk.shared.design_system.components.progress_indicator

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.domain.util.PlatformUtils.isIOS
import com.slapps.cupertino.CupertinoActivityIndicator
import com.slapps.cupertino.ExperimentalCupertinoApi

@OptIn(ExperimentalCupertinoApi::class)
@Composable
fun LynkProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    strokeWidth: Dp = 4.dp
) {
    if (isIOS()) {
        CupertinoActivityIndicator(modifier = modifier)
    } else {
        CircularProgressIndicator(
            modifier = modifier,
            color = if (color == Color.Unspecified) ProgressIndicatorDefaults.circularColor else color,
            strokeWidth = strokeWidth
        )
    }
}