package com.emdez.pixeldarts

data class GameHistory(
    val gameId: Long,
    val date: String,
    val mode: Int,
    val winner: String
)
