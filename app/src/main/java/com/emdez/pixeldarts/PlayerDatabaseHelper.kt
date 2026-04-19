package com.emdez.pixeldarts

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class PlayerDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "players.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {

        // GRACZE
        db.execSQL("""
            CREATE TABLE players (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT UNIQUE
            )
        """)

        // GRA (jedna sesja)
        db.execSQL("""
            CREATE TABLE games (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT,
                mode INTEGER
            )
        """)

        // WYNIKI GRY
        db.execSQL("""
            CREATE TABLE game_results (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                game_id INTEGER,
                player_name TEXT,
                score INTEGER,
                is_winner INTEGER
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS players")
        db.execSQL("DROP TABLE IF EXISTS games")
        db.execSQL("DROP TABLE IF EXISTS game_results")
        onCreate(db)
    }

    // =====================
    // PLAYERS
    // =====================
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

    // =====================
    // GAME START
    // =====================
    fun addGame(mode: Int): Long {
        val db = writableDatabase
        val values = ContentValues()

        values.put("date", System.currentTimeMillis().toString())
        values.put("mode", mode)

        return db.insert("games", null, values)
    }

    // =====================
    // GAME RESULTS
    // =====================
    fun addGameResult(gameId: Long, player: String, score: Int, isWinner: Boolean) {
        val db = writableDatabase
        val values = ContentValues()

        values.put("game_id", gameId)
        values.put("player_name", player)
        values.put("score", score)
        values.put("is_winner", if (isWinner) 1 else 0)

        db.insert("game_results", null, values)
    }

    // =====================
    // STATYSTYKI
    // =====================

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

        val query = """
        SELECT g.id, g.date, g.mode, r.player_name
        FROM games g
        JOIN game_results r ON g.id = r.game_id
        WHERE r.is_winner = 1
        ORDER BY g.id DESC
    """

        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {
            list.add(
                GameHistory(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getInt(2),
                    cursor.getString(3)
                )
            )
        }

        cursor.close()
        return list
    }

    fun getWins(player: String): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM game_results WHERE player_name = ? AND is_winner = 1",
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

    // =====================
    // RANKING
    // =====================
    fun getRanking(): List<PlayerStats> {
        val list = mutableListOf<PlayerStats>()
        val players = getPlayers()

        for (p in players) {
            val wins = getWins(p)
            val played = getGamesPlayed(p)
            val winrate = getWinRate(p)

            list.add(PlayerStats(p, wins, played, winrate))
        }

        return list.sortedByDescending { it.wins }
    }
}

// MODEL RANKINGU
data class PlayerStats(
    val name: String,
    val wins: Int,
    val played: Int,
    val winRate: Double
)