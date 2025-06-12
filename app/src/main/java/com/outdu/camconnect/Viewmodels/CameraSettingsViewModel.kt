package com.outdu.camconnect.Viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

//class CameraSettingsViewModel : ViewModel() {
//
//    private val VisibleViewMode = mutableStateOf(false)
//    private val InfraredViewMode = mutableStateOf(false)
//    private val HdrCameraMode = mutableStateOf(false)
//    private val StabilisationViewMode = mutableStateOf(false)
//
//
//    //IRonMotion
//    private val IROnMotionMode = mutableStateOf(false)
//
//    private val IRDayNightMode = mutableStateOf(false)
//
//    var visibleViewMode: State<Boolean> = VisibleViewMode
//    var infraredViewMode: State<Boolean> = InfraredViewMode
//    var hdrCameraMode: State<Boolean> = HdrCameraMode
//    var stabilisationViewMode: State<Boolean> = StabilisationViewMode
//    var irOnMotion: State<Boolean> = IROnMotionMode
//    var irDayNightMode: State<Boolean> = IRDayNightMode
//
//
//
//    fun changeVisibleViewMode() {
//        VisibleViewMode.value = !VisibleViewMode.value
//    }
//    fun changeInfraredViewMode() {
//        InfraredViewMode.value = !InfraredViewMode.value
//    }
//    fun changeHdrCameraMode() {
//        HdrCameraMode.value = !HdrCameraMode.value
//    }
//    fun changeStabilisationViewMode() {
//        StabilisationViewMode.value = !StabilisationViewMode.value
//    }
//    fun changeIROnMotionMode() {
//        IROnMotionMode.value = !IROnMotionMode.value
//    }
//    fun changeIRDayNightMode() {
//        IRDayNightMode.value = !IRDayNightMode.value
//    }
//}

class CameraSettingsViewModel : ViewModel() {

    private val VisibleViewMode = mutableStateOf(false)
    private val InfraredViewMode = mutableStateOf(false)
    private val HdrCameraMode = mutableStateOf(false)
    private val StabilisationViewMode = mutableStateOf(false)
    private val IrOnMotionMode = mutableStateOf(false)
    private val IrDayNightMode = mutableStateOf(false)

    // Added isChecked variables
    private val IsCheckedVisibleViewMode = mutableStateOf(false)
    private val IsCheckedInfraredViewMode = mutableStateOf(false)
    private val IsCheckedHdrCameraMode = mutableStateOf(false)
    private val IsCheckedStabilisationViewMode = mutableStateOf(false)
    private val IsCheckedIrOnMotionMode = mutableStateOf(false)
    private val IsCheckedIrDayNightMode = mutableStateOf(false)

    var visibleViewMode: State<Boolean> = VisibleViewMode
    var infraredViewMode: State<Boolean> = InfraredViewMode
    var hdrCameraMode: State<Boolean> =HdrCameraMode
    var stabilisationViewMode: State<Boolean> = StabilisationViewMode
    var irOnMotion: State<Boolean> = IrOnMotionMode
    var irDayNightMode: State<Boolean> = IrDayNightMode

    // Expose the isChecked variables
    var isCheckedVisibleViewMode: State<Boolean> = IsCheckedVisibleViewMode
    var isCheckedInfraredViewMode: State<Boolean> = IsCheckedInfraredViewMode
    var isCheckedHdrCameraMode: State<Boolean> = IsCheckedHdrCameraMode
    var isCheckedStabilisationViewMode: State<Boolean> = IsCheckedStabilisationViewMode
    var isCheckedIrOnMotionMode: State<Boolean> = IsCheckedIrOnMotionMode
    var isCheckedIrDayNightMode: State<Boolean> = IsCheckedIrDayNightMode

    // Existing change functions
    fun changeVisibleViewMode() {
        VisibleViewMode.value = !VisibleViewMode.value
    }

    fun changeInfraredViewMode() {
        InfraredViewMode.value = !InfraredViewMode.value
    }

    fun changeHdrCameraMode() {
        HdrCameraMode.value = !HdrCameraMode.value
    }

    fun changeStabilisationViewMode() {
        StabilisationViewMode.value = !StabilisationViewMode.value
    }

    fun changeIrOnMotionMode() {
        IrOnMotionMode.value = !IrOnMotionMode.value
    }

    fun changeIrDayNightMode() {
        IrDayNightMode.value = !IrDayNightMode.value
    }

    // New change functions for isChecked variables
    fun changeIsCheckedVisibleViewMode() {
        IsCheckedVisibleViewMode.value = !IsCheckedVisibleViewMode.value
    }

    fun changeIsCheckedInfraredViewMode() {
        IsCheckedInfraredViewMode.value = !IsCheckedInfraredViewMode.value
    }

    fun changeIsCheckedHdrCameraMode() {
        IsCheckedHdrCameraMode.value = !IsCheckedHdrCameraMode.value
    }

    fun changeIsCheckedStabilisationViewMode() {
        IsCheckedStabilisationViewMode.value = !IsCheckedStabilisationViewMode.value
    }

    fun changeIsCheckedIrOnMotionMode() {
        IsCheckedIrOnMotionMode.value = !IsCheckedIrOnMotionMode.value
    }

    fun changeIsCheckedIrDayNightMode() {
        IsCheckedIrDayNightMode.value = !IsCheckedIrDayNightMode.value
    }
}
