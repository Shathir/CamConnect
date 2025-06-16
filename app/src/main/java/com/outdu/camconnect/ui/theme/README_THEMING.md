# CamConnect Theming System

## Overview

The CamConnect app now features a comprehensive theming system that automatically adapts to the device's dark/light mode settings. This system provides consistent color management and easy customization for both dark and light themes.

## File Structure

```
ui/theme/
├── ColorsDark.kt      # Dark theme color definitions
├── ColorsLight.kt     # Light theme color definitions  
├── ThemeColors.kt     # Theme-aware color access system
├── Color.kt           # Legacy compatibility (deprecated)
├── Theme.kt           # Main theme configuration
└── README_THEMING.md  # This documentation
```

## How It Works

### 1. Automatic Theme Detection
The system automatically detects whether the device is in dark or light mode using `isSystemInDarkTheme()` and applies the appropriate colors.

### 2. Color Categories

#### Background Colors
- **Dark Theme**: Dark backgrounds (`0xFF0D0D0D`, `0xFF222222`, etc.)
- **Light Theme**: White and light gray backgrounds

#### Text Colors  
- **Dark Theme**: White text on dark backgrounds
- **Light Theme**: Black text on light backgrounds

#### Status & Accent Colors
- **Both Themes**: Same colors (but defined separately for future customization)
- Includes battery indicators, connection status, record buttons, etc.

## Usage Examples

### Basic Usage (Recommended)
```kotlin
@Composable
fun MyComponent() {
    Box(
        modifier = Modifier
            .background(VeryDarkBackground) // Automatically theme-aware
            .padding(16.dp)
    ) {
        Text(
            text = "Hello World",
            color = White // Automatically theme-aware (white in dark, black in light)
        )
    }
}
```

### Advanced Usage with AppColors
```kotlin
@Composable
fun AdvancedComponent() {
    Box(
        modifier = Modifier
            .background(AppColors.VeryDarkBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "Advanced Text",
            color = AppColors.PrimaryText
        )
    }
}
```

### Using CamConnectTheme
Always wrap your app content with `CamConnectTheme`:

```kotlin
setContent {
    CamConnectTheme {
        // Your app content here
        MyScreen()
    }
}
```

## Customizing Colors

### For Dark Theme
Edit `ColorsDark.kt`:
```kotlin
object DarkColors {
    val VeryDarkBackground = Color(0xFF0D0D0D) // Your custom dark color
    val PrimaryText = Color.White
    // ... other colors
}
```

### For Light Theme  
Edit `ColorsLight.kt`:
```kotlin
object LightColors {
    val VeryDarkBackground = Color.White // Maps to white in light theme
    val PrimaryText = Color.Black
    // ... other colors
}
```

## Available Colors

### Background Colors
- `VeryDarkBackground` - Main app background
- `DarkBackground1` - Secondary backgrounds
- `DarkBackground2` - Card backgrounds
- `DarkBackground3` - Component backgrounds
- `MediumDarkBackground` - Button backgrounds
- `CardBackground` - Card-specific background

### Text Colors
- `PrimaryText` - Main text color (white/black based on theme)
- `SecondaryText` - Secondary text color
- `DisabledText` - Disabled state text

### Status Colors
- `BatteryRed`, `BatteryYellow`, `BatteryOrange`, `BatteryGreen`
- `ConnectionGreen`, `ConnectionRed`
- `RecordRed`, `AlertYellow`

### Accent Colors
- `BluePrimary`, `BlueVariant`, `BrightBlue`
- `AiGradientStart`, `AiGradientEnd`, `SpyBlue`

### Gray Variations
- `LightGray`, `MediumGray`, `DarkGray` (with numbered variations)

## Migration from Old System

### Automatic Migration
Most existing code continues to work without changes:

```kotlin
// This still works and is now theme-aware
.background(VeryDarkBackground)
.color(White)
```

### Recommended Updates
For new code, prefer explicit theme-aware access:

```kotlin
// Old (still works)
.background(Color(0xFF2196F3))

// New (recommended)
.background(BluePrimary)

// Advanced (explicit)
.background(AppColors.BluePrimary)
```

## Best Practices

1. **Always use CamConnectTheme**: Wrap your content with `CamConnectTheme { }`

2. **Use semantic color names**: Prefer `BluePrimary` over hex codes

3. **Test both themes**: Verify your UI works in both dark and light modes

4. **Customize in theme files**: Don't hardcode colors in components

5. **Use theme-aware properties**: Leverage the automatic theme switching

## Testing Themes

### In Android Studio Preview
```kotlin
@Preview(name = "Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Light Theme", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun MyComponentPreview() {
    CamConnectTheme {
        MyComponent()
    }
}
```

### On Device
- Go to device Settings → Display → Dark theme
- Toggle between light and dark modes
- Verify your app adapts correctly

## Future Enhancements

The theming system supports:

- **Custom theme variants**: Add seasonal or branded themes
- **Per-component customization**: Override colors for specific components  
- **Runtime theme switching**: Allow users to manually select themes
- **Accessibility themes**: High contrast or colorblind-friendly variants

## Troubleshooting

### Colors not changing with theme
- Ensure you're using `CamConnectTheme { }`
- Check you're importing from `com.outdu.camconnect.ui.theme.*`
- Verify you're not using hardcoded `Color()` values

### Import errors
- Clean and rebuild the project
- Verify all theme files are in the correct package
- Check for naming conflicts

## Support

For questions about the theming system:
1. Check this documentation
2. Review the example usage in `MainActivity.kt`
3. Examine existing components for patterns
4. Test color changes in both `ColorsDark.kt` and `ColorsLight.kt` 