package com.outdu.camconnect.Viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class RecorderViewModel : ViewModel() {
    private val _isRecording = MutableStateFlow(false)
    private val _elapsedTime = MutableStateFlow(0)
    private val _fileSize = MutableStateFlow(0L)

    val isRecording: StateFlow<Boolean> = _isRecording
    val elapsedTime: StateFlow<Int> = _elapsedTime
    val fileSize: StateFlow<Long> = _fileSize

    private var timer: Job? = null

    fun start() {
        _isRecording.value = true
        timer = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            while (isActive) {
                delay(1000)
                val elapsed = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                _elapsedTime.value = elapsed
                updateFileSize()
            }
        }
    }

    fun stop() {
        _isRecording.value = false
        timer?.cancel()
    }

    private fun updateFileSize() {
        outputFilePath?.let {
            val file = File(it)
            if (file.exists()) {
                _fileSize.value = file.length()
            }
        }
    }

    companion object {
        var outputFilePath: String? = null
    }
}
