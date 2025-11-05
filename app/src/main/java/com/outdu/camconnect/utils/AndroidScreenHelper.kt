package com.outdu.camconnect.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration



enum class DeviceType {PHONE, TABLET}

@Composable
fun rememberDeviceType(): DeviceType {
    val configuration = LocalConfiguration.current
    val shortestWidthDp = minOf(configuration.screenWidthDp, configuration.screenHeightDp)

    return when {
        shortestWidthDp < 600 -> DeviceType.PHONE
        else -> DeviceType.TABLET
    }
}
