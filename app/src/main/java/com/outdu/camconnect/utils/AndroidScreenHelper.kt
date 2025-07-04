package com.outdu.camconnect.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration


enum class DeviceType {PHONE, TABLET}

@Composable
fun rememberDeviceType(): DeviceType {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    return if(screenWidth < 600) DeviceType.PHONE else DeviceType.TABLET
}