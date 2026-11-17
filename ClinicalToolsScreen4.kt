package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import java.util.Calendar
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AmbientSoundManager
import com.example.data.LgsCurriculum
import com.example.data.LgsRepository
import com.example.ui.theme.PrimaryLight
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun WeeklyReportScreen(
    repository: LgsRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dailyRecords by repository.dailyRecords.collectAsState()
    val trialExams by repository.trialExams.collectAsState()
    val studentName by repository.studentName.collectAsState()

    // Aggregate past 7 days
    val cal = Calendar.getInstance()
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var totalMin = 0
    var totalQ = 0
    var totalCorrect = 0
    var totalCards = 0
    var totalPomodoros = 0
    var activeDays = 0

    for (i in 0..6) {
        val dStr = sdf.format(cal.time)
        val rec = dailyRecords[dStr]
        if (rec != null) {
            totalMin += rec.studyMinutes
            totalQ += rec.questionsSolved
            totalCorrect += rec.questionsCorrect
            totalCards += rec.cardsFlipped
            totalPomodoros += rec.pomodoros
            if (rec.studyMinutes > 0 || rec.questionsSolved > 0) activeDays++
        }
        cal.add(Calendar.DAY_OF_YEAR, -1)
    }

    val acc = if (totalQ > 0) (totalCorrect * 100) / totalQ else 0
    val latestTrial = trialExams.lastOrNull()

    val reportText = buildString {
        appendLine("📋 LGS 2027 KLİNİK HAFTALIK CHECK-UP RAPORU")
        appendLine("Tarih: ${repository.getTodayString()} · Öğrenci: ${studentName.ifBlank { "Öğrenci" }}")
        appendLine("⏱️ Çalışma Süresi: $totalMin dakika (~${totalMin / 60} saat)")
        appendLine("🎯 Soru Sayısı: $totalQ soru (Doğruluk: %$acc)")
        appendLine("🎴 Tekrar Edilen Kart: $totalCards kart")
        appendLine("🍅 Tamamlanan Pomodoro: $totalPomodoros seans")
        appendLine("🔥 Aktif Çalışma Günü: $activeDays / 7 gün")
        if (latestTrial != null) {
            appendLine("🩺 Son Deneme Puanı: ${latestTrial.score} Puan (${latestTrial.totalNet} Net)")
        }
        appendLine("Doktor Notu: ${repository.getChiefPhysicianNote()}")
    }

    var copiedToast by remember { mutableStateOf(false) }

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
                Text("📋 Haftalık Rapor", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = {
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("LGS Rapor", reportText))
                    copiedToast = true
                }
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (copiedToast) "Kopyalandı!" else "Kopyala")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(reportText.lines().filter { it.isNotBlank() }) { line ->
                    Text(line, style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)
                }
            }
        }
    }
}

@Composable
fun TacticsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
            }
            Text("💡 Yeni Nesil Soru Taktikleri", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "LGS'de uzun ve karmaşık soruları saniyeler içinde çözmek için 7 klinik reçete:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(LgsCurriculum.studyTactics) { (title, desc) ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(desc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun BadgesScreen(
    repository: LgsRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unlockedMap by repository.unlockedBadges.collectAsState()
    val allBadges = LgsCurriculum.badgeDefinitions

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
            }
            Text("🏆 Başarım Vitrini", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Açılan rozetler: ${unlockedMap.size} / ${allBadges.size}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(allBadges) { badge ->
                val isUnlocked = unlockedMap.containsKey(badge.id)
                val unlockDate = unlockedMap[badge.id]

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = badge.emoji, fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = badge.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isUnlocked) (unlockDate?.let { "Açıldı: $it" } ?: badge.desc) else (if (badge.isSecret) "??? Gizli Rozet" else badge.desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PomodoroScreen(
    repository: LgsRepository,
    ambientSoundManager: AmbientSoundManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isRunning by remember { mutableStateOf(false) }
    var isWorkPhase by remember { mutableStateOf(true) } // true: 25 min work, false: 5 min break
    var secondsLeft by remember { mutableIntStateOf(25 * 60) }

    val isRainOn by ambientSoundManager.isRainPlaying.collectAsState()
    val isLofiOn by ambientSoundManager.isLofiPlaying.collectAsState()

    LaunchedEffect(isRunning, secondsLeft) {
        if (isRunning && secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        } else if (isRunning && secondsLeft == 0) {
            ambientSoundManager.playBeep()
            if (isWorkPhase) {
                repository.recordPomodoro()
                isWorkPhase = false
                secondsLeft = 5 * 60
            } else {
                isWorkPhase = true
                secondsLeft = 25 * 60
            }
        }
    }

    val minDisplay = secondsLeft / 60
    val secDisplay = secondsLeft % 60
    val timeFormatted = "%02d:%02d".format(Locale.US, minDisplay, secDisplay)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
            }
            Text("⏱️ Pomodoro ve Çalışma Odası", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Timer Display
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isWorkPhase) "🧠 ODAKLANMA FAZI" else "☕ DİNLENME MOLASI",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isWorkPhase) MaterialTheme.colorScheme.primary else SuccessGreen,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = timeFormatted,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { isRunning = !isRunning }
                    ) {
                        Icon(if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isRunning) "Duraklat" else "Başlat")
                    }

                    FilledTonalButton(
                        onClick = {
                            isRunning = false
                            isWorkPhase = true
                            secondsLeft = 25 * 60
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sıfırla")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Ambient Sound Controls
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🎧 Çalışma Ortamı Sesleri", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Konsantrasyonunu artırmak için arka plan beyaz gürültüsü ve lo-fi melodiler:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { ambientSoundManager.toggleRain() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRainOn) SuccessGreen else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isRainOn) "🌧️ Yağmur Açık" else "🌧️ Yağmur Sesi", color = if (isRainOn) Color.White else MaterialTheme.colorScheme.onSurface)
                    }

                    Button(
                        onClick = { ambientSoundManager.toggleLofi() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLofiOn) SuccessGreen else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isLofiOn) "🎵 Lo-Fi Açık" else "🎵 Lo-Fi Kütüphane", color = if (isLofiOn) Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun MotivationScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var quoteIndex by remember { mutableIntStateOf(0) }
    val quotes = LgsCurriculum.motivationQuotes
    val currentQuote = quotes[quoteIndex % quotes.size]

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
            }
            Text("💊 LGS Motivasyon Kapsülü", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Large quote card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🌟 GÜNÜN MOTİVASYON REÇETESİ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "\"$currentQuote\"",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = { quoteIndex = (quoteIndex + 1) % quotes.size }
                ) {
                    Text("Yeni Söz")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Başarılı Öğrencilerin Ortak Alışkanlıkları:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(LgsCurriculum.successfulHabitTips) { tip ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✓", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(tip, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
