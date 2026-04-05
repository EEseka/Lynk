package com.eeseka.lynk.shared.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.CircleUser
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Map
//import com.eeseka.lynk.main.presentation.navigation.DiscoverRoute
//import com.eeseka.lynk.main.presentation.navigation.ProfileRoute
import com.eeseka.lynk.shared.presentation.util.UiText
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.discover
import lynk.shared.generated.resources.profile

enum class LynkNavigationItem(
    val route: Any,
    val title: UiText,
    val icon: ImageVector
) {
    DISCOVER(
        route = "DiscoverRoute",
        title = UiText.Resource(Res.string.discover),
        icon = Lucide.Map
    ),
    PROFILE(
        route = "ProfileRoute",
        title = UiText.Resource(Res.string.profile),
        icon = Lucide.CircleUser
    )
}

/*
 * =========================================================================================
 * TODO: ARCHITECTURE REFACTOR (MAIN MODULE CREATION)
 * =========================================================================================
 * When you are ready to build the feature:main module, follow these exact steps:
 * * 1. CREATE ROUTES: In the main module, create your `@Serializable data object` routes
 * (DiscoverRoute, ProfileRoute, SettingsRoute).
 * * 2. MIGRATE COMPONENTS: Move this file (LynkNavigationItem.kt), along with LynkBottomBar.kt
 * and LynkNavigationRail.kt, OUT of the shared module and INTO the main module.
 * * 3. UPDATE ROUTES: In this enum, replace the hardcoded strings ("DiscoverRoute") with the
 * actual imported route objects (DiscoverRoute).
 * * 4. CLEANUP ROOT: Go to NavigationRoot.kt in your app module, delete the dummy
 * `MainGraphRoutes.Graph` composable, and replace it with your actual `MainScreen`
 * shell that hosts the inner NavHost and bottom bar.
 * =========================================================================================
 */