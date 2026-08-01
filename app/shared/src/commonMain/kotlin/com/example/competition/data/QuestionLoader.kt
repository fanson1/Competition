package com.example.competition.data

import competition.app.shared.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

object QuestionLoader {

    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadAndInitialize(): Int {
        return try {
            val bytes = Res.readBytes("files/questions.json")
            val jsonString = bytes.decodeToString()
            val questions = QuestionJsonParser.parse(jsonString)
            QuestionRepository.initialize(questions)
            questions.size
        } catch (e: Exception) {
            println("Failed to load questions: ${e.message}")
            0
        }
    }
}
