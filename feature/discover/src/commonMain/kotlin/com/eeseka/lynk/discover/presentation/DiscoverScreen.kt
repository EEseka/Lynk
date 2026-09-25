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
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Locate
import com.composables.icons.lucide.Lucide
import com.eeseka.lynk.AppConfig
import com.eeseka.lynk.create_hangout.presentation.CreateHangoutRoot
import com.eeseka.lynk.discover.presentation.components.SelectedSpotPinOverlay
import com.eeseka.lynk.discover.presentation.components.SpotLocationMapMarker
import com.eeseka.lynk.discover.presentation.components.SpotSearchSheet
import com.eeseka.lynk.discover.presentation.components.UserLocationMapMarker
import com.eeseka.lynk.discover.presentation.components.rememberSpotMapInteractions
import com.eeseka.lynk.discover.presentation.mappers.toUiText
import com.eeseka.lynk.discover.presentation.model.GuestPromptContext
import com.eeseka.lynk.discover.presentation.util.flightDurationTo
import com.eeseka.lynk.shared.design_system.components.buttons.LynkTonalIconButton
import com.eeseka.lynk.shared.design_system.components.layouts.LynkScaffold
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDialog
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDropDownItem
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkDropDownMenu
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.LynkFlashType
import com.eeseka.lynk.shared.design_system.components.modals_and_overlays.showFlashMessage
import com.eeseka.lynk.shared.design_system.components.progress_indicator.LynkProgressIndicator
import com.eeseka.lynk.shared.design_system.components.textfields.LynkSearchField
import com.eeseka.lynk.shared.design_system.components.textfields.LynkText
import com.eeseka.lynk.shared.design_system.components.util.AppHaptic
import com.eeseka.lynk.shared.design_system.components.util.rememberAppHaptic
import com.eeseka.lynk.shared.domain.location.LocationError
import com.eeseka.lynk.shared.domain.settings.AppTheme
import com.eeseka.lynk.shared.domain.util.onFailure
import com.eeseka.lynk.shared.domain.util.onSuccess
import com.eeseka.lynk.shared.presentation.components.GuestPromptSheet
import com.eeseka.lynk.shared.presentation.components.LynkErrorState
import com.eeseka.lynk.shared.presentation.components.SpotDetailSheet
import com.eeseka.lynk.shared.presentation.location.rememberLocationController
import com.eeseka.lynk.shared.presentation.permissions.LocationPermissionEffect
import com.eeseka.lynk.shared.presentation.permissions.Permission
import com.eeseka.lynk.shared.presentation.permissions.PermissionState
import com.eeseka.lynk.shared.presentation.permissions.rememberPermissionController
import com.eeseka.lynk.shared.presentation.util.ObserveAsEvents
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import lynk.feature.discover.generated.resources.Res
import lynk.feature.discover.generated.resources.create_a_hangout
import lynk.feature.discover.generated.resources.finding_your_location
import lynk.feature.discover.generated.resources.locate_me
import lynk.feature.discover.generated.resources.location_required
import lynk.feature.discover.generated.resources.location_required_message
import lynk.feature.discover.generated.resources.map_data_label
import lynk.feature.discover.generated.resources.maptiler_attribution
import lynk.feature.discover.generated.resources.not_now
import lynk.feature.discover.generated.resources.open_settings
import lynk.feature.discover.generated.resources.open_spot_search
import lynk.feature.discover.generated.resources.osm_attribution
import lynk.feature.discover.generated.resources.save_this_spot
import lynk.feature.discover.generated.resources.search_spots_hint
import lynk.feature.discover.generated.resources.show_map_attribution
import lynk.feature.discover.generated.resources.trending_load_error_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.maplibre.compose.camera.CameraAnimation
import org.maplibre.compose.camera.CameraUpdate
import org.maplibre.compose.map.CameraConstraints
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.overlay.CompassButtonStyle
import org.maplibre.compose.overlay.DisappearingCompassButton
import org.maplibre.compose.overlay.DisappearingScaleBar
import org.maplibre.compose.overlay.LocalViewportInsets
import org.maplibre.compose.overlay.MapOverlay
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
fun DiscoverRoot(
    navigateToHangouts: (String) -> Unit,
    mainShellPadding: PaddingValues,
    viewModel: DiscoverViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is DiscoverEvent.Error -> {
                snackbarHostState.showFlashMessage(
                    message = event.error.asStringAsync(),
                    type = LynkFlashType.Error
                )
            }
        }
    }

    DiscoverScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState,
        navigateToHangouts = navigateToHangouts,
        mainShellPadding = mainShellPadding
    )
}

@Composable
fun DiscoverScreen(
    state: DiscoverState,
    onAction: (DiscoverAction) -> Unit,
    snackbarHostState: SnackbarHostState,
    navigateToHangouts: (String) -> Unit,
    mainShellPadding: PaddingValues
) {
    val permissionController = rememberPermissionController()
    val locationController = rememberLocationController()

    val isDark = when (state.mapTheme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }

    val mapStyle = if (isDark) MAP_STYLE_URI_DARK else MAP_STYLE_URI_LIGHT

    val spotsToShow = state.searchResults.ifEmpty { state.trendingSpots }.toImmutableList()

    val mapState = rememberMapState(
        baseStyle = BaseStyle.Uri(mapStyle),
        content = {
            SpotLocationMapMarker(
                spots = spotsToShow,
                selectedSpotId = state.selectedSpotId
            )
        }
    )

    val scope = rememberCoroutineScope()
    val hapticFeedback = rememberAppHaptic()
    val uriHandler = LocalUriHandler.current

    var permissionState by remember { mutableStateOf(PermissionState.NOT_DETERMINED) }

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAttributionMenu by remember { mutableStateOf(false) }

    var hasCenteredOnUser by rememberSaveable { mutableStateOf(false) }

    var isFindingLocation by remember { mutableStateOf(false) }

    val openSettingsLabel = stringResource(Res.string.open_settings)

    val fetchCurrentLocationAndShowOnMap: suspend (isLocateMeTap: Boolean) -> Unit = { isLocateMeTap ->
        isFindingLocation = true
        // The cached fix opens the map, the tracker's fix sharpens it. Only the first one may
        // move the camera, so a later refinement never yanks the map out from under a pan.
        var hasMovedCameraThisFetch = false

        locationController.observeCurrentLocation().collect { locationResult ->
            locationResult
                .onSuccess { coordinate ->
                    isFindingLocation = false
                    onAction(DiscoverAction.OnLocationFetched(coordinate.latitude, coordinate.longitude))

                    if (!hasMovedCameraThisFetch) {
                        val currentPosition = mapState.cameraPosition
                        val userPosition = currentPosition.copy(
                            target = Position(
                                latitude = coordinate.latitude,
                                longitude = coordinate.longitude
                            ),
                            zoom = 14.0
                        )

                        if (isLocateMeTap) {
                            mapState.animateCamera(
                                update = CameraUpdate(target = userPosition.target, zoom = userPosition.zoom),
                                animation = CameraAnimation.Fly(duration = currentPosition.flightDurationTo(userPosition))
                            )
                        } else if (!hasCenteredOnUser) {
                            // Opening the screen lands on the user straight away
                            mapState.setCameraPosition(userPosition)
                        }
                        hasMovedCameraThisFetch = true
                        hasCenteredOnUser = true
                    }
                }
                .onFailure { error ->
                    isFindingLocation = false
                    onAction(DiscoverAction.OnLocationUnavailable)

                    val message = error.toUiText() ?: return@onFailure
                    val flashResult = snackbarHostState.showFlashMessage(
                        message = message.asStringAsync(),
                        type = LynkFlashType.Error,
                        // Only a refused permission is something they can go and change.
                        actionLabel = openSettingsLabel.takeIf { error == LocationError.PERMISSION_DENIED }
                    )
                    if (flashResult == SnackbarResult.ActionPerformed) {
                        permissionController.openAppSettings()
                    }
                }
        }
    }

    LocationPermissionEffect(isEnabled = true) { resolvedPermissionState ->
        permissionState = resolvedPermissionState
        if (resolvedPermissionState == PermissionState.PERMANENTLY_DENIED) {
            showSettingsDialog = true
        }
    }

    LaunchedEffect(permissionState) {
        if (permissionState == PermissionState.GRANTED) {
            fetchCurrentLocationAndShowOnMap(false)
        }
    }

    val selectedSpot = state.selectedSpotId?.let { selectedId ->
        state.searchResults.find { it.id == selectedId }
            ?: state.trendingSpots.find { it.id == selectedId }
    }

    val hangoutCreationSpot = state.hangoutCreationSpotId?.let { hangoutSpotId ->
        state.searchResults.find { it.id == hangoutSpotId }
            ?: state.trendingSpots.find { it.id == hangoutSpotId }
    }

    val showTrendingError = state.trendingError != null &&
            state.trendingSpots.isEmpty() &&
            !state.isTrendingLoading

    val userLatitude = state.userLatitude
    val userLongitude = state.userLongitude

    val isSearchActive = state.searchTextState.text.isNotBlank() ||
            state.selectedCategory != null ||
            state.selectedPriceLevel != null

    val spotMapInteractions = rememberSpotMapInteractions(mapState) { spotId ->
        hapticFeedback(AppHaptic.ImpactLight)
        onAction(DiscoverAction.OnSpotSelected(spotId))
    }

    LynkScaffold(
        snackbarHostState = snackbarHostState,
        applyHorizontalInsets = false
    ) { scaffoldPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            MaplibreMap(
                modifier = Modifier.fillMaxSize(),
                state = mapState,
                cameraConstraints = CameraConstraints(
                    minZoom = 2.0,
                    maxZoom = 20.0,
                    minPitch = 0.0,
                    maxPitch = 60.0
                ),
                interactions = spotMapInteractions,
                viewportInsets = WindowInsets(top = scaffoldPadding.calculateTopPadding() + 72.dp)
                    .union(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                    .asPaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(LocalViewportInsets.current)
                        .padding(horizontal = MapOverlay.Spacing)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .height(48.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        DisappearingScaleBar(
                            metersPerDp = { mapState.viewport?.metersPerDpAtTarget ?: 0.0 },
                            zoom = { mapState.cameraPosition.zoom },
                            color = MaterialTheme.colorScheme.onSurface,
                            haloColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                            textStyle = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                    DisappearingCompassButton(
                        style = CompassButtonStyle(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        ),
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }

                if (userLatitude != null && userLongitude != null) {
                    UserLocationMapMarker(
                        userLatitude = userLatitude,
                        userLongitude = userLongitude,
                        pulseKey = state.locationFetchEpoch
                    )
                }

                SelectedSpotPinOverlay(spot = selectedSpot)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .clearAndSetSemantics { }
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClickLabel = stringResource(Res.string.open_spot_search),
                            role = Role.Button,
                            onClick = {
                                hapticFeedback(AppHaptic.ImpactLight)
                                onAction(DiscoverAction.ToggleShowSearchSheet)
                            }
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
                items = persistentListOf(
                    LynkDropDownItem(
                        title = stringResource(Res.string.osm_attribution),
                        onClick = {
                            hapticFeedback(AppHaptic.ImpactLight)
                            uriHandler.openUri(OSM_COPYRIGHT_LINK)
                        }
                    ),
                    LynkDropDownItem(
                        title = stringResource(Res.string.maptiler_attribution),
                        onClick = {
                            hapticFeedback(AppHaptic.ImpactLight)
                            uriHandler.openUri(MAPTILER_COPYRIGHT_LINK)
                        }
                    )
                ),
                anchor = {
                    Row(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                            .clickable(
                                onClickLabel = stringResource(Res.string.show_map_attribution),
                                role = Role.Button
                            ) {
                                hapticFeedback(AppHaptic.ImpactLight)
                                showAttributionMenu = true
                            }
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
                    hapticFeedback(AppHaptic.ImpactLight)
                    if (permissionState == PermissionState.GRANTED) {
                        scope.launch { fetchCurrentLocationAndShowOnMap(true) }
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

            if (isFindingLocation) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = mainShellPadding.calculateBottomPadding() + 16.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LynkProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    LynkText(
                        text = stringResource(Res.string.finding_your_location),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            if (showTrendingError) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 16.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    LynkErrorState(
                        title = stringResource(Res.string.trending_load_error_title),
                        message = state.trendingError.asString(),
                        onRetry = {
                            hapticFeedback(AppHaptic.ImpactLight)
                            onAction(DiscoverAction.RetryTrending)
                        }
                    )
                }
            }
        }

        // Overlays & Sheets
        state.guestPromptContext?.let { guestPromptContext ->
            val actionStr = when (guestPromptContext) {
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
                searchTextState = state.searchTextState,
                spots = if (isSearchActive) state.searchResults else state.trendingSpots,
                isSearchActive = isSearchActive,
                isSearchLoading = state.isSearchLoading,
                searchError = state.searchError?.asString(),
                searchEndReached = state.searchEndReached,
                searchResetEpoch = state.searchResetEpoch,
                selectedCategory = state.selectedCategory,
                selectedPriceLevel = state.selectedPriceLevel,
                userLatitude = state.userLatitude,
                userLongitude = state.userLongitude,
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

        selectedSpot?.let { spot ->
            SpotDetailSheet(
                spot = spot,
                userLat = state.userLatitude,
                userLng = state.userLongitude,
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

        hangoutCreationSpot?.let { hangoutSpot ->
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
