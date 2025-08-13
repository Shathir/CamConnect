package com.outdu.camconnect.ui.components.settings.logout

import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outdu.camconnect.R
import com.outdu.camconnect.ui.theme.AppColors.ButtonBorderColor
import com.outdu.camconnect.ui.theme.AppColors.ButtonSelectedBgColor
import com.outdu.camconnect.utils.DeviceType
import com.outdu.camconnect.utils.rememberDeviceType
import com.outdu.camconnect.auth.SessionManager
import kotlinx.coroutines.launch

@Composable
fun LogoutLayout(
    modifier: Modifier = Modifier,
    onCancelClick: () -> Unit,
    onLogoutSuccess: () -> Unit = {}
)
{
    val deviceType = rememberDeviceType()
    val isDarkTheme = isSystemInDarkTheme()
    val coroutineScope = rememberCoroutineScope()
    
    var isLoggingOut by remember { mutableStateOf(false) }
    var logoutError by remember { mutableStateOf<String?>(null) }
    
    // Determine button size
    val buttonSize = if(deviceType == DeviceType.TABLET) 112.dp else 56.dp
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(start = 24.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    )
    {
        Box(
            modifier = modifier
                .size(buttonSize) // Enforce square shape
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize() // Fill the square parent
                    .clip(RoundedCornerShape(if(deviceType == DeviceType.TABLET)20.dp else 14.dp))
                    .background(ButtonSelectedBgColor)
                    .border(
                        width = if (isDarkTheme) 0.dp else 1.dp, // No border in dark theme
                        color = ButtonBorderColor,
                        shape = RoundedCornerShape(if(deviceType == DeviceType.TABLET)20.dp else 14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.signout),
                    contentDescription = null,
                    modifier = Modifier.size(if (deviceType == DeviceType.TABLET) 48.dp else 18.dp),
                    tint = Color.Red
                )
            }
        }

        Text(
            text = "Logout",
            style = TextStyle(
                fontSize = 16.sp,
                lineHeight = 18.69.sp,
                fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFF2B2B2B),
            )
        )

        Text(
            text = "Are you sure you want to logout ?",
            style = TextStyle(
                fontSize = 12.sp,
                lineHeight = 14.02.sp,
                fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFF9097A0),
            )
        )

        // Show error message if logout failed
        logoutError?.let { error ->
            Text(
                text = "Logout failed: $error",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                    fontWeight = FontWeight(400),
                    color = Color.Red,
                ),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        )
        {
            // Yes Button (Confirm Logout)
            Box(
                modifier = Modifier.width(if(deviceType == DeviceType.TABLET)224.dp else 112.dp)
                    .height(if(deviceType == DeviceType.TABLET)64.dp else 32.dp)
                    .clip(RoundedCornerShape(if(deviceType == DeviceType.TABLET)20.dp else 14.dp))
                    .background(if (isLoggingOut) Color.Gray else Color.Red)
                    .clickable(enabled = !isLoggingOut) {
                        // Perform LogOut
                        coroutineScope.launch {
                            isLoggingOut = true
                            logoutError = null
                            
                            try {
                                val result = SessionManager.logout()
                                if (result.isSuccess) {
                                    onLogoutSuccess()
                                } else {
                                    logoutError = result.exceptionOrNull()?.message ?: "Unknown error"
                                }
                            } catch (e: Exception) {
                                logoutError = e.message ?: "Logout failed"
                            } finally {
                                isLoggingOut = false
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            )
            {
                if (isLoggingOut) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Yes",
                        style = TextStyle(
                            fontSize = 16.sp,
                            lineHeight = 18.69.sp,
                            fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFFFFFFFF)
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // No Button (Cancel)
            Box(
                modifier = Modifier.width(if(deviceType == DeviceType.TABLET)224.dp else 112.dp)
                    .height(if(deviceType == DeviceType.TABLET)64.dp else 32.dp)
                    .clip(RoundedCornerShape(if(deviceType == DeviceType.TABLET)20.dp else 14.dp))
                    .border(
                        width = if (isDarkTheme) 0.dp else 1.dp, // No border in dark theme
                        color = ButtonBorderColor,
                        shape = RoundedCornerShape(if(deviceType == DeviceType.TABLET)20.dp else 14.dp)
                    )
                    .clickable(enabled = !isLoggingOut) {
                     onCancelClick()
                    },
                contentAlignment = Alignment.Center
            )
            {
                Text(
                    text = "No",
                    style = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 18.69.sp,
                        fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF9097A0)
                        ),
                    textAlign = TextAlign.Center
                )
            }
        }

    }
}