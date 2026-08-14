package com.eeseka.lynk.hangouts.presentation.hangout_detail.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.hangouts.presentation.util.toUiText
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.design_system.theme.extended
import com.eeseka.lynk.shared.domain.lobby.model.ConnectionState

@Composable
fun ConnectionBanner(
    connectionState: ConnectionState,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val (background, foreground) = when (connectionState) {
        ConnectionState.CONNECTING -> scheme.extended.gold to scheme.extended.onGold
        ConnectionState.DISCONNECTED,
        ConnectionState.ERROR_NETWORK,
        ConnectionState.ERROR_UNKNOWN -> scheme.errorContainer to scheme.onErrorContainer

        ConnectionState.CONNECTED -> scheme.extended.success to scheme.extended.onSuccess
    }

    AnimatedVisibility(
        visible = connectionState != ConnectionState.CONNECTED,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(background)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PulseDot(color = foreground)
            LynkText(
                text = connectionState.toUiText().asString(),
                style = MaterialTheme.typography.labelMedium,
                color = foreground
            )
        }
    }
}

@Composable
private fun PulseDot(color: Color) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun ConnectionBannerPreview(connectionState: ConnectionState) {
    LynkTheme {
        ConnectionBanner(connectionState = connectionState)
    }
}

@PreviewLightDark
@Composable
private fun ConnectionBannerConnectingPreview() =
    ConnectionBannerPreview(ConnectionState.CONNECTING)

@PreviewLightDark
@Composable
private fun ConnectionBannerDisconnectedPreview() =
    ConnectionBannerPreview(ConnectionState.DISCONNECTED)

@PreviewLightDark
@Composable
private fun ConnectionBannerErrorNetworkPreview() =
    ConnectionBannerPreview(ConnectionState.ERROR_NETWORK)

@PreviewLightDark
@Composable
private fun ConnectionBannerErrorUnknownPreview() =
    ConnectionBannerPreview(ConnectionState.ERROR_UNKNOWN)