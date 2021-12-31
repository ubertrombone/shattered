package com.example.shattered.model

data class LevelMeta(
    val level: Int = 1,
    val complexity: String = "easy",
    val lives: Boolean = false,
    val timer: Boolean = false
)