package com.example.competition.presentation.challengehero

import com.example.competition.mvi.MviEffect
import com.example.competition.mvi.MviIntent

sealed interface ChallengeHeroIntent : MviIntent {
    data class SelectTab(val tab: Int) : ChallengeHeroIntent
    data object Back : ChallengeHeroIntent
}

sealed interface ChallengeHeroEffect : MviEffect {
    data object Back : ChallengeHeroEffect
}
