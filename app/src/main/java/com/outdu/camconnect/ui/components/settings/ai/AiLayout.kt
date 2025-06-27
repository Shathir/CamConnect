package com.outdu.camconnect.ui.components.settings.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outdu.camconnect.R
import com.outdu.camconnect.ui.theme.LightGray


@Composable
fun AiLayout() {
    // This is a placeholder for the AI layout.
    // You can add your AI-related settings or information here.
    // For now, it will just display a simple message.
    // Replace this with your actual AI settings UI.
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            )
            {
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
                            color = Color(0xFFFFFFFF),

                            )
                    )
                }
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
                            color = Color(0xFFFFFFFF),

                            )
                    )
                }
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
                            color = Color(0xFFFFFFFF),

                            )
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            )
            {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Advanced Recording",
                        style = TextStyle(
                            fontSize = 16.sp,
                            lineHeight = 14.02.sp,
                            fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                            fontWeight = FontWeight(500),
                            color = Color(0xFFFFFFFF),

                            )
                    )
                }
            }

        }

    }
}