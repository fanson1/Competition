package com.example.competition.ui

import androidx.compose.runtime.Composable
import com.example.competition.model.Category
import com.example.competition.model.Difficulty
import competition.app.shared.generated.resources.Res
import competition.app.shared.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun levelTitle(level: Int): String = stringResource(levelTitleRes(level))

@Composable
fun levelSubtitle(level: Int): String = stringResource(levelSubtitleRes(level))

fun levelTitleRes(level: Int): StringResource = when (level) {
    1 -> Res.string.level_1_title
    2 -> Res.string.level_2_title
    3 -> Res.string.level_3_title
    4 -> Res.string.level_4_title
    5 -> Res.string.level_5_title
    6 -> Res.string.level_6_title
    7 -> Res.string.level_7_title
    8 -> Res.string.level_8_title
    9 -> Res.string.level_9_title
    10 -> Res.string.level_10_title
    else -> Res.string.level_10_title
}

fun levelSubtitleRes(level: Int): StringResource = when (level) {
    1 -> Res.string.level_1_subtitle
    2 -> Res.string.level_2_subtitle
    3 -> Res.string.level_3_subtitle
    4 -> Res.string.level_4_subtitle
    5 -> Res.string.level_5_subtitle
    6 -> Res.string.level_6_subtitle
    7 -> Res.string.level_7_subtitle
    8 -> Res.string.level_8_subtitle
    9 -> Res.string.level_9_subtitle
    10 -> Res.string.level_10_subtitle
    else -> Res.string.level_10_subtitle
}

@Composable
fun difficultyLabel(difficulty: Difficulty): String = stringResource(difficultyRes(difficulty))

fun difficultyRes(difficulty: Difficulty): StringResource = when (difficulty) {
    Difficulty.EASY -> Res.string.difficulty_easy
    Difficulty.MEDIUM -> Res.string.difficulty_medium
    Difficulty.HARD -> Res.string.difficulty_hard
}

@Composable
fun categoryLabel(category: Category): String = stringResource(categoryRes(category))

fun categoryRes(category: Category): StringResource = when (category) {
    Category.SCIENCE -> Res.string.category_science
    Category.HISTORY -> Res.string.category_history
    Category.GEOGRAPHY -> Res.string.category_geography
    Category.CULTURE -> Res.string.category_culture
    Category.GENERAL -> Res.string.category_general
}

@Composable
fun resolvePlayerTitle(title: com.example.competition.model.PlayerTitle?): String = when (title) {
    com.example.competition.model.PlayerTitle.RAMPANT -> stringResource(Res.string.player_title_rampant)
    com.example.competition.model.PlayerTitle.EXCELLENT -> stringResource(Res.string.player_title_excellent)
    com.example.competition.model.PlayerTitle.NOTABLE -> stringResource(Res.string.player_title_notable)
    com.example.competition.model.PlayerTitle.SHOWING_POTENTIAL -> stringResource(Res.string.player_title_showing_potential)
    com.example.competition.model.PlayerTitle.COURAGEOUS -> stringResource(Res.string.player_title_courageous)
    com.example.competition.model.PlayerTitle.GRANDMASTER -> stringResource(Res.string.player_title_grandmaster)
    null -> ""
}
