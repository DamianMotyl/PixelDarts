package com.emdez.pixeldarts

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class PlayersMenuActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var db: PlayerDatabaseHelper
    private var players = mutableListOf<String>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_players_menu)

        listView = findViewById(R.id.listPlayers)
        val btnAdd = findViewById<Button>(R.id.btnAddPlayer)

        val btnReturnToMenu =findViewById<Button>(R.id.btnReturnToMenu)


        db = PlayerDatabaseHelper(this)

        loadPlayers()

        btnReturnToMenu.setOnClickListener {
            val intent = Intent(this, MainMenuActivity::class.java)
            startActivity(intent)
        }

        btnAdd.setOnClickListener {
            showAddPlayerDialog()
        }
        listView.setOnItemLongClickListener { _, _, position, _ ->

            val name = players[position]

            AlertDialog.Builder(this)
                .setTitle("Usuń gracza")
                .setMessage("Czy na pewno chcesz usunąć $name?")
                .setPositiveButton("Tak") { _, _ ->
                    db.deletePlayer(name)
                    loadPlayers() // 🔥 odśwież listę
                }
                .setNegativeButton("Nie", null)
                .show()

            true
        }

    }

    private fun loadPlayers() {
        players.clear()
        players.addAll(db.getPlayers())

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, players)
        listView.adapter = adapter
    }


    private fun showAddPlayerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_player_add, null)

        val input = dialogView.findViewById<EditText>(R.id.editPlayerName)
        val btnAdd = dialogView.findViewById<Button>(R.id.btnAddPlayer)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnAdd.setOnClickListener {
            val name = input.text.toString().trim()
            if (name.isNotEmpty()) {
                db.addPlayer(name)
                loadPlayers() // 🔥 odśwież listę
                dialog.dismiss()
            } else {
                input.error = "Podaj imię!"
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}