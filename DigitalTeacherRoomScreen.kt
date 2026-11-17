package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SpeechManager
import com.example.data.LgsRepository
import com.example.data.SavedLessonItem
import com.example.data.TeacherChatMessage
import com.example.data.TeacherLessonReport
import com.example.data.WhiteboardElement
import com.example.gemini.GeminiService
import com.example.gemini.TeacherBeat
import com.example.gemini.TeacherDrawCommand
import com.example.ui.components.AvatarState
import com.example.ui.components.TeacherAvatar
import com.example.ui.components.WhiteboardCanvas
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DigitalTeacherRoomScreen(
    initialTopic: String?,
    initialReason: String?,
    repository: LgsRepository,
    geminiService: GeminiService,
    speechManager: SpeechManager,
    onExitRoom: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val apiKey by repository.apiKey.collectAsState()
    val teacherCharacter by repository.teacherCharacter.collectAsState()
    val ttsRate by repository.ttsRate.collectAsState()
    val ttsPitch by repository.ttsPitch.collectAsState()
    val savedLessons by repository.savedLessons.collectAsState()

    // Whiteboard state & undo/redo
    val whiteboardElements = remember { mutableStateListOf<WhiteboardElement>() }
    val undoStack = remember { mutableStateListOf<List<WhiteboardElement>>() }
    val redoStack = remember { mutableStateListOf<List<WhiteboardElement>>() }

    // Chat messages
    val chatMessages = remember { mutableStateListOf<TeacherChatMessage>() }
    var chatDrawerOpen by remember { mutableStateOf(false) }
    var chatInputText by remember { mutableStateOf("") }

    // Session stats
    val sessionStartTime = remember { mutableLongStateOf(System.currentTimeMillis()) }
    var retryCount by remember { mutableIntStateOf(0) }
    var avatarState by remember { mutableStateOf(AvatarState.IDLE) }
    var isPaused by remember { mutableStateOf(false) }

    // Dialogs
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showSavedLessonsDialog by remember { mutableStateOf(false) }
    var showSaveLessonDialog by remember { mutableStateOf(false) }
    var newLessonNameInput by remember { mutableStateOf("") }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    // Speech Recognition setup (STT)
    var isListeningSpeech by remember { mutableStateOf(false) }
    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else null
    }

    fun pushUndo() {
        undoStack.add(whiteboardElements.toList())
        redoStack.clear()
        if (undoStack.size > 40) undoStack.removeAt(0)
    }

    // Function to apply a beat from teacher
    fun applyTeacherBeat(beat: TeacherBeat) {
        pushUndo()
        beat.drawCommands.forEach { cmd ->
            when (cmd.type) {
                "clear" -> whiteboardElements.clear()
                "write" -> {
                    whiteboardElements.add(
                        WhiteboardElement(
                            id = System.currentTimeMillis().toString() + "_" + (0..999).random(),
                            byUser = false,
                            type = "write",
                            text = cmd.text ?: "",
                            x1 = cmd.x1,
                            y1 = cmd.y1,
                            colorHex = parseHexColor(cmd.color, 0xFF0284C7),
                            fontSize = cmd.fontSize
                        )
                    )
                }
                "line", "underline" -> {
                    whiteboardElements.add(
                        WhiteboardElement(
                            id = System.currentTimeMillis().toString() + "_" + (0..999).random(),
                            byUser = false,
                            type = cmd.type,
                            x1 = cmd.x1,
                            y1 = cmd.y1,
                            x2 = cmd.x2,
                            y2 = cmd.y2,
                            colorHex = parseHexColor(cmd.color, 0xFF0284C7),
                            strokeWidth = 3.5f
                        )
                    )
                }
                "arrow" -> {
                    whiteboardElements.add(
                        WhiteboardElement(
                            id = System.currentTimeMillis().toString() + "_" + (0..999).random(),
                            byUser = false,
                            type = "arrow",
                            x1 = cmd.x1,
                            y1 = cmd.y1,
                            x2 = cmd.x2,
                            y2 = cmd.y2,
                            colorHex = parseHexColor(cmd.color, 0xFF0284C7),
                            strokeWidth = 3.5f
                        )
                    )
                }
                "circle" -> {
                    whiteboardElements.add(
                        WhiteboardElement(
                            id = System.currentTimeMillis().toString() + "_" + (0..999).random(),
                            byUser = false,
                            type = "circle",
                            x1 = cmd.x1,
                            y1 = cmd.y1,
                            radius = cmd.radius,
                            colorHex = parseHexColor(cmd.color, 0xFF16A34A),
                            strokeWidth = 3f
                        )
                    )
                }
                "rect" -> {
                    whiteboardElements.add(
                        WhiteboardElement(
                            id = System.currentTimeMillis().toString() + "_" + (0..999).random(),
                            byUser = false,
                            type = "rect",
                            x1 = cmd.x1,
                            y1 = cmd.y1,
                            width = cmd.width,
                            height = cmd.height,
                            colorHex = parseHexColor(cmd.color, 0xFFEA580C),
                            strokeWidth = 3f
                        )
                    )
                }
            }
        }
    }

    fun askTeacherPrompt(prompt: String) {
        if (prompt.isBlank()) return
        chatMessages.add(TeacherChatMessage(id = System.currentTimeMillis().toString(), isUser = true, text = prompt))
        avatarState = AvatarState.THINKING

        scope.launch {
            val history = chatMessages.map { Pair(it.isUser, it.text) }
            val beats = geminiService.askTeacher(
                apiKey = apiKey,
                prompt = prompt,
                character = teacherCharacter,
                boardElements = whiteboardElements.toList(),
                chatHistory = history
            )

            // Play teacher beats sequentially
            for (beat in beats) {
                applyTeacherBeat(beat)
                chatMessages.add(TeacherChatMessage(id = System.currentTimeMillis().toString(), isUser = false, text = beat.speak))
                avatarState = AvatarState.SPEAKING
                speechManager.speak(beat.speak, rate = ttsRate, pitch = ttsPitch)
            }
            avatarState = AvatarState.IDLE
        }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Öğretmenine sorunu söyle...")
            }
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListeningSpeech = true
                    avatarState = AvatarState.LISTENING
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    isListeningSpeech = false
                    avatarState = AvatarState.IDLE
                }

                override fun onError(error: Int) {
                    isListeningSpeech = false
                    avatarState = AvatarState.IDLE
                }

                override fun onResults(results: Bundle?) {
                    isListeningSpeech = false
                    avatarState = AvatarState.IDLE
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val recognized = matches?.firstOrNull()
                    if (!recognized.isNullOrBlank()) {
                        askTeacherPrompt(recognized)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            speechRecognizer?.startListening(intent)
        }
    }

    // Initial greeting if arriving with topic
    LaunchedEffect(initialTopic) {
        if (!initialTopic.isNullOrBlank() && chatMessages.isEmpty()) {
            val welcomeText = "Merhaba! Ana panelden \"$initialTopic\" konusu için geldin. Bu konuda birlikte harika bir çalışma yapacağız. Neresinde takıldın, anlat bakalım!"
            chatMessages.add(TeacherChatMessage(id = "init_0", isUser = false, text = welcomeText))
            whiteboardElements.add(
                WhiteboardElement(
                    id = "init_board_0",
                    byUser = false,
                    type = "write",
                    text = "Konu: $initialTopic",
                    x1 = 50f,
                    y1 = 40f,
                    colorHex = 0xFF0284C7,
                    fontSize = 26f
                )
            )
            speechManager.speak(welcomeText, rate = ttsRate, pitch = ttsPitch)
        } else if (chatMessages.isEmpty()) {
            val welcomeText = "Merhaba sevgili öğrencim! Dijital Hoca Odası'na hoş geldin. Bugün hangi konuyu aydınlatmak istersin? Mikrofonla konuşabilir veya tahtaya yazabilirsin."
            chatMessages.add(TeacherChatMessage(id = "init_0", isUser = false, text = welcomeText))
            speechManager.speak(welcomeText, rate = ttsRate, pitch = ttsPitch)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechManager.stop()
            speechRecognizer?.destroy()
            // Record session log
            val elapsedMinutes = maxOf(1, ((System.currentTimeMillis() - sessionStartTime.longValue) / 60000).toInt())
            repository.addTeacherReport(
                TeacherLessonReport(
                    id = System.currentTimeMillis().toString(),
                    topic = initialTopic ?: "Genel Hoca Dersi",
                    date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                    minutes = elapsedMinutes,
                    messagesCount = chatMessages.size,
                    retryCount = retryCount,
                    strugglePoint = if (retryCount > 1) initialTopic else null
                )
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onExitRoom) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Odadan Çık")
                    }
                    Text(
                        text = "👨‍🏫 Hoca Odası",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showSavedLessonsDialog = true }) {
                        Icon(Icons.Default.Folder, contentDescription = "Dersler")
                    }
                    IconButton(onClick = { chatDrawerOpen = !chatDrawerOpen }) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Sohbet", tint = if (chatDrawerOpen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                    }
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Ayarlar")
                    }
                    IconButton(onClick = { showResetConfirmDialog = true }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sıfırla")
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Teacher Avatar Display
            TeacherAvatar(
                state = avatarState,
                title = "Prof. Dr. Yusuf Salih (${when (teacherCharacter) {
                    "enerjik" -> "Enerjik"
                    "mat" -> "Matematik"
                    "fen" -> "Fen"
                    "tur" -> "Türkçe"
                    else -> "Sakin"
                }})",
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Interactive Whiteboard Canvas
            WhiteboardCanvas(
                elements = whiteboardElements,
                onAddElement = { elem ->
                    pushUndo()
                    whiteboardElements.add(elem)
                },
                onClear = {
                    pushUndo()
                    whiteboardElements.clear()
                },
                onUndo = {
                    if (undoStack.isNotEmpty()) {
                        redoStack.add(whiteboardElements.toList())
                        val prev = undoStack.removeAt(undoStack.size - 1)
                        whiteboardElements.clear()
                        whiteboardElements.addAll(prev)
                    }
                },
                onRedo = {
                    if (redoStack.isNotEmpty()) {
                        undoStack.add(whiteboardElements.toList())
                        val next = redoStack.removeAt(redoStack.size - 1)
                        whiteboardElements.clear()
                        whiteboardElements.addAll(next)
                    }
                },
                onAskObject = { obj ->
                    val desc = obj.text ?: obj.type
                    askTeacherPrompt("Hocam, tahtadaki \"$desc\" ne anlama geliyor, tekrar açıklar mısınız?")
                },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic Button
                Button(
                    onClick = {
                        if (isListeningSpeech) {
                            speechRecognizer?.stopListening()
                            isListeningSpeech = false
                            avatarState = AvatarState.IDLE
                        } else {
                            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isListeningSpeech) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1.1f)
                ) {
                    Icon(if (isListeningSpeech) Icons.Default.MicOff else Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isListeningSpeech) "Dinliyor..." else "Konuş", fontSize = 13.sp)
                }

                // Pause / Resume speech
                FilledTonalButton(
                    onClick = {
                        if (isPaused) {
                            isPaused = false
                            avatarState = AvatarState.IDLE
                        } else {
                            isPaused = true
                            speechManager.stop()
                            avatarState = AvatarState.PAUSED
                        }
                    },
                    modifier = Modifier.weight(0.9f)
                ) {
                    Icon(if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isPaused) "Devam" else "Durdur", fontSize = 12.sp)
                }

                // Farklı Anlat (Re-explain)
                FilledTonalButton(
                    onClick = {
                        retryCount++
                        askTeacherPrompt("Hocam bunu farklı bir benzetmeyle ve günlük hayattan somut bir örnekle tekrar anlatır mısınız?")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Farklı Anlat", fontSize = 12.sp)
                }

                // Bana Soru Sor (Quiz Me)
                FilledTonalButton(
                    onClick = {
                        askTeacherPrompt("Hocam bana tahtada çözmem için bu konuyla ilgili güzel bir LGS sorusu sorar mısınız?")
                    },
                    modifier = Modifier.weight(1.1f)
                ) {
                    Icon(Icons.Default.QuestionMark, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Soru Sor", fontSize = 12.sp)
                }
            }
        }

        // Sliding Chat Drawer (Right-side or overlay)
        AnimatedVisibility(
            visible = chatDrawerOpen,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(320.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💬 Sohbet Geçmişi", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        IconButton(onClick = { chatDrawerOpen = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Kapat")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(chatMessages) { msg ->
                            val isMe = msg.isUser
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = msg.text,
                                        color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Chat Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatInputText,
                            onValueChange = { chatInputText = it },
                            placeholder = { Text("Sorunu yaz...", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                if (chatInputText.isNotBlank()) {
                                    val text = chatInputText
                                    chatInputText = ""
                                    askTeacherPrompt(text)
                                }
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gönder", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    // Settings Dialog
    if (showSettingsDialog) {
        var tempKey by remember { mutableStateOf(apiKey) }
        var selectedChar by remember { mutableStateOf(teacherCharacter) }
        var currentRate by remember { mutableStateOf(ttsRate) }
        var currentPitch by remember { mutableStateOf(ttsPitch) }

        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("⚙️ Hoca Odası Ayarları") },
            text = {
                Column {
                    Text(
                        text = "Gemini API Anahtarı (İsteğe bağlı):",
                        style = MaterialTheme.typography.labelMedium
                    )
                    OutlinedTextField(
                        value = tempKey,
                        onValueChange = { tempKey = it },
                        placeholder = { Text("AI Studio API Key") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Öğretmen Tarzı:", style = MaterialTheme.typography.labelMedium)
                    listOf(
                        "sakin" to "🩺 Sakin ve Anlaşılır",
                        "enerjik" to "⚡ Enerjik ve Coşkulu",
                        "mat" to "📐 Matematik Odaklı",
                        "fen" to "🧪 Fen Odaklı",
                        "tur" to "📖 Türkçe Odaklı"
                    ).forEach { (k, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedChar = k }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = if (selectedChar == k) "🔘 $label" else "⚪ $label",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selectedChar == k) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    repository.setApiKey(tempKey)
                    repository.setTeacherCharacter(selectedChar)
                    repository.setTtsVoiceSettings(currentRate, currentPitch)
                    showSettingsDialog = false
                }) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    // Reset Confirm Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Ders Sıfırlansın mı?") },
            text = { Text("Tahtadaki çizimler ve sohbet geçmişi sıfırlanacaktır.") },
            confirmButton = {
                Button(
                    onClick = {
                        whiteboardElements.clear()
                        undoStack.clear()
                        redoStack.clear()
                        chatMessages.clear()
                        speechManager.stop()
                        avatarState = AvatarState.IDLE
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sıfırla")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    // Saved Lessons Dialog
    if (showSavedLessonsDialog) {
        AlertDialog(
            onDismissRequest = { showSavedLessonsDialog = false },
            title = { Text("📂 Kayıtlı Dersler") },
            text = {
                Column {
                    Button(
                        onClick = {
                            newLessonNameInput = "Ders ${SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()).format(Date())}"
                            showSaveLessonDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("💾 Şu Anki Dersi Kaydet")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    if (savedLessons.isEmpty()) {
                        Text("Henüz kaydedilmiş ders bulunmuyor.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        LazyColumn(modifier = Modifier.height(200.dp)) {
                            items(savedLessons) { lesson ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            whiteboardElements.clear()
                                            whiteboardElements.addAll(lesson.elements)
                                            chatMessages.clear()
                                            chatMessages.addAll(lesson.messages)
                                            showSavedLessonsDialog = false
                                        }
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(lesson.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(lesson.dateString, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(onClick = { repository.deleteSavedLesson(lesson.id) }) {
                                        Icon(Icons.Default.Close, contentDescription = "Sil", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSavedLessonsDialog = false }) {
                    Text("Kapat")
                }
            }
        )
    }

    // Save Lesson Dialog
    if (showSaveLessonDialog) {
        AlertDialog(
            onDismissRequest = { showSaveLessonDialog = false },
            title = { Text("Dersi Kaydet") },
            text = {
                OutlinedTextField(
                    value = newLessonNameInput,
                    onValueChange = { newLessonNameInput = it },
                    label = { Text("Ders Başlığı") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    val lesson = SavedLessonItem(
                        id = System.currentTimeMillis().toString(),
                        name = newLessonNameInput.ifBlank { "Ders Notu" },
                        dateString = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date()),
                        messageCount = chatMessages.size,
                        elements = whiteboardElements.toList(),
                        messages = chatMessages.toList()
                    )
                    repository.saveTeacherLesson(lesson)
                    showSaveLessonDialog = false
                }) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveLessonDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

private fun parseHexColor(hex: String?, defaultColor: Long): Long {
    if (hex.isNullOrBlank()) return defaultColor
    return try {
        val clean = hex.removePrefix("#")
        val colorInt = clean.toLong(16)
        if (clean.length == 6) 0xFF000000 or colorInt else colorInt
    } catch (_: Exception) {
        defaultColor
    }
}
