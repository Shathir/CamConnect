package com.outdu.camconnect.ui.setupflow

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outdu.camconnect.R
import com.outdu.camconnect.viewmodels.SetupState
import com.outdu.camconnect.viewmodels.SetupViewModel
import com.outdu.camconnect.ui.auth.PinAuthDialog
import com.outdu.camconnect.auth.SessionManager
import com.outdu.camconnect.ui.components.indicators.aiIcon
import kotlin.math.min
import androidx.core.net.toUri
import com.outdu.camconnect.ui.components.login.OwnerLoginCard
import com.outdu.camconnect.ui.components.login.ViewerLoginCard
import com.outdu.camconnect.ui.theme.AppColors.StravionBlue
import com.outdu.camconnect.ui.theme.SpyBlue
import com.outdu.camconnect.utils.DeviceType
import com.outdu.camconnect.utils.rememberDeviceType

@Composable
fun LandingScreen(
    onGetStarted: () -> Unit
) {
    val deviceType = rememberDeviceType()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StravionBlue) // deep background
    ) {
        // 2. Blended Boat Image (Bottom Center)

        // 3. Foreground Content (Text + CTA)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 40.dp)
                .align(Alignment.TopStart),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Logo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.stravion_logo), // Replace with your logo
                    contentDescription = "Scout Logo",
                    tint = Color.White,
                    modifier = Modifier.width(if (deviceType == DeviceType.TABLET) 280.dp else 140.dp)
                        .height(if(deviceType == DeviceType.TABLET) 60.dp else 30.dp)
                )
            }


            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            )
            {
                // Title
                Text(
                    text = "We’re transforming navigation and situational awareness with trailblazing systems combining AI, radar, IR, and day & night vision",
                    style = TextStyle(
                        fontSize = 20.45.sp,
                        lineHeight = 26.59.sp,
                        fontFamily = FontFamily(Font(R.font.space_grotesk)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFFFFFFFF),
                        )
                )

            }

            // CTA Button
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        width = 1.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable {
                        onGetStarted()
                    },
                contentAlignment = Alignment.Center
            ) {

                Row(
                    modifier = Modifier.align(Alignment.Center)
                        .padding(vertical = 12.dp, horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                )
                {
                    Text(
                        text = "Get started",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontFamily = FontFamily(Font(R.font.space_grotesk)),
                            fontWeight = FontWeight(700),
                            color = Color(0xFFFFFFFF),

                            )
                    )

                    Icon(
                        painter = painterResource(R.drawable.key_right),
                        contentDescription = "Key Right",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
        )
        {
            Image(
                painter = painterResource(R.drawable.stravion_image),
                contentDescription = "Boat Image",
                modifier = Modifier.size(if (deviceType == DeviceType.TABLET) 480.dp else 240.dp)
                    .padding(bottom = if (deviceType == DeviceType.TABLET) 120.dp else 60.dp),
                alignment = Alignment.BottomEnd
            )
        }
    }
}

@Composable
fun LoginScreen(
    setupState: SetupState,
    onNext: () -> Unit,
    onUpdateDetails: (String, String, String, String) -> Unit,
    onAuthenticate: (Boolean) -> Unit
) {
    val deviceType = rememberDeviceType()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 40.dp)
                .align(Alignment.TopStart),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.stravion_logo), // Replace with your logo
                    contentDescription = "Scout Logo",
                    tint = SpyBlue,
                    modifier = Modifier
                        .width(88.dp)
                        .height(12.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            )
            {
                // Title
                Text(
                    text = "Hello",
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontFamily = FontFamily(Font(R.font.arial_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF1A1A1C),

                        )
                )

                Text(
                    text = "Please select how would you like to get started ?",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.arial_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFF9097A0),

                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(if(deviceType == DeviceType.TABLET) 12.dp else  4.dp)
            ) {
//                OwnerLoginCard(
//                    setupState = setupState,
//                    onUpdateDetails = onUpdateDetails
//                )
                ViewerLoginCard(
                    setupState = setupState,
                    onUpdateDetails = onUpdateDetails,
                    onAuthenticate = onAuthenticate
                )
            }

            UserCreationRow()

            SupportRow()
        }
    }
}


@Composable
fun EmailVerificationScreen(
    setupState: SetupState,
    onVerify: () -> Unit,
    onUpdateCode: (String) -> Unit
) {
    var verificationCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Verify Your Email",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "We've sent a verification code to ${setupState.email}",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        OutlinedTextField(
            value = verificationCode,
            onValueChange = {
                verificationCode = it
                onUpdateCode(it)
            },
            label = { Text("Verification Code") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onVerify,
            modifier = Modifier.fillMaxWidth(),
            enabled = verificationCode.isNotBlank()
        ) {
            Text("Verify")
        }
    }
}

@Composable
fun CameraConnectionScreen(
    onConnectCamera: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Connect Your Camera",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "To connect your camera:",
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = "1. Make sure your camera is powered on\n" +
                    "2. Enable WiFi on your camera\n" +
                    "3. Connect your phone to the camera's WiFi network\n" +
                    "4. Click the button below to start the connection process",
            style = MaterialTheme.typography.bodyMedium
        )

        Button(
            onClick = onConnectCamera,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Connect Camera")
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun SetupCompleteScreen(
    onStartStreaming: () -> Unit
) {
    var showPinDialog by remember { mutableStateOf(false) }

    // Check if already authenticated
    val isAuthenticated by derivedStateOf { SessionManager.isAuthenticated() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Setup Complete!",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Your camera is now connected and ready to stream.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Show authentication status
        if (isAuthenticated) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "✓ Authenticated - Ready to stream",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    text = "Authentication required to start streaming",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (isAuthenticated) {
                    // Already authenticated, go directly to streaming
                    onStartStreaming()
                } else {
                    // Show PIN dialog for authentication
                    showPinDialog = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isAuthenticated) "Start Streaming" else "Authenticate & Start Streaming")
        }
    }

    // PIN Authentication Dialog
    if (showPinDialog) {
        PinAuthDialog(
            onSuccess = {
                showPinDialog = false
                onStartStreaming()
            },
            onCancel = {
                showPinDialog = false
            }
        )
    }
}


@Composable
fun FadingImage(
    imageResId: Int,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val radius = with(LocalDensity.current) {
            (min(maxWidth.toPx(), maxHeight.toPx()) * 0.65f)
        }

        Box {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.matchParentSize()
            )

//            Canvas(modifier = Modifier.matchParentSize()) {
//                drawRect(
//                    brush = Brush.radialGradient(
//                        colors = listOf(Color.Transparent, Color(0xFF0D0D0D)),
//                        center = Offset(size.width / 2f, size.height / 2f),
//                        radius = radius
//                    ),
//                    size = size
//                )
//            }
        }
    }
}

@Composable
fun SupportRow() {
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Title
        Text(
            text = "Facing any issues?",
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 14.02.sp,
                fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                fontWeight = FontWeight(500),
                color = Color(0xFF9097A0)
            )
        )

        // Clickable & underlined mail text
        Text(
            text = "Drop us a mail",
            modifier = Modifier.clickable {
                val emailIntent = Intent(
                    Intent.ACTION_SENDTO,
                    "mailto:info@outdu.com".toUri() // replace with your actual email
                )
                context.startActivity(Intent.createChooser(emailIntent, "Send email"))
            },
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 14.02.sp,
                fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                fontWeight = FontWeight(500),
                color = Color(0xFF9097A0),
                textDecoration = TextDecoration.Underline
            )
        )
    }
}

@Composable
fun UserCreationRow()
{

    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Title
        Text(
            text = "First time here?",
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 14.02.sp,
                fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                fontWeight = FontWeight(500),
                color = Color(0xFF221F1F)
            )
        )

        // Clickable & underlined mail text
        Text(
            text = "Create Account",
            modifier = Modifier.clickable {
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    "https://frontend.192.168.1.129.nip.io".toUri()
                )
                context.startActivity(browserIntent)
            },
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 14.02.sp,
                fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                fontWeight = FontWeight(700),
                color = StravionBlue,
            )
        )
    }

}



