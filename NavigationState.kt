package com.example.ui.navigation

sealed class Screen {
    data object Home : Screen()
    data object Courses : Screen()
    data class CourseDetail(val courseId: String) : Screen()
    data class UnitDetail(val courseId: String, val unitName: String, val initialTab: String = "kart") : Screen()
    data class VideoLesson(val courseId: String, val unitName: String, val topicName: String) : Screen()
    data class TeacherRoom(val initialTopic: String? = null, val initialReason: String? = null) : Screen()

    // Tools
    data object AiCoach : Screen()
    data object CurriculumMap : Screen()
    data object AcademicAnalytics : Screen()
    data object HocaReports : Screen()
    data object QuestionBank : Screen()
    data object MistakeDetective : Screen()
    data object TodoList : Screen()
    data object ScoreCalculator : Screen()
    data object TrialExams : Screen()
    data object DailyCheckup : Screen()
    data object LearningDna : Screen()
    data object GoalTree : Screen()
    data object TopicHealth : Screen()
    data object SrsSpacedRepetition : Screen()
    data object MyQuestions : Screen()
    data object CalendarHistory : Screen()
    data object WeeklyReport : Screen()
    data object Tactics : Screen()
    data object Badges : Screen()
    data object Pomodoro : Screen()
    data object Motivation : Screen()
}
