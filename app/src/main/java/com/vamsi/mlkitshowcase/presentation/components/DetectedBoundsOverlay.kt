package com.vamsi.mlkitshowcase.presentation.components

import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun DetectedBoundsOverlay(
    bounds: List<Rect>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        bounds.forEach { rect ->
            drawRect(
                color = Color(0xFF00E5FF),
                topLeft = Offset(rect.left.toFloat(), rect.top.toFloat()),
                size = Size(rect.width().toFloat(), rect.height().toFloat()),
                style = Stroke(width = 4f)
            )
        }
    }
}
