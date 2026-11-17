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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Course
import com.example.data.LgsCurriculum
import com.example.data.LgsRepository
import com.example.ui.navigation.Screen
import com.example.ui.theme.PrimaryLight
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.delay
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    repository: LgsRepository,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val studentName by repository.studentName.collectAsState()
    val targetScore by repository.targetScore.collectAsState()
    val restSunday by repository.restSunday.collectAsState()
    val dailyRecords by repository.dailyRecords.collectAsState()
    val unitProgressMap by repository.unitProgressMap.collectAsState()
    val trialExams by repository.trialExams.collectAsState()

    var isEditingProfile by remember { mutableStateOf(false) }
    var tempName by remember(studentName) { mutableStateOf(studentName) }
    var tempScore by remember(targetScore) { mutableStateOf(targetScore.toString()) }

    // Countdown to LGS 2027 (e.g. 6 June 2027 09:30)
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    val examTimeMillis = remember {
        Calendar.getInstance().apply {
            set(2027, Calendar.JUNE, 6, 9, 30, 0)
        }.timeInMillis
    }
    val diffMillis = (examTimeMillis - currentTimeMillis).coerceAtLeast(0)
    val days = diffMillis / (1000 * 60 * 60 * 24)
    val hours = (diffMillis / (1000 * 60 * 60)) % 24
    val minutes = (diffMillis / (1000 * 60)) % 60
    val seconds = (diffMillis / 1000) % 60

    val streak = repository.calculateStreak()
    val (xp, level) = repository.calculateXpAndLevel()
    val xpInLevel = xp % 150
    val alarms = repository.getTopicAlarms()
    val chiefNote = repository.getChiefPhysicianNote()
    val latestExam = trialExams.lastOrNull()
    val completedTopicsCount = unitProgressMap.values.count { it.completed }
    val totalTopicsCount = LgsCurriculum.courses.sumOf { it.units.size }

    // Overall accuracy
    val totalQuestions = dailyRecords.values.sumOf { it.questionsSolved }
    val totalCorrect = dailyRecords.values.sumOf { it.questionsCorrect }
    val accuracyPercent = if (totalQuestions > 0) (totalCorrect * 100) / totalQuestions else 0

    // Topic Health count
    var healthyCount = 0
    var trackingCount = 0
    var criticalCount = 0
    unitProgressMap.values.forEach { prog ->
        if (prog.totalAnswers >= 3) {
            val ratio = prog.correctAnswers.toFloat() / prog.totalAnswers
            when {
                ratio >= 0.75f -> healthyCount++
                ratio >= 0.50f -> trackingCount++
                else -> criticalCount++
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // Countdown Ticker Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⏳ LGS 2027 KLİNİK GERİ SAYIM",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CountdownBlock(value = days.toString(), label = "Gün")
                        CountdownBlock(value = hours.toString().padStart(2, '0'), label = "Saat")
                        CountdownBlock(value = minutes.toString().padStart(2, '0'), label = "Dk")
                        CountdownBlock(value = seconds.toString().padStart(2, '0'), label = "Sn")
                    }
                }
            }
        }

        // Student Profile Header & Edit
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (studentName.isNotBlank()) "Öğrenci: $studentName" else "Öğrenci Dosyası",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Hedef LGS Puanı: $targetScore / 500",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(onClick = { isEditingProfile = !isEditingProfile }) {
                            Icon(Icons.Default.Edit, contentDescription = "Düzenle", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    AnimatedVisibility(visible = isEditingProfile) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            OutlinedTextField(
                                value = tempName,
                                onValueChange = { tempName = it },
                                label = { Text("Öğrenci Adı") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = tempScore,
                                onValueChange = { tempScore = it },
                                label = { Text("Hedef Puan (örn: 470)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { isEditingProfile = false }) {
                                    Text("İptal")
                                }
                                Button(onClick = {
                                    repository.setStudentName(tempName)
                                    val scoreInt = tempScore.toIntOrNull() ?: 450
                                    repository.setTargetScore(scoreInt.coerceIn(100, 500))
                                    isEditingProfile = false
                                }) {
                                    Text("Kaydet")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Chief Physician Note ("Başhekim Notu")
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.MedicalServices, contentDescription = "Başhekim", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Başhekim Prof. Dr. Yusuf Salih",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = chiefNote,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Topic Alarms (If any topic has >=3 wrong streak)
        if (alarms.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = "Alarm", tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "🚨 KLİNİK KONU ALARMI (${alarms.size} Konu)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        alarms.forEach { (courseId, unitName) ->
                            val course = LgsCurriculum.coursesMap[courseId]?.name ?: courseId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "$course · $unitName",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF991B1B)
                                    )
                                    Text(
                                        text = "3+ ardışık yanlış yapıldı, acil tedavi önerilir.",
                                        fontSize = 12.sp,
                                        color = Color(0xFF7F1D1D)
                                    )
                                }
                                Button(
                                    onClick = {
                                        onNavigate(Screen.TeacherRoom(initialTopic = unitName, initialReason = "Klinik konu alarmı"))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Hocayla Çalış", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Stats Grid
        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                maxItemsInEachRow = 2
            ) {
                // Streak Card
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Çalışma Serisi",
                    value = "$streak Gün",
                    subtitle = "Pazar dinlenme: ${if (restSunday) "Açık" else "Kapalı"}",
                    icon = "🔥",
                    highlightColor = Color(0xFFEA580C)
                )

                // Level / XP Card
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Seviye $level",
                    value = "$xp XP",
                    subtitle = "$xpInLevel/150 XP seviye atlama",
                    icon = "⭐",
                    highlightColor = MaterialTheme.colorScheme.primary,
                    progress = xpInLevel / 150f
                )

                // Soru Başarısı
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Soru Başarısı",
                    value = "%$accuracyPercent",
                    subtitle = "$totalCorrect doğru / $totalQuestions soru",
                    icon = "🎯",
                    highlightColor = SuccessGreen
                )

                // Son Deneme
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "Son Deneme Puanı",
                    value = latestExam?.score?.toString() ?: "—",
                    subtitle = if (latestExam != null) "${latestExam.totalNet} Net" else "Henüz deneme yok",
                    icon = "🩺",
                    highlightColor = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // Quick Clinical Action Cards
        item {
            Text(
                text = "Hızlı Klinik İşlemleri",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                maxItemsInEachRow = 2
            ) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Günlük Check-up",
                    desc = "5 soruluk hızlı tarama",
                    icon = "🧪",
                    onClick = { onNavigate(Screen.DailyCheckup) }
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Akıllı Tekrar (SRS)",
                    desc = "Leitner kutu nöbeti",
                    icon = "🗃️",
                    onClick = { onNavigate(Screen.SrsSpacedRepetition) }
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Öğrenme DNA'm",
                    desc = "5 eksenli analiz radarı",
                    icon = "🧬",
                    onClick = { onNavigate(Screen.LearningDna) }
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Hedef Ağacı",
                    desc = "Büyüme takibi",
                    icon = "🌳",
                    onClick = { onNavigate(Screen.GoalTree) }
                )
            }
        }

        // "Bugün Benim İçin Ne Seçtin?" Smart Recommendation Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💡 Bugün Benim İçin Ne Seçtin?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        TextButton(onClick = { onNavigate(Screen.AiCoach) }) {
                            Text("Koç Planı")
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Eksiklerine, yanlışlarına ve tekrar günlerine göre otomatik belirlendi:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val coachPlan = remember(dailyRecords, unitProgressMap) {
                        repository.generateCoachSchedule(60)
                    }

                    coachPlan.take(3).forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = item.text,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    when (item.targetTab) {
                                        "srs" -> onNavigate(Screen.SrsSpacedRepetition)
                                        "mistake" -> onNavigate(Screen.MistakeDetective)
                                        "trial" -> onNavigate(Screen.TrialExams)
                                        "bank" -> onNavigate(Screen.QuestionBank)
                                        "pomodoro" -> onNavigate(Screen.Pomodoro)
                                        "video" -> {
                                            if (item.targetCourse != null && item.targetUnit != null) {
                                                onNavigate(Screen.UnitDetail(item.targetCourse, item.targetUnit, "video"))
                                            } else {
                                                onNavigate(Screen.CurriculumMap)
                                            }
                                        }
                                        else -> onNavigate(Screen.TodoList)
                                    }
                                },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Başla", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Subjects & Progress Overview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Dersler ve Üniteler ($completedTopicsCount/$totalTopicsCount Bitti)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { onNavigate(Screen.CurriculumMap) }) {
                    Text("Harita")
                }
            }
        }

        items(LgsCurriculum.courses.size) { idx ->
            val course = LgsCurriculum.courses[idx]
            val completedCount = course.units.count { unit ->
                unitProgressMap["${course.id}|$unit"]?.completed == true
            }
            val progress = completedCount.toFloat() / course.units.size

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(Screen.CourseDetail(course.id)) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = course.icon, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = course.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "$completedCount/${course.units.size} Ünite",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
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

        // Daily Motivation Quote
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "💊", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Günün Motivasyonu",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = LgsCurriculum.motivationQuotes[0],
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CountdownBlock(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.85f)
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: String,
    highlightColor: Color,
    modifier: Modifier = Modifier,
    progress: Float? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(text = icon, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = highlightColor
            )
            if (progress != null) {
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = highlightColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    desc: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
