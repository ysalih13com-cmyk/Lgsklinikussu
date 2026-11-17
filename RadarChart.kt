package com.example.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadarChart(
    values: List<Pair<String, Int?>>, // (Axis Name, Score 0..100)
    modifier: Modifier = Modifier
) {
    val axisCount = 5
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.15f)
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f - 10.dp.toPx()
        val radius = size.minDimension / 2.5f

        // Draw concentric web polygons (33%, 66%, 100%)
        listOf(0.33f, 0.66f, 1.0f).forEach { scale ->
            val polyPath = Path()
            for (i in 0 until axisCount) {
                val angle = -Math.PI / 2.0 + i * (2.0 * Math.PI / axisCount)
                val px = cx + (cos(angle) * radius * scale).toFloat()
                val py = cy + (sin(angle) * radius * scale).toFloat()
                if (i == 0) polyPath.moveTo(px, py) else polyPath.lineTo(px, py)
            }
            polyPath.close()
            drawPath(path = polyPath, color = outlineColor.copy(alpha = 0.4f), style = Stroke(width = 1.dp.toPx()))
        }

        // Draw spokes
        for (i in 0 until axisCount) {
            val angle = -Math.PI / 2.0 + i * (2.0 * Math.PI / axisCount)
            val px = cx + (cos(angle) * radius).toFloat()
            val py = cy + (sin(angle) * radius).toFloat()
            drawLine(
                color = outlineColor.copy(alpha = 0.5f),
                start = Offset(cx, cy),
                end = Offset(px, py),
                strokeWidth = 1.dp.toPx()
            )

            // Draw axis labels
            val labelAngle = angle
            val labelDistance = radius + 22.dp.toPx()
            val lx = cx + (cos(labelAngle) * labelDistance).toFloat()
            val ly = cy + (sin(labelAngle) * labelDistance).toFloat()

            val axisData = values.getOrNull(i)
            val label = axisData?.first ?: ""
            val scoreText = axisData?.second?.let { "%$it" } ?: "%?"
            val fullText = "$label $scoreText"

            drawContext.canvas.nativeCanvas.drawText(
                fullText,
                lx,
                ly,
                Paint().apply {
                    this.color = textColor
                    this.textSize = 11.dp.toPx()
                    this.textAlign = Paint.Align.CENTER
                    this.isAntiAlias = true
                }
            )
        }

        // Draw student data filled polygon
        val dataPath = Path()
        var hasValid = false
        for (i in 0 until axisCount) {
            val angle = -Math.PI / 2.0 + i * (2.0 * Math.PI / axisCount)
            val score = (values.getOrNull(i)?.second ?: 50).coerceIn(10, 100) / 100f
            val px = cx + (cos(angle) * radius * score).toFloat()
            val py = cy + (sin(angle) * radius * score).toFloat()
            if (i == 0) {
                dataPath.moveTo(px, py)
                hasValid = true
            } else {
                dataPath.lineTo(px, py)
            }
        }
        if (hasValid) {
            dataPath.close()
            drawPath(path = dataPath, color = primaryColor.copy(alpha = 0.35f))
            drawPath(path = dataPath, color = primaryColor, style = Stroke(width = 2.5.dp.toPx()))

            // Draw data points
            for (i in 0 until axisCount) {
                val angle = -Math.PI / 2.0 + i * (2.0 * Math.PI / axisCount)
                val score = (values.getOrNull(i)?.second ?: 50).coerceIn(10, 100) / 100f
                val px = cx + (cos(angle) * radius * score).toFloat()
                val py = cy + (sin(angle) * radius * score).toFloat()
                drawCircle(color = primaryColor, radius = 4.dp.toPx(), center = Offset(px, py))
            }
        }
    }
}
