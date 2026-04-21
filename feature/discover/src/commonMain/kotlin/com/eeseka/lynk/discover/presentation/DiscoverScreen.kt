package com.eeseka.lynk.discover.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eeseka.lynk.shared.design_system.components.layouts.LynkScaffold
import com.eeseka.lynk.shared.design_system.components.navigation.LynkTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    mainShellPadding: PaddingValues
) {
    LynkScaffold(
        topBar = {
            LynkTopAppBar(title = "Discover")
        }
    ) { innerScaffoldPadding ->
        val combinedPadding = PaddingValues(
            top = innerScaffoldPadding.calculateTopPadding(),
            bottom = mainShellPadding.calculateBottomPadding(),
            start = 16.dp,
            end = 16.dp
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = combinedPadding
        ) {
            items(100) {
                Text("Discover Spot #$it", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}