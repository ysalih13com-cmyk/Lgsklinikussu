package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CustomQuestionItem
import com.example.data.DailyRecord
import com.example.data.LgsCurriculum
import com.example.data.LgsRepository
import com.example.ui.components.FlipCard
import com.example.ui.components.GoalTreeGraphic
import com.example.ui.components.RadarChart
import com.example.ui.navigation.Screen
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun LearningDnaScreen(
    repository: LgsRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mistakes by repository.mistakes.collectAsState()
    val dailyRecords by repository.dailyRecords.collectAsState()
    val unitProgressMap by repository.unitProgressMap.collectAsState()

    val totalQuestions = dailyRecords.values.sumOf { it.questionsSolved }
    val totalCorrect = dailyRecords.values.sumOf { it.questionsCorrect }
    val generalAccuracy = if (totalQuestions > 0) (totalCorrect * 100) / totalQuestions else 50

    // Mistakes analytics
    val totalMistakes = mistakes.size
    val dikkatMistakes = mistakes.count { it.reasonIndex == 1 || it.reasonIndex == 3 }
    val islemMistakes = mistakes.count { it.reasonIndex == 2 }
    val uzunMistakes = mistakes.count { it.reasonIndex == 5 }
    val zamanMistakes = mistakes.count { it.reasonIndex == 4 }

    val dikkatScore = if (totalMistakes >= 3) (100 - (dikkatMistakes * 100f / totalMistakes)).toInt().coerceIn(20, 100) else 80
    val islemScore = if (totalMistakes >= 3) (100 - (islemMistakes * 100f / totalMistakes)).toInt().coerceIn(20, 100) else 85
    val uzunSoruScore = if (totalMistakes >= 3) (100 - (uzunMistakes * 100f / totalMistakes)).toInt().coerceIn(20, 100) else 75
    val zamanScore = if (totalMistakes >= 3) (100 - (zamanMistakes * 100f / totalMistakes)).toInt().coerceIn(20, 100) else 70

    val radarValues = listOf(
        "Konu Hakimiyeti" to generalAccuracy,
        "Dikkat" to dikkatScore,
        "İşlem" to islemScore,
        "Uzun Soru" to uzunSoruScore,
        "Zaman Yönetimi" to zamanScore
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                }
                Text("🧬 Öğrenme DNA'm", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("5 Eksenli Yetkinlik Radarı", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    RadarChart(values = radarValues)
                }
            }
        }

        item {
            Text("Ders Güç ve Gelişim Alanları", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(LgsCurriculum.courses) { course ->
            var q = 0
            var c = 0
            course.units.forEach { u ->
                val p = unitProgressMap["${course.id}|$u"]
                if (p != null) {
                    q += p.totalAnswers
                    c += p.correctAnswers
                }
            }
            val acc = if (q > 0) (c * 100) / q else 0

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${course.icon} ${course.name}", fontWeight = FontWeight.Bold)
                        Text("%$acc", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (acc / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun GoalTreeScreen(
    repository: LgsRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val targetScore by repository.targetScore.collectAsState()
    val dailyQGoal by repository.dailyQuestionGoal.collectAsState()
    val dailyMinGoal by repository.dailyMinutesGoal.collectAsState()
    val weeklyTrialGoal by repository.weeklyTrialGoal.collectAsState()
    val unitProgressMap by repository.unitProgressMap.collectAsState()
    val trialExams by repository.trialExams.collectAsState()
    val dailyRecords by repository.dailyRecords.collectAsState()

    val todayRecord = dailyRecords[repository.getTodayString()] ?: DailyRecord(repository.getTodayString())
    val completedUnits = unitProgressMap.values.count { it.completed }
    val latestExam = trialExams.lastOrNull()

    // Overall completion toward targets
    val scoreProgress = ((latestExam?.score ?: 300f) - 100f) / (targetScore - 100f).coerceAtLeast(1f)
    val qProgress = (todayRecord.questionsSolved.toFloat() / dailyQGoal).coerceAtMost(1f)
    val minProgress = (todayRecord.studyMinutes.toFloat() / dailyMinGoal).coerceAtMost(1f)
    val overallTreeProgress = ((scoreProgress.coerceIn(0f, 1f) + qProgress + minProgress + (completedUnits.toFloat() / 38)) / 4f).coerceIn(0.05f, 1f)

    var showEditGoalsDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                    Text("🌳 Hedef Ağacı", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Button(onClick = { showEditGoalsDialog = true }) {
                    Text("Hedefleri Düzenle")
                }
            }
        }

        item {
            GoalTreeGraphic(progress = overallTreeProgress)
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Mevcut Hedef Durumun", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    GoalItemRow(
                        title = "🎯 Hedef LGS Puanı",
                        current = "${latestExam?.score ?: "—"}",
                        target = "$targetScore Puan"
                    )
                    GoalItemRow(
                        title = "📝 Günlük Soru Hedefi",
                        current = "${todayRecord.questionsSolved}",
                        target = "$dailyQGoal Soru"
                    )
                    GoalItemRow(
                        title = "⏱️ Günlük Çalışma Süresi",
                        current = "${todayRecord.studyMinutes} dk",
                        target = "$dailyMinGoal dk"
                    )
                    GoalItemRow(
                        title = "🩺 Haftalık Deneme",
                        current = "${trialExams.size}",
                        target = "$weeklyTrialGoal Deneme/hafta"
                    )
                }
            }
        }
    }

    if (showEditGoalsDialog) {
        var tempScore by remember { mutableStateOf(targetScore.toString()) }
        var tempQ by remember { mutableStateOf(dailyQGoal.toString()) }
        var tempMin by remember { mutableStateOf(dailyMinGoal.toString()) }

        AlertDialog(
            onDismissRequest = { showEditGoalsDialog = false },
            title = { Text("Hedefleri Güncelle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tempScore,
                        onValueChange = { tempScore = it },
                        label = { Text("Hedef LGS Puanı (100-500)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempQ,
                        onValueChange = { tempQ = it },
                        label = { Text("Günlük Soru Hedefi") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempMin,
                        onValueChange = { tempMin = it },
                        label = { Text("Günlük Süre Hedefi (dk)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val s = tempScore.toIntOrNull() ?: 450
                    val q = tempQ.toIntOrNull() ?: 30
                    val m = tempMin.toIntOrNull() ?: 60
                    repository.setGoals(q, m, weeklyTrialGoal, repository.subjectNetGoals.value)
                    repository.setTargetScore(s)
                    showEditGoalsDialog = false
                }) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditGoalsDialog = false }) { Text("İptal") }
            }
        )
    }
}

@Composable
fun GoalItemRow(title: String, current: String, target: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium)
        Text("$current / $target", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun TopicHealthScreen(
    repository: LgsRepository,
    onNavigate: (Screen) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unitProgressMap by repository.unitProgressMap.collectAsState()

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
                Text("🩻 Konu Sağlık Durumu", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }

        item {
            Text(
                text = "🟢 %75+ Sağlıklı · 🟡 %50-74 Takipte · 🔴 %50 altı Acil Müdahale · ⚪ <3 soru",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LgsCurriculum.courses.forEach { course ->
            item {
                Text("${course.icon} ${course.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            items(course.units) { unitName ->
                val key = "${course.id}|$unitName"
                val p = unitProgressMap[key]
                val acc = if (p != null && p.totalAnswers >= 3) (p.correctAnswers * 100) / p.totalAnswers else null

                val (dotColor, statusText) = when {
                    acc == null -> Color.Gray to "Veri yetersiz"
                    acc >= 75 -> SuccessGreen to "Sağlıklı"
                    acc >= 50 -> Color(0xFFF59E0B) to "Takipte"
                    else -> ErrorRed to "Acil Müdahale"
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(dotColor))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(unitName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("$statusText ${acc?.let { "· %$it" } ?: ""}", fontSize = 11.sp, color = dotColor)
                            }
                        }
                        Button(
                            onClick = { onNavigate(Screen.UnitDetail(course.id, unitName, "test")) },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Test", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SrsScreen(
    repository: LgsRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dueIndices = remember { repository.getDueSrsCardIndices() }
    var currentDueIndex by remember { mutableIntStateOf(0) }
    val srsCards by repository.srsCards.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
            }
            Text("🗃️ Akıllı Tekrar (Leitner)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Bildiklerin seyrekleşir (1→3→7→14→30 gün), bilemediklerin hemen tekrar sorulur.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (currentDueIndex >= dueIndices.size) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🎉 Tebrikler!", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Bugünlük nöbet listesindeki tüm kartları tekrar ettin.", fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(onClick = onBack) {
                        Text("Paneli Görüntüle")
                    }
                }
            }
        } else {
            val cardIdx = dueIndices[currentDueIndex]
            val card = LgsCurriculum.flashcards[cardIdx]
            val state = srsCards[cardIdx]
            val currentBox = (state?.box ?: 0) + 1

            Text(
                text = "Kalan: ${dueIndices.size - currentDueIndex} Kart · Kutu $currentBox/5 (${LgsCurriculum.coursesMap[card.courseId]?.name} · ${card.unit})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            FlipCard(
                frontText = card.front,
                backText = card.back,
                onFlip = { repository.recordCardFlip() }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Nasıl bildin?", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        repository.answerSrsCard(cardIdx, 0)
                        currentDueIndex++
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Bilemedim")
                }
                FilledTonalButton(
                    onClick = {
                        repository.answerSrsCard(cardIdx, 1)
                        currentDueIndex++
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Zorlandım")
                }
                Button(
                    onClick = {
                        repository.answerSrsCard(cardIdx, 2)
                        currentDueIndex++
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Bildim ✓")
                }
            }
        }
    }
}

@Composable
fun MyQuestionsScreen(
    repository: LgsRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val questions by repository.customQuestions.collectAsState()
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
                Text("📸 Kendi Sorularım", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Soru Ekle", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Denemelerde veya kitaplarda takıldığın soruları fotoğrafla veya yazarak sakla:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (questions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Henüz kendi sorun bulunmuyor. Sağ üstteki + ile ilk sorunu ekle!", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(questions) { item ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isSolved) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${LgsCurriculum.coursesMap[item.courseId]?.name ?: item.courseId} · ${item.unit}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp
                                )
                                Text(item.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            if (!item.imageBase64.isNullOrBlank()) {
                                val bitmap = remember(item.imageBase64) {
                                    try {
                                        val bytes = Base64.decode(item.imageBase64, Base64.DEFAULT)
                                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    } catch (_: Exception) { null }
                                }
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Soru Fotoğrafı",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }

                            if (item.questionText.isNotBlank()) {
                                Text(item.questionText, style = MaterialTheme.typography.bodyMedium)
                            }
                            if (item.answerNote.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("💡 Çözüm: ${item.answerNote}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { repository.toggleCustomQuestionSolved(item.id) }) {
                                    Text(if (item.isSolved) "Geri Al" else "✓ Çözüldü")
                                }
                                IconButton(onClick = { repository.deleteCustomQuestion(item.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var courseId by remember { mutableStateOf(LgsCurriculum.courses.first().id) }
        var unitName by remember { mutableStateOf(LgsCurriculum.courses.first().units.first()) }
        var qText by remember { mutableStateOf("") }
        var ansText by remember { mutableStateOf("") }
        var base64Img by remember { mutableStateOf<String?>(null) }

        val imagePickerLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                // Read & encode
                try {
                    // For quick preview
                    base64Img = ""
                } catch (_: Exception) {}
            }
        }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Kendi Sorunu Ekle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = qText,
                        onValueChange = { qText = it },
                        label = { Text("Soru metni veya açıklaması") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = ansText,
                        onValueChange = { ansText = it },
                        label = { Text("Doğru cevap veya çözüm notu") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (qText.isNotBlank()) {
                        repository.addCustomQuestion(courseId, unitName, qText, ansText, base64Img)
                    }
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
fun CalendarHistoryScreen(
    repository: LgsRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dailyRecords by repository.dailyRecords.collectAsState()
    val studyHistory by repository.studyHistory.collectAsState()

    var monthOffset by remember { mutableIntStateOf(0) }
    var selectedDateStr by remember { mutableStateOf(repository.getTodayString()) }

    var showAddLogDialog by remember { mutableStateOf(false) }

    val cal = remember(monthOffset) {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, monthOffset)
        }
    }
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH)
    val monthName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.forLanguageTag("tr-TR")) ?: ""
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday = 0

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
                Text("🗓️ Çalışma Takvimi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = { showAddLogDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Kayıt Ekle", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Month Header
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { monthOffset-- }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Önceki")
                    }
                    Text("$monthName $year", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { monthOffset++ }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Sonraki")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Day headers (Pt, Sa, Ça, Pe, Cu, Ct, Pz)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    listOf("Pt", "Sa", "Ça", "Pe", "Cu", "Ct", "Pz").forEach {
                        Text(it, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar days grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(210.dp)
                ) {
                    // Blank leading days
                    items(firstDayOfWeek) {
                        Spacer(modifier = Modifier.size(28.dp))
                    }
                    // Actual days
                    items(daysInMonth) { dIdx ->
                        val dayNum = dIdx + 1
                        val dateKey = "%04d-%02d-%02d".format(Locale.US, year, month + 1, dayNum)
                        val rec = dailyRecords[dateKey]
                        val activityVal = (rec?.studyMinutes ?: 0) + (rec?.questionsSolved ?: 0) * 2 + (rec?.cardsFlipped ?: 0)
                        val heatColor = when {
                            activityVal >= 90 -> Color(0xFF0284C7)
                            activityVal >= 45 -> Color(0xFF38BDF8)
                            activityVal >= 15 -> Color(0xFFBAE6FD)
                            activityVal > 0 -> Color(0xFFE0F2FE)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        }
                        val isSelected = dateKey == selectedDateStr

                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(heatColor)
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { selectedDateStr = dateKey },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayNum.toString(),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected || activityVal > 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (activityVal >= 45) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Selected day details
        val selectedDayRecord = dailyRecords[selectedDateStr] ?: DailyRecord(selectedDateStr)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Seçilen Gün: $selectedDateStr",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⏱️ ${selectedDayRecord.studyMinutes} dk · 📝 ${selectedDayRecord.questionsSolved} soru (${selectedDayRecord.questionsCorrect} doğru) · 🎴 ${selectedDayRecord.cardsFlipped} kart · 🍅 ${selectedDayRecord.pomodoros} pomodoro",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    if (showAddLogDialog) {
        var minText by remember { mutableStateOf("30") }
        var noteText by remember { mutableStateOf("") }
        var selectedCourseId by remember { mutableStateOf(LgsCurriculum.courses.first().id) }

        AlertDialog(
            onDismissRequest = { showAddLogDialog = false },
            title = { Text("Çalışma Kaydı Ekle") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minText,
                        onValueChange = { minText = it },
                        label = { Text("Çalışılan Süre (dakika)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Not (örn: Üslü sayılar 2 test)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val m = minText.toIntOrNull() ?: 30
                    repository.recordStudyMinutes(selectedCourseId, m, noteText)
                    showAddLogDialog = false
                }) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddLogDialog = false }) { Text("İptal") }
            }
        )
    }
}
