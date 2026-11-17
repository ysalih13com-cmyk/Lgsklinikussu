package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class LgsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("lgs_prefs_v1", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    private val _studentName = MutableStateFlow(prefs.getString("student_name", "") ?: "")
    val studentName: StateFlow<String> = _studentName.asStateFlow()

    private val _targetScore = MutableStateFlow(prefs.getInt("target_score", 450))
    val targetScore: StateFlow<Int> = _targetScore.asStateFlow()

    private val _restSunday = MutableStateFlow(prefs.getBoolean("rest_sunday", true))
    val restSunday: StateFlow<Boolean> = _restSunday.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("is_dark_theme", false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _apiKey = MutableStateFlow(
        prefs.getString("user_api_key", null) ?: BuildConfig.GEMINI_API_KEY.ifBlank { "" }
    )
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _teacherCharacter = MutableStateFlow(prefs.getString("teacher_character", "sakin") ?: "sakin")
    val teacherCharacter: StateFlow<String> = _teacherCharacter.asStateFlow()

    private val _ttsRate = MutableStateFlow(prefs.getFloat("tts_rate", 1.15f))
    val ttsRate: StateFlow<Float> = _ttsRate.asStateFlow()

    private val _ttsPitch = MutableStateFlow(prefs.getFloat("tts_pitch", 1.0f))
    val ttsPitch: StateFlow<Float> = _ttsPitch.asStateFlow()

    // Goals Map
    private val _dailyQuestionGoal = MutableStateFlow(prefs.getInt("goal_q", 30))
    val dailyQuestionGoal: StateFlow<Int> = _dailyQuestionGoal.asStateFlow()

    private val _dailyMinutesGoal = MutableStateFlow(prefs.getInt("goal_min", 60))
    val dailyMinutesGoal: StateFlow<Int> = _dailyMinutesGoal.asStateFlow()

    private val _weeklyTrialGoal = MutableStateFlow(prefs.getInt("goal_den", 1))
    val weeklyTrialGoal: StateFlow<Int> = _weeklyTrialGoal.asStateFlow()

    private val _subjectNetGoals = MutableStateFlow<Map<String, Float>>(loadSubjectNetGoals())
    val subjectNetGoals: StateFlow<Map<String, Float>> = _subjectNetGoals.asStateFlow()

    // Collections
    private val _mistakes = MutableStateFlow<List<MistakeItem>>(loadList("mistakes"))
    val mistakes: StateFlow<List<MistakeItem>> = _mistakes.asStateFlow()

    private val _todos = MutableStateFlow<List<TodoItem>>(loadList("todos"))
    val todos: StateFlow<List<TodoItem>> = _todos.asStateFlow()

    private val _trialExams = MutableStateFlow<List<TrialExamItem>>(loadList("trial_exams"))
    val trialExams: StateFlow<List<TrialExamItem>> = _trialExams.asStateFlow()

    private val _studyHistory = MutableStateFlow<List<StudyHistoryItem>>(loadList("study_history"))
    val studyHistory: StateFlow<List<StudyHistoryItem>> = _studyHistory.asStateFlow()

    private val _customQuestions = MutableStateFlow<List<CustomQuestionItem>>(loadList("custom_questions"))
    val customQuestions: StateFlow<List<CustomQuestionItem>> = _customQuestions.asStateFlow()

    private val _dailyCheckups = MutableStateFlow<List<DailyCheckupRecord>>(loadList("daily_checkups"))
    val dailyCheckups: StateFlow<List<DailyCheckupRecord>> = _dailyCheckups.asStateFlow()

    private val _teacherReports = MutableStateFlow<List<TeacherLessonReport>>(loadList("teacher_reports"))
    val teacherReports: StateFlow<List<TeacherLessonReport>> = _teacherReports.asStateFlow()

    private val _unlockedBadges = MutableStateFlow<Map<String, String>>(loadMap("unlocked_badges")) // badgeId -> date
    val unlockedBadges: StateFlow<Map<String, String>> = _unlockedBadges.asStateFlow()

    private val _unitProgressMap = MutableStateFlow<Map<String, UnitProgress>>(loadUnitProgressMap())
    val unitProgressMap: StateFlow<Map<String, UnitProgress>> = _unitProgressMap.asStateFlow()

    private val _dailyRecords = MutableStateFlow<Map<String, DailyRecord>>(loadDailyRecordsMap())
    val dailyRecords: StateFlow<Map<String, DailyRecord>> = _dailyRecords.asStateFlow()

    private val _srsCards = MutableStateFlow<Map<Int, SrsCardState>>(loadSrsMap())
    val srsCards: StateFlow<Map<Int, SrsCardState>> = _srsCards.asStateFlow()

    private val _savedLessons = MutableStateFlow<List<SavedLessonItem>>(loadList("saved_lessons"))
    val savedLessons: StateFlow<List<SavedLessonItem>> = _savedLessons.asStateFlow()

    private val _notifications = MutableStateFlow<List<String>>(loadList("notifications"))
    val notifications: StateFlow<List<String>> = _notifications.asStateFlow()

    init {
        checkDailyPlanReset()
        checkBadgeUnlocks()
    }

    fun getTodayString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun setStudentName(name: String) {
        _studentName.value = name
        prefs.edit().putString("student_name", name).apply()
    }

    fun setTargetScore(score: Int) {
        _targetScore.value = score
        prefs.edit().putInt("target_score", score).apply()
        checkBadgeUnlocks()
    }

    fun setRestSunday(enabled: Boolean) {
        _restSunday.value = enabled
        prefs.edit().putBoolean("rest_sunday", enabled).apply()
    }

    fun setDarkTheme(enabled: Boolean) {
        _isDarkTheme.value = enabled
        prefs.edit().putBoolean("is_dark_theme", enabled).apply()
    }

    fun setApiKey(key: String) {
        _apiKey.value = key
        prefs.edit().putString("user_api_key", key).apply()
    }

    fun setTeacherCharacter(charKey: String) {
        _teacherCharacter.value = charKey
        prefs.edit().putString("teacher_character", charKey).apply()
    }

    fun setTtsVoiceSettings(rate: Float, pitch: Float) {
        _ttsRate.value = rate
        _ttsPitch.value = pitch
        prefs.edit().putFloat("tts_rate", rate).putFloat("tts_pitch", pitch).apply()
    }

    fun setGoals(dailyQ: Int, dailyMin: Int, weeklyTrial: Int, subjectNets: Map<String, Float>) {
        _dailyQuestionGoal.value = dailyQ
        _dailyMinutesGoal.value = dailyMin
        _weeklyTrialGoal.value = weeklyTrial
        _subjectNetGoals.value = subjectNets
        prefs.edit()
            .putInt("goal_q", dailyQ)
            .putInt("goal_min", dailyMin)
            .putInt("goal_den", weeklyTrial)
            .apply()
        saveSubjectNetGoals(subjectNets)
    }

    // Daily records & Streak
    fun recordAnswer(courseId: String, unit: String, isCorrect: Boolean) {
        val today = getTodayString()
        val currentDay = _dailyRecords.value[today] ?: DailyRecord(today)
        val updatedDay = currentDay.copy(
            questionsSolved = currentDay.questionsSolved + 1,
            questionsCorrect = currentDay.questionsCorrect + if (isCorrect) 1 else 0
        )
        val newMap = _dailyRecords.value.toMutableMap()
        newMap[today] = updatedDay
        _dailyRecords.value = newMap
        saveDailyRecordsMap(newMap)

        // Update Unit Progress
        val unitKey = "$courseId|$unit"
        val curUnit = _unitProgressMap.value[unitKey] ?: UnitProgress()
        val newRecent = (curUnit.recentAnswers + if (isCorrect) 1 else 0).takeLast(5)
        val newWrongStreak = if (isCorrect) 0 else curUnit.wrongStreak + 1
        val updatedUnit = curUnit.copy(
            correctAnswers = curUnit.correctAnswers + if (isCorrect) 1 else 0,
            totalAnswers = curUnit.totalAnswers + 1,
            recentAnswers = newRecent,
            wrongStreak = newWrongStreak
        )
        val newUnitMap = _unitProgressMap.value.toMutableMap()
        newUnitMap[unitKey] = updatedUnit
        _unitProgressMap.value = newUnitMap
        saveUnitProgressMap(newUnitMap)

        checkBadgeUnlocks()
    }

    fun recordCardFlip() {
        val today = getTodayString()
        val currentDay = _dailyRecords.value[today] ?: DailyRecord(today)
        val updatedDay = currentDay.copy(cardsFlipped = currentDay.cardsFlipped + 1)
        val newMap = _dailyRecords.value.toMutableMap()
        newMap[today] = updatedDay
        _dailyRecords.value = newMap
        saveDailyRecordsMap(newMap)
        checkBadgeUnlocks()
    }

    fun recordStudyMinutes(courseId: String, minutes: Int, note: String = "") {
        val today = getTodayString()
        val currentDay = _dailyRecords.value[today] ?: DailyRecord(today)
        val updatedDay = currentDay.copy(studyMinutes = currentDay.studyMinutes + minutes)
        val newMap = _dailyRecords.value.toMutableMap()
        newMap[today] = updatedDay
        _dailyRecords.value = newMap
        saveDailyRecordsMap(newMap)

        val historyItem = StudyHistoryItem(
            id = System.currentTimeMillis().toString(),
            date = today,
            courseId = courseId,
            minutes = minutes,
            note = note
        )
        val newHistory = listOf(historyItem) + _studyHistory.value
        _studyHistory.value = newHistory
        saveList("study_history", newHistory)
        checkBadgeUnlocks()
    }

    fun recordPomodoro() {
        val today = getTodayString()
        val currentDay = _dailyRecords.value[today] ?: DailyRecord(today)
        val updatedDay = currentDay.copy(
            pomodoros = currentDay.pomodoros + 1,
            studyMinutes = currentDay.studyMinutes + 25
        )
        val newMap = _dailyRecords.value.toMutableMap()
        newMap[today] = updatedDay
        _dailyRecords.value = newMap
        saveDailyRecordsMap(newMap)
        checkBadgeUnlocks()
    }

    fun toggleUnitCompletion(courseId: String, unit: String) {
        val key = "$courseId|$unit"
        val current = _unitProgressMap.value[key] ?: UnitProgress()
        val updated = current.copy(
            completed = !current.completed,
            completedDate = if (!current.completed) getTodayString() else null
        )
        val newMap = _unitProgressMap.value.toMutableMap()
        newMap[key] = updated
        _unitProgressMap.value = newMap
        saveUnitProgressMap(newMap)
        checkBadgeUnlocks()
    }

    fun calculateStreak(): Int {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var streak = 0
        var isFirst = true

        for (i in 0..365) {
            val dateStr = sdf.format(cal.time)
            val isSunday = cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY && _restSunday.value
            val record = _dailyRecords.value[dateStr]
            val hasActivity = record != null && (record.questionsSolved > 0 || record.cardsFlipped > 0 || record.studyMinutes > 0)

            if (hasActivity) {
                streak++
            } else if (!(isSunday || (isFirst && dateStr == getTodayString()))) {
                break
            }
            isFirst = false
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }

    fun calculateXpAndLevel(): Pair<Int, Int> {
        val totalCorrect = _dailyRecords.value.values.sumOf { it.questionsCorrect }
        val completedUnits = _unitProgressMap.value.values.count { it.completed }
        val trialCount = _trialExams.value.size
        val pomodoros = _dailyRecords.value.values.sumOf { it.pomodoros }
        val streak = calculateStreak()
        val solvedMistakes = _mistakes.value.count { it.isFixed }

        val xp = totalCorrect * 2 + completedUnits * 30 + trialCount * 25 + pomodoros * 5 + streak * 3 + solvedMistakes * 10
        val level = xp / 150 + 1
        return Pair(xp, level)
    }

    // Mistakes Management
    fun addMistake(courseId: String, unit: String, text: String, reasonIndex: Int? = null) {
        val item = MistakeItem(
            id = System.currentTimeMillis().toString(),
            courseId = courseId,
            unit = unit,
            text = text,
            date = getTodayString(),
            reasonIndex = reasonIndex
        )
        val list = listOf(item) + _mistakes.value
        _mistakes.value = list
        saveList("mistakes", list)
        checkBadgeUnlocks()
    }

    fun toggleMistakeFixed(id: String) {
        val list = _mistakes.value.map {
            if (it.id == id) it.copy(isFixed = !it.isFixed, fixedDate = if (!it.isFixed) getTodayString() else "")
            else it
        }
        _mistakes.value = list
        saveList("mistakes", list)
        checkBadgeUnlocks()
    }

    fun setMistakeReason(id: String, reasonIndex: Int) {
        val list = _mistakes.value.map {
            if (it.id == id) it.copy(reasonIndex = reasonIndex)
            else it
        }
        _mistakes.value = list
        saveList("mistakes", list)
        checkBadgeUnlocks()
    }

    fun deleteMistake(id: String) {
        val list = _mistakes.value.filter { it.id != id }
        _mistakes.value = list
        saveList("mistakes", list)
    }

    // Todos
    fun addTodo(text: String, targetCourse: String? = null, targetUnit: String? = null, targetTab: String? = null, isAuto: Boolean = false) {
        val item = TodoItem(
            id = System.currentTimeMillis().toString(),
            text = text,
            targetCourse = targetCourse,
            targetUnit = targetUnit,
            targetTab = targetTab,
            isAuto = isAuto
        )
        val list = _todos.value + item
        _todos.value = list
        saveList("todos", list)
    }

    fun toggleTodo(id: String) {
        val list = _todos.value.map {
            if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
        }
        _todos.value = list
        saveList("todos", list)
    }

    fun deleteTodo(id: String) {
        val list = _todos.value.filter { it.id != id }
        _todos.value = list
        saveList("todos", list)
    }

    // Trial Exams
    fun addTrialExam(name: String, date: String, courseScores: Map<String, Pair<Int, Int>>, doctorNote: String = "") {
        // Calculate scores
        var rawScore = 0f
        var totalNet = 0f
        val bigCoeff = 4.348f
        val smallCoeff = 1.666f

        courseScores.forEach { (courseId, pair) ->
            val correct = pair.first
            val wrong = pair.second
            val net = max(0f, correct - (wrong / 3f))
            totalNet += net
            val coeff = if (courseId in listOf("tur", "mat", "fen")) bigCoeff else smallCoeff
            rawScore += net * coeff
        }

        val maxRaw = 4.348f * 60 + 1.666f * 30
        val lgsScore = 100f + (rawScore * 400f / maxRaw)

        val item = TrialExamItem(
            id = System.currentTimeMillis().toString(),
            name = name.ifBlank { "Deneme ${_trialExams.value.size + 1}" },
            date = date.ifBlank { getTodayString() },
            totalNet = (totalNet * 100).roundToInt() / 100f,
            score = (lgsScore * 10).roundToInt() / 10f,
            resultsByCourse = courseScores,
            doctorNote = doctorNote
        )
        val list = _trialExams.value + item
        _trialExams.value = list
        saveList("trial_exams", list)
        checkBadgeUnlocks()
    }

    fun deleteTrialExam(id: String) {
        val list = _trialExams.value.filter { it.id != id }
        _trialExams.value = list
        saveList("trial_exams", list)
    }

    // Daily Checkup
    fun recordDailyCheckup(record: DailyCheckupRecord) {
        val list = _dailyCheckups.value + record
        _dailyCheckups.value = list
        saveList("daily_checkups", list)
        checkBadgeUnlocks()
    }

    // Custom Questions
    fun addCustomQuestion(courseId: String, unit: String, question: String, answer: String, imageBase64: String?) {
        val item = CustomQuestionItem(
            id = System.currentTimeMillis().toString(),
            courseId = courseId,
            unit = unit,
            questionText = question,
            answerNote = answer,
            date = getTodayString(),
            imageBase64 = imageBase64
        )
        val list = listOf(item) + _customQuestions.value
        _customQuestions.value = list
        saveList("custom_questions", list)
        checkBadgeUnlocks()
    }

    fun toggleCustomQuestionSolved(id: String) {
        val list = _customQuestions.value.map {
            if (it.id == id) it.copy(isSolved = !it.isSolved) else it
        }
        _customQuestions.value = list
        saveList("custom_questions", list)
    }

    fun deleteCustomQuestion(id: String) {
        val list = _customQuestions.value.filter { it.id != id }
        _customQuestions.value = list
        saveList("custom_questions", list)
    }

    // SRS Spaced Repetition (Leitner 5 boxes)
    private val srsIntervals = listOf(1, 3, 7, 14, 30)

    fun answerSrsCard(cardIndex: Int, grade: Int) { // 0=Bilemedim, 1=Zor, 2=Bildim
        val current = _srsCards.value[cardIndex] ?: SrsCardState(cardIndex = cardIndex, box = 0, dueDate = getTodayString())
        val cal = Calendar.getInstance()
        val nextBox = when (grade) {
            0 -> 0
            1 -> current.box
            else -> min(4, current.box + 1)
        }
        cal.add(Calendar.DAY_OF_YEAR, if (grade == 0) 1 else srsIntervals[nextBox])
        val nextDue = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)

        val updated = current.copy(
            box = nextBox,
            dueDate = nextDue,
            reviewCount = current.reviewCount + 1
        )
        val newMap = _srsCards.value.toMutableMap()
        newMap[cardIndex] = updated
        _srsCards.value = newMap
        saveSrsMap(newMap)
        recordCardFlip()
        checkBadgeUnlocks()
    }

    // Teacher reports & Saved lessons
    fun addTeacherReport(report: TeacherLessonReport) {
        val list = listOf(report) + _teacherReports.value
        _teacherReports.value = list
        saveList("teacher_reports", list)
    }

    fun saveTeacherLesson(lesson: SavedLessonItem) {
        val list = listOf(lesson) + _savedLessons.value.filter { it.id != lesson.id }
        _savedLessons.value = list
        saveList("saved_lessons", list)
    }

    fun deleteSavedLesson(id: String) {
        val list = _savedLessons.value.filter { it.id != id }
        _savedLessons.value = list
        saveList("saved_lessons", list)
    }

    // Chief Physician Note Logic
    fun getChiefPhysicianNote(): String {
        val alarms = getTopicAlarms()
        if (alarms.isNotEmpty()) {
            val first = alarms.first()
            return "\"${first.second}\" konusunda alarm var! Önce konu anlatımını izle, ardından mini test ile küçük adımlarla toparlayalım."
        }
        val streak = calculateStreak()
        val todayRecord = _dailyRecords.value[getTodayString()] ?: DailyRecord(getTodayString())
        if (todayRecord.questionsSolved >= _dailyQuestionGoal.value) {
            return "Bugünkü soru hedefini tamamladın! Şimdi yanlış defterindeki eksikleri gözden geçirme zamanı."
        }
        if (streak >= 3) {
            return "$streak günlük serin sürüyor. Harika bir disiplin! Bugün de 30 dakika odaklanarak serini devam ettir."
        }
        val quotes = LgsCurriculum.motivationQuotes
        val dayIndex = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % quotes.size
        return quotes[dayIndex]
    }

    fun getTopicAlarms(): List<Pair<String, String>> { // List of (courseId, unit) with >= 3 wrong streak
        val alarms = mutableListOf<Pair<String, String>>()
        _unitProgressMap.value.forEach { (key, progress) ->
            if (progress.wrongStreak >= 3) {
                val parts = key.split("|")
                if (parts.size == 2) {
                    alarms.add(Pair(parts[0], parts[1]))
                }
            }
        }
        return alarms
    }

    // AI Coach auto schedule
    fun generateCoachSchedule(availableMinutes: Int): List<TodoItem> {
        val items = mutableListOf<TodoItem>()
        var remainingMin = availableMinutes

        // 1. Spaced Repetition Cards
        val dueCards = getDueSrsCardIndices()
        if (dueCards.isNotEmpty() && remainingMin >= 10) {
            val mins = min(20, max(5, (dueCards.size * 0.6f).toInt()))
            items.add(TodoItem(
                id = "coach_${System.currentTimeMillis()}_srs",
                text = "🗃️ Akıllı Tekrar: ${min(dueCards.size, 20)} kart tekrarı (~$mins dk)",
                targetTab = "srs",
                isAuto = true
            ))
            remainingMin -= mins
        }

        // 2. Mistakes
        val unfixedMistakes = _mistakes.value.filter { !it.isFixed }
        if (unfixedMistakes.isNotEmpty() && remainingMin >= 10) {
            items.add(TodoItem(
                id = "coach_${System.currentTimeMillis()}_mistake",
                text = "🕵️ Hata Kutusu: 5 yanlış soruyu tekrar çöz (~10 dk)",
                targetTab = "mistake",
                isAuto = true
            ))
            remainingMin -= 10
        }

        // 3. Alarms or Weak Topics
        val alarms = getTopicAlarms()
        for (alarm in alarms) {
            if (remainingMin < 12) break
            val course = LgsCurriculum.coursesMap[alarm.first]?.name ?: alarm.first
            items.add(TodoItem(
                id = "coach_${System.currentTimeMillis()}_alarm_${alarm.second}",
                text = "🚨 $course · ${alarm.second}: Konu tekrarı yap (~15 dk)",
                targetCourse = alarm.first,
                targetUnit = alarm.second,
                targetTab = "video",
                isAuto = true
            ))
            remainingMin -= 15
        }

        // 4. Trial exam reminder if none in past 7 days
        val hasRecentTrial = _trialExams.value.any { isWithin7Days(it.date) }
        if (!hasRecentTrial && remainingMin >= 40) {
            items.add(TodoItem(
                id = "coach_${System.currentTimeMillis()}_trial",
                text = "🩺 Bu hafta genel deneme sınavı çöz (~40 dk)",
                targetTab = "trial",
                isAuto = true
            ))
            remainingMin -= 40
        }

        // 5. Practice questions for remaining time
        if (remainingMin >= 15) {
            items.add(TodoItem(
                id = "coach_${System.currentTimeMillis()}_bank",
                text = "📑 Soru Bankasından serbest soru çöz (~$remainingMin dk)",
                targetTab = "bank",
                isAuto = true
            ))
        }

        if (items.isEmpty()) {
            items.add(TodoItem(
                id = "coach_${System.currentTimeMillis()}_pomo",
                text = "⏱️ 1 Pomodoro odaklanarak ders çalış (~25 dk)",
                targetTab = "pomodoro",
                isAuto = true
            ))
        }

        return items
    }

    fun getDueSrsCardIndices(): List<Int> {
        val today = getTodayString()
        return LgsCurriculum.flashcards.indices.filter { idx ->
            val cardState = _srsCards.value[idx]
            cardState == null || cardState.dueDate <= today
        }
    }

    private fun isWithin7Days(dateStr: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val d = sdf.parse(dateStr) ?: return false
            val diff = System.currentTimeMillis() - d.time
            diff < 7L * 24 * 60 * 60 * 1000
        } catch (e: Exception) {
            false
        }
    }

    private fun checkDailyPlanReset() {
        val today = getTodayString()
        val lastPlanDay = prefs.getString("last_plan_day", "")
        if (lastPlanDay != today) {
            // Keep user manual todos, generate new ones
            val manualTodos = _todos.value.filter { !it.isAuto }
            val autoTodos = generateCoachSchedule(60)
            val merged = manualTodos + autoTodos
            _todos.value = merged
            saveList("todos", merged)
            prefs.edit().putString("last_plan_day", today).apply()
        }
    }

    private fun checkBadgeUnlocks() {
        val today = getTodayString()
        val unlocked = _unlockedBadges.value.toMutableMap()
        var changed = false

        val completedUnits = _unitProgressMap.value.values.count { it.completed }
        val mathUnits = _unitProgressMap.value.filter { it.key.startsWith("mat|") && it.value.completed }.count()
        val fenUnits = _unitProgressMap.value.filter { it.key.startsWith("fen|") && it.value.completed }.count()
        val totalCorrect = _dailyRecords.value.values.sumOf { it.questionsCorrect }
        val totalCards = _dailyRecords.value.values.sumOf { it.cardsFlipped }
        val totalSolvedMistakes = _mistakes.value.count { it.isFixed }
        val totalPomodoro = _dailyRecords.value.values.sumOf { it.pomodoros }
        val streak = calculateStreak()
        val totalMin = _dailyRecords.value.values.sumOf { it.studyMinutes }
        val totalQuestions = _dailyRecords.value.values.sumOf { it.questionsSolved }
        val trialCount = _trialExams.value.size
        val customCount = _customQuestions.value.size
        val srsReviews = _srsCards.value.values.sumOf { it.reviewCount }

        fun grant(id: String) {
            if (!unlocked.containsKey(id)) {
                unlocked[id] = today
                changed = true
            }
        }

        if (completedUnits >= 1) grant("first_step")
        if (mathUnits >= 3) grant("math_master")
        if (fenUnits >= 3) grant("sci_explorer")
        if (totalCorrect >= 10) grant("q_hunter")
        if (totalCards >= 20) grant("card_pro")
        if (totalSolvedMistakes >= 3) grant("mistake_hunter")
        if (totalPomodoro >= 3) grant("focus_champ")
        if (completedUnits >= 12) grant("lgs_star")
        if (streak >= 3) grant("streak_3")
        if (streak >= 7) grant("streak_7")
        if (totalMin >= 300) grant("time_master")
        if (totalQuestions >= 50) grant("q_machine")
        if (trialCount >= 3) grant("trial_surgeon")
        if (customCount >= 3) grant("q_collector")
        if (srsReviews >= 30) grant("srs_master")

        if (changed) {
            _unlockedBadges.value = unlocked
            saveMap("unlocked_badges", unlocked)
        }
    }

    fun grantSecretBadge(badgeId: String) {
        val unlocked = _unlockedBadges.value.toMutableMap()
        if (!unlocked.containsKey(badgeId)) {
            unlocked[badgeId] = getTodayString()
            _unlockedBadges.value = unlocked
            saveMap("unlocked_badges", unlocked)
        }
    }

    // Persistence Helpers
    private inline fun <reified T> saveList(key: String, list: List<T>) {
        try {
            val type = Types.newParameterizedType(List::class.java, T::class.java)
            val adapter = moshi.adapter<List<T>>(type)
            prefs.edit().putString(key, adapter.toJson(list)).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private inline fun <reified T> loadList(key: String): List<T> {
        val json = prefs.getString(key, null) ?: return emptyList()
        return try {
            val type = Types.newParameterizedType(List::class.java, T::class.java)
            val adapter = moshi.adapter<List<T>>(type)
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveMap(key: String, map: Map<String, String>) {
        try {
            val type = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
            val adapter = moshi.adapter<Map<String, String>>(type)
            prefs.edit().putString(key, adapter.toJson(map)).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadMap(key: String): Map<String, String> {
        val json = prefs.getString(key, null) ?: return emptyMap()
        return try {
            val type = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
            val adapter = moshi.adapter<Map<String, String>>(type)
            adapter.fromJson(json) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun saveUnitProgressMap(map: Map<String, UnitProgress>) {
        try {
            val type = Types.newParameterizedType(Map::class.java, String::class.java, UnitProgress::class.java)
            val adapter = moshi.adapter<Map<String, UnitProgress>>(type)
            prefs.edit().putString("unit_progress_map", adapter.toJson(map)).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadUnitProgressMap(): Map<String, UnitProgress> {
        val json = prefs.getString("unit_progress_map", null) ?: return emptyMap()
        return try {
            val type = Types.newParameterizedType(Map::class.java, String::class.java, UnitProgress::class.java)
            val adapter = moshi.adapter<Map<String, UnitProgress>>(type)
            adapter.fromJson(json) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun saveDailyRecordsMap(map: Map<String, DailyRecord>) {
        try {
            val type = Types.newParameterizedType(Map::class.java, String::class.java, DailyRecord::class.java)
            val adapter = moshi.adapter<Map<String, DailyRecord>>(type)
            prefs.edit().putString("daily_records_map", adapter.toJson(map)).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadDailyRecordsMap(): Map<String, DailyRecord> {
        val json = prefs.getString("daily_records_map", null) ?: return emptyMap()
        return try {
            val type = Types.newParameterizedType(Map::class.java, String::class.java, DailyRecord::class.java)
            val adapter = moshi.adapter<Map<String, DailyRecord>>(type)
            adapter.fromJson(json) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun saveSrsMap(map: Map<Int, SrsCardState>) {
        try {
            // Map<String, SrsCardState> for clean JSON keys
            val strMap = map.mapKeys { it.key.toString() }
            val type = Types.newParameterizedType(Map::class.java, String::class.java, SrsCardState::class.java)
            val adapter = moshi.adapter<Map<String, SrsCardState>>(type)
            prefs.edit().putString("srs_cards_map", adapter.toJson(strMap)).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadSrsMap(): Map<Int, SrsCardState> {
        val json = prefs.getString("srs_cards_map", null) ?: return emptyMap()
        return try {
            val type = Types.newParameterizedType(Map::class.java, String::class.java, SrsCardState::class.java)
            val adapter = moshi.adapter<Map<String, SrsCardState>>(type)
            val strMap = adapter.fromJson(json) ?: emptyMap()
            strMap.mapKeys { it.key.toIntOrNull() ?: 0 }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun saveSubjectNetGoals(goals: Map<String, Float>) {
        try {
            val type = Types.newParameterizedType(Map::class.java, String::class.java, Float::class.javaObjectType)
            val adapter = moshi.adapter<Map<String, Float>>(type)
            prefs.edit().putString("subject_net_goals", adapter.toJson(goals)).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadSubjectNetGoals(): Map<String, Float> {
        val defaultGoals = mapOf(
            "tur" to 15f,
            "mat" to 15f,
            "fen" to 15f,
            "ink" to 8f,
            "din" to 8f,
            "ing" to 8f
        )
        val json = prefs.getString("subject_net_goals", null) ?: return defaultGoals
        return try {
            val type = Types.newParameterizedType(Map::class.java, String::class.java, Float::class.javaObjectType)
            val adapter = moshi.adapter<Map<String, Float>>(type)
            adapter.fromJson(json) ?: defaultGoals
        } catch (e: Exception) {
            defaultGoals
        }
    }
}
