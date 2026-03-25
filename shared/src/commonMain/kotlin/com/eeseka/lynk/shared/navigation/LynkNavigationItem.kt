package com.eeseka.lynk.shared.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.CircleUser
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Map
import com.eeseka.lynk.shared.presentation.util.UiText
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.discover
import lynk.shared.generated.resources.profile

enum class LynkNavigationItem(
    val route: Screen,
    val title: UiText,
    val icon: ImageVector
) {
    DISCOVER(
        route = Screen.Discover,
        title = UiText.Resource(Res.string.discover),
        icon = Lucide.Map
    ),
    PROFILE(
        route = Screen.Profile,
        title = UiText.Resource(Res.string.profile),
        icon = Lucide.CircleUser
    )
}