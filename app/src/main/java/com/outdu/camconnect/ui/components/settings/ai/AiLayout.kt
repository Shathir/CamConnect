package com.outdu.camconnect.ui.components.settings.ai

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.outdu.camconnect.R
import com.outdu.camconnect.Viewmodels.AppViewModel
import com.outdu.camconnect.ui.models.SystemStatus
import com.outdu.camconnect.ui.theme.AppColors.AIButtonTextColor
import com.outdu.camconnect.ui.theme.AppColors.ButtonBorderColor
import com.outdu.camconnect.ui.theme.DarkBackground3
import com.outdu.camconnect.ui.viewmodels.AiConfigurationViewModel
import com.outdu.camconnect.utils.DeviceType
import com.outdu.camconnect.utils.rememberDeviceType
import kotlinx.coroutines.launch

@Composable
fun YesNoButtons(
    isEnabled: Boolean,
    onValueChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val deviceType = rememberDeviceType()
    val isDarkTheme = isSystemInDarkTheme()
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .border(
                    width = if (isDarkTheme) 0.dp else 2.dp, // No border in dark theme
                    color = ButtonBorderColor,
                    shape = RoundedCornerShape(20.dp)
                )
                .background(if (isEnabled) Color(0xFF0C59E0) else DarkBackground3)
                .clickable(onClick = { onValueChange(true) })
                .align(Alignment.CenterVertically),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            )
            {
                Icon(
                    painter =painterResource(id = R.drawable.yes_line),
                    contentDescription = null,
                    tint = if(isEnabled) {if(!isDarkTheme) Color(0xFF222222) else Color(0xFFFFFFFF)} else{ if(!isDarkTheme) Color(0xFFAEAEAE) else Color(0xFF8E8E8E)},
                    modifier = Modifier.size(if(deviceType == DeviceType.TABLET) (24.dp) else (16.dp))
                        .padding(1.dp)
                )

                Text(
                    text = "ON",
                    style = TextStyle(
                        color = if (isEnabled) Color.White else AIButtonTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = if(deviceType == DeviceType.TABLET) 18.sp else 14.sp,
                    )
                )
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .border(
                    width = if (isDarkTheme) 0.dp else 2.dp, // No border in dark theme
                    color = ButtonBorderColor,
                    shape = RoundedCornerShape(20.dp)
                )
                .background(if (!isEnabled) Color(0xFF0C59E0) else DarkBackground3)
                .clickable(onClick = { onValueChange(false) })
                .align(Alignment.CenterVertically),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            )
            {
                Icon(
                    painter =painterResource(id = R.drawable.no_line),
                    contentDescription = null,
                    tint = if(!isEnabled) {if(!isDarkTheme) Color(0xFF222222) else Color(0xFFFFFFFF)} else{ if(!isDarkTheme) Color(0xFFAEAEAE) else Color(0xFF8E8E8E)},
                    modifier = Modifier.size(if(deviceType == DeviceType.TABLET) (24.dp) else (16.dp))
                        .padding(1.dp)
                )

                Text(
                    text = "OFF",
                    style = TextStyle(
                        color = if (!isEnabled) Color.White else AIButtonTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = if(deviceType == DeviceType.TABLET) 18.sp else 14.sp,
                    )
                )
            }
        }
    }
}


@Composable
fun AiLayout(
    systemStatus: SystemStatus = SystemStatus(),
    onSystemStatusChange: (SystemStatus) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDarkTheme = isSystemInDarkTheme()
    val deviceType = rememberDeviceType()
    val appViewModel: AppViewModel = viewModel()
    val aiConfigViewModel: AiConfigurationViewModel = viewModel()
    
    // Collect UI state
    val uiState by aiConfigViewModel.uiState.collectAsStateWithLifecycle()
    
    // Load configuration on first composition
    LaunchedEffect(Unit) {
        aiConfigViewModel.loadConfiguration(context)
    }
    
    // Show error message if any
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            Log.e("AiLayout", error)
            // You can show a Snackbar or other error UI here
        }
    }

    // Function to save changes
    val saveChanges = {
        scope.launch {
            try {
                appViewModel.setPlaying(false)
            } catch (e: Exception) {
                Log.e("AiLayout", "Error stopping stream", e)
            }
            
            aiConfigViewModel.saveConfiguration(context) {
                // Update system status to reflect AI enabled state
                onSystemStatusChange(systemStatus.copy(isAiEnabled = uiState.od))

                // Restart stream after successful save
                try {
                    appViewModel.setPlaying(true)
                } catch (e: Exception) {
                    Log.e("AiLayout", "Error starting stream", e)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Apply Changes Button - always visible, enabled only when there are changes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { saveChanges() },
                    enabled = uiState.hasUnsavedChanges && !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = Color(0xFF2C2C2C) // Dark theme disabled color
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Apply Changes",
                            color = if (uiState.hasUnsavedChanges) Color.White else Color(0xFF777777) // Gray text when disabled
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Object Detection
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Object Detection",
                        style = TextStyle(
                            fontSize = if(deviceType == DeviceType.TABLET) 16.sp else 14.sp,
                            lineHeight = 14.02.sp,
                            fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                            fontWeight = FontWeight(500),
                            color = if (isDarkTheme) Color.White else Color.Black
                        )
                    )
                    YesNoButtons(
                        isEnabled = uiState.od,
                        onValueChange = { aiConfigViewModel.updateOD(it) }
                    )
                }

                // Far Detection
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if(uiState.od) {
                        Text(
                            text = "Detect Far Away Objects",
                            style = TextStyle(
                                fontSize = if(deviceType == DeviceType.TABLET) 16.sp else 14.sp,
                                lineHeight = 14.02.sp,
                                fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                                fontWeight = FontWeight(500),
                                color = if (isDarkTheme) Color.White else Color.Black
                            )
                        )
                        YesNoButtons(
                            isEnabled = uiState.far,
                            onValueChange = { aiConfigViewModel.updateFAR(it) }
                        )
                    }
                }

                if(deviceType == DeviceType.TABLET) {
                    Column(modifier = Modifier.weight(1f)){}
                }
            }
        }
    }
}