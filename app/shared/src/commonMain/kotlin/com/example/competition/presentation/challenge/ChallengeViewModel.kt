package com.example.competition.presentation.challenge

import com.example.competition.data.UserManager
import com.example.competition.mvi.MviViewModel

class ChallengeViewModel : MviViewModel<ChallengeUiState, ChallengeIntent, ChallengeEffect>(ChallengeUiState()) {

    override fun onIntent(intent: ChallengeIntent) {
        when (intent) {
            is ChallengeIntent.SelectLevel -> {
                val challengers = UserManager.getLevelChallengers(intent.level)
                setState { it.copy(selectedLevel = intent.level, challengers = challengers) }
            }
            is ChallengeIntent.StartChallenge ->
                emit(ChallengeEffect.StartChallenge(state.value.selectedLevel, intent.target))
            ChallengeIntent.Back -> emit(ChallengeEffect.Back)
        }
    }
}
