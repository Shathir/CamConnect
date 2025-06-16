package com.outdu.camconnect.ui.components.indicators

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.PathParser
import com.outdu.camconnect.ui.theme.*

@Composable
fun aiIcon(isEnabled: Boolean): ImageVector {
    return ImageVector.Builder(
        name = "AiStatusVector",
        defaultWidth = 13.dp,
        defaultHeight = 13.dp,
        viewportWidth = 13f,
        viewportHeight = 13f
    ).apply {
        // Parse SVG pathData into PathNode list
        val nodes = PathParser().parsePathString(
            "M6.052,9.915C5.872,10.327 5.302,10.327 5.122,9.915L4.654,8.843C4.237,7.889 3.488,7.129 2.553,6.714L1.264,6.142C0.855,5.961 0.855,5.365 1.264,5.183L2.512,4.629C3.471,4.203 4.235,3.416 4.644,2.429L5.118,1.287C5.294,0.863 5.88,0.863 6.056,1.287L6.53,2.429C6.939,3.416 7.702,4.203 8.662,4.629L9.91,5.183C10.319,5.365 10.319,5.961 9.91,6.142L8.621,6.714C7.686,7.129 6.937,7.889 6.52,8.843L6.052,9.915ZM2.812,5.663C4.031,6.204 5.023,7.093 5.587,8.317C6.151,7.093 7.143,6.204 8.362,5.663C7.128,5.115 6.134,4.182 5.587,2.937C5.04,4.182 4.046,5.115 2.812,5.663ZM10.738,12.525L10.87,12.223C11.104,11.685 11.527,11.257 12.054,11.023L12.46,10.842C12.679,10.745 12.679,10.426 12.46,10.329L12.077,10.159C11.536,9.919 11.106,9.474 10.875,8.918L10.74,8.592C10.646,8.365 10.332,8.365 10.238,8.592L10.102,8.918C9.872,9.474 9.442,9.919 8.901,10.159L8.518,10.329C8.299,10.426 8.299,10.745 8.518,10.842L8.923,11.023C9.451,11.257 9.873,11.685 10.108,12.223L10.24,12.525C10.336,12.746 10.642,12.746 10.738,12.525ZM10.19,10.582L10.49,10.284L10.784,10.582L10.49,10.872L10.19,10.582Z"
        ).toNodes()

        addPath(
            pathData = nodes,
            fill = if (!isEnabled) {
                SolidColor(Gray)
            } else {
                Brush.linearGradient(
                    colors = listOf(AiGradientStart, AiGradientEnd),
                    start = Offset(1.021f, 3.669f),
                    end = Offset(9.088f, 12.627f)
                )
            },
            fillAlpha = 1.0f
        )
    }.build()
}

