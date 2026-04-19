package com.emdez.pixeldarts

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RankingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        val db = PlayerDatabaseHelper(this)

        // =====================
        // 🏆 RANKING
        // =====================
        val recycler = findViewById<RecyclerView>(R.id.rankingRecycler)
        recycler.layoutManager = LinearLayoutManager(this)

        val ranking = db.getRanking()
        recycler.adapter = RankingAdapter(ranking)

        // =====================
        // 📜 HISTORIA
        // =====================
        val listView = findViewById<ListView>(R.id.historyList)

        val history = db.getGameHistory()

        val items = history.map {
            "🎮 #${it.gameId} | ${it.date}\n🏆 ${it.winner} | ${it.mode}"
        }

        listView.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            items
        )
    }
}