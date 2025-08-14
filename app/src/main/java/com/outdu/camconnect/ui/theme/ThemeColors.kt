package com.outdu.camconnect.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * Theme-aware color access system
 * Automatically provides the correct colors based on current theme (dark/light)
 * 
 * Usage: AppColors.VeryDarkBackground instead of hardcoded colors
 * This will automatically resolve to the correct color for the current theme
 */
object AppColors {

    val StravionBlue: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.StravionBlue else LightColors.StravionBlue
    val BorderColor: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.BorderColor else LightColors.BorderColor
    val immersiveButtonBorderColor: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.immersiveButtonBorderColor else LightColors.immersiveButtonBorderColor

    // Material Design Theme Colors
    val Purple80: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.Purple80 else LightColors.Purple80
    val PurpleGrey80: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.PurpleGrey80 else LightColors.PurpleGrey80
    val Pink80: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.Pink80 else LightColors.Pink80
    val Purple40: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.Purple40 else LightColors.Purple40
    val PurpleGrey40: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.PurpleGrey40 else LightColors.PurpleGrey40
    val Pink40: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.Pink40 else LightColors.Pink40

    // Background Colors
    val VeryDarkBackground: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.VeryDarkBackground else LightColors.VeryDarkBackground
    val DarkBackground1: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.DarkBackground1 else LightColors.DarkBackground1
    val DarkBackground2: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.DarkBackground2 else LightColors.DarkBackground2
    val DarkBackground3: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.DarkBackground3 else LightColors.DarkBackground3
    val MediumDarkBackground: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.MediumDarkBackground else LightColors.MediumDarkBackground
    val SurfaceBackground: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.SurfaceBackground else LightColors.SurfaceBackground
    val CardBackground: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.CardBackground else LightColors.CardBackground

    // Text Colors
    val PrimaryText: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.PrimaryText else LightColors.PrimaryText
    val SecondaryText: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.SecondaryText else LightColors.SecondaryText
    val DisabledText: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.DisabledText else LightColors.DisabledText
    val OnBackground: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.OnBackground else LightColors.OnBackground
    val OnSurface: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.OnSurface else LightColors.OnSurface
    val AIButtonTextColor: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.AIButtonTextColor else LightColors.AIButtonTextColor

    // Primary & Accent Colors
    val BluePrimary: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.BluePrimary else LightColors.BluePrimary
    val BlueVariant: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.BlueVariant else LightColors.BlueVariant
    val BrightBlue: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.BrightBlue else LightColors.BrightBlue

    // Status Colors - Battery & Connection
    val BatteryRed: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.BatteryRed else LightColors.BatteryRed
    val BatteryYellow: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.BatteryYellow else LightColors.BatteryYellow
    val BatteryOrange: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.BatteryOrange else LightColors.BatteryOrange
    val BatteryGreen: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.BatteryGreen else LightColors.BatteryGreen
    val ConnectionGreen: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.ConnectionGreen else LightColors.ConnectionGreen
    val ConnectionRed: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.ConnectionRed else LightColors.ConnectionRed

    // Record & Alert Colors
    val RecordRed: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.RecordRed else LightColors.RecordRed
    val RedVariant: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.RedVariant else LightColors.RedVariant
    val BrightRed: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.BrightRed else LightColors.BrightRed
    val AlertYellow: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.AlertYellow else LightColors.AlertYellow
    val BrightGreen: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.BrightGreen else LightColors.BrightGreen

    // AI & Gradient Colors
    val AiGradientStart: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.AiGradientStart else LightColors.AiGradientStart
    val AiGradientEnd: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.AiGradientEnd else LightColors.AiGradientEnd
    val SpyBlue: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.SpyBlue else LightColors.SpyBlue

    // Gray Variations
    val LightGray: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.LightGray else LightColors.LightGray
    val LightGray2: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.LightGray2 else LightColors.LightGray2
    val MediumLightGray: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.MediumLightGray else LightColors.MediumLightGray
    val MediumGray: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.MediumGray else LightColors.MediumGray
    val MediumGray2: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.MediumGray2 else LightColors.MediumGray2
    val MediumGray3: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.MediumGray3 else LightColors.MediumGray3
    val MediumGray4: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.MediumGray4 else LightColors.MediumGray4
    val DarkGray: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.DarkGray else LightColors.DarkGray
    val DarkGray2: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.DarkGray2 else LightColors.DarkGray2
    val DarkGray3: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.DarkGray3 else LightColors.DarkGray3
    val DarkSlate: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.DarkSlate else LightColors.DarkSlate
    val Gray: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.Gray else LightColors.Gray

    // Standard Colors
    val White: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.White else LightColors.White
    val Black: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.Black else LightColors.Black

    //Button Colors
    val ButtonSelectedBgColor: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.ButtonSelectedBgColor else LightColors.ButtonSelectedBgColor
    val ButtonSelectedIconColor: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.ButtonSelectedIconColor else LightColors.ButtonSelectedIconColor
    val ButtonBgColor: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.ButtonBgColor else LightColors.ButtonBgColor
    val ButtonIconColor: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.ButtonIconColor else LightColors.ButtonIconColor
    val ButtonBorderColor: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.ButtonBorderColor else LightColors.ButtonBorderColor

    // Toggle Icon Colors
    val IconOnSelected: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.IconOnSelected else LightColors.IconOnSelected
    // XML Drawable Colors
    val DrawableWhite: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.DrawableWhite else LightColors.DrawableWhite
    val DrawableBatteryYellow: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.DrawableBatteryYellow else LightColors.DrawableBatteryYellow
    val DrawableRecordRed: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.DrawableRecordRed else LightColors.DrawableRecordRed
    val DrawableRouterRed: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.DrawableRouterRed else LightColors.DrawableRouterRed
    val DrawableEarthGreen: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.DrawableEarthGreen else LightColors.DrawableEarthGreen
    val DrawableExpandGray: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.DrawableExpandGray else LightColors.DrawableExpandGray
    val DrawableSettingsGray: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.DrawableSettingsGray else LightColors.DrawableSettingsGray
    val DrawableEyeGray: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.DrawableEyeGray else LightColors.DrawableEyeGray
    val DrawableSliderGray: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.DrawableSliderGray else LightColors.DrawableSliderGray
    val DrawableLauncherGreen: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.DrawableLauncherGreen else LightColors.DrawableLauncherGreen
    val DrawableTransparent: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.DrawableTransparent else LightColors.DrawableTransparent
    val DrawableSemiTransparentBlack: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.DrawableSemiTransparentBlack else LightColors.DrawableSemiTransparentBlack
    val DrawableSemiTransparentWhite: Color @Composable @ReadOnlyComposable get() = if (isSystemInDarkTheme()) DarkColors.DrawableSemiTransparentWhite else LightColors.DrawableSemiTransparentWhite
}

/**
 * Non-Composable colors for use in data classes, default parameters, and other non-Composable contexts
 * These use dark theme colors as defaults but can be overridden at usage points
 */
object DefaultColors {
    // Use dark theme colors as defaults for data classes
    val BluePrimary = DarkColors.BluePrimary
    val MediumGray2 = DarkColors.MediumGray2
    val SpyBlue = DarkColors.SpyBlue
    val RecordRed = DarkColors.RecordRed
    val Gray = DarkColors.Gray
    val White = DarkColors.White
    val Black = DarkColors.Black
    val MediumDarkBackground = DarkColors.MediumDarkBackground
    val DarkGray = DarkColors.DarkGray
    val IconOnSelected = LightColors.IconOnSelected
}

// Legacy aliases for backward compatibility - Composable versions
// These maintain the existing naming convention while using the new theme system
val VeryDarkBackground: Color @Composable @ReadOnlyComposable get() = AppColors.VeryDarkBackground
val DarkBackground1: Color @Composable @ReadOnlyComposable get() = AppColors.DarkBackground1
val DarkBackground2: Color @Composable @ReadOnlyComposable get() = AppColors.DarkBackground2
val DarkBackground3: Color @Composable @ReadOnlyComposable get() = AppColors.DarkBackground3
val MediumDarkBackground: Color @Composable @ReadOnlyComposable get() = AppColors.MediumDarkBackground
val LightGrayBackground: Color @Composable @ReadOnlyComposable get() = AppColors.SurfaceBackground

// Text Colors - Legacy aliases
val White: Color @Composable @ReadOnlyComposable get() = AppColors.PrimaryText
val Black: Color @Composable @ReadOnlyComposable get() = AppColors.Black

// All other colors remain the same
val BluePrimary: Color @Composable @ReadOnlyComposable get() = AppColors.BluePrimary
val BlueVariant: Color @Composable @ReadOnlyComposable get() = AppColors.BlueVariant
val BrightBlue: Color @Composable @ReadOnlyComposable get() = AppColors.BrightBlue
val BatteryRed: Color @Composable @ReadOnlyComposable get() = AppColors.BatteryRed
val BatteryYellow: Color @Composable @ReadOnlyComposable get() = AppColors.BatteryYellow
val BatteryOrange: Color @Composable @ReadOnlyComposable get() = AppColors.BatteryOrange
val BatteryGreen: Color @Composable @ReadOnlyComposable get() = AppColors.BatteryGreen
val ConnectionGreen: Color @Composable @ReadOnlyComposable get() = AppColors.ConnectionGreen
val ConnectionRed: Color @Composable @ReadOnlyComposable get() = AppColors.ConnectionRed
val RecordRed: Color @Composable @ReadOnlyComposable get() = AppColors.RecordRed
val RedVariant: Color @Composable @ReadOnlyComposable get() = AppColors.RedVariant
val BrightRed: Color @Composable @ReadOnlyComposable get() = AppColors.BrightRed
val AlertYellow: Color @Composable @ReadOnlyComposable get() = AppColors.AlertYellow
val BrightGreen: Color @Composable @ReadOnlyComposable get() = AppColors.BrightGreen
val AiGradientStart: Color @Composable @ReadOnlyComposable get() = AppColors.AiGradientStart
val AiGradientEnd: Color @Composable @ReadOnlyComposable get() = AppColors.AiGradientEnd
val SpyBlue: Color @Composable @ReadOnlyComposable get() = AppColors.SpyBlue
val LightGray: Color @Composable @ReadOnlyComposable get() = AppColors.LightGray
val LightGray2: Color @Composable @ReadOnlyComposable get() = AppColors.LightGray2
val MediumLightGray: Color @Composable @ReadOnlyComposable get() = AppColors.MediumLightGray
val MediumGray: Color @Composable @ReadOnlyComposable get() = AppColors.MediumGray
val MediumGray2: Color @Composable @ReadOnlyComposable get() = AppColors.MediumGray2
val MediumGray3: Color @Composable @ReadOnlyComposable get() = AppColors.MediumGray3
val MediumGray4: Color @Composable @ReadOnlyComposable get() = AppColors.MediumGray4
val DarkGray: Color @Composable @ReadOnlyComposable get() = AppColors.DarkGray
val DarkGray2: Color @Composable @ReadOnlyComposable get() = AppColors.DarkGray2
val DarkGray3: Color @Composable @ReadOnlyComposable get() = AppColors.DarkGray3
val DarkSlate: Color @Composable @ReadOnlyComposable get() = AppColors.DarkSlate
val Gray: Color @Composable @ReadOnlyComposable get() = AppColors.Gray 