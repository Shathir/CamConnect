package com.outdu.camconnect.ui.components.buttons

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outdu.camconnect.R
import com.outdu.camconnect.ui.theme.AppColors.ButtonBgColor
import com.outdu.camconnect.ui.theme.AppColors.ButtonIconColor
import com.outdu.camconnect.ui.theme.AppColors.ButtonSelectedBgColor
import com.outdu.camconnect.ui.theme.AppColors.ButtonSelectedIconColor
import kotlinx.coroutines.launch

@Composable
fun ZoomSelector(
    zoomLevels: List<Float> = listOf(1f, 2f, 4f),
    initialZoom: Float = 1f,
    onZoomChanged: (Float) -> Unit
) {
    val selectedIndex = remember { mutableStateOf(zoomLevels.indexOf(initialZoom)) }

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        zoomLevels.forEachIndexed { index, zoom ->
            val isSelected = index == selectedIndex.value

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) ButtonSelectedBgColor else ButtonBgColor)
                    .clickable {
                        selectedIndex.value = index
                        onZoomChanged(zoom)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${zoom.toInt()}X",
                    color = if (isSelected) ButtonSelectedIconColor else ButtonIconColor,
                    fontFamily = FontFamily(Font(R.font.just_sans_regular)),
                    fontWeight = FontWeight(500),
                    fontSize = if (isSelected) 20.sp else 16.sp
                )
            }
        }
    }
}
