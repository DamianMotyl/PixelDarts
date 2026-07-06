package com.emdez.pixeldarts

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class GamePlayerResult(
    val playerName: String,
    val score: Int,
    val place: Int
)



class PlayerDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "players.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE players (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT UNIQUE
            )
        """)

        db.execSQL("""
            CREATE TABLE games (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT,
                mode INTEGER
            )
        """)

        db.execSQL("""
            CREATE TABLE game_results (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                game_id INTEGER,
                player_name TEXT,
                score INTEGER,
                place INTEGER
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS players")
        db.execSQL("DROP TABLE IF EXISTS games")
        db.execSQL("DROP TABLE IF EXISTS game_results")
        onCreate(db)
    }

    fun addPlayer(name: String) {
        val db = writableDatabase
        val values = ContentValues()
        values.put("name", name)
        db.insert("players", null, values)
    }

    fun getPlayers(): List<String> {
        val list = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT name FROM players", null)
        while (cursor.moveToNext()) {
            list.add(cursor.getString(0))
        }
        cursor.close()
        return list
    }

    fun deletePlayer(name: String) {
        val db = writableDatabase
        db.delete("players", "name = ?", arrayOf(name))
    }

    fun addGame(mode: Int): Long {
        val db = writableDatabase
        val values = ContentValues()
        values.put("date", System.currentTimeMillis().toString())
        values.put("mode", mode)
        return db.insert("games", null, values)
    }

    fun getGamesPlayed(player: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM game_results WHERE player_name = ?",
            arrayOf(player)
        )
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        return count
    }

    fun getGameHistory(): List<GameHistory> {
        val list = mutableListOf<GameHistory>()
        val db = readableDatabase

        val gamesQuery = "SELECT id, date, mode FROM games ORDER BY id DESC"
        val gamesCursor = db.rawQuery(gamesQuery, null)
        val sdf = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())

        while (gamesCursor.moveToNext()) {
            val gameId = gamesCursor.getLong(0)
            val rawDate = gamesCursor.getString(1)
            val mode = gamesCursor.getInt(2)

            val formattedDate = try {
                if (rawDate != null) sdf.format(java.util.Date(rawDate.toLong())) else "Brak daty"
            } catch (e: Exception) { "Data nieznana" }

            val resultsList = mutableListOf<GamePlayerResult>()
            val resultsQuery = """
                SELECT player_name, score, place 
                FROM game_results 
                WHERE game_id = ? 
                ORDER BY place ASC, score ASC
            """
            val resultsCursor = db.rawQuery(resultsQuery, arrayOf(gameId.toString()))

            while (resultsCursor.moveToNext()) {
                resultsList.add(
                    GamePlayerResult(
                        playerName = resultsCursor.getString(0),
                        score = resultsCursor.getInt(1),
                        place = resultsCursor.getInt(2)
                    )
                )
            }
            resultsCursor.close()

            list.add(GameHistory(gameId, formattedDate, mode, resultsList))
        }
        gamesCursor.close()
        return list
    }

    fun addInitialPlayersToGame(gameId: Long, players: List<String>, startingScore: Int) {
        val db = writableDatabase
        for (player in players) {
            val values = ContentValues()
            values.put("game_id", gameId)
            values.put("player_name", player)
            values.put("score", startingScore)
            values.put("place", 0)
            db.insert("game_results", null, values)
        }
    }

    fun updatePlayerResult(gameId: Long, player: String, finalScore: Int, place: Int) {
        val db = writableDatabase
        val values = ContentValues()
        values.put("score", finalScore)
        values.put("place", place)
        db.update("game_results", values, "game_id = ? AND player_name = ?", arrayOf(gameId.toString(), player))
    }

    fun getWins(player: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM game_results WHERE player_name = ? AND place = 1",
            arrayOf(player)
        )
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        return count
    }

    fun getWinRate(player: String): Double {
        val played = getGamesPlayed(player)
        if (played == 0) return 0.0
        val wins = getWins(player)
        return (wins.toDouble() / played.toDouble()) * 100.0
    }

    fun getRanking(): List<PlayerRankingStats> {
        val list = mutableListOf<PlayerRankingStats>()
        val players = getPlayers()
        for (p in players) {
            list.add(PlayerRankingStats(p, getWins(p), getGamesPlayed(p), getWinRate(p)))
        }
        return list.sortedByDescending { it.wins }
    }
}

data class PlayerRankingStats(
    val name: String,
    val wins: Int,
    val played: Int,
    val winRate: Double
)