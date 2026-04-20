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

        // Sprawdzamy, czy w bazie są w ogóle jakieś zapisane gry
        if (history.isEmpty()) {
            Toast.makeText(this, "Brak historii do wyświetlenia", Toast.LENGTH_SHORT).show()

            // Pokazujemy komunikat na liście
            val emptyItems = listOf("Zagraj pierwszą grę, aby zobaczyć historię!")
            listView.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                emptyItems
            )
        } else {
            // Mapujemy dane na ładne teksty (usunięte emoji z początku dla lepszej czytelności na KitKat)
            val items = history.map {
                "Gra #${it.gameId} | ${it.date}\nWygrana: ${it.winner} | Tryb: ${it.mode}"
            }

            listView.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_2, // Zmiana na item_2 (dla dwóch linii tekstu) - lepiej wygląda na KitKat
                android.R.id.text1,
                items
            )
        }
    }
}