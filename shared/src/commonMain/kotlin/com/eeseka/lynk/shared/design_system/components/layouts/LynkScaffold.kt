package com.eeseka.lynk.shared.design_system.components.layouts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFlashMessageHost
import com.eeseka.lynk.shared.design_system.theme.LynkTheme
import com.eeseka.lynk.shared.domain.util.PlatformUtils.isIOS
import com.slapps.cupertino.CupertinoScaffold
import com.slapps.cupertino.ExperimentalCupertinoApi

@OptIn(ExperimentalCupertinoApi::class)
@Composable
fun LynkScaffold(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable (() -> Unit)? = null,
    containerColor: Color = LynkTheme.colors.background,
    contentColor: Color = LynkTheme.colors.textMain,
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.ime),
    content: @Composable (PaddingValues) -> Unit
) {
    // Master Box that holds the Scaffold + The Top-Down Flash Message
    Box(modifier = modifier.fillMaxSize()) {
        if (isIOS()) {
            CupertinoScaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = topBar,
                bottomBar = bottomBar,
                containerColor = containerColor,
                contentColor = contentColor,
                contentWindowInsets = contentWindowInsets
            ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize()) {

                    // Render Screen Content
                    content(paddingValues)

                    // Manually Overlay the FAB for iOS
                    if (floatingActionButton != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(paddingValues)
                                .padding(16.dp)
                        ) {
                            floatingActionButton()
                        }
                    }
                }
            }
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = topBar,
                bottomBar = bottomBar,
                floatingActionButton = floatingActionButton ?: {},
                containerColor = containerColor,
                contentColor = contentColor,
                contentWindowInsets = contentWindowInsets,
                content = content
            )
        }

        if (snackbarHostState != null) {
            LynkFlashMessageHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.statusBars.union(WindowInsets.displayCutout))
            )
        }
    }
}