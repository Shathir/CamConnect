package com.outdu.camconnect.ui.components.settings.ai

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
import com.outdu.camconnect.R
import com.outdu.camconnect.communication.CameraConfigurationManager
import com.outdu.camconnect.ui.components.settings.ControlTab
import com.outdu.camconnect.ui.models.SystemStatus
import com.outdu.camconnect.ui.theme.AppColors.AIButtonTextColor
import com.outdu.camconnect.ui.theme.AppColors.ButtonBorderColor
import com.outdu.camconnect.ui.theme.DarkBackground3
import com.outdu.camconnect.ui.theme.LightGray
import com.outdu.camconnect.ui.theme.VeryDarkBackground
import kotlinx.coroutines.launch

@Composable
fun YesNoButtons(
    isEnabled: Boolean,
    onValueChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {

    val isDarkTheme = isSystemInDarkTheme()
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .border(
                    width = if (isDarkTheme) 0.dp else 2.dp, // No border in dark theme
                    color = ButtonBorderColor,
                    shape = RoundedCornerShape(20.dp)
                )
                .background(if (isEnabled) Color(0xFF0C59E0) else DarkBackground3)
                .clickable(onClick = { onValueChange(true) })
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            )
            {
                Icon(
                    painter =painterResource(id = R.drawable.ai_line),
                    contentDescription = null,
                    tint = if(isDarkTheme) Color.White else Color.Black,
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = "ON",
                    style = TextStyle(
                        color = if (isEnabled) Color.White else AIButtonTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    )
                )
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .border(
                    width = if (isDarkTheme) 0.dp else 2.dp, // No border in dark theme
                    color = ButtonBorderColor,
                    shape = RoundedCornerShape(20.dp)
                )
                .background(if (!isEnabled) Color(0xFF0C59E0) else DarkBackground3)
                .clickable(onClick = { onValueChange(true) })
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            )
            {
                Icon(
                    painter =painterResource(id = R.drawable.ai_line),
                    contentDescription = null,
                    tint = if(isDarkTheme) Color.White else Color.Black,
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = "ON",
                    style = TextStyle(
                        color = if (isEnabled) Color.White else AIButtonTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    )
                )
            }
        }
    }
}


@Composable
fun YesNoToggleBox(
    isYesSelected: Boolean,
    onValueChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // YES Box
        ToggleBox(
            text = "Yes",
            isSelected = isYesSelected,
            onClick = { onValueChange(true) },
            selectedColor = if (isDarkTheme) Color(0xFF515151) else Color(0xFFD7D7D7),
            unselectedColor = if (isDarkTheme) Color(0xFF333333) else Color(0xFFFFFFFF),
            isDarkTheme = isDarkTheme
        )

        // NO Box
        ToggleBox(
            text = "No",
            isSelected = !isYesSelected,
            onClick = { onValueChange(false) },
            selectedColor = if (isDarkTheme) Color(0xFF515151) else Color(0xFFD7D7D7),
            unselectedColor = if (isDarkTheme) Color(0xFF333333) else Color(0xFFFFFFFF),
            isDarkTheme = isDarkTheme
        )
    }
}

@Composable
private fun ToggleBox(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color,
    unselectedColor: Color,
    isDarkTheme: Boolean
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = if (isDarkTheme) 0.dp else 2.dp,
                color = Color.Gray,
                shape = RoundedCornerShape(20.dp)
            )
            .background(if (isSelected) selectedColor else unselectedColor)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.Black else Color.Gray,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
        )
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
    
    // Initial values using mutable state to allow updates after applying changes
    var initialObjectDetection by remember { mutableStateOf(CameraConfigurationManager.isObjectDetectionEnabled()) }
    var initialFarDetection by remember { mutableStateOf(CameraConfigurationManager.isFarDetectionEnabled()) }
    var initialMotionDetection by remember { mutableStateOf(CameraConfigurationManager.isDrowsinessDetectionEnabled()) }
    
    // Current state
    var objectDetectionEnabled by remember { mutableStateOf(initialObjectDetection) }
    var farDetectionEnabled by remember { mutableStateOf(initialFarDetection) }
    var motionDetectionEnabled by remember { mutableStateOf(initialMotionDetection) }
    
    // Check if there are actual changes from initial values
    val hasChanges = remember(
        objectDetectionEnabled, farDetectionEnabled, motionDetectionEnabled,
        initialObjectDetection, initialFarDetection, initialMotionDetection
    ) {
        objectDetectionEnabled != initialObjectDetection ||
        farDetectionEnabled != initialFarDetection ||
        motionDetectionEnabled != initialMotionDetection
    }
    
    // Function to save changes
    val saveChanges = {
        scope.launch {
            val config = CameraConfigurationManager.CameraConfig(
                farDetectionEnabled = farDetectionEnabled,
                objectDetectionEnabled = objectDetectionEnabled,
                drowsinessDetectionEnabled = motionDetectionEnabled,
                audioEnabled = CameraConfigurationManager.isAudioEnabled(),
                modelVersion = CameraConfigurationManager.getModelVersion(),
                drowsinessThreshold = CameraConfigurationManager.getDrowsinessThreshold()
            )
            val result = CameraConfigurationManager.updateConfiguration(context, config)
            
            // Update initial values and system status after successful save
            if (result.isSuccess) {
                initialObjectDetection = objectDetectionEnabled
                initialFarDetection = farDetectionEnabled
                initialMotionDetection = motionDetectionEnabled
                
                // Update system status to reflect AI enabled state
                onSystemStatusChange(systemStatus.copy(isAiEnabled = objectDetectionEnabled))
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Transparent)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Apply Changes Button - always visible, enabled only when there are changes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { saveChanges() },
                    enabled = hasChanges,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                    )
                ) {
                    Text("Apply Changes")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Object Detection
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Object Detection",
                        style = TextStyle(
                            fontSize = 16.sp,
                            lineHeight = 14.02.sp,
                            fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                            fontWeight = FontWeight(500),
                            color = if(isDarkTheme) Color.White else Color.Black
                        )
                    )
                    YesNoButtons(
                        isEnabled = objectDetectionEnabled,
                        onValueChange = { objectDetectionEnabled = it }
                    )
                }

                // Far Detection
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Detect Far Away Objects",
                        style = TextStyle(
                            fontSize = 16.sp,
                            lineHeight = 14.02.sp,
                            fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                            fontWeight = FontWeight(500),
                            color = if(isDarkTheme) Color.White else Color.Black
                        )
                    )
                    YesNoButtons(
                        isEnabled = farDetectionEnabled,
                        onValueChange = { farDetectionEnabled = it }
                    )
                }

                // Motion Detection
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Motion Detection",
                        style = TextStyle(
                            fontSize = 16.sp,
                            lineHeight = 14.02.sp,
                            fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                            fontWeight = FontWeight(500),
                            color = if(isDarkTheme) Color.White else Color.Black
                        )
                    )
                    YesNoButtons(
                        isEnabled = motionDetectionEnabled,
                        onValueChange = { motionDetectionEnabled = it }
                    )
                }
            }
        }
    }
}