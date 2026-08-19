package com.example.competition.presentation.challengehero

import com.example.competition.data.UserManager
import com.example.competition.mvi.MviViewModel

class ChallengeHeroViewModel(
    private val userId: String,
) : MviViewModel<ChallengeHeroUiState, ChallengeHeroIntent, ChallengeHeroEffect>(ChallengeHeroUiState()) {

    override fun onIntent(intent: ChallengeHeroIntent) {
        when (intent) {
            is ChallengeHeroIntent.SelectTab -> {
                setState {
                    it.copy(
                        selectedTab = intent.tab,
                        challenges = loadChallenges(intent.tab)
                    )
                }
            }
            ChallengeHeroIntent.Back -> emit(ChallengeHeroEffect.Back)
        }
    }

    private fun loadChallenges(tab: Int): List<com.example.competition.model.ChallengeRecord> {
        return if (tab == 0) {
            UserManager.getChallengesAsChallenger(userId)
        } else {
            UserManager.getChallengesAsTarget(userId)
        }
    }
}
