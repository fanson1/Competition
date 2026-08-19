package com.example.competition.presentation.game

import com.example.competition.mvi.MviEffect

sealed interface GameEffect : MviEffect {
    data object GameOver : GameEffect
    data object LevelComplete : GameEffect
}