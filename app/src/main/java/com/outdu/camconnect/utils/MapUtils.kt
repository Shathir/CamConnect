package com.outdu.camconnect.utils

import com.google.android.gms.maps.model.LatLng
import kotlin.math.asin
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.atan2

fun calculateOffsetLocation(
    origin: LatLng,
    distanceMeters: Double,
    bearingDegrees: Double
): LatLng {
    val radius = 6371000.0 // Earth radius in meters
    val bearingRad = Math.toRadians(bearingDegrees)
    val latRad = Math.toRadians(origin.latitude)
    val lonRad = Math.toRadians(origin.longitude)

    val angularDistance = distanceMeters / radius

    val destLat = asin(
        sin(latRad) * cos(angularDistance) +
                cos(latRad) * sin(angularDistance) * cos(bearingRad)
    )

    val destLon = lonRad + atan2(
        sin(bearingRad) * sin(angularDistance) * cos(latRad),
        cos(angularDistance) - sin(latRad) * sin(destLat)
    )

    return LatLng(Math.toDegrees(destLat), Math.toDegrees(destLon))
}