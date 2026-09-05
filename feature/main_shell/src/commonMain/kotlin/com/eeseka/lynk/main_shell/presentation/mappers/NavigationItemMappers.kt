package com.eeseka.lynk.main_shell.presentation.mappers

import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.CalendarRange
import com.composables.icons.lucide.CircleUser
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Map
import com.eeseka.lynk.main_shell.presentation.model.LynkNavigationItem
import com.eeseka.lynk.shared.presentation.util.UiText
import lynk.feature.main_shell.generated.resources.Res
import lynk.feature.main_shell.generated.resources.discover
import lynk.feature.main_shell.generated.resources.hangouts
import lynk.feature.main_shell.generated.resources.profile

fun LynkNavigationItem.toTitle(): UiText = when (this) {
    LynkNavigationItem.DISCOVER -> UiText.Resource(Res.string.discover)
    LynkNavigationItem.HANGOUTS -> UiText.Resource(Res.string.hangouts)
    LynkNavigationItem.PROFILE -> UiText.Resource(Res.string.profile)
}

fun LynkNavigationItem.getIcon(): ImageVector = when (this) {
    LynkNavigationItem.DISCOVER -> Lucide.Map
    LynkNavigationItem.HANGOUTS -> Lucide.CalendarRange
    LynkNavigationItem.PROFILE -> Lucide.CircleUser
}

/**
 * Calf's [com.mohamedrejeb.calf.ui.navigation.UIKitUITabBarItem] has no badge property, so the
 * unread signal on iOS is a symbol swap rather than a dot.
 */
fun LynkNavigationItem.toSfSymbolName(hasUnseenNotifications: Boolean): String = when (this) {
    LynkNavigationItem.DISCOVER -> "map.fill"
    LynkNavigationItem.HANGOUTS -> if (hasUnseenNotifications) "calendar.badge.exclamationmark" else "calendar"
    LynkNavigationItem.PROFILE -> "person.crop.circle.fill"
}