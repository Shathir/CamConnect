package com.outdu.camconnect.singleton

import com.outdu.camconnect.MainActivity
import com.outdu.camconnect.communication.Data

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

    fun nativePlay(width: Int, height: Int, od: Boolean) {
        mainActivity?.nativePlay(width, height, od)
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
        return Data.isOD()
    }

    fun getDS():Boolean
    {
        return  Data.isDS()
    }

    fun getODFar():Boolean
    {
        return Data.isFAR()
    }

    fun getMODEL():Int
    {
        return Data.getMODEL()
    }

    fun setOD(od: Boolean) {
        Data.setOD(mainActivity, od)
    }

    fun setDS(ds: Boolean) {
        Data.setDS(mainActivity, ds)
    }

    fun setFar(odFar: Boolean) {
        Data.setFAR(mainActivity, odFar);
    }

    fun setMODEL(model: Int) {
        Data.setMODEL(mainActivity, model)
    }
}