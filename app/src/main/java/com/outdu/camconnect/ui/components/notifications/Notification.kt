package com.outdu.camconnect.ui.components.notifications

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.outdu.camconnect.ui.theme.RecordRed
import com.outdu.camconnect.ui.viewmodels.CameraControlViewModel
import kotlinx.coroutines.delay

@Composable
fun NotificationCard(
    cameraControlViewModel: CameraControlViewModel
){
    val cameraControlState by cameraControlViewModel.cameraControlState.collectAsStateWithLifecycle()

    // Auto-hide the notification after 2 seconds
    LaunchedEffect(cameraControlState.isIrChanged) {
        if (cameraControlState.isIrChanged) {
            delay(2000) // Show notification for 2 seconds
            cameraControlViewModel.clearIrNotification()
        }
    }

    AnimatedVisibility(
        visible = cameraControlState.isIrChanged,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(RecordRed)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val irLevel = when(cameraControlState.irIntensityLevel){
                com.outdu.camconnect.ui.viewmodels.IrIntensityLevel.OFF -> "OFF"
                com.outdu.camconnect.ui.viewmodels.IrIntensityLevel.LOW -> "LOW"
                com.outdu.camconnect.ui.viewmodels.IrIntensityLevel.MEDIUM -> "MEDIUM"
                com.outdu.camconnect.ui.viewmodels.IrIntensityLevel.HIGH -> "HIGH"
                com.outdu.camconnect.ui.viewmodels.IrIntensityLevel.MAX -> "MAX"
                com.outdu.camconnect.ui.viewmodels.IrIntensityLevel.ULTRA -> "ULTRA"
            }
            Text(
                text = "IR $irLevel",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}