package com.outdu.camconnect.utils

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.platform.LocalContext

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
fun rememberDeviceType(): DeviceType {
    val activity = LocalContext.current as Activity
    val windowSizeClass = calculateWindowSizeClass(activity)

    return when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> DeviceType.PHONE
        WindowWidthSizeClass.Medium -> DeviceType.FOLDABLE // ← usually foldables like Pixel Fold, Z Fold
        WindowWidthSizeClass.Expanded -> DeviceType.TABLET
        else -> DeviceType.PHONE
    }
}