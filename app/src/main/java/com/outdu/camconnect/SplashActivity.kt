package com.outdu.camconnect

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.outdu.camconnect.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Splash screen activity with Lottie animation
 * Shows intro animation and then navigates to SetupActivity
 */
class SplashActivity : ComponentActivity() {
    
    companion object {
        const val SPLASH_DURATION = 3500L // 3.5 seconds
        const val ANIMATION_SPEED = 1.2f
        const val FADE_IN_DURATION = 800
        const val SCALE_ANIMATION_DURATION = 1000
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            CamConnectTheme {
                SplashScreen(
                    onSplashComplete = {
                        // Navigate to SetupActivity
                        startActivity(Intent(this@SplashActivity, SetupActivity::class.java))
//                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                        finish()
                        // Add custom transition animation
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                )
            }
        }
    }
}


//                brush = Brush.verticalGradient(
//                    colors = listOf(
//                        DarkBackground1,
//                        DarkBackground2,
//                        DarkSlate
//                    )


@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Show content with fade in
        showContent = true

        // Wait for the splash duration
        delay(SplashActivity.SPLASH_DURATION)

        // Navigate to next screen
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2061F2)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(32.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.stravion_logo),
                contentDescription = "Stravion Logo",
                tint = Color.White,
                modifier = Modifier
                    .size(140.dp)
            )
        }

        // Version info at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Version 1.1",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White,
                    fontSize = 12.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}