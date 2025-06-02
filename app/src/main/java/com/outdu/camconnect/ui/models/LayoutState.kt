package com.outdu.camconnect.ui.models

/**
 * Represents the different layout modes for the camera streaming app
 */
enum class LayoutMode {
    MINIMAL_CONTROL,    // Layout 1: Stream Focused (90% left, 10% right)
    EXPANDED_CONTROL,   // Layout 2: Interactive Controls (60% left, 40% right)
    FULL_CONTROL        // Layout 3: Settings and Configuration (45% left, 55% right)
}

/**
 * Data class to manage the layout state and proportions
 */
data class LayoutState(
    val mode: LayoutMode = LayoutMode.MINIMAL_CONTROL,
    val leftPaneWeight: Float = when (mode) {
        LayoutMode.MINIMAL_CONTROL -> 0.9f
        LayoutMode.EXPANDED_CONTROL -> 0.6f
        LayoutMode.FULL_CONTROL -> 0.45f
    },
    val rightPaneWeight: Float = when (mode) {
        LayoutMode.MINIMAL_CONTROL -> 0.1f
        LayoutMode.EXPANDED_CONTROL -> 0.4f
        LayoutMode.FULL_CONTROL -> 0.55f
    }
) {
    companion object {
        fun getLayoutProportions(mode: LayoutMode): Pair<Float, Float> {
            return when (mode) {
                LayoutMode.MINIMAL_CONTROL -> 0.9f to 0.1f
                LayoutMode.EXPANDED_CONTROL -> 0.6f to 0.4f
                LayoutMode.FULL_CONTROL -> 0.45f to 0.55f
            }
        }
    }
} 