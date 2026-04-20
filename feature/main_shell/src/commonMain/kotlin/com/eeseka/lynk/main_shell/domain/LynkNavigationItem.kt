package com.eeseka.lynk.main_shell.domain

import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.CalendarRange
import com.composables.icons.lucide.CircleUser
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Map
import com.eeseka.lynk.shared.presentation.util.UiText
import lynk.feature.main_shell.generated.resources.Res
import lynk.feature.main_shell.generated.resources.discover
import lynk.feature.main_shell.generated.resources.hangouts
import lynk.feature.main_shell.generated.resources.profile
import com.eeseka.lynk.main_shell.presentation.navigation.DiscoverRoute
import com.eeseka.lynk.main_shell.presentation.navigation.HangoutsRoute
import com.eeseka.lynk.main_shell.presentation.navigation.ProfileRoute

enum class LynkNavigationItem(
    val route: Any,
    val title: UiText,
    val icon: ImageVector
) {
    DISCOVER(
        route = DiscoverRoute,
        title = UiText.Resource(Res.string.discover),
        icon = Lucide.Map
    ),
    HANGOUTS(
        route = HangoutsRoute,
        title = UiText.Resource(Res.string.hangouts),
        icon = Lucide.CalendarRange
    ),
    PROFILE(
        route = ProfileRoute,
        title = UiText.Resource(Res.string.profile),
        icon = Lucide.CircleUser
    )
}