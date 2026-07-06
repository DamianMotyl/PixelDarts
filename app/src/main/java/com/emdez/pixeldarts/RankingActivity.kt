package com.emdez.pixeldarts

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RankingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        val db = PlayerDatabaseHelper(this)

        // =====================
        // 🏆 RANKING (RecyclerView)
        // =====================
        val recycler = findViewById<RecyclerView>(R.id.rankingRecycler)
        recycler.layoutManager = LinearLayoutManager(this)

        val ranking = db.getRanking()
        recycler.adapter = RankingAdapter(ranking)

        // =====================
        // 📜 HISTORIA (ListView)
        // =====================
        val listView = findViewById<ListView>(R.id.historyList)
        val history = db.getGameHistory()

        if (history.isEmpty()) {
            Toast.makeText(this, "Brak historii do wyświetlenia", Toast.LENGTH_SHORT).show()

            val emptyItems = listOf("Zagraj pierwszą grę, aby zobaczyć historię!")
            listView.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                emptyItems
            )
        } else {
            // 🎯 MAPOWANIE: Pokazuje tylko lokatę i imię gracza, np: 🥇 Ania | 2. Bartek | 3. Czarek
            val items = history.map { game ->
                val playersLine = game.playersResults.joinToString(" | ") { result ->
                    if (result.place == 1) {
                        "🥇1. ${result.playerName}"
                    } else {
                        "${result.place}. ${result.playerName}"
                    }
                }

                "Gra #${game.gameId} — Tryb: ${game.mode} | ${game.date}\nWyniki: $playersLine"
            }

            listView.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                items
            )
        }
    }
}