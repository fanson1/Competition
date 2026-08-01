package com.example.competition.repository.local

import com.example.competition.data.UserManager
import com.example.competition.model.LeaderboardEntry
import com.example.competition.repository.LeaderboardRepository

class LocalLeaderboardRepository : LeaderboardRepository {

    override suspend fun getLeaderboard(level: Int?): List<LeaderboardEntry> {
        return UserManager.getLeaderboard(level)
    }

    override suspend fun updateLeaderboard(entry: LeaderboardEntry) {
        UserManager.updateLeaderboard(entry)
    }

    override suspend fun getLevelChallengers(level: Int): List<LeaderboardEntry> {
        return UserManager.getLevelChallengers(level)
    }
}
