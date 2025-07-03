package com.outdu.camconnect.ui.components.buttons

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ZoomSelector(
    zoomLevels: List<Float> = listOf(1f, 2f, 4f),
    initialZoom: Float = 1f,
    onZoomChanged: (Float) -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val selectedIndex = remember { mutableStateOf(zoomLevels.indexOf(initialZoom)) }

    LaunchedEffect(Unit) {
        // Scroll to initial index
        listState.scrollToItem(selectedIndex.value)
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val center = listState.firstVisibleItemIndex +
                    if (listState.firstVisibleItemScrollOffset > 100) 1 else 0
            val clampedIndex = center.coerceIn(0, zoomLevels.lastIndex)
            selectedIndex.value = clampedIndex
            onZoomChanged(zoomLevels[clampedIndex])
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier
            .height(60.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 32.dp),
        flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    ) {
        itemsIndexed(zoomLevels) { index, zoom ->
            val isSelected = index == selectedIndex.value
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(if (isSelected) 60.dp else 50.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color.Black else Color.LightGray)
                    .clickable {
                        coroutineScope.launch {
                            listState.animateScrollToItem(index)
                        }
                    }
            ) {
                Text(
                    text = "${zoom.toInt()}X",
                    color = if (isSelected) Color.White else Color.Black,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = if (isSelected) 20.sp else 16.sp
                )
            }
        }
    }
}
