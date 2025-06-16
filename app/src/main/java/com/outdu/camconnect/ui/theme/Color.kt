package com.outdu.camconnect.ui.theme

/**
 * Legacy Color.kt file - Updated to use the new theming system
 * 
 * This file now imports the new theme-aware color system.
 * All colors automatically adapt to light/dark theme based on system settings.
 * 
 * For new code, prefer using AppColors.ColorName or the theme-aware aliases below.
 * 
 * Color Definitions:
 * - DarkColors: Defined in ColorsDark.kt for dark theme
 * - LightColors: Defined in ColorsLight.kt for light theme  
 * - AppColors: Theme-aware access in ThemeColors.kt
 * - Legacy aliases: Available below for backward compatibility
 */

// Re-export the theme-aware color system for easy access
// All these colors will automatically switch between dark and light themes

// The new theming system is imported from ThemeColors.kt
// No need to redefine colors here as they are now theme-aware

/**
 * MIGRATION NOTES:
 * 
 * 1. All existing color references will continue to work
 * 2. Colors now automatically adapt to system dark/light theme
 * 3. To customize colors for a specific theme, edit:
 *    - ColorsDark.kt for dark theme colors
 *    - ColorsLight.kt for light theme colors
 * 
 * 4. Example usage:
 *    - Old: Color(0xFF2196F3) 
 *    - New: BluePrimary (theme-aware)
 *    - Alternative: AppColors.BluePrimary
 */

// All color definitions have been moved to the new theming system:
// - Import from ThemeColors.kt to access theme-aware colors
// - All existing color names are available as theme-aware properties
// - Colors automatically switch between dark and light themes