package com.outdu.camconnect.Viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
//import com.outdu.camconnect.OverlayPoints

class AppViewModel : ViewModel() {

    private var MotionMode = mutableStateOf(false)
    private var MotionOverride = mutableStateOf(false)
    private val RecordMode = mutableStateOf(false)
    private val ModeClicked = mutableStateOf(false)

    //Camera Modes
    private val IRMode = mutableStateOf(false)
    private val IrCutFilterMode = mutableStateOf(false)
    private val FlipCameraMode = mutableStateOf(false)
    private val MirrorCameraMode = mutableStateOf(false)
    private val ZoomCameraMode = mutableStateOf(0)
    private val IrMidIntensity = mutableStateOf(0)
    private val IrExtremeIntensity = mutableStateOf(0)

//    private val OverlayPoints=mutableStateOf(
//        OverlayPoints(
//        labels = intArrayOf(),
//        probs = floatArrayOf(),
//        pointXs = intArrayOf(),
//        pointYs = intArrayOf(),
//        pointWs = intArrayOf(),
//        pointHs = intArrayOf(),
//        depThres = floatArrayOf()
//    )
//    )
    //Playing State
    private val IsPlaying = mutableStateOf(true)

    var motionvalue: State<Boolean> = MotionMode
    var motionoverridevalue: State<Boolean> = MotionOverride
    var recordmodevalue: State<Boolean> = RecordMode
    var modeclicked: State<Boolean> = ModeClicked

    //Modes
    var irMode: State<Boolean> = IRMode
    var irCutFilterMode: State<Boolean> = IrCutFilterMode
    var flipCameraMode: State<Boolean> = FlipCameraMode
    var mirrorCameraMode: State<Boolean> = MirrorCameraMode
    var zoomCameraMode: State<Int> = ZoomCameraMode
    var irMidIntensity: State<Int> = IrMidIntensity
    var irExtremeIntensity: State<Int> = IrExtremeIntensity

    //Playing State
    var isPlaying: State<Boolean> = IsPlaying


    //overlay points
//    var overlayPoints: State<OverlayPoints> =OverlayPoints



    fun changeMotionMode() {
        MotionMode.value = !MotionMode.value
    }

    fun changeMotionOverrideMode() {
        MotionOverride.value = !MotionOverride.value
    }

    fun changeRecordMode() {
        RecordMode.value = !RecordMode.value
    }

    fun changeModeClick() {
        ModeClicked.value = !ModeClicked.value
    }

    //Mode Functions

    fun changeIRMode() {
        IRMode.value = !IRMode.value
    }
    fun changeIrCutFilterMode() {
        IrCutFilterMode.value = !IrCutFilterMode.value
    }
    fun changeFlipCameraMode() {
        FlipCameraMode.value = !FlipCameraMode.value
    }
    fun changeMirrorCameraMode() {
        MirrorCameraMode.value = !MirrorCameraMode.value
    }
    fun changeZoomCameraMode(n : Int) {
        ZoomCameraMode.value = n
    }
    fun changeIrMidIntensity(n : Int) {
        IrMidIntensity.value = n
    }
    fun changeIrExtremeIntensity(n : Int) {
        IrExtremeIntensity.value = n
    }


    //Playing State
    fun changePlayingState(){
        IsPlaying.value = !IsPlaying.value
    }



    fun changeOverlayPoints(
        labels: IntArray,
        probs: FloatArray,
        pointXs: IntArray,
        pointYs: IntArray,
        pointWs: IntArray,
        pointHs: IntArray,
        depThres: FloatArray
    ) {
        // Create a new instance of OverlayPoints with the new data
//        OverlayPoints.value = OverlayPoints(
//            labels = labels,
//            probs = probs,
//            pointXs = pointXs,
//            pointYs = pointYs,
//            pointWs = pointWs,
//            pointHs = pointHs,
//            depThres = depThres
//        )
    }
}
