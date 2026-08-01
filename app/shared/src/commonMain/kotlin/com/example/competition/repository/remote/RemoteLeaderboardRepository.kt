package com.example.competition.repository.remote

import com.example.competition.api.ApiClient
import com.example.competition.model.LeaderboardEntry
import com.example.competition.repository.LeaderboardRepository

class RemoteLeaderboardRepository : LeaderboardRepository {
    private var cache: List<LeaderboardEntry> = emptyList()

    override suspend fun getLeaderboard(level: Int?): List<LeaderboardEntry> {
        val result = ApiClient.getLeaderboard(level)
        return result.map { dtos ->
            val entries = dtos.map { toEntry(it) }
            val aggregated = if (level != null) {
                entries
            } else {
                entries.groupBy { it.userId }.map { (_, userEntries) ->
                    userEntries.maxBy { it.score }.copy(
                        score = userEntries.sumOf { it.score },
                        level = userEntries.maxOf { it.level },
                        correctCount = userEntries.sumOf { it.correctCount }
                    )
                }.sortedByDescending { it.score }
            }
            cache = aggregated
            aggregated
        }.getOrElse {
            if (level != null) cache.filter { it.level == level } else cache
        }
    }

    override suspend fun updateLeaderboard(entry: LeaderboardEntry) {
        ApiClient.syncLeaderboardEntry(
            com.example.competition.api.dto.LeaderboardEntryDto(
                userId = entry.userId,
                username = entry.username,
                nickname = entry.nickname,
                avatarEmoji = entry.avatarEmoji,
                score = entry.score,
                level = entry.level,
                correctCount = entry.correctCount,
                timestamp = entry.timestamp
            )
        )
    }

    override suspend fun getLevelChallengers(level: Int): List<LeaderboardEntry> {
        return getLeaderboard(level)
    }

    private fun toEntry(dto: com.example.competition.api.dto.LeaderboardEntryDto): LeaderboardEntry {
        return LeaderboardEntry(
            userId = dto.userId,
            username = dto.username,
            nickname = dto.nickname,
            avatarEmoji = dto.avatarEmoji,
            score = dto.score,
            level = dto.level,
            correctCount = dto.correctCount,
            timestamp = dto.timestamp
        )
    }
}
