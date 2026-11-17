package com.example.data

data class Course(
    val id: String,
    val name: String,
    val icon: String,
    val units: List<String>
)

data class FlashCard(
    val courseId: String,
    val unit: String,
    val front: String,
    val back: String
)

data class QuizQuestion(
    val courseId: String,
    val unit: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

sealed class BoardStep {
    data class Header(val title: String) : BoardStep()
    data class Paragraph(val text: String) : BoardStep()
    data class Equation(val formula: String) : BoardStep()
    data class PairEq(val a: Int, val b: Int) : BoardStep()
    data class ItemList(val items: List<String>) : BoardStep()
}

data class MiniQuizItem(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class VideoSection(
    val key: String,
    val title: String,
    val script: List<String>,
    val board: List<List<BoardStep>>,
    val examples: List<Pair<String, String>> = emptyList(),
    val mistakes: List<Pair<String, String>> = emptyList(),
    val quiz: List<MiniQuizItem> = emptyList(),
    val summary: List<String> = emptyList()
)

data class VideoLesson(
    val name: String,
    val sections: List<VideoSection>
)

data class MistakeItem(
    val id: String,
    val courseId: String,
    val unit: String = "",
    val text: String,
    val date: String,
    val isFixed: Boolean = false,
    val fixedDate: String = "",
    val reasonIndex: Int? = null // 0..5
)

data class TodoItem(
    val id: String,
    val text: String,
    val isCompleted: Boolean = false,
    val isAuto: Boolean = false,
    val targetCourse: String? = null,
    val targetUnit: String? = null,
    val targetTab: String? = null // "video", "kart", "test"
)

data class TrialExamItem(
    val id: String,
    val name: String,
    val date: String,
    val totalNet: Float,
    val score: Float,
    val resultsByCourse: Map<String, Pair<Int, Int>> = emptyMap(), // courseId -> (Doğru, Yanlış)
    val doctorNote: String = ""
)

data class DailyRecord(
    val date: String,
    val questionsSolved: Int = 0,
    val questionsCorrect: Int = 0,
    val cardsFlipped: Int = 0,
    val studyMinutes: Int = 0,
    val pomodoros: Int = 0
)

data class StudyHistoryItem(
    val id: String,
    val date: String,
    val courseId: String,
    val minutes: Int,
    val note: String = ""
)

data class CustomQuestionItem(
    val id: String,
    val courseId: String,
    val unit: String = "",
    val questionText: String = "",
    val answerNote: String = "",
    val date: String,
    val imageBase64: String? = null,
    val isSolved: Boolean = false
)

data class DailyCheckupRecord(
    val date: String,
    val correctCount: Int,
    val totalCount: Int,
    val weakCourse: String,
    val hardTopic: String,
    val statusText: String, // STABİL, DİKKAT, KRİTİK
    val durationSec: Int
)

data class TeacherLessonReport(
    val id: String,
    val topic: String,
    val date: String,
    val minutes: Int,
    val messagesCount: Int,
    val retryCount: Int,
    val strugglePoint: String? = null
)

data class SrsCardState(
    val cardIndex: Int,
    val box: Int = 0, // 0..4 (1, 3, 7, 14, 30 days)
    val dueDate: String,
    val reviewCount: Int = 0
)

data class UnitProgress(
    val completed: Boolean = false,
    val completedDate: String? = null,
    val correctAnswers: Int = 0,
    val totalAnswers: Int = 0,
    val recentAnswers: List<Int> = emptyList(), // 1=right, 0=wrong
    val wrongStreak: Int = 0
)

data class BadgeItem(
    val id: String,
    val emoji: String,
    val title: String,
    val desc: String,
    val isSecret: Boolean = false
)

data class WhiteboardElement(
    val id: String,
    val byUser: Boolean,
    val type: String, // "stroke", "line", "arrow", "circle", "rect", "write", "underline"
    val points: List<Pair<Float, Float>> = emptyList(),
    val text: String? = null,
    val x1: Float = 0f,
    val y1: Float = 0f,
    val x2: Float = 0f,
    val y2: Float = 0f,
    val radius: Float = 0f,
    val width: Float = 0f,
    val height: Float = 0f,
    val colorHex: Long = 0xFF0284C7,
    val strokeWidth: Float = 4f,
    val fontSize: Float = 22f
)

data class TeacherChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class SavedLessonItem(
    val id: String,
    val name: String,
    val dateString: String,
    val messageCount: Int,
    val elements: List<WhiteboardElement> = emptyList(),
    val messages: List<TeacherChatMessage> = emptyList()
)
