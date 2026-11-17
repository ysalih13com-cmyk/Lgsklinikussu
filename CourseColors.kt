package com.example.ui.theme

import androidx.compose.ui.graphics.Color

/** Ders kimliğine (mat, fen, tur, ink, din, ing) göre vurgu rengi. */
fun courseAccent(courseId: String): Color = when (courseId) {
    "mat" -> CourseMath
    "fen" -> CourseScience
    "tur" -> CourseTurkish
    "ink" -> CourseHistory
    "din" -> CourseReligion
    "ing" -> CourseEnglish
    else -> PrimaryLight
}
