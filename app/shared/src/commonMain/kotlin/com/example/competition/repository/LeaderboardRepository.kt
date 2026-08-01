package com.example.competition.repository

import com.example.competition.model.LeaderboardEntry

interface LeaderboardRepository {
    suspend fun getLeaderboard(level: Int? = null): List<LeaderboardEntry>
    suspend fun updateLeaderboard(entry: LeaderboardEntry)
    suspend fun getLevelChallengers(level: Int): List<LeaderboardEntry>
}
