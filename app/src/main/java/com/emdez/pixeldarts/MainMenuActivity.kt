package com.emdez.pixeldarts

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.gridlayout.widget.GridLayout // Ważne: używamy wersji z AndroidX

class MainMenuActivity : AppCompatActivity() {

    // Używamy LinkedHashSet, aby zachować kolejność wybierania graczy
    private val selectedPlayers = mutableSetOf<String>()

    private var logoClickCount = 0
    private val handler = Handler()
    private var resetRunnable: Runnable? = null

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
        // Używamy bezpiecznego wywołania ?, na wypadek gdyby przycisku nie było w XML
        val btnNewPlayer = findViewById<Button>(R.id.btnNewPlayer)

        // Wywołujemy generowanie przycisków
        generatePlayersButtons()

        btnStart.setOnClickListener { showPlayerSelectionDialog() }

        // Obsługa przycisku dodawania gracza
        btnNewPlayer?.setOnClickListener {
            val intent = Intent(this, PlayersMenuActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showPlayerSelectionDialog() {
        // 1. Nadmuchujemy Twój layout XML dla dialogu
        val dialogView = layoutInflater.inflate(R.layout.dialog_player_selection, null)

        // 2. Szukamy elementów WEWNĄTRZ dialogView (to jest kluczowe!)
        val grid = dialogView.findViewById<androidx.gridlayout.widget.GridLayout>(R.id.gridPlayers)
        val btnStart = dialogView.findViewById<Button>(R.id.btnStartGame)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // 3. Generujemy przyciski graczy wewnątrz Grida z dialogu
        val db = PlayerDatabaseHelper(this)
        val players = db.getPlayers()

        selectedPlayers.clear() // Czyścimy listę przy każdym otwarciu

        for (name in players) {
            val btn = Button(this)
            btn.text = name

            // Parametry dla GridLayout
            val params = androidx.gridlayout.widget.GridLayout.LayoutParams()
            params.width = 0
            params.columnSpec = androidx.gridlayout.widget.GridLayout.spec(androidx.gridlayout.widget.GridLayout.UNDEFINED, 1f)
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
            grid.addView(btn)
        }

        // 4. Obsługa przycisku START wewnątrz dialogu
        btnStart.setOnClickListener {
            if (selectedPlayers.isEmpty()) {
                Toast.makeText(this, "Wybierz graczy!", Toast.LENGTH_SHORT).show()
            } else {
                dialog.dismiss()
                startGame() // wywołuje Twoją funkcję z Intentem
            }
        }

        dialog.show()
    }
    private fun generatePlayersButtons() {
        val grid = findViewById<GridLayout>(R.id.gridPlayers)

        // Zabezpieczenie: jeśli nie znajdzie Grida, nie idź dalej
        if (grid == null) return

        grid.removeAllViews()

        val db = PlayerDatabaseHelper(this)
        val players = db.getPlayers()

        for (name in players) {
            val btn = Button(this)
            btn.text = name

            // Ustawiamy parametry dla GridLayout z AndroidX
            val params = GridLayout.LayoutParams()
            params.width = 0
            // Specyfikujemy wagę (column weight), aby przyciski równo wypełniały wiersz
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            params.setMargins(8, 8, 8, 8)

            btn.layoutParams = params

            // Logika zaznaczania
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
        if (selectedPlayers.isEmpty()) {
            Toast.makeText(this, "Wybierz przynajmniej jednego gracza", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, MainActivity::class.java)
        // Przekazujemy listę jako ArrayList
        intent.putStringArrayListExtra("PLAYERS_LIST", ArrayList(selectedPlayers))
        startActivity(intent)
    }

}