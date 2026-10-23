package com.eeseka.lynk.shared.presentation.media

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberTextSharer(): TextSharer {
    val context = LocalContext.current
    return remember { TextSharerAndroid(context) }
}

private class TextSharerAndroid(private val context: Context) : TextSharer {

    override fun share(text: String): Boolean {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(sendIntent, null).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(chooser)
            true
        } catch (_: Exception) {
            false
        }
    }
}
