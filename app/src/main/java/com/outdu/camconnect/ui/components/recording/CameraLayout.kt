//@Composable
//fun CameraLayout(
//    viewModel: CameraLayoutViewModel,
//    systemStatus: SystemStatus,
//    detectionSettings: DetectionSettings,
//    customButtons: List<ButtonConfig>,
//    modifier: Modifier = Modifier
//) {
//    val isDarkTheme = isSystemInDarkTheme()
//    val deviceType = rememberDeviceType()
//
//    // Observe states from ViewModel
//    val autoDayNightEnabled = viewModel.isAutoDayNightEnabled.value
//    val currentVisionMode = viewModel.currentVisionMode.value
//    val currentCameraMode = viewModel.currentCameraMode.value
//    val currentOrientationMode = viewModel.currentOrientationMode.value
//    val hasChanges = viewModel.hasUnsavedChanges.value
//
//    Column(
//        modifier = modifier
//            .fillMaxWidth(),
//        verticalArrangement = Arrangement.spacedBy(24.dp)
//    ) {
//        // Apply Changes Button - always visible, enabled only when there are changes
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.End
//        ) {
//            Button(
//                onClick = { viewModel.applyChanges() },
//                enabled = hasChanges,
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = MaterialTheme.colorScheme.primary,
//                    disabledContainerColor = Color(0xFF2C2C2C)
//                ),
//                shape = RoundedCornerShape(8.dp)
//            ) {
//                Text(
//                    text = "Apply Changes",
//                    color = if (hasChanges) Color.White else Color(0xFF777777)
//                )
//            }
//        }
//
//        // Settings sections
//        Column(
//            verticalArrangement = Arrangement.spacedBy(24.dp)
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(24.dp)
//            ) {
//                // Auto Day/Night
//                Column(
//                    modifier = Modifier.weight(1f),
//                    verticalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    Text(
//                        text = "Auto Low Light",
//                        style = TextStyle(
//                            fontSize = if(deviceType == DeviceType.TABLET) 16.sp else 14.sp,
//                            lineHeight = 14.02.sp,
//                            fontFamily = FontFamily(Font(R.font.just_sans_regular)),
//                            fontWeight = FontWeight(500),
//                            color = if (isDarkTheme) Color.White else Color.Black
//                        )
//                    )
//                    Row(
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        OptionButton(
//                            text = "ON",
//                            isSelected = autoDayNightEnabled,
//                            onClick = { viewModel.setAutoDayNight(true) },
//                            modifier = Modifier.weight(1f),
//                            iconVal = R.drawable.yes_line
//                        )
//                        OptionButton(
//                            text = "OFF",
//                            isSelected = !autoDayNightEnabled,
//                            onClick = { viewModel.setAutoDayNight(false) },
//                            modifier = Modifier.weight(1f),
//                            iconVal = R.drawable.no_line
//                        )
//                    }
//                }
//
//                // Display Modes
//                Column(
//                    modifier = Modifier.weight(1f),
//                    verticalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    Text(
//                        text = "Display Modes",
//                        style = TextStyle(
//                            fontSize = if(deviceType == DeviceType.TABLET) 16.sp else 14.sp,
//                            lineHeight = 14.02.sp,
//                            fontFamily = FontFamily(Font(R.font.just_sans_regular)),
//                            fontWeight = FontWeight(500),
//                            color = if (isDarkTheme) Color.White else Color.Black
//                        )
//                    )
//                    Row(
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        OptionButton(
//                            text = "Visible",
//                            isSelected = currentVisionMode == VisionMode.VISION,
//                            onClick = { viewModel.setVisionMode(VisionMode.VISION) },
//                            modifier = Modifier.weight(1f),
//                            iconVal = R.drawable.eye_line
//                        )
//                        OptionButton(
//                            text = "Low Light",
//                            isSelected = currentVisionMode == VisionMode.INFRARED,
//                            onClick = { viewModel.setVisionMode(VisionMode.INFRARED) },
//                            modifier = Modifier.weight(1f),
//                            isRed = true,
//                            iconVal = R.drawable.router_line
//                        )
//                    }
//                }
//            }
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(24.dp)
//            ) {
//                // Camera Capture section
//                Column(
//                    modifier = Modifier.weight(1f),
//                    verticalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    Text(
//                        text = "Camera Capture",
//                        style = TextStyle(
//                            fontSize = if(deviceType == DeviceType.TABLET) 16.sp else 14.sp,
//                            lineHeight = 14.02.sp,
//                            fontFamily = FontFamily(Font(R.font.just_sans_regular)),
//                            fontWeight = FontWeight(500),
//                            color = if (isDarkTheme) Color.White else Color.Black
//                        )
//                    )
//                    Row(
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        OptionButton(
//                            text = "EIS",
//                            isSelected = currentCameraMode == CameraMode.EIS || currentCameraMode == CameraMode.BOTH,
//                            onClick = { viewModel.toggleCameraMode(CameraMode.EIS) },
//                            modifier = Modifier.weight(1f),
//                            iconVal = R.drawable.git_commit_line
//                        )
//                        OptionButton(
//                            text = "HDR",
//                            isSelected = currentCameraMode == CameraMode.HDR || currentCameraMode == CameraMode.BOTH,
//                            onClick = { viewModel.toggleCameraMode(CameraMode.HDR) },
//                            modifier = Modifier.weight(1f),
//                            iconVal = R.drawable.hd_settings_line
//                        )
//                    }
//                }
//
//                // Orientation section
//                Column(
//                    modifier = Modifier.weight(1f),
//                    verticalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    Text(
//                        text = "Orientation",
//                        style = TextStyle(
//                            fontSize = if(deviceType == DeviceType.TABLET) 16.sp else 14.sp,
//                            lineHeight = 14.02.sp,
//                            fontFamily = FontFamily(Font(R.font.just_sans_regular)),
//                            fontWeight = FontWeight(500),
//                            color = if (isDarkTheme) Color.White else Color.Black
//                        )
//                    )
//                    Row(
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        OptionButton(
//                            text = if(deviceType == DeviceType.TABLET) "Flip Vertical" else "Flip",
//                            isSelected = currentOrientationMode == OrientationMode.FLIP || currentOrientationMode == OrientationMode.BOTH,
//                            onClick = { viewModel.toggleOrientationMode(OrientationMode.FLIP) },
//                            modifier = Modifier.weight(1f),
//                            iconVal = R.drawable.flip_vertical_line
//                        )
//                        OptionButton(
//                            text = "Mirror",
//                            isSelected = currentOrientationMode == OrientationMode.MIRROR || currentOrientationMode == OrientationMode.BOTH,
//                            onClick = { viewModel.toggleOrientationMode(OrientationMode.MIRROR) },
//                            modifier = Modifier.weight(1f),
//                            iconVal = R.drawable.flip_horizontal_line
//                        )
//                    }
//                }
//            }
//
//            // Warning text for zoom control
//            Text(
//                text = "To Activate Zoom Control, Disable WDR & EIS",
//                style = TextStyle(
//                    fontSize = if(deviceType == DeviceType.TABLET) 16.sp else 14.sp,
//                    lineHeight = 10.51.sp,
//                    fontFamily = FontFamily(Font(R.font.just_sans_regular)),
//                    fontWeight = FontWeight(500),
//                    color = if(isDarkTheme) Color(0xFFFFFFFF) else Color(0xFF777777)
//                )
//            )
//        }
//    }
//
//    // Effect to refresh settings when the composable enters composition
//    LaunchedEffect(Unit) {
//        viewModel.refreshSettings()
//    }
//}