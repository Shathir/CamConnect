package com.outdu.camconnect.ui.components.loading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outdu.camconnect.R
import com.outdu.camconnect.ui.theme.*
import com.outdu.camconnect.ui.theme.AppColors.StravionBlue
import kotlinx.coroutines.delay

/**
 * Full-screen overlay showing AI model loading progress
 */
@Composable
fun ModelLoadingOverlay(
    loadingStartTime: Long = System.currentTimeMillis(),
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showExtendedMessage by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableStateOf(0) }
    
    // Watchdog timer - show extended message after 6 seconds
    LaunchedEffect(loadingStartTime) {
        delay(6000)
        showExtendedMessage = true
    }
    
    // Update elapsed time every second
    LaunchedEffect(loadingStartTime) {
        while (true) {
            delay(1000)
            elapsedSeconds = ((System.currentTimeMillis() - loadingStartTime) / 1000).toInt()
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            // Stravion Logo
            Icon(
                painter = painterResource(id = R.drawable.stravion_logo),
                contentDescription = "Stravion",
                modifier = Modifier.size(120.dp, 40.dp),
                tint = StravionBlue
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Loading spinner
//            CircularProgressIndicator(
//                modifier = Modifier.size(48.dp),
//                color = StravionBlue,
//                strokeWidth = 4.dp
//            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Primary text
            Text(
                text = "Loading AI Model...",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp
                ),
                textAlign = TextAlign.Center
            )
            
            // Secondary text
            Text(
                text = "Preparing object detection system",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MediumLightGray,
                    fontSize = 14.sp
                ),
                textAlign = TextAlign.Center
            )
            
            // Extended loading message (appears after 6 seconds)
//            AnimatedVisibility(
//                visible = showExtendedMessage,
//                enter = fadeIn() + expandVertically(),
//                exit = fadeOut() + shrinkVertically()
//            ) {
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.spacedBy(8.dp),
//                    modifier = Modifier.padding(top = 16.dp)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Warning,
//                        contentDescription = "Taking longer",
//                        tint = Color(0xFFFFA726),
//                        modifier = Modifier.size(24.dp)
//                    )
//
//                    Text(
//                        text = "Taking longer than expected...",
//                        style = MaterialTheme.typography.bodySmall.copy(
//                            color = Color(0xFFFFA726),
//                            fontWeight = FontWeight.Medium
//                        ),
//                        textAlign = TextAlign.Center
//                    )
//
//                    Text(
//                        text = "This may happen on slower devices",
//                        style = MaterialTheme.typography.bodySmall.copy(
//                            color = MediumGray,
//                            fontSize = 12.sp
//                        ),
//                        textAlign = TextAlign.Center
//                    )
//                }
//            }
            
//            Spacer(modifier = Modifier.height(8.dp))
            
            // Elapsed time indicator
//            Text(
//                text = "${elapsedSeconds}s elapsed",
//                style = MaterialTheme.typography.bodySmall.copy(
//                    color = MediumGray,
//                    fontSize = 12.sp
//                ),
//                textAlign = TextAlign.Center
//            )
            
//            Spacer(modifier = Modifier.height(24.dp))
            
            // Skip button (subtle, at bottom)
//            TextButton(
//                onClick = onSkip,
//                colors = ButtonDefaults.textButtonColors(
//                    contentColor = MediumLightGray
//                )
//            ) {
//                Text(
//                    text = "Continue Without AI",
//                    style = MaterialTheme.typography.bodySmall.copy(
//                        fontSize = 13.sp
//                    )
//                )
//            }
        }
    }
}

/**
 * Error state overlay with retry and skip options
 */
@Composable
fun ModelLoadingErrorOverlay(
    error: ModelLoadState.Error,
    onRetry: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            // Stravion Logo
            Icon(
                painter = painterResource(id = R.drawable.stravion_logo),
                contentDescription = "Stravion",
                modifier = Modifier.size(120.dp, 40.dp),
                tint = StravionBlue
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Error icon
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                tint = Color(0xFFFF5252),
                modifier = Modifier.size(64.dp)
            )
            
            // Error title
            Text(
                text = "AI Model Load Failed",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp
                ),
                textAlign = TextAlign.Center
            )
            
            // Error message
            Text(
                text = error.error.message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MediumLightGray,
                    fontSize = 14.sp
                ),
                textAlign = TextAlign.Center
            )
            
            // Attempt counter (if retrying)
            if (error.attemptNumber > 1) {
                Text(
                    text = "Attempt ${error.attemptNumber} of ${error.maxAttempts}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MediumGray,
                        fontSize = 12.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Retry button (if retry is available)
                if (error.canRetry) {
                    Button(
                        onClick = onRetry,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StravionBlue
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Retry",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
                
                // Continue Without AI button
                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MediumLightGray
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Continue Without AI",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
            
            // Technical details (for debugging, subtle)
            if (!error.error.technicalDetails.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Technical: ${error.error.technicalDetails}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MediumGray.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}
