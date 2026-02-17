package com.outdu.camconnect.ui.components.camera

import androidx.compose.runtime.State
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf

/**
 * Manages the visibility state of tooltips to ensure only one tooltip is visible at a time.
 * This provides a centralized way to coordinate multiple tooltip components.
 */
@Stable
class TooltipManager {
    private val _activeTooltipId = mutableStateOf<String?>(null)
    val activeTooltipId: State<String?> = _activeTooltipId
    
    /**
     * Checks if the specified tooltip is currently visible
     * @param id Unique identifier for the tooltip
     * @return true if this tooltip is active, false otherwise
     */
    fun isTooltipVisible(id: String): Boolean {
        return _activeTooltipId.value == id
    }
    
    /**
     * Toggles the visibility of the specified tooltip.
     * If the tooltip is currently visible, it will be closed.
     * If it's not visible, it will be opened and any other tooltip will be closed.
     * @param id Unique identifier for the tooltip
     */
    fun toggleTooltip(id: String) {
        _activeTooltipId.value = if (_activeTooltipId.value == id) null else id
    }
    
    /**
     * Closes the specified tooltip if it's currently visible
     * @param id Unique identifier for the tooltip
     */
    fun closeTooltip(id: String) {
        if (_activeTooltipId.value == id) {
            _activeTooltipId.value = null
        }
    }
    
    /**
     * Opens the specified tooltip, closing any other tooltip that's currently visible
     * @param id Unique identifier for the tooltip
     */
    fun openTooltip(id: String) {
        _activeTooltipId.value = id
    }
}
