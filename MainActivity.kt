package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AmbientSoundManager
import com.example.audio.SpeechManager
import com.example.data.LgsCurriculum
import com.example.data.LgsRepository
import com.example.gemini.GeminiService
import com.example.ui.components.SearchDialog
import com.example.ui.components.LumiBottomBar
import com.example.ui.components.LumiTopBar
import com.example.ui.components.NotificationsDialog
import com.example.ui.components.ProfileDialog
import com.example.ui.navigation.Screen
import com.example.ui.screens.AcademicAnalyticsScreen
import com.example.ui.screens.AiCoachScreen
import com.example.ui.screens.BadgesScreen
import com.example.ui.screens.CalendarHistoryScreen
import com.example.ui.screens.CourseDetailScreen
import com.example.ui.screens.CoursesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.CurriculumMapScreen
import com.example.ui.screens.DailyCheckupScreen
import com.example.ui.screens.DigitalTeacherRoomScreen
import com.example.ui.screens.GoalTreeScreen
import com.example.ui.screens.HocaReportsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LearningDnaScreen
import com.example.ui.screens.MistakeDetectiveScreen
import com.example.ui.screens.MotivationScreen
import com.example.ui.screens.MyQuestionsScreen
import com.example.ui.screens.PomodoroScreen
import com.example.ui.screens.QuestionBankScreen
import com.example.ui.screens.ScoreCalculatorScreen
import com.example.ui.screens.SrsScreen
import com.example.ui.screens.TacticsScreen
import com.example.ui.screens.TodoListScreen
import com.example.ui.screens.TopicHealthScreen
import com.example.ui.screens.TrialExamsScreen
import com.example.ui.screens.UnitDetailScreen
import com.example.ui.screens.VideoLessonScreen
import com.example.ui.screens.WeeklyReportScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var repository: LgsRepository
    private lateinit var geminiService: GeminiService
    private lateinit var speechManager: SpeechManager
    private lateinit var ambientSoundManager: AmbientSoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = LgsRepository(this)
        geminiService = GeminiService()
        speechManager = SpeechManager(this)
        ambientSoundManager = AmbientSoundManager()

        setContent {
            val isDark by repository.isDarkTheme.collectAsState()

            MyApplicationTheme(darkTheme = isDark) {
                MainApp(
                    repository = repository,
                    geminiService = geminiService,
                    speechManager = speechManager,
                    ambientSoundManager = ambientSoundManager
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechManager.shutdown()
        ambientSoundManager.releaseAll()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    repository: LgsRepository,
    geminiService: GeminiService,
    speechManager: SpeechManager,
    ambientSoundManager: AmbientSoundManager
) {
    val isDark by repository.isDarkTheme.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val screenStack = remember { mutableStateListOf<Screen>(Screen.Home) }
    val currentScreen = screenStack.lastOrNull() ?: Screen.Home

    fun navigateTo(screen: Screen) {
        if (currentScreen != screen) {
            screenStack.add(screen)
        }
    }

    fun popBack() {
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.size - 1)
        }
    }

    BackHandler(enabled = screenStack.size > 1) {
        popBack()
    }

    var showSearchDialog by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }
    val studentName by repository.studentName.collectAsState()
    val targetScore by repository.targetScore.collectAsState()
    val notifications by repository.notifications.collectAsState()

    fun goTab(target: Screen) {
        screenStack.clear()
        screenStack.add(Screen.Home)
        if (target != Screen.Home) screenStack.add(target)
    }

    val selectedTab = when (currentScreen) {
        is Screen.Home -> 0
        is Screen.AiCoach -> 1
        is Screen.Courses, is Screen.CourseDetail, is Screen.UnitDetail, is Screen.VideoLesson -> 2
        is Screen.TrialExams, is Screen.AcademicAnalytics, is Screen.WeeklyReport,
        is Screen.LearningDna, is Screen.TopicHealth, is Screen.DailyCheckup -> 3
        else -> -1
    }

    // If on DigitalTeacherRoomScreen, give it full screen experience
    if (currentScreen is Screen.TeacherRoom) {
        DigitalTeacherRoomScreen(
            initialTopic = currentScreen.initialTopic,
            initialReason = currentScreen.initialReason,
            repository = repository,
            geminiService = geminiService,
            speechManager = speechManager,
            onExitRoom = { popBack() }
        )
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    item {
                        // Drawer Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🩺", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Klinik Üssü",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Prof. Dr. Yusuf Salih · LGS 2027",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Main Destinations
                    item {
                        DrawerCategoryHeader("Genel")
                        DrawerItem("🏠 Öğrenci Dosyası (Ana Panel)", currentScreen is Screen.Home) {
                            screenStack.clear()
                            screenStack.add(Screen.Home)
                            scope.launch { drawerState.close() }
                        }
                        DrawerItem("👨‍🏫 Dijital Hoca Odası", currentScreen is Screen.TeacherRoom) {
                            navigateTo(Screen.TeacherRoom())
                            scope.launch { drawerState.close() }
                        }
                    }

                    item {
                        DrawerCategoryHeader("Hızlı İşlemler")
                        DrawerItem("🔍 Ara", false) {
                            showSearchDialog = true
                            scope.launch { drawerState.close() }
                        }
                        DrawerItem(if (isDark) "☀️ Açık temaya geç" else "🌙 Koyu temaya geç", false) {
                            repository.setDarkTheme(!isDark)
                            scope.launch { drawerState.close() }
                        }
                    }

                    // Dersler
                    item {
                        DrawerCategoryHeader("Dersler ve Üniteler")
                    }
                    items(LgsCurriculum.courses) { course ->
                        DrawerItem(
                            label = "${course.icon} ${course.name}",
                            selected = (currentScreen as? Screen.CourseDetail)?.courseId == course.id
                        ) {
                            navigateTo(Screen.CourseDetail(course.id))
                            scope.launch { drawerState.close() }
                        }
                    }

                    // Platform & Araçlar
                    item {
                        DrawerCategoryHeader("Platform ve Araçlar")
                        DrawerItem("🤖 AI Çalışma Koçu", currentScreen is Screen.AiCoach) {
                            navigateTo(Screen.AiCoach)
                            scope.launch { drawerState.close() }
                        }
                        DrawerItem("🗺️ LGS Konu Haritası", currentScreen is Screen.CurriculumMap) {
                            navigateTo(Screen.CurriculumMap)
                            scope.launch { drawerState.close() }
                        }
                        DrawerItem("📊 Akademik Analiz Merkezi", currentScreen is Screen.AcademicAnalytics) {
                            navigateTo(Screen.AcademicAnalytics)
                            scope.launch { drawerState.close() }
                        }
                        DrawerItem("📋 Hoca Ders Raporları", currentScreen is Screen.HocaReports) {
                            navigateTo(Screen.HocaReports)
                            scope.launch { drawerState.close() }
                        }
                        DrawerItem("📑 Soru Bankası", currentScreen is Screen.QuestionBank) {
                            navigateTo(Screen.QuestionBank)
                            scope.launch { drawerState.close() }
                        }
                        DrawerItem("🕵️ Hata Dedektifi", currentScreen is Screen.MistakeDetective) {
                            navigateTo(Screen.MistakeDetective)
                            scope.launch { drawerState.close() }
                        }
                        DrawerItem("✓ Bugün Ne Çalışmalıyım?", currentScreen is Screen.TodoList) {
                            navigateTo(Screen.TodoList)
                            scope.launch { drawerState.close() }
                        }
                        DrawerItem("🧮 LGS Puan Hesaplama", currentScreen is Screen.ScoreCalculator) {
                            navigateTo(Screen.ScoreCalculator)
                            scope.launch { drawerState.close() }
                        }
                        DrawerItem("🩺 Deneme Hasta Dosyası", currentScreen is Screen.TrialExams) {
                            navigateTo(Screen.TrialExams)
                            scope.launch { drawerState.close() }
                        }
                        DrawerItem("🧪 Günlük Check-up", currentScreen is Screen.DailyCheckup) {
                            navigateTo(Screen.DailyCheckup)
                            scope.launch { drawerState.close() }
                        }
                        DrawerItem("🧬 Öğrenme DNA'm", currentScreen is Screen.LearningDna) {
                            navigateTo(Screen.LearningDna)
                            scope.launch { drawerState.close() }
                        }
                        DrawerItem("🌳 Hedef Ağacı", currentScreen is Screen.GoalTree) {
                            navigateTo(Screen.GoalTree)
                            scope.launch { drawerState.close() }
                        }
                        DrawerItem("🩻 Konu Sağlık Durumu", currentScreen is Screen.TopicHealth) {
                            navigateTo(Screen.TopicHealth)
                            scope.launch { drawerState.close() }
                        }
                        DrawerItem("🗃️ Akıllı Tekrar (SRS)", currentScreen is Screen.SrsSpacedRepetition) {
                            navigateTo(Screen.SrsSpacedRepetition)
                            scope.launch { drawerState.close() }
                        }
                        DrawerItem("📸 Kendi Sorularım", currentScreen is Screen.MyQuestions) {
                            navigateTo(Screen.MyQuestions)
                            scope.launch { drawerState.close() }
                        }
                        DrawerItem("🗓️ Takvim ve Çalışma Geçmişi", currentScreen is Screen.CalendarHistory) {
                            navigateTo(Screen.CalendarHistory)
                            scope.launch { drawerState.close() }
                        }
                        DrawerItem("📋 Haftalık Check-up", currentScreen is Screen.WeeklyReport) {
                            navigateTo(Screen.WeeklyReport)
                            scope.launch { drawerState.close() }
                        }
                        DrawerItem("💡 Yeni Nesil Soru Taktikleri", currentScreen is Screen.Tactics) {
                            navigateTo(Screen.Tactics)
                            scope.launch { drawerState.close() }
                        }
                        DrawerItem("🏆 Başarım Vitrini", currentScreen is Screen.Badges) {
                            navigateTo(Screen.Badges)
                            scope.launch { drawerState.close() }
                        }
                        DrawerItem("⏱️ Pomodoro ve Çalışma Odası", currentScreen is Screen.Pomodoro) {
                            navigateTo(Screen.Pomodoro)
                            scope.launch { drawerState.close() }
                        }
                        DrawerItem("💊 Motivasyon Kapsülü", currentScreen is Screen.Motivation) {
                            navigateTo(Screen.Motivation)
                            scope.launch { drawerState.close() }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                if (currentScreen is Screen.Home || currentScreen is Screen.Courses) {
                    LumiTopBar(
                        studentName = studentName,
                        hasNotifications = notifications.isNotEmpty(),
                        onBell = { showNotifications = true },
                        onAvatar = { showProfile = true }
                    )
                }
            },
            bottomBar = {
                LumiBottomBar(
                    selectedIndex = selectedTab,
                    onSelect = { index ->
                        when (index) {
                            0 -> goTab(Screen.Home)
                            1 -> goTab(Screen.AiCoach)
                            2 -> goTab(Screen.Courses)
                            3 -> goTab(Screen.TrialExams)
                            else -> scope.launch { drawerState.open() }
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    is Screen.Home -> DashboardScreen(
                        repository = repository,
                        onNavigate = { navigateTo(it) },
                        onOpenLumi = { goTab(Screen.AiCoach) }
                    )
                    is Screen.Courses -> CoursesScreen(
                        repository = repository,
                        onNavigate = { navigateTo(it) }
                    )
                    is Screen.CourseDetail -> CourseDetailScreen(
                        courseId = currentScreen.courseId,
                        repository = repository,
                        onNavigate = { navigateTo(it) },
                        onBack = { popBack() }
                    )
                    is Screen.UnitDetail -> UnitDetailScreen(
                        courseId = currentScreen.courseId,
                        unitName = currentScreen.unitName,
                        initialTab = currentScreen.initialTab,
                        repository = repository,
                        onNavigate = { navigateTo(it) },
                        onBack = { popBack() }
                    )
                    is Screen.VideoLesson -> VideoLessonScreen(
                        courseId = currentScreen.courseId,
                        unitName = currentScreen.unitName,
                        topicName = currentScreen.topicName,
                        speechManager = speechManager,
                        repository = repository,
                        onNavigate = { navigateTo(it) },
                        onBack = { popBack() }
                    )
                    is Screen.AiCoach -> AiCoachScreen(
                        repository = repository,
                        onNavigate = { navigateTo(it) },
                        onBack = { popBack() }
                    )
                    is Screen.CurriculumMap -> CurriculumMapScreen(
                        repository = repository,
                        onNavigate = { navigateTo(it) },
                        onBack = { popBack() }
                    )
                    is Screen.AcademicAnalytics -> AcademicAnalyticsScreen(
                        repository = repository,
                        onNavigate = { navigateTo(it) },
                        onBack = { popBack() }
                    )
                    is Screen.HocaReports -> HocaReportsScreen(
                        repository = repository,
                        onNavigate = { navigateTo(it) },
                        onBack = { popBack() }
                    )
                    is Screen.QuestionBank -> QuestionBankScreen(
                        repository = repository,
                        onBack = { popBack() }
                    )
                    is Screen.MistakeDetective -> MistakeDetectiveScreen(
                        repository = repository,
                        onBack = { popBack() }
                    )
                    is Screen.TodoList -> TodoListScreen(
                        repository = repository,
                        onNavigate = { navigateTo(it) },
                        onBack = { popBack() }
                    )
                    is Screen.ScoreCalculator -> ScoreCalculatorScreen(
                        onBack = { popBack() }
                    )
                    is Screen.TrialExams -> TrialExamsScreen(
                        repository = repository,
                        onNavigate = { navigateTo(it) },
                        onBack = { popBack() }
                    )
                    is Screen.DailyCheckup -> DailyCheckupScreen(
                        repository = repository,
                        onNavigate = { navigateTo(it) },
                        onBack = { popBack() }
                    )
                    is Screen.LearningDna -> LearningDnaScreen(
                        repository = repository,
                        onBack = { popBack() }
                    )
                    is Screen.GoalTree -> GoalTreeScreen(
                        repository = repository,
                        onBack = { popBack() }
                    )
                    is Screen.TopicHealth -> TopicHealthScreen(
                        repository = repository,
                        onNavigate = { navigateTo(it) },
                        onBack = { popBack() }
                    )
                    is Screen.SrsSpacedRepetition -> SrsScreen(
                        repository = repository,
                        onBack = { popBack() }
                    )
                    is Screen.MyQuestions -> MyQuestionsScreen(
                        repository = repository,
                        onBack = { popBack() }
                    )
                    is Screen.CalendarHistory -> CalendarHistoryScreen(
                        repository = repository,
                        onBack = { popBack() }
                    )
                    is Screen.WeeklyReport -> WeeklyReportScreen(
                        repository = repository,
                        onBack = { popBack() }
                    )
                    is Screen.Tactics -> TacticsScreen(
                        onBack = { popBack() }
                    )
                    is Screen.Badges -> BadgesScreen(
                        repository = repository,
                        onBack = { popBack() }
                    )
                    is Screen.Pomodoro -> PomodoroScreen(
                        repository = repository,
                        ambientSoundManager = ambientSoundManager,
                        onBack = { popBack() }
                    )
                    is Screen.Motivation -> MotivationScreen(
                        onBack = { popBack() }
                    )
                    else -> {}
                }
            }
        }
    }

    if (showSearchDialog) {
        SearchDialog(
            repository = repository,
            onNavigate = { navigateTo(it) },
            onDismiss = { showSearchDialog = false }
        )
    }

    if (showNotifications) {
        NotificationsDialog(
            items = notifications,
            onDismiss = { showNotifications = false }
        )
    }

    if (showProfile) {
        ProfileDialog(
            name = studentName,
            targetScore = targetScore,
            onSave = { newName, newScore ->
                repository.setStudentName(newName)
                repository.setTargetScore(newScore)
            },
            onDismiss = { showProfile = false }
        )
    }
}

@Composable
fun DrawerCategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@Composable
fun DrawerItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(label, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(vertical = 1.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}
