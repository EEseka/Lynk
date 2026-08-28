package com.eeseka.lynk

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.eeseka.lynk.shared.domain.notification.model.NotificationType
import com.eeseka.lynk.shared.presentation.navigation.ExternalUriHandler

class MainActivity : ComponentActivity() {

    private companion object {
        const val EXTRA_TYPE = "type"
        const val EXTRA_HANGOUT_ID = "hangoutId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        var shouldShowSplashScreen = true

        installSplashScreen().setKeepOnScreenCondition {
            shouldShowSplashScreen
        }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        handleNotificationDeepLink(intent)

        setContent {
            App(
                onAuthenticationChecked = {
                    shouldShowSplashScreen = false
                }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationDeepLink(intent)
    }

    private fun handleNotificationDeepLink(intent: Intent) {
        intent.data?.let { uri ->
            ExternalUriHandler.onNewUri(uri.toString())
            return
        }

        val hangoutId = intent.getStringExtra(EXTRA_HANGOUT_ID) ?: return
        val type = intent.getStringExtra(EXTRA_TYPE)

        val deepLinkUri = when (type) {
            NotificationType.PARTICIPANT_INVITED.name -> "lynk://notifications/$hangoutId"
            NotificationType.INVITE_CANCELLED.name,
            NotificationType.REMOVED_FOR_NON_PAYMENT.name -> return

            else -> "lynk://hangout_detail/$hangoutId"
        }

        ExternalUriHandler.onNewUri(deepLinkUri)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}