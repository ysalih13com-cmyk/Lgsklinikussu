package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import java.util.Locale
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyCheckupRecord
import com.example.data.LgsCurriculum
import com.example.data.LgsRepository
import com.example.data.MiniQuizItem
import com.example.data.QuizQuestion
import com.example.data.TrialExamItem
import com.example.ui.navigation.Screen
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import kotlin.math.max
import kotlin.math.roundToInt

val mistakeReasons = listOf(
    "Konuyu bilmiyordum",
    "Dikkat hatası",
    "İşlem hatası",
    "Soruyu yanlış okudum",
    "Süre yetmedi",
    "Sorunun mantığını anlayamadım"
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MistakeDetectiveScreen(
    repository: LgsRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mistakes by repository.mistakes.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedReasonFilter by remember { mutableStateOf<Int?>(null) }

    val filteredMistakes = remember(mistakes, selectedReasonFilter) {
        if (selectedReasonFilter == null) mistakes else mistakes.filter { it.reasonIndex == selectedReasonFilter }
    }

    // Analytics: count reasons
    val reasonCounts = remember(mistakes) {
        val counts = IntArray(6) { 0 }
        mistakes.forEach { m ->
            m.reasonIndex?.let { if (it in 0..5) counts[it]++ }
        }
        counts
    }
    val totalWithReason = reasonCounts.sum()
    val maxReasonIdx = if (totalWithReason > 0) reasonCounts.indices.maxByOrNull { reasonCounts[it] } ?: 0 else 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                }
                Text("🕵️ Hata Dedektifi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Yanlış Ekle", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Analytics card
        if (totalWithReason > 0) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "📊 Hata Sebebi Analizi (En sık sebep: ${mistakeReasons[maxReasonIdx]})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    mistakeReasons.forEachIndexed { idx, reason ->
                        val count = reasonCounts[idx]
                        if (count > 0) {
                            val pct = (count * 100) / totalWithReason
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(reason, fontSize = 12.sp)
                                Text("%$pct ($count)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Filter chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = selectedReasonFilter == null,
                onClick = { selectedReasonFilter = null },
                label = { Text("Tümü (${mistakes.size})") }
            )
            mistakeReasons.forEachIndexed { i, reason ->
                FilterChip(
                    selected = selectedReasonFilter == i,
                    onClick = { selectedReasonFilter = if (selectedReasonFilter == i) null else i },
                    label = { Text(reason) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredMistakes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Tebrikler! Çözülecek kayıtlı yanlış bulunmuyor.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredMistakes) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isFixed) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val course = LgsCurriculum.coursesMap[item.courseId]
                                    Text(
                                        text = "${course?.icon ?: ""} ${course?.name ?: item.courseId} · ${item.unit}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (item.isFixed) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("✓ Çözüldü", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(item.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.text,
                                style = MaterialTheme.typography.bodyMedium,
                                textDecoration = if (item.isFixed) TextDecoration.LineThrough else TextDecoration.None
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Reason tag selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                var expanded by remember { mutableStateOf(false) }
                                Box {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.clickable { expanded = true }
                                    ) {
                                        Text(
                                            text = item.reasonIndex?.let { mistakeReasons.getOrNull(it) } ?: "Sebep Ata ▾",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    androidx.compose.material3.DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        mistakeReasons.forEachIndexed { rIdx, rText ->
                                            DropdownMenuItem(
                                                text = { Text(rText, fontSize = 12.sp) },
                                                onClick = {
                                                    repository.setMistakeReason(item.id, rIdx)
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Row {
                                    TextButton(onClick = { repository.toggleMistakeFixed(item.id) }) {
                                        Text(if (item.isFixed) "Geri Al" else "Çözüldü İşaretle", fontSize = 12.sp)
                                    }
                                    IconButton(onClick = { repository.deleteMistake(item.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Sil", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var inputCourseId by remember { mutableStateOf(LgsCurriculum.courses.first().id) }
        var inputUnit by remember { mutableStateOf(LgsCurriculum.courses.first().units.first()) }
        var inputText by remember { mutableStateOf("") }
        var inputReasonIndex by remember { mutableStateOf<Int?>(null) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Yanlış Defterine Soru Ekle") },
            text = {
                Column {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        label = { Text("Soru metni veya çözüm notu") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (inputText.isNotBlank()) {
                        repository.addMistake(inputCourseId, inputUnit, inputText, inputReasonIndex)
                    }
                    showAddDialog = false
                }) {
                    Text("Ekle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("İptal") }
            }
        )
    }
}

@Composable
fun TodoListScreen(
    repository: LgsRepository,
    onNavigate: (Screen) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val todos by repository.todos.collectAsState()
    var newTodoText by remember { mutableStateOf("") }

    val completedCount = todos.count { it.isCompleted }
    val progress = if (todos.isNotEmpty()) completedCount.toFloat() / todos.size else 0f

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
            }
            Text("✓ Bugün Ne Çalışmalıyım?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Günlük görevlerin ve çalışma planın:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Progress Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Günlük İlerleme", fontWeight = FontWeight.Bold)
                    Text("$completedCount / ${todos.size} Tamamlandı", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Add task input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newTodoText,
                onValueChange = { newTodoText = it },
                placeholder = { Text("Yeni görev ekle (örn: 30 Matematik sorusu)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Button(
                onClick = {
                    if (newTodoText.isNotBlank()) {
                        repository.addTodo(newTodoText.trim())
                        newTodoText = ""
                    }
                }
            ) {
                Text("Ekle")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = {
                listOf(
                    "30 Matematik sorusu çöz",
                    "1 konu tekrarı yap",
                    "Yanlış defterini gözden geçir",
                    "20 dk paragraf oku"
                ).forEach { repository.addTodo(it) }
            }) {
                Text("💡 Öneri Görevler Ekle")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(todos) { item ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (item.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.isCompleted,
                            onCheckedChange = { repository.toggleTodo(item.id) }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.text,
                            style = MaterialTheme.typography.bodyMedium,
                            textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { repository.deleteTodo(item.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Sil", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreCalculatorScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Subjects count: Tur(20), Mat(20), Fen(20), Ink(10), Din(10), Ing(10)
    var turD by remember { mutableStateOf("18") }
    var turY by remember { mutableStateOf("2") }
    var matD by remember { mutableStateOf("15") }
    var matY by remember { mutableStateOf("3") }
    var fenD by remember { mutableStateOf("17") }
    var fenY by remember { mutableStateOf("2") }
    var inkD by remember { mutableStateOf("9") }
    var inkY by remember { mutableStateOf("1") }
    var dinD by remember { mutableStateOf("9") }
    var dinY by remember { mutableStateOf("1") }
    var ingD by remember { mutableStateOf("9") }
    var ingY by remember { mutableStateOf("1") }

    fun calcNet(dStr: String, yStr: String): Float {
        val d = dStr.toIntOrNull() ?: 0
        val y = yStr.toIntOrNull() ?: 0
        return max(0f, d - (y / 3f))
    }

    val netTur = calcNet(turD, turY)
    val netMat = calcNet(matD, matY)
    val netFen = calcNet(fenD, fenY)
    val netInk = calcNet(inkD, inkY)
    val netDin = calcNet(dinD, dinY)
    val netIng = calcNet(ingD, ingY)

    val totalNet = netTur + netMat + netFen + netInk + netDin + netIng
    val rawScore = (netTur * 4.348f) + (netMat * 4.348f) + (netFen * 4.348f) +
            (netInk * 1.666f) + (netDin * 1.666f) + (netIng * 1.666f)
    val maxRaw = 4.348f * 60 + 1.666f * 30
    val lgsScore = 100f + (rawScore * 400f / maxRaw)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                }
                Text("🧮 LGS Puan Hesaplama", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("TAHMİNİ LGS PUANI", color = Color.White.copy(alpha = 0.85f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "%.2f".format(Locale.US, lgsScore),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Toplam Net: ${"%.2f".format(Locale.US, totalNet)} / 90",
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        item {
            Text("Ders Doğru ve Yanlış Sayıları (3 Yanlış 1 Doğruyu Götürür):", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ScoreSubjectRow("📖 Türkçe (20)", turD, { turD = it }, turY, { turY = it }, netTur)
                    ScoreSubjectRow("📐 Matematik (20)", matD, { matD = it }, matY, { matY = it }, netMat)
                    ScoreSubjectRow("🧪 Fen Bilimleri (20)", fenD, { fenD = it }, fenY, { fenY = it }, netFen)
                    ScoreSubjectRow("🏛️ T.C. İnkılap (10)", inkD, { inkD = it }, inkY, { inkY = it }, netInk)
                    ScoreSubjectRow("🌙 Din Kültürü (10)", dinD, { dinD = it }, dinY, { dinY = it }, netDin)
                    ScoreSubjectRow("🌍 İngilizce (10)", ingD, { ingD = it }, ingY, { ingY = it }, netIng)
                }
            }
        }
    }
}

@Composable
fun ScoreSubjectRow(
    title: String,
    dValue: String,
    onDChange: (String) -> Unit,
    yValue: String,
    onYChange: (String) -> Unit,
    net: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
        Row(
            modifier = Modifier.weight(2f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = dValue,
                onValueChange = onDChange,
                singleLine = true,
                label = { Text("D", fontSize = 10.sp) },
                modifier = Modifier.width(60.dp)
            )
            OutlinedTextField(
                value = yValue,
                onValueChange = onYChange,
                singleLine = true,
                label = { Text("Y", fontSize = 10.sp) },
                modifier = Modifier.width(60.dp)
            )
            Text(
                text = "${"%.2f".format(Locale.US, net)} Net",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun TrialExamsScreen(
    repository: LgsRepository,
    onNavigate: (Screen) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val trialExams by repository.trialExams.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                }
                Text("🩺 Deneme Hasta Dosyası", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Deneme Ekle", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Her deneme sonrası klinik teşhis ve otomatik reçete çıkar:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (trialExams.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Henüz deneme kaydı yok. Sağ üstteki + butonuna basarak ilk denemeni ekle!", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(trialExams.reversed()) { revIdx, exam ->
                    val origIndex = trialExams.size - 1 - revIdx
                    val prevExam = trialExams.getOrNull(origIndex - 1)
                    val scoreDiff = if (prevExam != null) exam.score - prevExam.score else null

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(exam.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text(exam.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${exam.score} Puan",
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text("${exam.totalNet} Net", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    if (scoreDiff != null) {
                                        Text(
                                            text = "${if (scoreDiff >= 0) "+" else ""}${"%.1f".format(Locale.US, scoreDiff)}",
                                            color = if (scoreDiff >= 0) SuccessGreen else ErrorRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            if (exam.doctorNote.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("🩺 Teşhis: ${exam.doctorNote}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                IconButton(onClick = { repository.deleteTrialExam(exam.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Sil", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var examName by remember { mutableStateOf("") }
        var doctorNote by remember { mutableStateOf("") }
        var turD by remember { mutableStateOf("17") }
        var turY by remember { mutableStateOf("3") }
        var matD by remember { mutableStateOf("14") }
        var matY by remember { mutableStateOf("4") }
        var fenD by remember { mutableStateOf("16") }
        var fenY by remember { mutableStateOf("3") }
        var inkD by remember { mutableStateOf("8") }
        var inkY by remember { mutableStateOf("2") }
        var dinD by remember { mutableStateOf("9") }
        var dinY by remember { mutableStateOf("1") }
        var ingD by remember { mutableStateOf("8") }
        var ingY by remember { mutableStateOf("2") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Yeni Deneme Sınavı Ekle") },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    item {
                        OutlinedTextField(
                            value = examName,
                            onValueChange = { examName = it },
                            label = { Text("Deneme Adı (örn: Özdebir 1)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        ScoreSubjectRow("Türkçe", turD, { turD = it }, turY, { turY = it }, 0f)
                        ScoreSubjectRow("Matematik", matD, { matD = it }, matY, { matY = it }, 0f)
                        ScoreSubjectRow("Fen", fenD, { fenD = it }, fenY, { fenY = it }, 0f)
                        ScoreSubjectRow("İnkılap", inkD, { inkD = it }, inkY, { inkY = it }, 0f)
                        ScoreSubjectRow("Din", dinD, { dinD = it }, dinY, { dinY = it }, 0f)
                        ScoreSubjectRow("İngilizce", ingD, { ingD = it }, ingY, { ingY = it }, 0f)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = doctorNote,
                            onValueChange = { doctorNote = it },
                            label = { Text("Doktor Notu (Nerede hata yaptın?)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val map = mapOf(
                        "tur" to Pair(turD.toIntOrNull() ?: 0, turY.toIntOrNull() ?: 0),
                        "mat" to Pair(matD.toIntOrNull() ?: 0, matY.toIntOrNull() ?: 0),
                        "fen" to Pair(fenD.toIntOrNull() ?: 0, fenY.toIntOrNull() ?: 0),
                        "ink" to Pair(inkD.toIntOrNull() ?: 0, inkY.toIntOrNull() ?: 0),
                        "din" to Pair(dinD.toIntOrNull() ?: 0, dinY.toIntOrNull() ?: 0),
                        "ing" to Pair(ingD.toIntOrNull() ?: 0, ingY.toIntOrNull() ?: 0)
                    )
                    repository.addTrialExam(examName, "", map, doctorNote)
                    showAddDialog = false
                }) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("İptal") }
            }
        )
    }
}

@Composable
fun DailyCheckupScreen(
    repository: LgsRepository,
    onNavigate: (Screen) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 5 questions across 5 subjects
    val checkupQuestions = remember {
        val list = mutableListOf<QuizQuestion>()
        LgsCurriculum.courses.take(5).forEach { c ->
            val qList = LgsCurriculum.quizQuestions.filter { it.courseId == c.id }
            if (qList.isNotEmpty()) list.add(qList.random())
        }
        list
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var correctCount by remember { mutableIntStateOf(0) }
    var isAnswered by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }
    val startTime = remember { System.currentTimeMillis() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
            }
            Text("🧪 Günlük Mini Check-up", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "5 ders, 5 soru ile anlık klinik durum tespiti:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (isFinished) {
            val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            val status = when {
                correctCount >= 4 -> "STABİL"
                correctCount == 3 -> "DİKKAT"
                else -> "KRİTİK"
            }
            val statusColor = when (status) {
                "STABİL" -> SuccessGreen
                "DİKKAT" -> Color(0xFFF59E0B)
                else -> ErrorRed
            }

            LaunchedEffect(Unit) {
                repository.recordDailyCheckup(
                    DailyCheckupRecord(
                        date = repository.getTodayString(),
                        correctCount = correctCount,
                        totalCount = 5,
                        weakCourse = "Matematik",
                        hardTopic = "EBOB-EKOK",
                        statusText = status,
                        durationSec = elapsedSec
                    )
                )
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("KLİNİK MUAYENE RAPORU", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(status, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = statusColor)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Sonuç: $correctCount / 5 Doğru ($elapsedSec saniye)", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(onClick = onBack) {
                        Text("Klinik Üssüne Dön")
                    }
                }
            }
        } else if (checkupQuestions.isNotEmpty()) {
            val q = checkupQuestions[currentIndex]

            Text("Soru ${currentIndex + 1} / 5: ${LgsCurriculum.coursesMap[q.courseId]?.name}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(q.question, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(14.dp))

                    val labels = listOf("A", "B", "C", "D")
                    q.options.forEachIndexed { optIdx, optText ->
                        val isChosen = selectedOption == optIdx
                        val isCorrect = optIdx == q.correctIndex

                        val containerColor = when {
                            !isAnswered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            isCorrect -> Color(0xFFDCFCE7)
                            isChosen -> Color(0xFFFEE2E2)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(containerColor)
                                .clickable(enabled = !isAnswered) {
                                    selectedOption = optIdx
                                    isAnswered = true
                                    if (optIdx == q.correctIndex) {
                                        correctCount++
                                    } else {
                                        repository.addMistake(q.courseId, q.unit, "${q.question} (Check-up)")
                                    }
                                }
                                .padding(12.dp)
                        ) {
                            Text("${labels.getOrElse(optIdx) { "?" }}) $optText", fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal)
                        }
                    }

                    if (isAnswered) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (currentIndex < checkupQuestions.size - 1) {
                                    currentIndex++
                                    selectedOption = null
                                    isAnswered = false
                                } else {
                                    isFinished = true
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(if (currentIndex < checkupQuestions.size - 1) "Sonraki Soru" else "Raporu Gör")
                        }
                    }
                }
            }
        }
    }
}
