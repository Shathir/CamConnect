package com.outdu.camconnect.Viewmodels


import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class AiControlViewModel : ViewModel() {
    private var DetectObjectsMode = mutableStateOf(false)
    private var DetectFarObjectsMode = mutableStateOf(false)
    private val DepthSenseEnable = mutableStateOf(false)
    private val DetectionThreshold = mutableStateOf(0)
    private val DetectionModel = mutableStateOf(0)

    private val IsCheckedDetectObjectsMode=mutableStateOf(false)
    private var IsCheckedDetectFarObjectsMode = mutableStateOf(false)
    private val IsCheckedDepthSenseEnable = mutableStateOf(false)
    private val IsCheckedDetectionModel = mutableStateOf(false)

    var detectObjects: State<Boolean> = DetectObjectsMode
    var detectFarObjects: State<Boolean> = DetectFarObjectsMode
    var depthSenseEnable: State<Boolean> = DepthSenseEnable
    var detectionThreshold: State<Int> = DetectionThreshold
    var detectionModel: State<Int> = DetectionModel


    var isCheckedDetectObjects: State<Boolean> = IsCheckedDetectObjectsMode
    var isCheckedDetectFarObjects: State<Boolean> = IsCheckedDetectFarObjectsMode
    var isCheckedDepthSenseEnable: State<Boolean> = IsCheckedDepthSenseEnable
    var isCheckedDetectionModel: State<Boolean> = IsCheckedDetectionModel

    fun changeDetectObjectMode() {
        DetectObjectsMode.value = !DetectObjectsMode.value
    }

    fun changeDetectFarObjectsMode() {
        DetectFarObjectsMode.value = !DetectFarObjectsMode.value
    }

    fun changeDepthSenseEnable() {
        DepthSenseEnable.value = !DepthSenseEnable.value
    }

    fun changeDetectionThreshold(threshold : Int) {
        DetectionThreshold.value = threshold
    }

    fun changeDetectionModel(model : Int) {
        DetectionModel.value = model
    }


    fun changeIsCheckedDetectObjectMode() {
        IsCheckedDetectObjectsMode.value = !IsCheckedDetectObjectsMode.value
    }

    fun changeIsCheckedDetectFarObjectsMode() {
        IsCheckedDetectFarObjectsMode.value = !IsCheckedDetectFarObjectsMode.value
    }

    fun changeIsCheckedDepthSenseEnable() {
        IsCheckedDepthSenseEnable.value = !IsCheckedDepthSenseEnable.value
    }

    fun changeIsCheckedDetectionModel() {
        IsCheckedDetectionModel.value = !IsCheckedDetectionModel.value
    }

}
