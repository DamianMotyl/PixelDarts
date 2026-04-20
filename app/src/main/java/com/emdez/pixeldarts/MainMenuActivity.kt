package com.emdez.pixeldarts

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.gridlayout.widget.GridLayout

class MainMenuActivity : AppCompatActivity() {

    // Używamy LinkedHashSet, aby zachować kolejność wybierania graczy
    private val selectedPlayers = mutableSetOf<String>()

    private var logoClickCount = 0
    private val handler = Handler(Looper.getMainLooper())
    private var resetRunnable: Runnable? = null

    // Domyślny tryb gry
    private var selectedMode = 301

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        val logo = findViewById<ImageView>(R.id.ivLogo)

        logo.setOnClickListener {
            logoClickCount++

            // usuń poprzedni reset
            resetRunnable?.let { handler.removeCallbacks(it) }

            // nowy reset po 2 sekundach
            resetRunnable = Runnable {
                logoClickCount = 0
            }

            handler.postDelayed(resetRunnable!!, 2000)

            if (logoClickCount == 5) {
                logoClickCount = 0
                handler.removeCallbacks(resetRunnable!!)
                startActivity(Intent(this, RankingActivity::class.java))
            }
        }

        val btnStart = findViewById<Button>(R.id.btnStartGame)
        val btnNewPlayer = findViewById<Button>(R.id.btnNewPlayer)

        // Wywołujemy generowanie przycisków w głównym menu (jeśli takie masz)
        generatePlayersButtons()

        btnStart.setOnClickListener { showPlayerSelectionDialog() }

        // Obsługa przycisku dodawania gracza
        btnNewPlayer?.setOnClickListener {
            val intent = Intent(this, PlayersMenuActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showPlayerSelectionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_player_selection, null)

        val grid = dialogView.findViewById<GridLayout>(R.id.gridPlayers)
        val btnStart = dialogView.findViewById<Button>(R.id.btnStartGame)

        // Przyciski trybu gry (z Twojego layoutu dialog_player_selection.xml)
        val btn301 = dialogView.findViewById<Button>(R.id.btn301)
        val btn501 = dialogView.findViewById<Button>(R.id.btn501)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Na starcie ustawiamy domyślny wygląd przycisków trybu
        selectedMode = 301
        btn301?.setBackgroundResource(R.drawable.btn_selected)
        btn501?.setBackgroundResource(R.drawable.btn_blue)

        // --- OBSŁUGA WYBORU TRYBU ---
        btn301?.setOnClickListener {
            selectedMode = 301
            btn301.setBackgroundResource(R.drawable.btn_selected)
            btn501?.setBackgroundResource(R.drawable.btn_blue)
        }

        btn501?.setOnClickListener {
            selectedMode = 501
            btn501.setBackgroundResource(R.drawable.btn_selected)
            btn301?.setBackgroundResource(R.drawable.btn_blue)
        }

        val db = PlayerDatabaseHelper(this)
        val players = db.getPlayers()

        selectedPlayers.clear() // Czyścimy listę przy każdym otwarciu dialogu

        // --- GENEROWANIE PRZYCISKÓW GRACZY ---
        for (name in players) {
            val btn = Button(this)
            btn.text = name

            val params = GridLayout.LayoutParams()
            params.width = 0
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            params.setMargins(8, 8, 8, 8)
            btn.layoutParams = params

            btn.setBackgroundResource(R.drawable.btn_blue)

            btn.setOnClickListener {
                if (selectedPlayers.contains(name)) {
                    selectedPlayers.remove(name)
                    btn.setBackgroundResource(R.drawable.btn_blue)
                } else {
                    if (selectedPlayers.size >= 4) {
                        Toast.makeText(this, "Max 4 graczy", Toast.LENGTH_SHORT).show()
                    } else {
                        selectedPlayers.add(name)
                        btn.setBackgroundResource(R.drawable.btn_selected)
                    }
                }
            }
            grid?.addView(btn)
        }

        // --- OBSŁUGA STARTU ---
        btnStart.setOnClickListener {
            if (selectedPlayers.isEmpty()) {
                Toast.makeText(this, "Wybierz graczy!", Toast.LENGTH_SHORT).show()
            } else {
                dialog.dismiss()
                startGame()
            }
        }

        dialog.show()
    }

    private fun generatePlayersButtons() {
        val grid = findViewById<GridLayout>(R.id.gridPlayers)
        if (grid == null) return

        grid.removeAllViews()

        val db = PlayerDatabaseHelper(this)
        val players = db.getPlayers()

        for (name in players) {
            val btn = Button(this)
            btn.text = name

            val params = GridLayout.LayoutParams()
            params.width = 0
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            params.setMargins(8, 8, 8, 8)
            btn.layoutParams = params

            if (selectedPlayers.contains(name)) {
                btn.setBackgroundResource(R.drawable.btn_selected)
            } else {
                btn.setBackgroundResource(R.drawable.btn_blue)
            }

            btn.setOnClickListener {
                if (selectedPlayers.contains(name)) {
                    selectedPlayers.remove(name)
                    btn.setBackgroundResource(R.drawable.btn_blue)
                } else {
                    if (selectedPlayers.size >= 4) {
                        Toast.makeText(this, "Maksymalnie 4 graczy", Toast.LENGTH_SHORT).show()
                    } else {
                        selectedPlayers.add(name)
                        btn.setBackgroundResource(R.drawable.btn_selected)
                    }
                }
            }
            grid.addView(btn)
        }
    }

    private fun startGame() {
        val intent = Intent(this, MainActivity::class.java)

        // 1. Przekazujemy listę graczy
        intent.putStringArrayListExtra("PLAYERS_LIST", ArrayList(selectedPlayers))

        // 2. Przekazujemy wybrany tryb gry (301 lub 501)
        intent.putExtra("GAME_MODE", selectedMode)

        startActivity(intent)
    }
}