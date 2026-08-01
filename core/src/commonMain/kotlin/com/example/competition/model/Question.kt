package com.example.competition.model

enum class Difficulty {
    EASY, MEDIUM, HARD
}

enum class Category {
    SCIENCE, HISTORY, GEOGRAPHY, CULTURE, GENERAL
}

data class Question(
    val id: Int,
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val category: Category,
    val difficulty: Difficulty
) {
    val timeLimitSeconds: Int
        get() = when (difficulty) {
            Difficulty.EASY -> 10
            Difficulty.MEDIUM -> 15
            Difficulty.HARD -> 20
        }
}

data class QuestionWithShuffledOptions(
    val question: Question,
    val shuffledOptions: List<String>,
    val shuffledCorrectIndex: Int
) {
    val timeLimitSeconds: Int get() = question.timeLimitSeconds
    val text: String get() = question.text
    val category: Category get() = question.category
    val difficulty: Difficulty get() = question.difficulty
}
