package com.outdu.camconnect.ui.components.login

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outdu.camconnect.R
import com.outdu.camconnect.auth.InvalidPinException
import com.outdu.camconnect.auth.MaxAttemptsExceededException
import com.outdu.camconnect.auth.SessionManager
import com.outdu.camconnect.viewmodels.SetupState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun OwnerLoginCard(
    setupState: SetupState,
    onUpdateDetails: (String, String, String, String) -> Unit
)
{
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
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

            Column(
                modifier = Modifier
                    .padding(end = 24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            )
            {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                )
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

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                )
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

                Text(
                    text = "Forgot Password ?",
                    style = TextStyle(
                        fontSize = 9.sp,
                        fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                        fontWeight = FontWeight(500),
                        color = Color(0xFFFFFFFF),
                    ),
                    modifier = Modifier.align(Alignment.End)
                )
            }


        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerLoginCard(
    setupState: SetupState,
    onUpdateDetails: (String, String, String, String) -> Unit,
    onAuthenticate: (Boolean) -> Unit
) {
    var isAuthenticating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(isAuthenticating) {
        if (isAuthenticating) {
            errorMessage = null
        }
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .fillMaxWidth()
            .background(Color(0xFF222222)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .align(Alignment.TopStart),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
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

            Column() {
                AccessKeyInput(
                    onPinEntered = { pin ->
                        // When PIN is exactly 4 digits, attempt authentication
                        if (pin.length == 4 && !isAuthenticating) {
                            isAuthenticating = true
                            // Launch coroutine for authentication
                            CoroutineScope(Dispatchers.Main).launch {
                                try {
                                    val result = SessionManager.authenticateWithPin(pin)
                                    result.fold(
                                        onSuccess = { success ->
                                            onAuthenticate(success)
                                        },
                                        onFailure = { error ->
                                            errorMessage = when (error) {
                                                is InvalidPinException -> "Invalid PIN"
                                                is MaxAttemptsExceededException -> error.message
                                                else -> "Authentication failed"
                                            }
                                            onAuthenticate(false)
                                        }
                                    )
                                } finally {
                                    isAuthenticating = false
                                }
                            }
                        }
                    },
                    isLoading = isAuthenticating,
                    errorMessage = errorMessage
                )
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessKeyInput(
    onPinEntered: (String) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var accessKey by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val isPinComplete = accessKey.length == 4

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextField(
            value = accessKey,
            onValueChange = { newValue ->
                if (newValue.length <= 4 && newValue.all { it.isDigit() }) {
                    accessKey = newValue
                    if (newValue.length == 4) {
                        onPinEntered(newValue)
                    }
                }
            },
            enabled = !isLoading,
            placeholder = {
                Text(
                    text = "Enter 4 digit Access Key",
                    color = Color(0xFFAAAAAA)
                )
            },
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(onClick = {
                        isPasswordVisible = !isPasswordVisible
                    }) {
                        Icon(
                            painter = painterResource(
                                id = if (isPasswordVisible) R.drawable.eye else R.drawable.eye_slash
                            ),
                            contentDescription = if (isPasswordVisible) "Hide PIN" else "Show PIN",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(Color(0xFF333333), shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp)),
            singleLine = true,
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.just_sans_regular))
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (isPinComplete && !isLoading) {
                        onPinEntered(accessKey)
                    }
                }
            ),
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.White,
                disabledTextColor = Color.White.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Error message
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color(0xFFFF3B30),
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = FontFamily(Font(R.font.just_sans_regular))
                ),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyledAccessKeyField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Animate background color on focus
    val backgroundColor by animateColorAsState(
        if (isFocused) Color(0xFF444444) else Color(0xFF333333),
        label = "FocusBackgroundColor"
    )

    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = "Enter 6 digit Access Key",
                color = Color(0xFFAAAAAA),
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.usercircle), // replace with your icon
                contentDescription = "Key Icon",
                tint = Color(0xFFFF3C1F)
            )
        },
        modifier = modifier
            .fillMaxWidth(0.9f)
            .background(backgroundColor, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp)),
        singleLine = true,
        textStyle = TextStyle(
            color = Color.White,
            fontSize = 14.sp,
            fontFamily = FontFamily(Font(R.font.just_sans_regular))
        ),
        interactionSource = interactionSource,
        colors = TextFieldDefaults.textFieldColors(
            containerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Color.White,
            disabledIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp)
    )
}
