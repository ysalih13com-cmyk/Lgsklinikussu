package com.example.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Rectangle
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WhiteboardElement
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

enum class WhiteboardTool {
    PEN, ERASER, TEXT, LINE, ARROW, CIRCLE, RECT, ASK
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WhiteboardCanvas(
    elements: List<WhiteboardElement>,
    onAddElement: (WhiteboardElement) -> Unit,
    onClear: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onAskObject: (WhiteboardElement) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTool by remember { mutableStateOf(WhiteboardTool.PEN) }
    var selectedColorHex by remember { mutableStateOf(0xFF0284C7) }
    var selectedStrokeWidth by remember { mutableFloatStateOf(4f) }

    // In-progress drawing
    var dragStartOffset by remember { mutableStateOf<Offset?>(null) }
    var currentDragOffset by remember { mutableStateOf<Offset?>(null) }
    val freehandPoints = remember { mutableStateListOf<Pair<Float, Float>>() }

    // Text Input Dialog
    var showTextDialog by remember { mutableStateOf(false) }
    var textInputPosition by remember { mutableStateOf(Offset(50f, 50f)) }
    var textInputValue by remember { mutableStateOf("") }

    val logicalWidth = 800f
    val logicalHeight = 450f

    val colorOptions = listOf(
        0xFF0F172A, // Dark slate
        0xFF0284C7, // Sky blue
        0xFF2563EB, // Royal blue
        0xFFDC2626, // Red
        0xFF16A34A, // Green
        0xFFEA580C  // Orange
    )

    Column(modifier = modifier) {
        // Whiteboard Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(selectedTool) {
                        detectTapGestures { tapOffset ->
                            val scaleX = size.width / logicalWidth
                            val scaleY = size.height / logicalHeight
                            val scale = min(scaleX, scaleY).coerceAtLeast(0.01f)
                            val offX = (size.width - logicalWidth * scale) / 2f
                            val offY = (size.height - logicalHeight * scale) / 2f

                            val lx = (tapOffset.x - offX) / scale
                            val ly = (tapOffset.y - offY) / scale

                            if (selectedTool == WhiteboardTool.TEXT) {
                                textInputPosition = Offset(lx, ly)
                                textInputValue = ""
                                showTextDialog = true
                            } else if (selectedTool == WhiteboardTool.ASK) {
                                // Find closest element
                                val nearest = elements.minByOrNull { elem ->
                                    hypot(elem.x1 - lx, elem.y1 - ly)
                                }
                                if (nearest != null) {
                                    onAskObject(nearest)
                                }
                            }
                        }
                    }
                    .pointerInput(selectedTool, selectedColorHex, selectedStrokeWidth) {
                        detectDragGestures(
                            onDragStart = { startPos ->
                                val scale = min(size.width / logicalWidth, size.height / logicalHeight).coerceAtLeast(0.01f)
                                val offX = (size.width - logicalWidth * scale) / 2f
                                val offY = (size.height - logicalHeight * scale) / 2f

                                val lx = (startPos.x - offX) / scale
                                val ly = (startPos.y - offY) / scale

                                dragStartOffset = Offset(lx, ly)
                                currentDragOffset = Offset(lx, ly)
                                freehandPoints.clear()
                                freehandPoints.add(Pair(lx, ly))
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val scale = min(size.width / logicalWidth, size.height / logicalHeight).coerceAtLeast(0.01f)
                                val offX = (size.width - logicalWidth * scale) / 2f
                                val offY = (size.height - logicalHeight * scale) / 2f

                                val lx = (change.position.x - offX) / scale
                                val ly = (change.position.y - offY) / scale
                                currentDragOffset = Offset(lx, ly)
                                if (selectedTool == WhiteboardTool.PEN || selectedTool == WhiteboardTool.ERASER) {
                                    freehandPoints.add(Pair(lx, ly))
                                }
                            },
                            onDragEnd = {
                                val start = dragStartOffset
                                val end = currentDragOffset
                                if (start != null && end != null) {
                                    when (selectedTool) {
                                        WhiteboardTool.PEN -> {
                                            if (freehandPoints.size > 1) {
                                                onAddElement(
                                                    WhiteboardElement(
                                                        id = System.currentTimeMillis().toString(),
                                                        byUser = true,
                                                        type = "stroke",
                                                        points = freehandPoints.toList(),
                                                        colorHex = selectedColorHex,
                                                        strokeWidth = selectedStrokeWidth
                                                    )
                                                )
                                            }
                                        }
                                        WhiteboardTool.ERASER -> {
                                            if (freehandPoints.size > 1) {
                                                onAddElement(
                                                    WhiteboardElement(
                                                        id = System.currentTimeMillis().toString(),
                                                        byUser = true,
                                                        type = "stroke",
                                                        points = freehandPoints.toList(),
                                                        colorHex = 0xFFFFFFFF, // white eraser
                                                        strokeWidth = selectedStrokeWidth * 4f
                                                    )
                                                )
                                            }
                                        }
                                        WhiteboardTool.LINE -> {
                                            onAddElement(
                                                WhiteboardElement(
                                                    id = System.currentTimeMillis().toString(),
                                                    byUser = true,
                                                    type = "line",
                                                    x1 = start.x,
                                                    y1 = start.y,
                                                    x2 = end.x,
                                                    y2 = end.y,
                                                    colorHex = selectedColorHex,
                                                    strokeWidth = selectedStrokeWidth
                                                )
                                            )
                                        }
                                        WhiteboardTool.ARROW -> {
                                            onAddElement(
                                                WhiteboardElement(
                                                    id = System.currentTimeMillis().toString(),
                                                    byUser = true,
                                                    type = "arrow",
                                                    x1 = start.x,
                                                    y1 = start.y,
                                                    x2 = end.x,
                                                    y2 = end.y,
                                                    colorHex = selectedColorHex,
                                                    strokeWidth = selectedStrokeWidth
                                                )
                                            )
                                        }
                                        WhiteboardTool.CIRCLE -> {
                                            val r = hypot(end.x - start.x, end.y - start.y)
                                            onAddElement(
                                                WhiteboardElement(
                                                    id = System.currentTimeMillis().toString(),
                                                    byUser = true,
                                                    type = "circle",
                                                    x1 = start.x,
                                                    y1 = start.y,
                                                    radius = r,
                                                    colorHex = selectedColorHex,
                                                    strokeWidth = selectedStrokeWidth
                                                )
                                            )
                                        }
                                        WhiteboardTool.RECT -> {
                                            val rx = min(start.x, end.x)
                                            val ry = min(start.y, end.y)
                                            val rw = kotlin.math.abs(end.x - start.x)
                                            val rh = kotlin.math.abs(end.y - start.y)
                                            onAddElement(
                                                WhiteboardElement(
                                                    id = System.currentTimeMillis().toString(),
                                                    byUser = true,
                                                    type = "rect",
                                                    x1 = rx,
                                                    y1 = ry,
                                                    width = rw,
                                                    height = rh,
                                                    colorHex = selectedColorHex,
                                                    strokeWidth = selectedStrokeWidth
                                                )
                                            )
                                        }
                                        else -> {}
                                    }
                                }
                                dragStartOffset = null
                                currentDragOffset = null
                                freehandPoints.clear()
                            },
                            onDragCancel = {
                                dragStartOffset = null
                                currentDragOffset = null
                                freehandPoints.clear()
                            }
                        )
                    }
            ) {
                val scale = min(size.width / logicalWidth, size.height / logicalHeight).coerceAtLeast(0.01f)
                val offX = (size.width - logicalWidth * scale) / 2f
                val offY = (size.height - logicalHeight * scale) / 2f

                fun toCanvasX(lx: Float) = offX + lx * scale
                fun toCanvasY(ly: Float) = offY + ly * scale

                // Render committed elements
                elements.forEach { elem ->
                    val color = Color(elem.colorHex)
                    val sWidth = elem.strokeWidth * scale

                    when (elem.type) {
                        "stroke" -> {
                            if (elem.points.size > 1) {
                                val p = Path()
                                p.moveTo(toCanvasX(elem.points[0].first), toCanvasY(elem.points[0].second))
                                for (i in 1 until elem.points.size) {
                                    p.lineTo(toCanvasX(elem.points[i].first), toCanvasY(elem.points[i].second))
                                }
                                drawPath(
                                    path = p,
                                    color = color,
                                    style = Stroke(width = sWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                        }
                        "line", "underline" -> {
                            drawLine(
                                color = color,
                                start = Offset(toCanvasX(elem.x1), toCanvasY(elem.y1)),
                                end = Offset(toCanvasX(elem.x2), toCanvasY(elem.y2)),
                                strokeWidth = sWidth,
                                cap = StrokeCap.Round
                            )
                        }
                        "arrow" -> {
                            val sx = toCanvasX(elem.x1)
                            val sy = toCanvasY(elem.y1)
                            val ex = toCanvasX(elem.x2)
                            val ey = toCanvasY(elem.y2)
                            drawLine(color = color, start = Offset(sx, sy), end = Offset(ex, ey), strokeWidth = sWidth, cap = StrokeCap.Round)
                            val angle = atan2(ey - sy, ex - sx)
                            val arrowLen = 14f * scale
                            drawLine(color = color, start = Offset(ex, ey), end = Offset(ex - arrowLen * cos(angle - 0.45f), ey - arrowLen * sin(angle - 0.45f)), strokeWidth = sWidth, cap = StrokeCap.Round)
                            drawLine(color = color, start = Offset(ex, ey), end = Offset(ex - arrowLen * cos(angle + 0.45f), ey - arrowLen * sin(angle + 0.45f)), strokeWidth = sWidth, cap = StrokeCap.Round)
                        }
                        "circle" -> {
                            drawCircle(
                                color = color,
                                radius = elem.radius * scale,
                                center = Offset(toCanvasX(elem.x1), toCanvasY(elem.y1)),
                                style = Stroke(width = sWidth)
                            )
                        }
                        "rect" -> {
                            drawRect(
                                color = color,
                                topLeft = Offset(toCanvasX(elem.x1), toCanvasY(elem.y1)),
                                size = Size(elem.width * scale, elem.height * scale),
                                style = Stroke(width = sWidth)
                            )
                        }
                        "write" -> {
                            val textStr = elem.text ?: ""
                            drawContext.canvas.nativeCanvas.drawText(
                                textStr,
                                toCanvasX(elem.x1),
                                toCanvasY(elem.y1) + (elem.fontSize * scale),
                                Paint().apply {
                                    this.color = android.graphics.Color.parseColor(
                                        String.format("#%06X", 0xFFFFFF and elem.colorHex.toInt())
                                    )
                                    this.textSize = elem.fontSize * scale
                                    this.isAntiAlias = true
                                    this.typeface = android.graphics.Typeface.DEFAULT_BOLD
                                }
                            )
                        }
                    }
                }

                // Render temporary in-progress preview
                val s = dragStartOffset
                val c = currentDragOffset
                if (s != null && c != null) {
                    val previewColor = Color(selectedColorHex).copy(alpha = 0.85f)
                    val pWidth = selectedStrokeWidth * scale
                    when (selectedTool) {
                        WhiteboardTool.PEN, WhiteboardTool.ERASER -> {
                            if (freehandPoints.size > 1) {
                                val p = Path()
                                p.moveTo(toCanvasX(freehandPoints[0].first), toCanvasY(freehandPoints[0].second))
                                for (i in 1 until freehandPoints.size) {
                                    p.lineTo(toCanvasX(freehandPoints[i].first), toCanvasY(freehandPoints[i].second))
                                }
                                drawPath(
                                    path = p,
                                    color = if (selectedTool == WhiteboardTool.ERASER) Color.White else previewColor,
                                    style = Stroke(width = if (selectedTool == WhiteboardTool.ERASER) pWidth * 4f else pWidth, cap = StrokeCap.Round)
                                )
                            }
                        }
                        WhiteboardTool.LINE -> {
                            drawLine(color = previewColor, start = Offset(toCanvasX(s.x), toCanvasY(s.y)), end = Offset(toCanvasX(c.x), toCanvasY(c.y)), strokeWidth = pWidth, cap = StrokeCap.Round)
                        }
                        WhiteboardTool.ARROW -> {
                            val sx = toCanvasX(s.x)
                            val sy = toCanvasY(s.y)
                            val ex = toCanvasX(c.x)
                            val ey = toCanvasY(c.y)
                            drawLine(color = previewColor, start = Offset(sx, sy), end = Offset(ex, ey), strokeWidth = pWidth, cap = StrokeCap.Round)
                            val angle = atan2(ey - sy, ex - sx)
                            val arrowLen = 14f * scale
                            drawLine(color = previewColor, start = Offset(ex, ey), end = Offset(ex - arrowLen * cos(angle - 0.45f), ey - arrowLen * sin(angle - 0.45f)), strokeWidth = pWidth, cap = StrokeCap.Round)
                            drawLine(color = previewColor, start = Offset(ex, ey), end = Offset(ex - arrowLen * cos(angle + 0.45f), ey - arrowLen * sin(angle + 0.45f)), strokeWidth = pWidth, cap = StrokeCap.Round)
                        }
                        WhiteboardTool.CIRCLE -> {
                            val r = hypot(c.x - s.x, c.y - s.y) * scale
                            drawCircle(color = previewColor, radius = r, center = Offset(toCanvasX(s.x), toCanvasY(s.y)), style = Stroke(width = pWidth))
                        }
                        WhiteboardTool.RECT -> {
                            val rx = min(toCanvasX(s.x), toCanvasX(c.x))
                            val ry = min(toCanvasY(s.y), toCanvasY(c.y))
                            val rw = kotlin.math.abs(toCanvasX(c.x) - toCanvasX(s.x))
                            val rh = kotlin.math.abs(toCanvasY(c.y) - toCanvasY(s.y))
                            drawRect(color = previewColor, topLeft = Offset(rx, ry), size = Size(rw, rh), style = Stroke(width = pWidth))
                        }
                        else -> {}
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Toolbar
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                // Tools Row
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    WhiteboardToolButton(
                        icon = Icons.Default.Edit,
                        label = "Kalem",
                        selected = selectedTool == WhiteboardTool.PEN,
                        onClick = { selectedTool = WhiteboardTool.PEN }
                    )
                    WhiteboardToolButton(
                        icon = Icons.Default.TextFields,
                        label = "Yazı",
                        selected = selectedTool == WhiteboardTool.TEXT,
                        onClick = { selectedTool = WhiteboardTool.TEXT }
                    )
                    WhiteboardToolButton(
                        icon = Icons.Default.HorizontalRule,
                        label = "Çizgi",
                        selected = selectedTool == WhiteboardTool.LINE,
                        onClick = { selectedTool = WhiteboardTool.LINE }
                    )
                    WhiteboardToolButton(
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        label = "Ok",
                        selected = selectedTool == WhiteboardTool.ARROW,
                        onClick = { selectedTool = WhiteboardTool.ARROW }
                    )
                    WhiteboardToolButton(
                        icon = Icons.Default.RadioButtonUnchecked,
                        label = "Daire",
                        selected = selectedTool == WhiteboardTool.CIRCLE,
                        onClick = { selectedTool = WhiteboardTool.CIRCLE }
                    )
                    WhiteboardToolButton(
                        icon = Icons.Default.Rectangle,
                        label = "Kutu",
                        selected = selectedTool == WhiteboardTool.RECT,
                        onClick = { selectedTool = WhiteboardTool.RECT }
                    )
                    WhiteboardToolButton(
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        label = "Bu Neden?",
                        selected = selectedTool == WhiteboardTool.ASK,
                        onClick = { selectedTool = WhiteboardTool.ASK }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Colors, Thickness and Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Color swatches
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        colorOptions.forEach { col ->
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(col))
                                    .border(
                                        width = if (selectedColorHex == col) 2.5.dp else 1.dp,
                                        color = if (selectedColorHex == col) MaterialTheme.colorScheme.onSurface else Color.LightGray,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColorHex = col }
                            )
                        }
                    }

                    // Stroke width
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        listOf(2f to "İnce", 4f to "Orta", 8f to "Kalın").forEach { (w, lbl) ->
                            Text(
                                text = lbl,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selectedStrokeWidth == w) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedStrokeWidth == w) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .clickable { selectedStrokeWidth = w }
                            )
                        }
                    }

                    // Undo / Redo / Clear
                    Row {
                        IconButton(onClick = onUndo, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Geri Al", modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onRedo, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "İleri Al", modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onClear, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Temizle", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }

    if (showTextDialog) {
        AlertDialog(
            onDismissRequest = { showTextDialog = false },
            title = { Text("Tahtaya Metin Ekle") },
            text = {
                OutlinedTextField(
                    value = textInputValue,
                    onValueChange = { textInputValue = it },
                    label = { Text("Metin") },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (textInputValue.isNotBlank()) {
                        onAddElement(
                            WhiteboardElement(
                                id = System.currentTimeMillis().toString(),
                                byUser = true,
                                type = "write",
                                text = textInputValue,
                                x1 = textInputPosition.x,
                                y1 = textInputPosition.y,
                                colorHex = selectedColorHex,
                                fontSize = 22f
                            )
                        )
                    }
                    showTextDialog = false
                }) {
                    Text("Ekle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTextDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

@Composable
fun WhiteboardToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    if (selected) {
        Button(
            onClick = onClick,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier.height(34.dp)
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, fontSize = 12.sp)
        }
    } else {
        FilledTonalButton(
            onClick = onClick,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ),
            modifier = Modifier.height(34.dp)
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, fontSize = 12.sp)
        }
    }
}
