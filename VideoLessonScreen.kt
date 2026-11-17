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
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SpeechManager
import com.example.data.BoardStep
import com.example.data.LgsCurriculum
import com.example.data.LgsRepository
import com.example.data.MiniQuizItem
import com.example.ui.components.AvatarState
import com.example.ui.components.TeacherAvatar
import com.example.ui.navigation.Screen
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.delay

@Composable
fun VideoLessonScreen(
    courseId: String,
    unitName: String,
    topicName: String,
    speechManager: SpeechManager,
    repository: LgsRepository,
    onNavigate: (Screen) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lesson = remember(courseId, unitName, topicName) {
        LgsCurriculum.getVideoLesson(courseId, unitName, topicName)
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("🎬 Anlatım", "📝 Notlar", "🔍 Mini Test")

    // Playback state
    var currentSectionIndex by remember { mutableIntStateOf(0) }
    var currentScriptIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var playRate by remember { mutableFloatStateOf(1.1f) }

    val currentSection = lesson.sections.getOrElse(currentSectionIndex) { lesson.sections.first() }
    val cumulativeBoard = remember { mutableStateListOf<BoardStep>() }

    // TTS sync and section advancement
    LaunchedEffect(currentSectionIndex) {
        cumulativeBoard.clear()
        currentScriptIndex = 0
    }

    LaunchedEffect(isPlaying, currentSectionIndex, currentScriptIndex) {
        if (isPlaying && currentSectionIndex < lesson.sections.size) {
            val scripts = currentSection.script
            if (currentScriptIndex < scripts.size) {
                // Add board step for this script index if any
                val steps = currentSection.board.getOrNull(currentScriptIndex) ?: emptyList()
                steps.forEach { if (!cumulativeBoard.contains(it)) cumulativeBoard.add(it) }

                val textToSpeak = scripts[currentScriptIndex]
                if (!isMuted) {
                    speechManager.speak(textToSpeak, rate = playRate) {
                        // When speech finishes:
                        if (currentScriptIndex < scripts.size - 1) {
                            currentScriptIndex++
                        } else {
                            if (currentSectionIndex < lesson.sections.size - 1) {
                                currentSectionIndex++
                            } else {
                                isPlaying = false
                            }
                        }
                    }
                } else {
                    // Muted mode timing based on word length
                    val delayMs = maxOf(2500L, textToSpeak.length * 50L)
                    delay(delayMs)
                    if (currentScriptIndex < scripts.size - 1) {
                        currentScriptIndex++
                    } else {
                        if (currentSectionIndex < lesson.sections.size - 1) {
                            currentSectionIndex++
                        } else {
                            isPlaying = false
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechManager.stop()
        }
    }

    val totalSections = lesson.sections.size
    val progress = (currentSectionIndex.toFloat() + (currentScriptIndex.toFloat() / maxOf(1, currentSection.script.size))) / totalSections

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                speechManager.stop()
                onBack()
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lesson.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$unitName · Bölüm ${currentSectionIndex + 1}/$totalSections: ${currentSection.title}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabTitles.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedTabIndex == idx,
                    onClick = { selectedTabIndex = idx },
                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedTabIndex) {
            0 -> {
                // Lecture Player Tab
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        // Teacher avatar with speech status
                        TeacherAvatar(
                            state = if (isPlaying) AvatarState.SPEAKING else AvatarState.IDLE,
                            title = "Prof. Dr. Yusuf Salih"
                        )
                    }

                    item {
                        // Virtual Blackboard Display
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(cumulativeBoard) { _, step ->
                                    when (step) {
                                        is BoardStep.Header -> {
                                            Text(
                                                text = step.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        is BoardStep.Paragraph -> {
                                            Text(
                                                text = step.text,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        is BoardStep.Equation -> {
                                            Surface(
                                                color = MaterialTheme.colorScheme.surface,
                                                shape = RoundedCornerShape(8.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                            ) {
                                                Text(
                                                    text = step.formula,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                        is BoardStep.PairEq -> {
                                            Surface(
                                                color = MaterialTheme.colorScheme.surface,
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = "${step.a} × ${step.b} = ${step.a * step.b}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                        is BoardStep.ItemList -> {
                                            Column {
                                                step.items.forEach { itm ->
                                                    Text(
                                                        text = "• $itm",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        // Current script caption
                        val currentText = currentSection.script.getOrNull(currentScriptIndex) ?: ""
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💬 \"$currentText\"",
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    item {
                        // Playback Controls
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        if (currentSectionIndex > 0) {
                                            speechManager.stop()
                                            currentSectionIndex--
                                        }
                                    },
                                    enabled = currentSectionIndex > 0
                                ) {
                                    Icon(Icons.Default.SkipPrevious, contentDescription = "Önceki")
                                }

                                Button(
                                    onClick = {
                                        isPlaying = !isPlaying
                                        if (!isPlaying) speechManager.stop()
                                    },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isPlaying) "Durdur" else "Oynat")
                                }

                                IconButton(
                                    onClick = {
                                        if (currentSectionIndex < totalSections - 1) {
                                            speechManager.stop()
                                            currentSectionIndex++
                                        }
                                    },
                                    enabled = currentSectionIndex < totalSections - 1
                                ) {
                                    Icon(Icons.Default.SkipNext, contentDescription = "Sonraki")
                                }

                                IconButton(
                                    onClick = {
                                        isMuted = !isMuted
                                        if (isMuted) speechManager.stop()
                                    }
                                ) {
                                    Icon(if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Ses")
                                }

                                Text(
                                    text = "${playRate}x",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clickable {
                                            playRate = when (playRate) {
                                                1.0f -> 1.25f
                                                1.25f -> 1.5f
                                                1.5f -> 0.75f
                                                else -> 1.0f
                                            }
                                        }
                                        .padding(6.dp)
                                )
                            }
                        }
                    }

                    // Special section contents (Examples / Mistakes / Summary)
                    if (currentSection.examples.isNotEmpty()) {
                        item {
                            Text("💡 Örnekler", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        itemsIndexed(currentSection.examples) { _, (exTitle, exDesc) ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(exTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(exDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    if (currentSection.mistakes.isNotEmpty()) {
                        item {
                            Text("⚠️ Sık Yapılan Hatalar", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        itemsIndexed(currentSection.mistakes) { _, (wrong, right) ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("❌ $wrong", color = ErrorRed, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("✓ $right", color = SuccessGreen, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    if (currentSection.summary.isNotEmpty()) {
                        item {
                            Text("📌 Bölüm Özeti", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        itemsIndexed(currentSection.summary) { _, summItem ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("•", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(summItem, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            1 -> {
                // Lesson Notes Tab
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            text = "${lesson.name} — Ders Notları",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    itemsIndexed(lesson.sections) { sIdx, sec ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "${sIdx + 1}. ${sec.title}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = sec.script.joinToString(" "),
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                // Mini Quiz Tab
                val allQuizzes = remember(lesson) {
                    lesson.sections.flatMap { it.quiz }
                }
                if (allQuizzes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Bu ders için mini test hazırlanıyor.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(allQuizzes) { qIndex, qItem ->
                            MiniQuizCard(qIndex = qIndex, item = qItem)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MiniQuizCard(qIndex: Int, item: MiniQuizItem) {
    var selectedOpt by remember { mutableStateOf<Int?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "${qIndex + 1}. ${item.question}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            val labels = listOf("A", "B", "C", "D")
            item.options.forEachIndexed { optIndex, optText ->
                val isCorrect = optIndex == item.correctIndex
                val isChosen = selectedOpt == optIndex

                val containerColor = when {
                    !isSubmitted -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
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
                        .clickable(enabled = !isSubmitted) {
                            selectedOpt = optIndex
                            isSubmitted = true
                        }
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${labels.getOrElse(optIndex) { "?" }})",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = optText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isChosen || (isSubmitted && isCorrect)) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isSubmitted) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    val wasCorrect = selectedOpt == item.correctIndex
                    Text(
                        text = if (wasCorrect) "✓ Doğru!" else "✗ Yanlış!",
                        fontWeight = FontWeight.Bold,
                        color = if (wasCorrect) SuccessGreen else ErrorRed
                    )
                    Text(
                        text = item.explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
