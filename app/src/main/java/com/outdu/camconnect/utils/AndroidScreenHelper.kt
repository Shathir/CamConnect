package com.outdu.camconnect.utils

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.window.layout.WindowMetricsCalculator
import android.util.Log
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass


enum class DeviceType {PHONE, TABLET, FOLDABLE}

@Composable
fun rememberDeviceType1(): DeviceType {
    val screenWidth = LocalConfiguration.current.screenWidthDp

    return if(screenWidth < 600) DeviceType.PHONE else DeviceType.TABLET
}



enum class DeviceType1 { PHONE, FOLDABLE, TABLET }

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun rememberDeviceType2(): DeviceType {
    val activity = LocalContext.current as Activity
    val windowSizeClass = calculateWindowSizeClass(activity)

    return when (windowSizeClass.heightSizeClass) {
        WindowHeightSizeClass.Compact -> DeviceType.PHONE
        WindowHeightSizeClass.Medium -> DeviceType.FOLDABLE // ← usually foldables like Pixel Fold, Z Fold
        WindowHeightSizeClass.Expanded -> DeviceType.TABLET
        else -> DeviceType.PHONE
    }
}



@Composable
fun rememberDeviceType(): DeviceType {
    val configuration = LocalConfiguration.current
    val shortestWidthDp = minOf(configuration.screenWidthDp, configuration.screenHeightDp)

    Log.d("Density Calculator", "Shortest width: $shortestWidthDp")
    return when {
        shortestWidthDp < 600 -> DeviceType.PHONE
        else -> DeviceType.TABLET
    }
}
