package com.eeseka.lynk.discover.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Locate
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.AppConfig
import com.eeseka.lynk.create_hangout.presentation.CreateHangoutRoot
import com.eeseka.lynk.shared.presentation.components.SpotDetailSheet
import com.eeseka.lynk.discover.presentation.components.SpotLocationMapMarker
import com.eeseka.lynk.discover.presentation.components.SpotSearchSheet
import com.eeseka.lynk.discover.presentation.components.UserLocationMapMarker
import com.eeseka.lynk.shared.design_system.components.buttons.LynkTonalIconButton
import com.eeseka.lynk.shared.design_system.components.layouts.LynkScaffold
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDialog
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDropDownItem
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDropDownMenu
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFlashType
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.showFlashMessage
import com.eeseka.lynk.shared.design_system.components.textfields.LynkSearchField
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.domain.settings.AppTheme
import com.eeseka.lynk.shared.presentation.components.GuestPromptSheet
import com.eeseka.lynk.shared.presentation.location.rememberLocationController
import com.eeseka.lynk.shared.presentation.permissions.Permission
import com.eeseka.lynk.shared.presentation.permissions.PermissionState
import com.eeseka.lynk.shared.presentation.permissions.rememberPermissionController
import com.eeseka.lynk.shared.presentation.util.ObserveAsEvents
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import lynk.feature.discover.generated.resources.Res
import lynk.feature.discover.generated.resources.create_a_hangout
import lynk.feature.discover.generated.resources.locate_me
import lynk.feature.discover.generated.resources.location_fetch_error
import lynk.feature.discover.generated.resources.location_required
import lynk.feature.discover.generated.resources.location_required_message
import lynk.feature.discover.generated.resources.map_data_label
import lynk.feature.discover.generated.resources.maptiler_attribution
import lynk.feature.discover.generated.resources.not_now
import lynk.feature.discover.generated.resources.open_settings
import lynk.feature.discover.generated.resources.osm_attribution
import lynk.feature.discover.generated.resources.save_this_spot
import lynk.feature.discover.generated.resources.search_spots_hint
import org.jetbrains.compose.resources.stringResource
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position

private val MAP_TILER_API_KEY = AppConfig.MAP_TILER_API_KEY
private val MAP_STYLE_URI_DARK =
    "https://api.maptiler.com/maps/streets-v4-dark/style.json?key=$MAP_TILER_API_KEY"
private val MAP_STYLE_URI_LIGHT =
    "https://api.maptiler.com/maps/streets-v4/style.json?key=$MAP_TILER_API_KEY"

private const val OSM_COPYRIGHT_LINK = "https://www.openstreetmap.org/copyright"
private const val MAPTILER_COPYRIGHT_LINK = "https://www.maptiler.com/copyright"

@Composable
fun DiscoverScreen(
    state: DiscoverState,
    events: Flow<DiscoverEvent>,
    onAction: (DiscoverAction) -> Unit,
    navigateToHangouts: (String) -> Unit,
    mainShellPadding: PaddingValues
) {
    val permissionController = rememberPermissionController()
    val locationController = rememberLocationController()
    val cameraState = rememberCameraState()
    var userPosition by remember { mutableStateOf<Position?>(null) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val hapticFeedback = rememberAppHaptic()
    val uriHandler = LocalUriHandler.current

    var permissionState by remember { mutableStateOf(PermissionState.NOT_DETERMINED) }

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAttributionMenu by remember { mutableStateOf(false) }

    val locationFetchError = stringResource(Res.string.location_fetch_error)

    ObserveAsEvents(events) { event ->
        when (event) {
            is DiscoverEvent.Error -> {
                hapticFeedback(AppHaptic.Error)
                snackbarHostState.showFlashMessage(
                    message = event.error.asStringAsync(),
                    type = LynkFlashType.Error
                )
            }
        }
    }

    val fetchCurrentLocationAndShowOnMap: suspend () -> Unit = {
        val coordinate = locationController.getCurrentLocation()
        if (coordinate == null) {
            snackbarHostState.showFlashMessage(
                message = locationFetchError,
                type = LynkFlashType.Error
            )
        } else {
            val position = Position(
                latitude = coordinate.latitude,
                longitude = coordinate.longitude
            )
            userPosition = position

            onAction(DiscoverAction.OnLocationFetched(coordinate.latitude, coordinate.longitude))

            cameraState.animateTo(
                finalPosition = cameraState.position.copy(
                    target = position,
                    zoom = 14.0
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        permissionState = permissionController.getPermissionState(Permission.LOCATION)
        if (permissionState == PermissionState.NOT_DETERMINED || permissionState == PermissionState.DENIED) {
            permissionState = permissionController.requestPermission(Permission.LOCATION)
        }
        if (permissionState == PermissionState.PERMANENTLY_DENIED) {
            showSettingsDialog = true
        }
    }

    LaunchedEffect(permissionState) {
        if (permissionState == PermissionState.GRANTED) {
            fetchCurrentLocationAndShowOnMap()
        }
    }

    val isDark = when (state.mapTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    val mapStyle = if (isDark) MAP_STYLE_URI_DARK else MAP_STYLE_URI_LIGHT

    val spotsToShow = state.searchResults.ifEmpty { state.trendingSpots }

    LynkScaffold(
        snackbarHostState = snackbarHostState
    ) { scaffoldPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            MaplibreMap(
                modifier = Modifier.fillMaxSize(),
                baseStyle = BaseStyle.Uri(mapStyle),
                cameraState = cameraState,
                zoomRange = 2f..20f,
                pitchRange = 0f..60f,
                options = MapOptions(
                    ornamentOptions = OrnamentOptions(
                        isCompassEnabled = true,
                        compassAlignment = Alignment.TopEnd,
                        isScaleBarEnabled = true,
                        scaleBarAlignment = Alignment.TopStart,
                        isLogoEnabled = false,
                        isAttributionEnabled = false,
                        padding = PaddingValues(
                            top = scaffoldPadding.calculateTopPadding() + 80.dp,
                            start = 16.dp,
                            end = 16.dp
                        )
                    )
                )
            ) {
                userPosition?.let { position ->
                    UserLocationMapMarker(
                        userLatitude = position.latitude,
                        userLongitude = position.longitude
                    )
                }

                SpotLocationMapMarker(
                    spots = spotsToShow,
                    selectedSpotId = state.selectedSpotId,
                    onSpotClick = { spotId ->
                        hapticFeedback(AppHaptic.Selection)
                        onAction(DiscoverAction.OnSpotSelected(spotId))
                    }
                )
            }

            // Search Bar
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = scaffoldPadding.calculateTopPadding() + 16.dp)
                    .padding(horizontal = 16.dp)
                    .widthIn(max = 480.dp)
            ) {
                LynkSearchField(
                    state = state.searchTextState,
                    placeholder = stringResource(Res.string.search_spots_hint),
                    modifier = Modifier.fillMaxWidth()
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onAction(DiscoverAction.ToggleShowSearchSheet) }
                        )
                )
            }

            // Attribution
            LynkDropDownMenu(
                expanded = showAttributionMenu,
                onDismissRequest = { showAttributionMenu = false },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(
                        bottom = mainShellPadding.calculateBottomPadding() + 16.dp,
                        start = 16.dp
                    ),
                items = listOf(
                    LynkDropDownItem(
                        title = stringResource(Res.string.osm_attribution),
                        onClick = { uriHandler.openUri(OSM_COPYRIGHT_LINK) }
                    ),
                    LynkDropDownItem(
                        title = stringResource(Res.string.maptiler_attribution),
                        onClick = { uriHandler.openUri(MAPTILER_COPYRIGHT_LINK) }
                    )
                ),
                anchor = {
                    Row(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                            .clickable { showAttributionMenu = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Lucide.Info,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        LynkText(
                            text = stringResource(Res.string.map_data_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            )

            // Action Button
            LynkTonalIconButton(
                onClick = {
                    if (permissionState == PermissionState.GRANTED) {
                        scope.launch { fetchCurrentLocationAndShowOnMap() }
                    } else {
                        scope.launch {
                            permissionState =
                                permissionController.requestPermission(Permission.LOCATION)
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.End))
                    .padding(
                        bottom = mainShellPadding.calculateBottomPadding() + 16.dp,
                        end = 16.dp
                    )
            ) {
                Icon(
                    imageVector = Lucide.Locate,
                    contentDescription = stringResource(Res.string.locate_me)
                )
            }
        }

        // Overlays & Sheets
        if (state.guestPromptContext != null) {
            val actionStr = when (state.guestPromptContext) {
                GuestPromptContext.SAVE_SPOT -> stringResource(Res.string.save_this_spot)
                GuestPromptContext.CREATE_HANGOUT -> stringResource(Res.string.create_a_hangout)
            }
            GuestPromptSheet(
                actionStr = actionStr,
                onCreateAccountClick = { onAction(DiscoverAction.SignOutGuest) },
                onDismissRequest = { onAction(DiscoverAction.HideGuestPrompt) },
                isLoading = state.isGuestSigningOut
            )
        }

        if (state.showSearchSheet) {
            SpotSearchSheet(
                state = state,
                onLoadNextSearchPage = { onAction(DiscoverAction.LoadNextSearchPage) },
                onSelectPriceLevel = { onAction(DiscoverAction.OnPriceLevelSelected(it)) },
                onSelectCategory = { onAction(DiscoverAction.OnCategorySelected(it)) },
                onSpotClick = { spotId ->
                    onAction(DiscoverAction.ToggleShowSearchSheet)
                    onAction(DiscoverAction.OnSpotSelected(spotId))
                },
                onDismissRequest = { onAction(DiscoverAction.ToggleShowSearchSheet) }
            )
        }

        if (state.selectedSpotId != null) {
            spotsToShow.find { it.id == state.selectedSpotId }?.let { selectedSpot ->
                SpotDetailSheet(
                    spot = selectedSpot,
                    userLat = userPosition?.latitude,
                    userLng = userPosition?.longitude,
                    onCreateHangoutClick = { spotId ->
                        onAction(DiscoverAction.OnSpotSelected(null))
                        if (state.isGuest) {
                            onAction(DiscoverAction.ShowGuestPrompt(GuestPromptContext.CREATE_HANGOUT))
                        } else {
                            onAction(DiscoverAction.OnHangoutCreationSelected(spotId))
                        }
                    },
                    onToggleSave = { spotId, isSaved ->
                        if (state.isGuest) {
                            onAction(DiscoverAction.OnSpotSelected(null))
                            onAction(DiscoverAction.ShowGuestPrompt(GuestPromptContext.SAVE_SPOT))
                        } else {
                            onAction(DiscoverAction.OnToggleSaveSpot(spotId, isSaved))
                        }
                    },
                    onDismissRequest = {
                        onAction(DiscoverAction.OnSpotSelected(null))
                    }
                )
            }
        }

        if (state.hangoutCreationSpotId != null) {
            spotsToShow.find { it.id == state.hangoutCreationSpotId }?.let { hangoutSpot ->
                CreateHangoutRoot(
                    visible = true,
                    spot = hangoutSpot,
                    onDismiss = { onAction(DiscoverAction.OnHangoutCreationSelected(null)) },
                    onSuccess = { newHangoutId ->
                        onAction(DiscoverAction.OnHangoutCreationSelected(null))
                        navigateToHangouts(newHangoutId)
                    }
                )
            }
        }

        if (showSettingsDialog) {
            LynkDialog(
                title = stringResource(Res.string.location_required),
                message = stringResource(Res.string.location_required_message),
                confirmText = stringResource(Res.string.open_settings),
                dismissText = stringResource(Res.string.not_now),
                onConfirm = {
                    showSettingsDialog = false
                    permissionController.openAppSettings()
                },
                onDismissRequest = { showSettingsDialog = false }
            )
        }
    }
}