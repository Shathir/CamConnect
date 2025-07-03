package com.outdu.camconnect.ui.setupflow

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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

@Composable
fun LandingScreen(
    onGetStarted: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D)) // deep background
    ) {
        // 2. Blended Boat Image (Bottom Center)
        Image(
            painter = painterResource(R.drawable.landing_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
        )

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
                    painter = painterResource(R.drawable.scout_logo), // Replace with your logo
                    contentDescription = "Scout Logo",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Scout",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
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
                    text = "Long Range",
                    style = TextStyle(
                        fontSize = 40.sp,
                        lineHeight = 14.02.sp,
                        fontFamily = FontFamily(Font(R.font.onest_regular)),
                        fontWeight = FontWeight(700),
                        color = Color(0xFFCFCFCF),

                        )
                )

                Text(
                    text = "Night Vision with",
                    style = TextStyle(
                        fontSize = 40.sp,
                        lineHeight = 14.02.sp,
                        fontFamily = FontFamily(Font(R.font.onest_regular)),
                        fontWeight = FontWeight(700),
                        color = Color(0xFFCFCFCF),

                        )
                )

                Text(
                    text = "Scout",
                    style = TextStyle(
                        fontSize = 40.sp,
                        lineHeight = 14.02.sp,
                        fontFamily = FontFamily(Font(R.font.onest_regular)),
                        fontWeight = FontWeight(700),
                        color = Color(0xFFCFCFCF),

                        )
                )

                // Subtitle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val iconPainter = rememberVectorPainter(image = aiIcon(true))
                    Image(
                        painter = iconPainter,
                        contentDescription = "Ai Status Icon",
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Powered with AI Vision",
                        style = TextStyle(
                            fontSize = 16.sp,
                            lineHeight = 14.02.sp,
                            fontFamily = FontFamily(Font(R.font.onest_regular)),
                            fontWeight = FontWeight(500),
                            color = Color(0xFFCFCFCF),

                            )
                    )
                }
            }

            // CTA Button
            Button(
                onClick = onGetStarted,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Get Started", color = Color.Black)
            }
        }
    }
}

@Composable
fun LoginScreen(
    setupState: SetupState,
    onNext: () -> Unit,
    onUpdateDetails: (String, String, String, String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    )
    {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 40.dp)
                .align(Alignment.TopStart),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.scout_logo), // Replace with your logo
                    contentDescription = "Scout Logo",
                    tint = Color.Red,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Scout",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
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
                    text = "Welcome back,",
                    style = TextStyle(
                        fontSize = 40.sp,
                        lineHeight = 14.02.sp,
                        fontFamily = FontFamily(Font(R.font.onest_regular)),
                        fontWeight = FontWeight(700),
                        color = Color(0xFFCFCFCF),

                        )
                )

                Text(
                    text = "Please login to start streaming",
                    style = TextStyle(
                        fontSize = 18.sp,
                        lineHeight = 14.02.sp,
                        fontFamily = FontFamily(Font(R.font.onest_regular)),
                        fontWeight = FontWeight(500),
                        color = Color(0xFF7D7D7D),

                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            )
            {

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .fillMaxWidth()
                        .background(Color(0xFF222222)),
                    contentAlignment = Alignment.Center
                )
                {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp)
                            .align(Alignment.TopStart),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    )
                    {

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        )
                        {
                            Icon(
                                painter = painterResource(id = R.drawable.user_icon),
                                contentDescription = "User Icon",
                                tint = Color(0xFFF43823)
                            )

                            Text(
                                text = "Login as Owner",
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    lineHeight = 14.02.sp,
                                    fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                                    fontWeight = FontWeight(500),
                                    color = Color.White
                                )
                            )

                            Text(
                                text = "For adding, viewing & managing your account",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    lineHeight = 14.02.sp,
                                    fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                                    fontWeight = FontWeight(400),
                                    color = Color.White
                                )
                            )
                        }

                        Column()
                        {

                            Text(
                                text = "Username/ Email ID",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    lineHeight = 14.02.sp,
                                    fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                                    fontWeight = FontWeight(500),
                                    color = Color.White
                                )
                            )
                            // Email Input
                            OutlinedTextField(
                                value = setupState.email,
                                onValueChange = {
                                    onUpdateDetails(
                                        it,
                                        setupState.password,
                                        setupState.confirmPassword,
                                        setupState.verificationCode
                                    )
                                },
                                label = { Text("Email") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        Column()
                        {
                            Text(
                                text = "Password",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    lineHeight = 14.02.sp,
                                    fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                                    fontWeight = FontWeight(500),
                                    color = Color.White
                                )
                            )
                            // Password Input
                            OutlinedTextField(
                                value = setupState.password,
                                onValueChange = {
                                    onUpdateDetails(
                                        setupState.email,
                                        it,
                                        setupState.confirmPassword,
                                        setupState.verificationCode
                                    )
                                },
                                label = { Text("Password") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .fillMaxWidth()
                        .background(Color(0xFF222222)),
                    contentAlignment = Alignment.Center
                )
                {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp)
                            .align(Alignment.TopStart),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    )
                    {

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        )
                        {
                            Icon(
                                painter = painterResource(id = R.drawable.users_icon),
                                contentDescription = "User Icon",
                                tint = Color(0xFFF43823)
                            )

                            Text(
                                text = "Login as Viewer",
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    lineHeight = 14.02.sp,
                                    fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                                    fontWeight = FontWeight(500),
                                    color = Color.White
                                )
                            )

                            Text(
                                text = "For only streaming access across cameras",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    lineHeight = 14.02.sp,
                                    fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                                    fontWeight = FontWeight(400),
                                    color = Color.White
                                )
                            )
                        }

                        Column()
                        {

                            // Email Input
                            OutlinedTextField(
                                value = setupState.email,
                                onValueChange = {
                                    onUpdateDetails(
                                        it,
                                        setupState.password,
                                        setupState.confirmPassword,
                                        setupState.verificationCode
                                    )
                                },
                                label = { Text("Enter 6 digit Access Key",
                                    color = Color(0xFF474747)
                                    ) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }
            }

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
                color = Color.White
            )
        )

        // Clickable & underlined mail text
        Text(
            text = "Drop us a mail",
            modifier = Modifier.clickable {
                val emailIntent = Intent(
                    Intent.ACTION_SENDTO,
                    "mailto:shathir.h@outdu.com".toUri() // replace with your actual email
                )
                context.startActivity(Intent.createChooser(emailIntent, "Send email"))
            },
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 14.02.sp,
                fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                fontWeight = FontWeight(500),
                color = Color.White,
                textDecoration = TextDecoration.Underline
            )
        )
    }
}



