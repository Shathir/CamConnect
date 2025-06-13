package com.outdu.camconnect.ui.layouts.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style

@SuppressLint("MissingPermission")
@Composable
fun MapLibreTrackingScreen(
    onSpeedUpdate: (Float) -> Unit = {},
    onLocationUpdate: (Location) -> Unit = {},
    onDirectionUpdate: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationState = remember { mutableStateOf<Location?>(null) }
    val directionState = remember { mutableStateOf("N") }
    val speedState = remember { mutableStateOf(0f) }
    val previousLocation = remember { mutableStateOf<Location?>(null) }
    val mapView = remember { MapView(context) }
    val MIN_VALID_SPEED = 0.5f

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    locationState.value = location

                    // Calculate speed with validation
                    val rawSpeed = location.speed
                    val calculatedSpeed = if (rawSpeed >= MIN_VALID_SPEED) {
                        rawSpeed
                    } else {
                        calculateSpeedFromLocation1(previousLocation.value, location)
                    }

                    val validSpeed = if (calculatedSpeed >= MIN_VALID_SPEED) calculatedSpeed else 0f
                    speedState.value = smoothSpeed1(speedState.value, validSpeed)
                    
                    val direction = getCompassDirection1(location.bearing)
                    directionState.value = direction

                    // Notify parent components
                    onSpeedUpdate(validSpeed)
                    onLocationUpdate(location)
                    onDirectionUpdate(direction)

                    previousLocation.value = location
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fusedClient.requestLocationUpdates(
                LocationRequest.create().apply {
                    interval = 2000
                    fastestInterval = 1000
                    priority = Priority.PRIORITY_HIGH_ACCURACY
                },
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    // Request location permission on launch
    LaunchedEffect(Unit) {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        val permissionResult = ContextCompat.checkSelfPermission(context, permission)
        if (permissionResult == PackageManager.PERMISSION_GRANTED) {
            fusedClient.requestLocationUpdates(
                LocationRequest.create().apply {
                    interval = 2000
                    fastestInterval = 1000
                    priority = Priority.PRIORITY_HIGH_ACCURACY
                },
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            permissionLauncher.launch(permission)
        }
    }

    // Clean up location updates when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            fusedClient.removeLocationUpdates(locationCallback)
            mapView.onDestroy()
        }
    }

    Column(Modifier.fillMaxSize()) {
        // Speed and direction display
        Text(
            text = "Speed: ${"%.1f".format(speedState.value * 3.6f)} km/h | Dir: ${directionState.value}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(8.dp)
        )

        // MapLibre view
        AndroidView(
            factory = {
                mapView.apply {
                    onCreate(null)
                    onResume()
                    
                    getMapAsync { map ->
                        map.setStyle(Style.Builder()) { style: Style ->
                            // Enable UI controls
                            map.uiSettings.apply {
                                isCompassEnabled = true
                                isZoomGesturesEnabled = true
                                isScrollGesturesEnabled = true
                                isRotateGesturesEnabled = true
                            }

                            // Setup location component
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                map.locationComponent.apply {
                                    activateLocationComponent(
                                        LocationComponentActivationOptions.builder(context, style)
                                            .build()
                                    )
                                    isLocationComponentEnabled = true
                                    cameraMode = CameraMode.TRACKING
                                    renderMode = RenderMode.COMPASS
                                }
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Update camera position when location changes
        LaunchedEffect(locationState.value) {
            locationState.value?.let { location ->
                val latLng = LatLng(location.latitude, location.longitude)
                mapView.getMapAsync { map ->
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(latLng, 16.0)
                    )
                }
            }
        }
    }
}

// Helper functions (reusing existing logic patterns)
private fun getCompassDirection1(degrees: Float): String {
    return when ((degrees % 360 + 360) % 360) {
        in 0f..22.5f -> "N"
        in 22.5f..67.5f -> "NE"
        in 67.5f..112.5f -> "E"
        in 112.5f..157.5f -> "SE"
        in 157.5f..202.5f -> "S"
        in 202.5f..247.5f -> "SW"
        in 247.5f..292.5f -> "W"
        in 292.5f..337.5f -> "NW"
        else -> "N"
    }
}

private fun calculateSpeedFromLocation1(previous: Location?, current: Location): Float {
    if (previous == null) return 0f
    val deltaTime = (current.time - previous.time) / 1000f // seconds
    if (deltaTime <= 0f) return 0f

    val distance = previous.distanceTo(current) // meters
    return distance / deltaTime // m/s
}

private fun smoothSpeed1(prev: Float, new: Float, alpha: Float = 0.8f): Float {
    return alpha * prev + (1 - alpha) * new
}
