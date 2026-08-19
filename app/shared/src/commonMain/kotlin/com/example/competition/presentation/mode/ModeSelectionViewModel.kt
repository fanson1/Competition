package com.example.competition.presentation.mode

import com.example.competition.mvi.MviViewModel
import com.example.competition.repository.ModeRouter

class ModeSelectionViewModel : MviViewModel<ModeSelectionUiState, ModeSelectionIntent, ModeSelectionEffect>(ModeSelectionUiState()) {

    override fun onIntent(intent: ModeSelectionIntent) {
        when (intent) {
            is ModeSelectionIntent.SelectMode -> setState { it.copy(selectedMode = intent.mode) }
            ModeSelectionIntent.Confirm -> {
                val mode = state.value.selectedMode ?: return
                ModeRouter.switch(mode)
                ModeRouter.markModeSelected()
                emit(ModeSelectionEffect.Complete)
            }
        }
    }
}
