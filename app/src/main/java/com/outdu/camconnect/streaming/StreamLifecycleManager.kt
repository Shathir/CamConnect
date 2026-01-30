package com.outdu.camconnect.streaming

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.outdu.camconnect.Viewmodels.AppViewModel
import com.outdu.camconnect.singleton.MainActivitySingleton
import java.lang.ref.WeakReference

/**
 * Production-grade bridge between process lifecycle (foreground/background) and stream lifecycle.
 *
 * - Uses a WeakReference to avoid leaking Activity/ViewModel.
 * - Ensures all UI state mutations happen on the main thread.
 * - Captures whether the stream was playing before background and restores it on foreground.
 */
object StreamLifecycleManager {
    private const val TAG = "StreamLifecycleManager"

    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var appViewModelRef: WeakReference<AppViewModel>? = null

    @Volatile
    private var wasPlayingBeforeBackground: Boolean = true

    /** Bind the current stream ViewModel (typically Activity-scoped). Safe to call multiple times. */
    fun bind(viewModel: AppViewModel) {
        appViewModelRef = WeakReference(viewModel)
        Log.d(TAG, "bind(viewModel) (isPlaying=${viewModel.isPlaying.value})")
    }

    /** Clear binding when the owning Activity is destroyed. */
    fun unbind(viewModel: AppViewModel) {
        val current = appViewModelRef?.get()
        if (current === viewModel) {
            appViewModelRef = null
            Log.d(TAG, "unbind(viewModel)")
        }
    }

    /** Called when the app process goes to foreground (ProcessLifecycleOwner onStart). */
    fun onAppForegrounded() {
        Log.d(TAG, "onAppForegrounded (restore=$wasPlayingBeforeBackground)")
        if (!wasPlayingBeforeBackground) return

        mainHandler.post {
            // If we stopped on background, this will recreate the TextureView and re-init pipeline.
            appViewModelRef?.get()?.setPlaying(true)
        }
    }

    /** Called when the app process goes to background (ProcessLifecycleOwner onStop). */
    fun onAppBackgrounded() {
        val vm = appViewModelRef?.get()
        wasPlayingBeforeBackground = vm?.isPlaying?.value ?: true
        Log.d(TAG, "onAppBackgrounded (wasPlaying=$wasPlayingBeforeBackground)")

        mainHandler.post {
            if (wasPlayingBeforeBackground) {
                // Drive Compose disposal path (preferred) so TextureView cleanup runs.
                appViewModelRef?.get()?.setPlaying(false)
            }
        }
    }
}


