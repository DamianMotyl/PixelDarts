package com.emdez.pixeldarts

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class PlayerDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "players.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE players (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS players")
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
}