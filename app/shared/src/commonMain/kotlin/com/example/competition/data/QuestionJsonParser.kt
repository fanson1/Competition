package com.example.competition.data

import com.example.competition.model.Category
import com.example.competition.model.Difficulty
import com.example.competition.model.Question

object QuestionJsonParser {

    fun parse(json: String): List<Question> {
        val questions = mutableListOf<Question>()
        val trimmed = json.trim()

        if (!trimmed.startsWith("[")) return emptyList()

        val content = trimmed.removeSurrounding("[", "]")
        val objects = splitJsonObjects(content)

        for (obj in objects) {
            val question = parseQuestionObject(obj)
            if (question != null) {
                questions.add(question)
            }
        }

        return questions
    }

    private fun splitJsonObjects(content: String): List<String> {
        val objects = mutableListOf<String>()
        var depth = 0
        var start = -1

        for (i in content.indices) {
            when (content[i]) {
                '{' -> {
                    if (depth == 0) start = i
                    depth++
                }
                '}' -> {
                    depth--
                    if (depth == 0 && start >= 0) {
                        objects.add(content.substring(start, i + 1))
                        start = -1
                    }
                }
            }
        }

        return objects
    }

    private fun parseQuestionObject(obj: String): Question? {
        return try {
            val id = extractInt(obj, "id")
            val text = extractString(obj, "text")
            val options = extractStringArray(obj, "options")
            val correctIndex = extractInt(obj, "correctIndex")
            val categoryStr = extractString(obj, "category") ?: "GENERAL"
            val difficultyStr = extractString(obj, "difficulty") ?: "EASY"
            val category = Category.valueOf(categoryStr)
            val difficulty = Difficulty.valueOf(difficultyStr)

            if (id != null && text != null && options.size == 4 && correctIndex != null) {
                Question(
                    id = id,
                    text = text,
                    options = options,
                    correctIndex = correctIndex,
                    category = category,
                    difficulty = difficulty
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun extractString(json: String, key: String): String? {
        val pattern = "\"$key\"\\s*:\\s*\""
        val startIndex = Regex(pattern).find(json)?.range?.last ?: return null
        val endIndex = findClosingQuote(json, startIndex + 1) ?: return null
        return unescapeJson(json.substring(startIndex + 1, endIndex))
    }

    private fun extractInt(json: String, key: String): Int? {
        val pattern = "\"$key\"\\s*:\\s*"
        val match = Regex(pattern).find(json) ?: return null
        val numberStart = match.range.last + 1
        val numberEnd = (numberStart until json.length).firstOrNull { !json[it].isDigit() && json[it] != '-' }
            ?: json.length
        val numberStr = json.substring(numberStart, numberEnd).trim()
        return numberStr.toIntOrNull()
    }

    private fun extractStringArray(json: String, key: String): List<String> {
        val pattern = "\"$key\"\\s*:\\s*\\["
        val match = Regex(pattern).find(json) ?: return emptyList()
        val arrayStart = match.range.last + 1
        val arrayEnd = findClosingBracket(json, arrayStart) ?: return emptyList()
        val arrayContent = json.substring(arrayStart, arrayEnd)

        val items = mutableListOf<String>()
        var i = 0
        while (i < arrayContent.length) {
            if (arrayContent[i] == '"') {
                val endQuote = findClosingQuote(arrayContent, i + 1)
                if (endQuote != null) {
                    items.add(unescapeJson(arrayContent.substring(i + 1, endQuote)))
                    i = endQuote + 1
                } else break
            } else {
                i++
            }
        }

        return items
    }

    private fun findClosingQuote(str: String, start: Int): Int? {
        var i = start
        while (i < str.length) {
            if (str[i] == '\\') {
                i += 2
                continue
            }
            if (str[i] == '"') return i
            i++
        }
        return null
    }

    private fun findClosingBracket(str: String, start: Int): Int? {
        var depth = 1
        for (i in start until str.length) {
            when (str[i]) {
                '[' -> depth++
                ']' -> {
                    depth--
                    if (depth == 0) return i
                }
            }
        }
        return null
    }

    private fun unescapeJson(s: String): String {
        val sb = StringBuilder(s.length)
        var i = 0
        while (i < s.length) {
            if (s[i] == '\\' && i + 1 < s.length) {
                when (s[i + 1]) {
                    '"' -> { sb.append('"'); i++ }
                    '\\' -> { sb.append('\\'); i++ }
                    'n' -> { sb.append('\n'); i++ }
                    'r' -> { sb.append('\r'); i++ }
                    't' -> { sb.append('\t'); i++ }
                    'b' -> { sb.append('\b'); i++ }
                    'f' -> { sb.append('\u000C'); i++ }
                    'u' -> {
                        if (i + 5 < s.length) {
                            val hex = s.substring(i + 2, i + 6)
                            val code = hex.toIntOrNull(16)
                            if (code != null) {
                                sb.append(code.toChar())
                                i += 5
                            } else {
                                sb.append(s[i])
                            }
                        } else {
                            sb.append(s[i])
                        }
                    }
                    else -> { sb.append(s[i]); sb.append(s[i + 1]); i++ }
                }
            } else {
                sb.append(s[i])
            }
            i++
        }
        return sb.toString()
    }
}
