package com.example.competition.data

object LevelConfigs {
    val levels = listOf(
        LevelConfig(1, 0.80f, 0.20f, 0.00f),
        LevelConfig(2, 0.70f, 0.25f, 0.05f),
        LevelConfig(3, 0.60f, 0.30f, 0.10f),
        LevelConfig(4, 0.50f, 0.30f, 0.20f),
        LevelConfig(5, 0.40f, 0.30f, 0.30f),
        LevelConfig(6, 0.30f, 0.30f, 0.40f),
        LevelConfig(7, 0.20f, 0.30f, 0.50f),
        LevelConfig(8, 0.10f, 0.25f, 0.65f),
        LevelConfig(9, 0.05f, 0.20f, 0.75f),
        LevelConfig(10, 0.00f, 0.15f, 0.85f),
    )

    fun getConfig(level: Int): LevelConfig {
        return levels.firstOrNull { it.level == level }
            ?: levels.last()
    }
}
