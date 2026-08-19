package com.example.competition.presentation.game

import com.example.competition.model.LeaderboardEntry
import com.example.competition.mvi.MviIntent

sealed interface GameIntent : MviIntent {
    data object LoadQuestions : GameIntent
    data class StartLevel(val level: Int) : GameIntent
    data object StartGame : GameIntent
    data class StartChallenge(val level: Int, val target: LeaderboardEntry) : GameIntent
    data class SelectAnswer(val answerIndex: Int) : GameIntent
    data object StartNextLevel : GameIntent
    data object RetryCurrentLevel : GameIntent
    data object ResetGame : GameIntent
    data object ClearChallengeTarget : GameIntent
    data object ReloadProgress : GameIntent
    data object SyncLeaderboardFromServer : GameIntent
}