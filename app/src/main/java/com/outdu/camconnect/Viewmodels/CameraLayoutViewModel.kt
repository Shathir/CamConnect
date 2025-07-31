package com.outdu.camconnect.Viewmodels

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.outdu.camconnect.communication.MotocamAPIAndroidHelper
import com.outdu.camconnect.communication.MotocamAPIHelper
import com.outdu.camconnect.ui.models.CameraMode
import com.outdu.camconnect.ui.models.OrientationMode
import com.outdu.camconnect.ui.models.VisionMode
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.outdu.camconnect.singleton.MainActivitySingleton
import com.outdu.camconnect.utils.MemoryManager
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class CameraLayoutViewModel : ViewModel() {
    // Auto Low Light (maps to DAYMODE)
    private val _isAutoDayNightEnabled = mutableStateOf(false)
    val isAutoDayNightEnabled: State<Boolean> = _isAutoDayNightEnabled

    // Display Modes (maps to MISC)
    private val _currentVisionMode = mutableStateOf(VisionMode.VISION)
    val currentVisionMode: State<VisionMode> = _currentVisionMode

    // Camera Capture (maps to WDR and EIS)
    private val _currentCameraMode = mutableStateOf(CameraMode.OFF)
    val currentCameraMode: State<CameraMode> = _currentCameraMode

    // Orientation (maps to FLIP and MIRROR)
    private val _currentOrientationMode = mutableStateOf(OrientationMode.NORMAL)
    val currentOrientationMode: State<OrientationMode> = _currentOrientationMode

    // Track if there are unsaved changes
    private val _hasUnsavedChanges = mutableStateOf(false)
    val hasUnsavedChanges: State<Boolean> = _hasUnsavedChanges

    // Initial values to track changes
    private var initialAutoDayNight = false
    private var initialVisionMode = VisionMode.VISION
    private var initialCameraMode = CameraMode.OFF
    private var initialOrientationMode = OrientationMode.NORMAL

    // Add stream reload state
    private val _isStreamReloading = MutableStateFlow(false)
    val isStreamReloading = _isStreamReloading.asStateFlow()

    // Add state to track if UI should be interactive
    private val _isUIInteractive = MutableStateFlow(true)
    val isUIInteractive = _isUIInteractive.asStateFlow()

    private var isSurfaceFinalized = false

    // Add callback for stream control
    private var onStreamReload: (() -> Unit)? = null

    fun setStreamReloadCallback(callback: () -> Unit) {
        onStreamReload = callback
    }

    init {
        fetchCameraSettings()
    }

    private fun fetchCameraSettings() {
        viewModelScope.launch {
            try {
                MotocamAPIAndroidHelper.getConfigAsync(
                    viewModelScope,
                    type = "Current"
                ) { config, error ->
                    if (error != null) {
                        Log.e(TAG, "Error fetching camera config: $error")
                        return@getConfigAsync
                    }

                    config?.let { conf ->
                        Log.i(TAG, "Fetched camera config: $conf")

                        // Parse DAYMODE for Auto Low Light
                        val dayMode = conf["DAYMODE"]?.toString()
                        _isAutoDayNightEnabled.value = dayMode == "ON"

                        // Parse MISC for Vision Mode and Camera Mode
                        val misc = conf["MISC"]?.toString()?.toIntOrNull() ?: 1

                        // Parse Vision Mode from MISC
                        _currentVisionMode.value = when {
                            misc in 1..4 -> VisionMode.VISION
                            misc in 5..8 -> VisionMode.BOTH
                            misc in 9..12 -> VisionMode.INFRARED
                            else -> VisionMode.VISION
                        }

                        // Parse Camera Mode from MISC
                        _currentCameraMode.value = when (misc % 4) {
                            1 -> CameraMode.OFF
                            2 -> CameraMode.EIS
                            3 -> CameraMode.HDR
                            0 -> CameraMode.BOTH
                            else -> CameraMode.OFF
                        }

                        // Parse FLIP and MIRROR for Orientation Mode
                        val flip = conf["FLIP"]?.toString() == "ON"
                        val mirror = conf["MIRROR"]?.toString() == "ON"
                        _currentOrientationMode.value = when {
                            flip && mirror -> OrientationMode.BOTH
                            flip -> OrientationMode.FLIP
                            mirror -> OrientationMode.MIRROR
                            else -> OrientationMode.NORMAL
                        }

                        // Save initial values
                        saveInitialValues()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in fetchCameraSettings", e)
            }
        }
    }

    private fun calculateMiscValue(): Int {
        // Extract HDR and EIS from camera mode - ensure they're mutually exclusive
        val hdr = _currentCameraMode.value == CameraMode.HDR
        val eis = _currentCameraMode.value == CameraMode.EIS

        // Extract visible and infrared from vision mode
        val visible = _currentVisionMode.value == VisionMode.VISION || _currentVisionMode.value == VisionMode.BOTH
        val infrared = _currentVisionMode.value == VisionMode.INFRARED || _currentVisionMode.value == VisionMode.BOTH

        // Calculate WDR/EIS base value
        val wdreisval = when {
            !hdr && !eis -> 1    // No HDR, No EIS
            !hdr && eis -> 2     // No HDR, Yes EIS
            hdr && !eis -> 3     // Yes HDR, No EIS
            else -> 1            // Default to OFF if somehow both are set
        }

        // Calculate final MISC value
        return when {
            visible && !infrared -> wdreisval           // Only visible
            visible && infrared -> 4 + wdreisval        // Both visible and infrared
            !visible && infrared -> 8 + wdreisval       // Only infrared
            else -> 1                                   // Default to visible mode
        }
    }

    fun applyChanges1() {
        viewModelScope.launch {
            try {
                // Check if only orientation (flip/mirror) has changed
                val onlyOrientationChanged = initialVisionMode == _currentVisionMode.value &&
                        initialCameraMode == _currentCameraMode.value &&
                        initialAutoDayNight == _isAutoDayNightEnabled.value &&
                        initialOrientationMode != _currentOrientationMode.value

                // If only orientation changed, apply changes without stream reload
                if (onlyOrientationChanged) {
                    _isUIInteractive.value = false
                    val orientationSuccess = applyOrientationChanges()
                    if (orientationSuccess) {
                        saveInitialValues()
                    }
                    _isUIInteractive.value = true
                    return@launch
                }

                // For other changes, reload stream and disable UI
                _isStreamReloading.value = true
                _isUIInteractive.value = false

                // Track completion of all API calls
                val apiCalls = mutableListOf<Deferred<Boolean>>()

                // Execute MISC task
                val miscVal = calculateMiscValue()
                val miscDeferred = async {
                    var success = false
                    MotocamAPIAndroidHelper.setMiscAsync(
                        scope = viewModelScope,
                        miscValue = miscVal
                    ) { result, error ->
                        if (error != null) {
                            Log.e(TAG, "Error setting MISC value $error")
                        } else {
                            success = true
                        }
                    }
                    // Wait a bit for the async call to complete
                    delay(500)
                    success
                }
                apiCalls.add(miscDeferred)

                // Execute DAYMODE task
                val dayModeDeferred = async {
                    var success = false
                    MotocamAPIAndroidHelper.setDayModeAsync(
                        scope = viewModelScope,
                        dayMode = if (_isAutoDayNightEnabled.value) MotocamAPIHelper.DAYMODE.ON else MotocamAPIHelper.DAYMODE.OFF
                    ) { result, error ->
                        if (error != null) {
                            Log.e(TAG, "Error setting day mode $error")
                        } else {
                            success = true
                        }
                    }
                    // Wait a bit for the async call to complete
                    delay(500)
                    success
                }
                apiCalls.add(dayModeDeferred)

                // Apply orientation changes
                val orientationDeferred = async {
                    var flipSuccess = false
                    var mirrorSuccess = false

                    // Execute FLIP task
                    val flip = _currentOrientationMode.value == OrientationMode.FLIP ||
                            _currentOrientationMode.value == OrientationMode.BOTH
                    MotocamAPIAndroidHelper.setFlipAsync(
                        scope = viewModelScope,
                        flip = if (flip) MotocamAPIHelper.FLIP.ON else MotocamAPIHelper.FLIP.OFF
                    ) { result, error ->
                        if (error != null) {
                            Log.e(TAG, "Error setting flip $error")
                        } else {
                            flipSuccess = true
                        }
                    }

                    // Execute MIRROR task
                    val mirror = _currentOrientationMode.value == OrientationMode.MIRROR ||
                            _currentOrientationMode.value == OrientationMode.BOTH
                    MotocamAPIAndroidHelper.setMirrorAsync(
                        scope = viewModelScope,
                        mirror = if (mirror) MotocamAPIHelper.MIRROR.ON else MotocamAPIHelper.MIRROR.OFF
                    ) { result, error ->
                        if (error != null) {
                            Log.e(TAG, "Error setting mirror $error")
                        } else {
                            mirrorSuccess = true
                        }
                    }

                    // Wait for both orientation calls to complete
                    delay(1000)
                    flipSuccess && mirrorSuccess
                }
                apiCalls.add(orientationDeferred)

                // Wait for all API calls to complete
                val results = apiCalls.awaitAll()
                val allSuccessful = results.all { it }

                if (allSuccessful) {
                    // Save initial values after successful updates
                    saveInitialValues()
                    Log.d(TAG, "All camera settings applied successfully")
                } else {
                    Log.w(TAG, "Some camera settings failed to apply")
                }

                // Reset loading state and re-enable UI
                _isStreamReloading.value = false
                _isUIInteractive.value = true

            } catch (e: Exception) {
                Log.e(TAG, "Error in applyChanges", e)
                _isStreamReloading.value = false
                _isUIInteractive.value = true
            }
        }
    }

    fun applyChanges() {
        viewModelScope.launch {
            try {
                val onlyOrientationChanged = initialVisionMode == _currentVisionMode.value &&
                        initialCameraMode == _currentCameraMode.value &&
                        initialAutoDayNight == _isAutoDayNightEnabled.value &&
                        initialOrientationMode != _currentOrientationMode.value

                if (onlyOrientationChanged) {
                    _isUIInteractive.value = false
                    val orientationSuccess = applyOrientationChanges()
                    if (orientationSuccess) {
                        saveInitialValues()
                    }
                    _isUIInteractive.value = true
                    return@launch
                }

                _isStreamReloading.value = true
                _isUIInteractive.value = false

                val apiCalls = mutableListOf<Deferred<Boolean>>()

                // MISC Task
                val miscVal = calculateMiscValue()
                val miscDeferred = async {
                    val start = System.currentTimeMillis()
                    val success = suspendCancellableCoroutine<Boolean> { cont ->
                        MotocamAPIAndroidHelper.setMiscAsync(
                            scope = viewModelScope,
                            miscValue = miscVal
                        ) { result, error ->
                            if (error != null) {
                                Log.e(TAG, "Error setting MISC value $error")
                            }
                            cont.resume(error == null)
                        }
                    }
                    val end = System.currentTimeMillis()
                    Log.d(TAG, "API TIME - MISC: ${end - start} ms")
                    success
                }
                apiCalls.add(miscDeferred)

                // DAYMODE Task
                val dayModeDeferred = async {
                    val start = System.currentTimeMillis()
                    val success = suspendCancellableCoroutine<Boolean> { cont ->
                        MotocamAPIAndroidHelper.setDayModeAsync(
                            scope = viewModelScope,
                            dayMode = if (_isAutoDayNightEnabled.value) MotocamAPIHelper.DAYMODE.ON else MotocamAPIHelper.DAYMODE.OFF
                        ) { result, error ->
                            if (error != null) {
                                Log.e(TAG, "Error setting day mode $error")
                            }
                            cont.resume(error == null)
                        }
                    }
                    val end = System.currentTimeMillis()
                    Log.d(TAG, "API TIME - DAYMODE: ${end - start} ms")
                    success
                }
                apiCalls.add(dayModeDeferred)

                // Orientation (FLIP + MIRROR)
                val orientationDeferred = async {
                    val start = System.currentTimeMillis()

                    val flipDeferred = async {
                        suspendCancellableCoroutine<Boolean> { cont ->
                            val flip = _currentOrientationMode.value == OrientationMode.FLIP ||
                                    _currentOrientationMode.value == OrientationMode.BOTH
                            MotocamAPIAndroidHelper.setFlipAsync(
                                scope = viewModelScope,
                                flip = if (flip) MotocamAPIHelper.FLIP.ON else MotocamAPIHelper.FLIP.OFF
                            ) { result, error ->
                                if (error != null) {
                                    Log.e(TAG, "Error setting flip $error")
                                }
                                cont.resume(error == null)
                            }
                        }
                    }

                    val mirrorDeferred = async {
                        suspendCancellableCoroutine<Boolean> { cont ->
                            val mirror = _currentOrientationMode.value == OrientationMode.MIRROR ||
                                    _currentOrientationMode.value == OrientationMode.BOTH
                            MotocamAPIAndroidHelper.setMirrorAsync(
                                scope = viewModelScope,
                                mirror = if (mirror) MotocamAPIHelper.MIRROR.ON else MotocamAPIHelper.MIRROR.OFF
                            ) { result, error ->
                                if (error != null) {
                                    Log.e(TAG, "Error setting mirror $error")
                                }
                                cont.resume(error == null)
                            }
                        }
                    }

                    val flipSuccess = flipDeferred.await()
                    val mirrorSuccess = mirrorDeferred.await()

                    val end = System.currentTimeMillis()
                    Log.d(TAG, "API TIME - ORIENTATION (FLIP + MIRROR): ${end - start} ms")

                    flipSuccess && mirrorSuccess
                }
                apiCalls.add(orientationDeferred)

                val results = apiCalls.awaitAll()
                val allSuccessful = results.all { it }

                if (allSuccessful) {
                    saveInitialValues()
                    Log.d(TAG, "All camera settings applied successfully")
                } else {
                    Log.w(TAG, "Some camera settings failed to apply")
                }

                delay(1000)

                _isStreamReloading.value = false
                _isUIInteractive.value = true

            } catch (e: Exception) {
                Log.e(TAG, "Error in applyChanges", e)
                _isStreamReloading.value = false
                _isUIInteractive.value = true
            }
        }
    }



    private suspend fun applyOrientationChanges(): Boolean {
        var flipSuccess = false
        var mirrorSuccess = false

        // Execute FLIP task
        val flip = _currentOrientationMode.value == OrientationMode.FLIP ||
                _currentOrientationMode.value == OrientationMode.BOTH
        MotocamAPIAndroidHelper.setFlipAsync(
            scope = viewModelScope,
            flip = if (flip) MotocamAPIHelper.FLIP.ON else MotocamAPIHelper.FLIP.OFF
        ) { result, error ->
            if (error != null) {
                Log.e(TAG, "Error setting flip $error")
            } else {
                flipSuccess = true
            }
        }

        // Execute MIRROR task
        val mirror = _currentOrientationMode.value == OrientationMode.MIRROR ||
                _currentOrientationMode.value == OrientationMode.BOTH
        MotocamAPIAndroidHelper.setMirrorAsync(
            scope = viewModelScope,
            mirror = if (mirror) MotocamAPIHelper.MIRROR.ON else MotocamAPIHelper.MIRROR.OFF
        ) { result, error ->
            if (error != null) {
                Log.e(TAG, "Error setting mirror $error")
            } else {
                mirrorSuccess = true
            }
        }

        // Wait for both calls to complete
        delay(1000)
        return flipSuccess && mirrorSuccess
    }

    private fun saveInitialValues() {
        initialAutoDayNight = _isAutoDayNightEnabled.value
        initialVisionMode = _currentVisionMode.value
        initialCameraMode = _currentCameraMode.value
        initialOrientationMode = _currentOrientationMode.value
        _hasUnsavedChanges.value = false
    }

    private fun checkForChanges() {
        _hasUnsavedChanges.value = initialAutoDayNight != _isAutoDayNightEnabled.value ||
                initialVisionMode != _currentVisionMode.value ||
                initialCameraMode != _currentCameraMode.value ||
                initialOrientationMode != _currentOrientationMode.value
    }

    // State update functions remain the same
    fun setAutoDayNight(enabled: Boolean) {
        _isAutoDayNightEnabled.value = enabled
        checkForChanges()
    }

    fun setVisionMode(mode: VisionMode) {
        // Handle Low Light mode logic
        if (mode == VisionMode.BOTH) {
            // When Low Light is selected, turn off EIS and turn on HDR
            _currentCameraMode.value = CameraMode.HDR
        } else if (_currentVisionMode.value == VisionMode.BOTH) {
            // When switching away from Low Light mode, turn off HDR
            _currentCameraMode.value = CameraMode.OFF
        }

        _currentVisionMode.value = mode
        checkForChanges()
    }

    // Computed property to check if Low Light mode is active
    val isLowLightModeActive: Boolean
        get() = _currentVisionMode.value == VisionMode.BOTH

    fun setCameraMode(mode: CameraMode) {
        _currentCameraMode.value = mode
        checkForChanges()
    }

    fun setOrientationMode(mode: OrientationMode) {
        _currentOrientationMode.value = mode
        checkForChanges()
    }

    fun toggleCameraMode(mode: CameraMode) {
        _currentCameraMode.value = when (mode) {
            CameraMode.HDR -> {
                when (_currentCameraMode.value) {
                    CameraMode.HDR -> CameraMode.OFF  // If HDR is on, turn it off
                    CameraMode.EIS -> CameraMode.HDR  // If EIS is on, switch directly to HDR
                    CameraMode.BOTH -> CameraMode.OFF // BOTH is not possible anymore
                    CameraMode.OFF -> CameraMode.HDR  // If nothing is on, turn on HDR
                }
            }
            CameraMode.EIS -> {
                when (_currentCameraMode.value) {
                    CameraMode.EIS -> CameraMode.OFF  // If EIS is on, turn it off
                    CameraMode.HDR -> CameraMode.EIS  // If HDR is on, switch directly to EIS
                    CameraMode.BOTH -> CameraMode.OFF // BOTH is not possible anymore
                    CameraMode.OFF -> CameraMode.EIS  // If nothing is on, turn on EIS
                }
            }
            else -> _currentCameraMode.value
        }
        checkForChanges()
    }

    fun toggleOrientationMode(mode: OrientationMode) {
        _currentOrientationMode.value = when (mode) {
            OrientationMode.FLIP -> {
                when (_currentOrientationMode.value) {
                    OrientationMode.FLIP -> OrientationMode.NORMAL
                    OrientationMode.MIRROR -> OrientationMode.BOTH
                    OrientationMode.BOTH -> OrientationMode.MIRROR
                    OrientationMode.NORMAL -> OrientationMode.FLIP
                }
            }
            OrientationMode.MIRROR -> {
                when (_currentOrientationMode.value) {
                    OrientationMode.MIRROR -> OrientationMode.NORMAL
                    OrientationMode.FLIP -> OrientationMode.BOTH
                    OrientationMode.BOTH -> OrientationMode.FLIP
                    OrientationMode.NORMAL -> OrientationMode.MIRROR
                }
            }
            else -> _currentOrientationMode.value
        }
        checkForChanges()
    }

    fun refreshSettings() {
        fetchCameraSettings()
    }

    companion object {
        private const val TAG = "CameraLayoutViewModel"
    }
} 