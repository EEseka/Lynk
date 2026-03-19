package com.eeseka.lynk.shared.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.eeseka.lynk.shared.domain.util.PlatformUtils.isIOS
import com.eeseka.lynk.shared.presentation.util.UiText
import com.slapps.cupertino.icons.CupertinoIcons
import com.slapps.cupertino.icons.filled.Person
import com.slapps.cupertino.icons.filled.Safari
import com.slapps.cupertino.icons.outlined.Person
import com.slapps.cupertino.icons.outlined.Safari
import lynk.shared.generated.resources.Res
import lynk.shared.generated.resources.discover
import lynk.shared.generated.resources.profile

enum class LynkNavigationItem(
    val route: Screen,
    val title: UiText
) {
    DISCOVER(
        route = Screen.Discover,
        title = UiText.Resource(Res.string.discover)
    ),
    PROFILE(
        route = Screen.Profile,
        title = UiText.Resource(Res.string.profile)
    );

    val unselectedIcon: ImageVector
        get() = if (isIOS()) {
            when (this) {
                DISCOVER -> CupertinoIcons.Outlined.Safari
                PROFILE -> CupertinoIcons.Outlined.Person
            }
        } else {
            when (this) {
                DISCOVER -> Icons.Outlined.Explore
                PROFILE -> Icons.Outlined.Person
            }
        }

    val selectedIcon: ImageVector
        get() = if (isIOS()) {
            when (this) {
                DISCOVER -> CupertinoIcons.Filled.Safari
                PROFILE -> CupertinoIcons.Filled.Person
            }
        } else {
            when (this) {
                DISCOVER -> Icons.Filled.Explore
                PROFILE -> Icons.Filled.Person
            }
        }
}