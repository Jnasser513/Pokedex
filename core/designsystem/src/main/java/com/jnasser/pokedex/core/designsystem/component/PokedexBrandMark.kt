package com.jnasser.pokedex.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun PokedexBrandMark(
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerLowest
    val outlineColor = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = modifier.size(40.dp)) {
        val diameter = size.minDimension
        val topLeft = Offset(
            x = (size.width - diameter) / 2f,
            y = (size.height - diameter) / 2f,
        )
        val circleSize = Size(diameter, diameter)
        val lineWidth = diameter * 0.09f

        drawArc(
            color = primaryColor,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = topLeft,
            size = circleSize,
        )
        drawArc(
            color = surfaceColor,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = topLeft,
            size = circleSize,
        )
        drawCircle(
            color = outlineColor,
            radius = diameter / 2f,
            center = center,
            style = Stroke(width = lineWidth),
        )
        drawLine(
            color = outlineColor,
            start = Offset(topLeft.x, center.y),
            end = Offset(topLeft.x + diameter, center.y),
            strokeWidth = lineWidth,
            cap = StrokeCap.Round,
        )
        drawCircle(
            color = surfaceColor,
            radius = diameter * 0.18f,
            center = center,
        )
        drawCircle(
            color = outlineColor,
            radius = diameter * 0.18f,
            center = center,
            style = Stroke(width = lineWidth * 0.75f),
        )
        drawCircle(
            color = primaryColor,
            radius = diameter * 0.07f,
            center = center,
        )
    }
}
