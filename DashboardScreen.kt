package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LgsCurriculum
import com.example.data.LgsRepository
import com.example.data.TodoItem
import com.example.data.TrialExamItem
import com.example.ui.navigation.Screen
import com.example.ui.theme.CourseHistory
import com.example.ui.theme.CourseMath
import com.example.ui.theme.CourseScience
import com.example.ui.theme.CourseTurkish
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TrackGray
import com.example.ui.theme.courseAccent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val TrLocale: Locale = Locale.forLanguageTag("tr-TR")

private fun fmt2(value: Float): String = String.format(TrLocale, "%.2f", value)

private fun fmtSigned(value: Float): String = String.format(TrLocale, "%+.2f", value)

/** Ana Sayfa: selamlama, LGS sayacı, net gelişimi, ders netleri, bugünün görevleri. */
@Composable
fun DashboardScreen(
    repository: LgsRepository,
    onNavigate: (Screen) -> Unit,
    onOpenLumi: () -> Unit
) {
    val studentName by repository.studentName.collectAsState()
    val trialExams by repository.trialExams.collectAsState()
    val todos by repository.todos.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { GreetingRow(studentName = studentName) }
            item { NetTrendCard(exams = trialExams, onAddExam = { onNavigate(Screen.TrialExams) }) }
            item { CourseNetsCard(exams = trialExams) }
            item {
                TodayTasksCard(
                    todos = todos,
                    onToggle = { id -> repository.toggleTodo(id) },
                    onNavigate = onNavigate
                )
            }
        }

        LumiAskBar(
            onClick = onOpenLumi,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
        )
    }
}

@Composable
private fun ContentCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun GreetingRow(studentName: String) {
    val now = remember { Calendar.getInstance() }
    val dateText = remember { SimpleDateFormat("d MMMM EEEE", TrLocale).format(now.time) }
    val hour = now.get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Günaydın"
        hour < 18 -> "İyi günler"
        else -> "İyi akşamlar"
    }
    val title = if (studentName.isBlank()) greeting else greeting + " " + studentName.trim()

    // LGS 2027 sayacı: eski ana sayfadaki tarihle aynı (6 Haziran 2027).
    val daysLeft = remember {
        val exam = Calendar.getInstance().apply { set(2027, Calendar.JUNE, 6, 9, 30, 0) }.timeInMillis
        ((exam - System.currentTimeMillis()).coerceAtLeast(0L) / (1000L * 60 * 60 * 24)).toInt()
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dateText,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = title,
                fontSize = 26.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Bugün de hedeflerine bir adım daha yaklaş!",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "LGS'ye",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = daysLeft.toString() + " gün",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun NetTrendCard(
    exams: List<TrialExamItem>,
    onAddExam: () -> Unit
) {
    var selected by remember { mutableIntStateOf(-1) }
    var menuOpen by remember { mutableStateOf(false) }

    ContentCard {
        if (exams.isEmpty()) {
            Text(
                text = "Deneme Net Gelişimi",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Henüz deneme eklemedin. İlk denemeni ekle, net gelişimin burada görünsün.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onAddExam) { Text("Deneme ekle") }
        } else {
            val idx = if (selected in exams.indices) selected else exams.lastIndex
            val current = exams[idx]
            val previous = exams.getOrNull(idx - 1)
            val window = exams.subList(0, idx + 1).takeLast(6)
            val firstNumber = idx + 1 - window.size + 1

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Deneme Net Gelişimi",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .border(
                                BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                RoundedCornerShape(18.dp)
                            )
                            .clickable { menuOpen = true }
                            .padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = current.name,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.width(96.dp)
                        )
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        exams.indices.reversed().take(12).forEach { i ->
                            DropdownMenuItem(
                                text = { Text(exams[i].name) },
                                onClick = {
                                    selected = i
                                    menuOpen = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Toplam net",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = fmt2(current.totalNet),
                fontSize = 40.sp,
                lineHeight = 44.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (previous != null) {
                val delta = current.totalNet - previous.totalNet
                Text(
                    text = fmtSigned(delta) + " önceki denemeye göre",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (delta >= 0f) SuccessGreen else ErrorRed
                )
            } else {
                Text(
                    text = "İlk denemen",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            NetLineChart(
                values = window.map { it.totalNet },
                firstNumber = firstNumber
            )
        }
    }
}

@Composable
private fun NetLineChart(
    values: List<Float>,
    firstNumber: Int
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurface
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant
    val pointFill = MaterialTheme.colorScheme.surface
    val chartHeight: Dp = 110.dp
    val axisWidth: Dp = 28.dp
    val maxNet = 90f

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val plotWidth: Dp = maxWidth - axisWidth
        val cell: Dp = plotWidth / values.size

        Column {
            Box(modifier = Modifier.fillMaxWidth().height(chartHeight)) {
                Canvas(
                    modifier = Modifier
                        .width(plotWidth)
                        .height(chartHeight)
                ) {
                    val cellPx = size.width / values.size
                    val pts = values.mapIndexed { i, v ->
                        Offset(
                            x = cellPx * (i + 0.5f),
                            y = size.height * (1f - (v / maxNet).coerceIn(0f, 1f))
                        )
                    }
                    val fill = Path().apply {
                        moveTo(pts.first().x, size.height)
                        pts.forEach { lineTo(it.x, it.y) }
                        lineTo(pts.last().x, size.height)
                        close()
                    }
                    drawPath(
                        path = fill,
                        brush = Brush.verticalGradient(
                            listOf(lineColor.copy(alpha = 0.22f), Color.Transparent)
                        )
                    )
                    for (i in 0 until pts.size - 1) {
                        drawLine(
                            color = lineColor,
                            start = pts[i],
                            end = pts[i + 1],
                            strokeWidth = 2.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                    pts.forEach { p ->
                        drawCircle(color = pointFill, radius = 5.dp.toPx(), center = p)
                        drawCircle(
                            color = lineColor,
                            radius = 5.dp.toPx(),
                            center = p,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }

                values.forEachIndexed { i, v ->
                    val pointY: Dp = chartHeight * (1f - (v / maxNet).coerceIn(0f, 1f))
                    val labelY: Dp = if (pointY - 20.dp < 0.dp) 0.dp else pointY - 20.dp
                    Box(
                        modifier = Modifier
                            .offset(x = cell * i, y = labelY)
                            .width(cell),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = fmt2(v),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = labelColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .height(chartHeight),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    listOf("90", "60", "30", "0").forEach { t ->
                        Text(text = t, fontSize = 11.sp, color = axisColor)
                    }
                }
            }

            Row(modifier = Modifier.width(plotWidth)) {
                values.indices.forEach { i ->
                    Text(
                        text = "Deneme " + (firstNumber + i),
                        fontSize = 10.sp,
                        color = axisColor,
                        maxLines = 1,
                        softWrap = false,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(cell)
                    )
                }
            }
        }
    }
}

private data class CourseRowInfo(val id: String, val name: String, val color: Color)

private val courseRows = listOf(
    CourseRowInfo("mat", "Matematik", CourseMath),
    CourseRowInfo("fen", "Fen", CourseScience),
    CourseRowInfo("tur", "Türkçe", CourseTurkish),
    CourseRowInfo("ink", "İnkılap", CourseHistory),
    CourseRowInfo("din", "Din", courseAccent("din")),
    CourseRowInfo("ing", "İngilizce", courseAccent("ing"))
)

@Composable
private fun CourseNetsCard(exams: List<TrialExamItem>) {
    var onlyMain by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    val latest = exams.lastOrNull()
    val rows = if (onlyMain) courseRows.take(4) else courseRows

    ContentCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Ders Bazlı Netler",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .border(
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            RoundedCornerShape(18.dp)
                        )
                        .clickable { menuOpen = true }
                        .padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (onlyMain) "Ana Dersler" else "Tüm Dersler",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Tüm Dersler") },
                        onClick = {
                            onlyMain = false
                            menuOpen = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Ana Dersler") },
                        onClick = {
                            onlyMain = true
                            menuOpen = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (latest == null) {
            Text(
                text = "Deneme eklediğinde ders netlerin burada görünecek.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                rows.forEach { row ->
                    val pair = latest.resultsByCourse[row.id]
                    val net = if (pair == null) 0f else maxOf(0f, pair.first - pair.second / 3f)
                    CourseNetRow(info = row, net = net)
                }
            }
        }
    }
}

@Composable
private fun CourseNetRow(info: CourseRowInfo, net: Float) {
    val icon: ImageVector? = when (info.id) {
        "fen" -> Icons.Filled.Science
        "tur" -> Icons.Filled.MenuBook
        "ink" -> Icons.Filled.AccountBalance
        "din" -> Icons.Filled.Favorite
        "ing" -> Icons.Filled.Translate
        else -> null
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(info.color),
            contentAlignment = Alignment.Center
        ) {
            if (icon == null) {
                Text(
                    text = "π",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = info.name,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(78.dp),
            maxLines = 1
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(TrackGray)
        ) {
            val fraction = (net / 20f).coerceIn(0f, 1f)
            if (fraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(info.color)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = fmt2(net),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(48.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun TodayTasksCard(
    todos: List<TodoItem>,
    onToggle: (String) -> Unit,
    onNavigate: (Screen) -> Unit
) {
    val shown = todos.sortedBy { it.isCompleted }.take(3)

    ContentCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Bugünün Görevleri",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onNavigate(Screen.TodoList) }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tümünü Gör",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (shown.isEmpty()) {
            Text(
                text = "Bugün için görev yok.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = { onNavigate(Screen.TodoList) }) { Text("Görev ekle") }
        } else {
            shown.forEachIndexed { i, todo ->
                TaskRow(
                    todo = todo,
                    onToggle = { onToggle(todo.id) },
                    onOpen = {
                        val course = todo.targetCourse
                        val unit = todo.targetUnit
                        if (course != null && unit != null) {
                            onNavigate(Screen.UnitDetail(course, unit, todo.targetTab ?: "kart"))
                        } else {
                            onNavigate(Screen.TodoList)
                        }
                    }
                )
                if (i < shown.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskRow(
    todo: TodoItem,
    onToggle: () -> Unit,
    onOpen: () -> Unit
) {
    val course = LgsCurriculum.courses.firstOrNull { it.id == todo.targetCourse }
    val subtitle = when {
        course != null && !todo.targetUnit.isNullOrBlank() -> course.name + " · " + todo.targetUnit
        course != null -> course.name
        else -> "Görev"
    }
    val icon: ImageVector = when (todo.targetTab) {
        "video" -> Icons.Filled.PlayCircle
        "test" -> Icons.Filled.Quiz
        "kart" -> Icons.Filled.Style
        else -> Icons.Filled.Description
    }
    val primary = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (todo.isCompleted) primary else Color.Transparent)
                .border(
                    BorderStroke(1.5.dp, if (todo.isCompleted) primary else MaterialTheme.colorScheme.outline),
                    CircleShape
                )
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            if (todo.isCompleted) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onOpen),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = todo.text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LumiAskBar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(27.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Lumi'ye bir şey sor...",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AttachFile,
                    contentDescription = "Dosya ekle",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = "Sesle sor",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
