package com.example.competition.data

import com.example.competition.model.Category
import com.example.competition.model.Difficulty
import com.example.competition.model.Question
import com.example.competition.model.QuestionWithShuffledOptions

data class LevelConfig(
    val level: Int,
    val easyRatio: Float,
    val mediumRatio: Float,
    val hardRatio: Float,
    val questionsPerLevel: Int = 10
) {
    val easyCount: Int get() = (questionsPerLevel * easyRatio).toInt()
    val mediumCount: Int get() = (questionsPerLevel * mediumRatio).toInt()
    val hardCount: Int get() = questionsPerLevel - easyCount - mediumCount
}

object QuestionRepository {

    private var questions: List<Question> = emptyList()
    private val usedQuestionIds: MutableSet<Int> = mutableSetOf()

    fun initialize(allQuestions: List<Question>) {
        questions = allQuestions
    }

    fun getQuestionsForLevel(
        config: LevelConfig,
        excludeIds: Set<Int> = emptySet()
    ): List<QuestionWithShuffledOptions> {
        val allExcludeIds = usedQuestionIds + excludeIds

        fun pickFromDifficulty(difficulty: Difficulty, count: Int): List<Question> {
            if (count <= 0) return emptyList()
            val fresh = questions.filter { it.difficulty == difficulty && it.id !in allExcludeIds }
            if (fresh.size >= count) {
                return fresh.shuffled().take(count)
            }
            val freshTaken = fresh.shuffled()
            val freshIds = freshTaken.map { it.id }.toSet()
            val rest = questions.filter { it.difficulty == difficulty && it.id !in freshIds }
                .shuffled().take(count - freshTaken.size)
            return (freshTaken + rest).shuffled()
        }

        val easy = pickFromDifficulty(Difficulty.EASY, config.easyCount)
        val medium = pickFromDifficulty(Difficulty.MEDIUM, config.mediumCount)
        val hard = pickFromDifficulty(Difficulty.HARD, config.hardCount)

        val selectedQuestions = (easy + medium + hard).shuffled()
            .take(config.questionsPerLevel)

        return selectedQuestions.map { shuffleOptions(it) }
    }

    fun markQuestionsAsUsed(ids: Collection<Int>) {
        usedQuestionIds.addAll(ids)
    }

    fun clearUsedQuestions() {
        usedQuestionIds.clear()
    }

    private fun shuffleOptions(question: Question): QuestionWithShuffledOptions {
        val optionIndices = question.options.indices.toMutableList()
        optionIndices.shuffle()

        val shuffledOptions = optionIndices.map { question.options[it] }
        val newCorrectIndex = optionIndices.indexOf(question.correctIndex)

        return QuestionWithShuffledOptions(
            question = question,
            shuffledOptions = shuffledOptions,
            shuffledCorrectIndex = newCorrectIndex
        )
    }

    fun getTotalCount(): Int = questions.size

    fun getQuestionsByCategory(category: Category, count: Int = 10): List<Question> {
        return questions.filter { it.category == category }.shuffled().take(count)
    }
}
