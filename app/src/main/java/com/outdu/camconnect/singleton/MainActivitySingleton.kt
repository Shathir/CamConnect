package com.outdu.camconnect.singleton

import com.outdu.camconnect.MainActivity
import com.outdu.camconnect.communication.Data
import com.outdu.camconnect.communication.CameraConfigurationManager

object MainActivitySingleton {

    private var mainActivity: MainActivity? = null

    fun setMainActivity(activity: MainActivity) {
        mainActivity = activity
    }

    fun clearMainActivity() {
        mainActivity = null
    }
    fun testingCallback(){
//        mainActivity?.testingHandler()
    }

    // Expose JNI methods using the reference to MainActivity
    fun nativeInit(avcDecoder: String) {
        mainActivity?.nativeInit(avcDecoder)
    }

    fun nativeFinalize() {
        mainActivity?.nativeFinalize()
    }

    fun nativePlay(width: Int, height: Int, od: Boolean, ds: Boolean, far_roi: Boolean) {
        mainActivity?.nativePlay(width, height, od, ds, far_roi)
    }

    fun nativePause() {
        mainActivity?.nativePause()
    }

    fun nativeSurfaceInit(surface: Any) {
        mainActivity?.nativeSurfaceInit(surface)
    }

    fun nativeSurfaceFinalize() {
        mainActivity?.nativeSurfaceFinalize()
    }

    fun getRecordingPath() : String{
//        val path = mainActivity?.getStoragePath();
//        return path.toString();
        return "";
    }

    fun startRecording(Path: String) {
//        Log.i("Sample Settings Load", Path)
//        mainActivity?.startRecording(Path)
    }

    fun stopRecording() {
//        mainActivity?.stopRecording()
    }

    fun getExternalStorage(): Pair<Long, Long>?
    {
//        var output = mainActivity?.getExternalStorageInfo()
//        val availableMemoery = output?.second?.let { mainActivity?.formatSize(it) }
//        val totalMemory = output?.first?.let { mainActivity?.formatSize(it)}
//
//        if (totalMemory != null && availableMemoery != null) {
//            return Pair(totalMemory.toLong(), availableMemoery.toLong())
//        }
//        else {
        return Pair(0,0)
//        }
    }

    fun getOD():Boolean
    {
        return CameraConfigurationManager.isObjectDetectionEnabled()
    }

    fun getDS():Boolean
    {
        return CameraConfigurationManager.isDepthSensingEnabled()
    }

    fun getODFar():Boolean
    {
        return CameraConfigurationManager.isFarDetectionEnabled()
    }

    fun getMODEL():Int
    {
        return CameraConfigurationManager.getModelVersion()
    }

    suspend fun setOD(od: Boolean): Result<Unit>? {
        return mainActivity?.let { CameraConfigurationManager.setObjectDetectionEnabled(it, od) }
    }

    suspend fun setDS(ds: Boolean): Result<Unit>? {
        return mainActivity?.let { CameraConfigurationManager.setDepthSensingEnabled(it, ds) }
    }

    suspend fun setFar(odFar: Boolean): Result<Unit>? {
        return mainActivity?.let { CameraConfigurationManager.setFarDetectionEnabled(it, odFar) }
    }

    suspend fun setMODEL(model: Int): Result<Unit>? {
        return mainActivity?.let { CameraConfigurationManager.setModelVersion(it, model) }
    }
}