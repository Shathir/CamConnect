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
fun SplashScreen1(
    onSplashComplete: () -> Unit
) {
    // Animation states
    var showContent by remember { mutableStateOf(false) }
    var showLoadingIndicator by remember { mutableStateOf(false) }
    var animationCompleted by remember { mutableStateOf(false) }
    
    // Lottie animation composition
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.scout_intro)
    )
    
    // Animation state
    val animationState = rememberLottieAnimatable()
    
    // Check if animation loaded successfully
    val isAnimationReady = composition != null
    
    // Fade in animation for content
    val contentAlpha by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = tween(
            durationMillis = SplashActivity.FADE_IN_DURATION,
            easing = EaseOut
        ),
        label = "content_fade"
    )
    
    // Scale animation for logo
    val logoScale by animateFloatAsState(
        targetValue = if (showContent) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logo_scale"
    )
    
    // Handle splash sequence
    LaunchedEffect(isAnimationReady) {
        // Show content with fade in
        showContent = true
        
        // Wait a bit then start animation (if available)
//        delay(300)
        
        if (isAnimationReady) {
            // Start Lottie animation
            animationState.animate(
                composition = composition,
                speed = SplashActivity.ANIMATION_SPEED,
                iterations = 1
            )
            animationCompleted = true
        } else {
            // Fallback: wait a bit longer if no animation
            delay(1000)
            animationCompleted = true
        }
        
        // Show loading indicator in the last part
        delay(1000)
        showLoadingIndicator = true
        
        // Calculate remaining time
        val remainingTime = if (isAnimationReady) {
            SplashActivity.SPLASH_DURATION - 1300
        } else {
            SplashActivity.SPLASH_DURATION - 2300 // Extra time for fallback
        }
        
        if (remainingTime > 0) {
            delay(remainingTime)
        }
        
        onSplashComplete()
    }
    
    // UI Layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background( Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(32.dp)
//                .alpha(contentAlpha)
        ) {
            // Animation or Fallback Logo
            if (isAnimationReady) {
                // Lottie Animation
                LottieAnimation(
                    composition = composition,
                    progress = { animationState.progress },
                    modifier = Modifier
                        .size(80.dp)
                        .scale(logoScale)
                        .padding(bottom = 24.dp)
                )
            }
        }
        
        // Version info at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(contentAlpha)
        ) {
            Text(
                text = "Version 1.0",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MediumGray,
                    fontSize = 12.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

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
            .background( Color(0xFF2061F2)),
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