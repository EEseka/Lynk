package com.eeseka.lynk.main_shell.presentation.model

import com.eeseka.lynk.discover.presentation.navigation.DiscoverGraphRoutes
import com.eeseka.lynk.hangouts.presentation.navigation.HangoutsGraphRoutes
import com.eeseka.lynk.profile.presentation.navigation.ProfileGraphRoutes

enum class LynkNavigationItem(val route: Any) {
    DISCOVER(route = DiscoverGraphRoutes.Graph),
    HANGOUTS(route = HangoutsGraphRoutes.Graph),
    PROFILE(route = ProfileGraphRoutes.Graph)
}