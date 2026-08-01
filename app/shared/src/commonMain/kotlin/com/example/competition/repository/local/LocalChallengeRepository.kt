package com.example.competition.repository.local

import com.example.competition.data.UserManager
import com.example.competition.model.ChallengeRecord
import com.example.competition.model.ChallengeStats
import com.example.competition.repository.ChallengeRepository

class LocalChallengeRepository : ChallengeRepository {

    override suspend fun addChallenge(record: ChallengeRecord) {
        UserManager.addChallenge(record)
    }

    override suspend fun getChallenges(userId: String): List<ChallengeRecord> {
        return UserManager.getChallenges(userId)
    }

    override suspend fun getChallengesAsChallenger(userId: String): List<ChallengeRecord> {
        return UserManager.getChallengesAsChallenger(userId)
    }

    override suspend fun getChallengesAsTarget(userId: String): List<ChallengeRecord> {
        return UserManager.getChallengesAsTarget(userId)
    }

    override suspend fun getChallengeStats(userId: String): ChallengeStats {
        return UserManager.getChallengeStats(userId)
    }
}
