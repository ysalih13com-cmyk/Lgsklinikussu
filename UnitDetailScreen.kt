package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FlashCard
import com.example.data.LgsCurriculum
import com.example.data.LgsRepository
import com.example.data.QuizQuestion
import com.example.ui.components.FlipCard
import com.example.ui.navigation.Screen
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.delay

@Composable
fun UnitDetailScreen(
    courseId: String,
    unitName: String,
    initialTab: String = "kart",
    repository: LgsRepository,
    onNavigate: (Screen) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val course = LgsCurriculum.coursesMap[courseId] ?: return
    val unitProgressMap by repository.unitProgressMap.collectAsState()
    val unitKey = "$courseId|$unitName"
    val isCompleted = unitProgressMap[unitKey]?.completed == true

    var selectedTabIndex by remember {
        mutableIntStateOf(
            when (initialTab) {
                "video" -> 1
                "test" -> 2
                else -> 0
            }
        )
    }

    val tabTitles = listOf("🎴 Hafıza Kartları", "🎬 Video Ders", "📝 Test Çöz")

    // Filter cards for this unit (or fallback to course)
    val unitCards = remember(courseId, unitName) {
        val filtered = LgsCurriculum.flashcards.filter { it.courseId == courseId && it.unit == unitName }
        if (filtered.isNotEmpty()) filtered else LgsCurriculum.flashcards.filter { it.courseId == courseId }
    }

    // Filter questions for this unit (or fallback to course)
    val unitQuestions = remember(courseId, unitName) {
        val filtered = LgsCurriculum.quizQuestions.filter { it.courseId == courseId && it.unit == unitName }
        if (filtered.isNotEmpty()) filtered else LgsCurriculum.quizQuestions.filter { it.courseId == courseId }
    }

    // Video topics
    val videoTopics = remember(courseId, unitName) {
        LgsCurriculum.videoTopics["$courseId|$unitName"] ?: listOf(unitName)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${course.icon} ${course.name}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = unitName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { repository.toggleUnitCompletion(courseId, unitName) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCompleted) SuccessGreen else MaterialTheme.colorScheme.primary
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(
                    if (isCompleted) Icons.Default.CheckCircle else Icons.Outlined.CheckCircleOutline,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isCompleted) "Bitti" else "Bitirdim", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Content
        when (selectedTabIndex) {
            0 -> FlashcardsTabContent(unitCards, repository)
            1 -> VideoLessonsTabContent(
                courseId = courseId,
                unitName = unitName,
                topics = videoTopics,
                onNavigate = onNavigate
            )
            2 -> QuizTabContent(
                courseId = courseId,
                unitName = unitName,
                questions = unitQuestions,
                repository = repository
            )
        }
    }
}

@Composable
fun FlashcardsTabContent(
    cards: List<FlashCard>,
    repository: LgsRepository
) {
    if (cards.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Bu ünite için henüz kart eklenmedi.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Kartın arkasını görmek için üzerine dokun (Toplam: ${cards.size} Kart):",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        itemsIndexed(cards) { index, card ->
            Column {
                Text(
                    text = "Kart ${index + 1}/${cards.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                FlipCard(
                    frontText = card.front,
                    backText = card.back,
                    onFlip = { repository.recordCardFlip() }
                )
            }
        }
    }
}

@Composable
fun VideoLessonsTabContent(
    courseId: String,
    unitName: String,
    topics: List<String>,
    onNavigate: (Screen) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "👨‍🏫", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Prof. Dr. Yusuf Salih ile Birebir",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Akıllı tahta üzerinde adım adım ders anlatımı ve soru çözümleri.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Ders Konuları",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        itemsIndexed(topics) { index, topic ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onNavigate(Screen.VideoLesson(courseId, unitName, topic))
                    },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Oynat", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${index + 1}. $topic",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "İnteraktif Tahta & Sesli Anlatım",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = { onNavigate(Screen.VideoLesson(courseId, unitName, topic)) }
                    ) {
                        Text("İzle", fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Dijital Hoca Odası'nda Çalış",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Öğretmenle doğrudan mikrofonla konuşabilir veya tahtada çizerek soru sorabilirsin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            onNavigate(Screen.TeacherRoom(initialTopic = unitName, initialReason = "Ünite video sekmesinden açıldı"))
                        }
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hoca Odasına Geç")
                    }
                }
            }
        }
    }
}

@Composable
fun QuizTabContent(
    courseId: String,
    unitName: String,
    questions: List<QuizQuestion>,
    repository: LgsRepository
) {
    if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Bu ünite için soru hazırlanıyor.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var timerSeconds by remember { mutableIntStateOf(0) }
    var isAnswered by remember { mutableStateOf(false) }

    LaunchedEffect(currentIndex, isAnswered) {
        if (!isAnswered) {
            timerSeconds = 0
            while (!isAnswered) {
                delay(1000)
                timerSeconds++
            }
        }
    }

    val q = questions[currentIndex % questions.size]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        // Progress & Timer header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Soru ${currentIndex + 1} / ${questions.size}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "⏱️ ${timerSeconds}s",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Question card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = q.question,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Options
                val labels = listOf("A", "B", "C", "D")
                q.options.forEachIndexed { optIndex, optionText ->
                    val isCorrect = optIndex == q.correctIndex
                    val isChosen = selectedOption == optIndex

                    val containerColor = when {
                        !isAnswered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        isCorrect -> Color(0xFFDCFCE7) // green
                        isChosen -> Color(0xFFFEE2E2)  // red
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    }

                    val borderColor = when {
                        !isAnswered && isChosen -> MaterialTheme.colorScheme.primary
                        isAnswered && isCorrect -> SuccessGreen
                        isAnswered && isChosen -> ErrorRed
                        else -> Color.Transparent
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(containerColor)
                            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                            .clickable(enabled = !isAnswered) {
                                selectedOption = optIndex
                                isAnswered = true
                                val correct = optIndex == q.correctIndex
                                repository.recordAnswer(courseId, unitName, correct)
                                if (!correct) {
                                    repository.addMistake(
                                        courseId = courseId,
                                        unit = unitName,
                                        text = "${q.question} → Doğru: ${q.options[q.correctIndex]}. ${q.explanation}"
                                    )
                                }
                            }
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${labels.getOrElse(optIndex) { "?" }})",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = optionText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isChosen || (isAnswered && isCorrect)) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Explanation & Next Button
                AnimatedVisibility(visible = isAnswered) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        val wasCorrect = selectedOption == q.correctIndex
                        Text(
                            text = if (wasCorrect) "✓ Doğru!" else "✗ Yanlış!",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (wasCorrect) SuccessGreen else ErrorRed
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = q.explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (currentIndex < questions.size - 1) {
                                    currentIndex++
                                } else {
                                    currentIndex = 0 // loop
                                }
                                selectedOption = null
                                isAnswered = false
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(if (currentIndex < questions.size - 1) "Sonraki Soru" else "Yeniden Başla")
                        }
                    }
                }
            }
        }
    }
}
