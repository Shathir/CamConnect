package com.outdu.camconnect.ui.layouts.maps

import android.annotation.SuppressLint
import android.os.Looper
import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.Canvas
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.outdu.camconnect.R
import com.outdu.camconnect.ui.models.MapType
import com.outdu.camconnect.utils.calculateOffsetLocation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("MissingPermission")
@Composable
fun LiveTrackingMap(
    onSpeedUpdate: (Float) -> Unit = {}
) {
    val context = LocalContext.current
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val mapTypeState = rememberSaveable { mutableStateOf(MapType.NORMAL) }
    val userLocation = remember { mutableStateOf<LatLng?>(null) }
    val directionLabel = remember { mutableStateOf("Unknown") }
    val currentTime = remember { mutableStateOf<Date?>(null) }
    val mapRef = remember { mutableStateOf<GoogleMap?>(null) }
    val shouldFollowLocation = remember { mutableStateOf(true) }
    val speed = remember { mutableStateOf(0f) }
    val prevLocation = remember { mutableStateOf<Location?>(null) }
    val MIN_VALID_SPEED = 1f

    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    userLocation.value = LatLng(it.latitude, it.longitude)
                    currentTime.value = Date(it.time)
                    directionLabel.value = getCompassDirection(it.bearing)

                    val rawSpeed = it.speed

                    val validSpeed = if (rawSpeed >= MIN_VALID_SPEED) rawSpeed else 0f
//                    speed.value = smoothSpeed(speed.value, validSpeed) // to km/h
                    speed.value = validSpeed
                    onSpeedUpdate(validSpeed) // Pass speed to parent component
                    prevLocation.value = it
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

    // Request location on launch
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // Update map type
//    LaunchedEffect(mapTypeState.value) {
//        mapRef.value?.mapType = mapTypeState.value.googleMapType
//    }

    Box(modifier = Modifier.fillMaxSize()) {
//        MapTypeSelector(currentMapType = mapTypeState.value) {
//            mapTypeState.value = it
//        }
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(factory = { ctx ->
                val mapView = MapView(ctx)
                mapView.onCreate(null)
                mapView.onResume()

                mapView.getMapAsync { map ->
                    mapRef.value = map
                    map.mapType = mapTypeState.value.googleMapType
                    map.uiSettings.apply {
                        isZoomControlsEnabled = false
                        isCompassEnabled = false
                        isRotateGesturesEnabled = true
                        isMyLocationButtonEnabled = true
                    }
                    map.isMyLocationEnabled = true

                    map.setOnCameraMoveStartedListener { reason ->
                        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                            shouldFollowLocation.value = false
                        }
                    }
                }

                mapView
            }, modifier = Modifier.fillMaxSize())

            // Update map content with live location
            LaunchedEffect(userLocation.value) {
                val map = mapRef.value ?: return@LaunchedEffect
                val origin = userLocation.value ?: return@LaunchedEffect
                val detection = calculateOffsetLocation(origin, 50.0, 90.0)

                map.clear()


                val location = LatLng(12.992507, 77.692644)
                map.addMarker(
                    MarkerOptions()
                        .position(location)
                        .title("Detection Point")
                        .snippet("50 meters east of your location")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.boat1))
                )

                map.addPolyline(
                    PolylineOptions()
                        .add(origin, location)
                        .color(Color.Magenta.hashCode())
                        .width(5f)
                )


                if(shouldFollowLocation.value)
                {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(origin, 16f))
                }
            }
        }

        val formatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
        val timeString = currentTime.value?.let { formatter.format(it) } ?: "Getting GPS time..."

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .background(Color.White.copy(alpha = 0.8f))
        ) {

//                Text(
//                    text = "GPS Time: $timeString",
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.Medium,
//                    color = Color.Black,
//                    modifier = Modifier.padding(8.dp)
//                )
//                Text(
//                    text = "Direction: ${directionLabel.value}",
//                    fontSize = 16.sp,
//                    color = Color.Black,
//                    modifier = Modifier.padding(8.dp)
//                )
                Text(
                    text = "Speed: ${"%.0f".format(speed.value * 3.6)} km/h",
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(8.dp)
                )
        }
    }
}

fun calculateSpeedFromLocation(
    previous: Location?,
    current: Location
): Float {
    if (previous == null) return 0f
    val deltaTime = (current.time - previous.time) / 1000f // seconds
    if (deltaTime <= 0f) return 0f

    val distance = previous.distanceTo(current) // meters
    return distance / deltaTime // m/s
}

fun smoothSpeed(prev: Float, new: Float, alpha: Float = 0.8f): Float {
    return alpha * prev + (1 - alpha) * new
}

@Composable
fun MapTypeSelector(
    currentMapType: MapType,
    onMapTypeChange: (MapType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE0E0E0))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MapType.entries.forEach { type ->
            val isSelected = type == currentMapType

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Color(0xFF424242) else Color(0xFFBDBDBD))
                    .clickable { onMapTypeChange(type) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = type.name,
                    color = Color.White,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }
    }
}

fun getCompassDirection(degrees: Float): String {
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


