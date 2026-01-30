package com.outdu.camconnect.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Single source of truth for "is system dark theme?" inside Compose.
 *
 * This value is provided once from [CamConnectTheme] and should be used everywhere else,
 * instead of calling `isSystemInDarkTheme()` directly in multiple composables.
 */
val LocalCamConnectDarkTheme = staticCompositionLocalOf { false }

@Composable
@ReadOnlyComposable
fun camConnectIsDarkTheme(): Boolean = LocalCamConnectDarkTheme.current


