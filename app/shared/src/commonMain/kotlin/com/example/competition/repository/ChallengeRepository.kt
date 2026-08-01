package com.example.competition.repository

import com.example.competition.model.ChallengeRecord
import com.example.competition.model.ChallengeStats

interface ChallengeRepository {
    suspend fun addChallenge(record: ChallengeRecord)
    suspend fun getChallenges(userId: String): List<ChallengeRecord>
    suspend fun getChallengesAsChallenger(userId: String): List<ChallengeRecord>
    suspend fun getChallengesAsTarget(userId: String): List<ChallengeRecord>
    suspend fun getChallengeStats(userId: String): ChallengeStats
}
