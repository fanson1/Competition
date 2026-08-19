package com.example.competition.util

/**
 * Reusable codec for serializing `Map<Int, Int>` (e.g. per-level best scores)
 * into the compact `"1:200,2:450"` string format used by the DB + server DTOs.
 */
object LevelMapCodec {

    /** Encodes [scores] as `"level:value,..."`, empty string when empty. */
    fun encode(scores: Map<Int, Int>): String {
        if (scores.isEmpty()) return ""
        return scores.entries
            .joinToString(",") { "${it.key}:${it.value}" }
    }

    /** Encodes each (level, value) pair from [entries]. */
    fun encode(entries: Iterable<Pair<Int, Int>>): String =
        encode(entries.toMap())

    /** Parses a `"1:10,2:150"` string back into a map. */
    fun decode(encoded: String): Map<Int, Int> {
        if (encoded.isBlank()) return emptyMap()
        val map = linkedMapOf<Int, Int>()
        encoded.split(",").forEach { pair ->
            val parts = pair.trim().split(":")
            if (parts.size == 2) {
                val key = parts[0].toIntOrNull()
                val value = parts[1].toIntOrNull()
                if (key != null && value != null) {
                    map[key] = value
                }
            }
        }
        return map
    }
}