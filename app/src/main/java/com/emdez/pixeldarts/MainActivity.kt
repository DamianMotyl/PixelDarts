package com.emdez.pixeldarts

import android.os.Bundle
import android.widget.*
import androidx.gridlayout.widget.GridLayout
import android.graphics.Color
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class MainActivity : AppCompatActivity() {

    private var scores = mutableListOf(301) // Zmienione na 301

    private var playersList: ArrayList<String>? = null
    private var currentPlayerIndex = 0
    private var numPlayers = 1
    private var currentTurnThrows = mutableListOf<Int>()
    private val MAX_THROWS = 3
    private var activeMultiplier = 1

    private var gameMode = 301

    private var lastTurnMode = false

    private val db by lazy { PlayerDatabaseHelper(this) }
    private var currentGameId: Long = -1

    private val winners = mutableSetOf<Int>() // indeksy graczy, którzy wygrali

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // NAJPIERW pobierz listę
        playersList = intent.getStringArrayListExtra("PLAYERS_LIST")
        gameMode = intent.getIntExtra("GAME_MODE", 301)

        // POTEM sprawdź i ustaw punkty
        if (!playersList.isNullOrEmpty()) {
            numPlayers = playersList!!.size
            setupInitialScores(numPlayers)
        } else {
            playersList = arrayListOf("Gracz 1")
            numPlayers = 1
            setupInitialScores(1)
        }
        currentGameId = db.addGame(gameMode)
        db.addInitialPlayersToGame(currentGameId, playersList!!, gameMode)

        setupGameButtons()
        generatePointsGrid()
        updateUI()
    }


    private fun setupInitialScores(count: Int) {
        scores = MutableList(count){gameMode}
        currentPlayerIndex = 0
        currentTurnThrows.clear()
    }

    private fun setupGameButtons() {
        findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btnConfirmTurn).setOnClickListener { confirmTurn() }
        findViewById<AppCompatButton>(R.id.btnMiss).setOnClickListener { addMisses() }
        findViewById<AppCompatButton>(R.id.btnUndo).setOnClickListener { undoLastThrow() }

        findViewById<Button>(R.id.btnEndGame).setOnClickListener {
            showEndGameDialog() }
        findViewById<Button>(R.id.btnAddAnotherPlayer).setOnClickListener {TODO() }

        val btnAddPlayer = findViewById<Button>(R.id.btnAddAnotherPlayer)
        btnAddPlayer.isEnabled = false
        btnAddPlayer.alpha = 0.5f

        // Obsługa przycisków Double i Triple
        val btnD = findViewById<Button>(R.id.btnDouble)
        val btnT = findViewById<Button>(R.id.btnTriple)

        btnD.setOnClickListener {
            if (activeMultiplier == 2) {
                // WYŁĄCZ
                activeMultiplier = 1
                btnD.setBackgroundResource(R.drawable.btn_blue)
            } else {
                // WŁĄCZ x2
                activeMultiplier = 2
                btnD.setBackgroundResource(R.drawable.btn_yellow)
                btnT.setBackgroundResource(R.drawable.btn_blue)
            }
        }

        btnT.setOnClickListener {
            if (activeMultiplier == 3) {
                // WYŁĄCZ
                activeMultiplier = 1
                btnT.setBackgroundResource(R.drawable.btn_blue)
            } else {
                // WŁĄCZ x3
                activeMultiplier = 3
                btnT.setBackgroundResource(R.drawable.btn_yellow)
                btnD.setBackgroundResource(R.drawable.btn_blue)
            }
        }
    }


    private fun showEndGameDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Zakończyć grę?")
            .setMessage("Gracz ${playersList?.get(currentPlayerIndex)} prowadzi.\nNa pewno zakończyć?")
            .setPositiveButton("Tak") { _, _ ->
                resetGame()
            }
            .setNegativeButton("Nie", null)
            .show()
    }
    private fun addPoints(baseValue: Int) {
        if (currentTurnThrows.size < MAX_THROWS) {
            val finalValue = baseValue * activeMultiplier

            // Walidacja: 25 i 50 nie mają potrójnego mnożnika (x3)
            if (activeMultiplier == 3 && baseValue > 20) {
                Toast.makeText(this, "Tylko pola 1-20 mają Triple!", Toast.LENGTH_SHORT).show()
                resetMultipliers()
                return
            }

            currentTurnThrows.add(finalValue)
            resetMultipliers()
            updateUI()
        } else {
            Toast.makeText(this, "Zatwierdź turę!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun undoLastThrow() {
        if (currentTurnThrows.isNotEmpty()) {
            currentTurnThrows.removeAt(currentTurnThrows.size - 1)
            updateUI()
        } else {
            Toast.makeText(this, "Brak rzutów do cofnięcia", Toast.LENGTH_SHORT).show()
        }
    }
    private fun resetMultipliers() {
        activeMultiplier = 1
        findViewById<Button>(R.id.btnDouble).setBackgroundResource(R.drawable.btn_blue)
        findViewById<Button>(R.id.btnTriple).setBackgroundResource(R.drawable.btn_blue)
    }

    private fun updateUI() {
        val tvScore = findViewById<TextView>(R.id.tvScore)
        val tvProjectedScore = findViewById<TextView>(R.id.tvProjectedScore)
        val tvPlayer = findViewById<TextView>(R.id.tvCurrentPlayer)
        val tvTurn = findViewById<TextView>(R.id.tvTurnCurrent)
        val layoutAllScores = findViewById<LinearLayout>(R.id.layoutAllScores)

        tvPlayer.text = playersList?.get(currentPlayerIndex) ?: "Gracz"

        // --- NOWOŚĆ: Obliczanie przewidywanego wyniku w nawiasie ---
        val currentBaseScore = scores[currentPlayerIndex]
        val sumOfThrows = currentTurnThrows.sum()
        val projectedScore = currentBaseScore - sumOfThrows

        if (sumOfThrows > 0) {
            if (projectedScore < 0) {
                tvScore.text = "$currentBaseScore (FURA!)"
                tvScore.setTextColor(Color.RED) // Czerwony kolor przy furze
            } else {
                tvScore.text = "$currentBaseScore"
                if (currentBaseScore <=160) {
                    tvProjectedScore.text = "($projectedScore)"
                }
                tvScore.setTextColor(Color.BLACK) // Wracamy do czarnego
            }
        } else {
            // Jeśli jeszcze nic nie rzucił w tej turze, pokazujemy samą główną liczbę
            tvScore.text = currentBaseScore.toString()
            tvScore.setTextColor(Color.BLACK)
        }
        // -----------------------------------------------------------

        val rzutyText = currentTurnThrows.joinToString(" | ")
        tvTurn.text = "Tura: $rzutyText (Suma: $sumOfThrows)"

        // Odświeżamy górny pasek wszystkich wyników
        layoutAllScores.removeAllViews()
        playersList?.forEachIndexed { i, name ->
            val tv = TextView(this)
            val isWinner = winners.contains(i)

            tv.text = if (isWinner) {
                "🏆 $name: WYGRANY"
            } else {
                "$name: ${scores[i]}"
            }
            tv.textSize = 18f
            tv.setPadding(10, 5, 10, 5)
            if (i == currentPlayerIndex) {
                tv.setTextColor(Color.parseColor("#1976D2")) // Niebieski aktywny
                tv.setTypeface(null, Typeface.BOLD)
            }
            layoutAllScores.addView(tv)
        }

        findViewById<Button>(R.id.btnConfirmTurn).isEnabled = currentTurnThrows.size == MAX_THROWS
    }

    private fun generatePointsGrid() {
        val grid = findViewById<GridLayout>(R.id.gridPoints)
        grid.removeAllViews()
        val pointsValues = (0..20).toList() + listOf(25, 50)

        for (value in pointsValues) {
            val b = Button(this)
            b.text = value.toString()
            b.textSize = 28f
            b.setBackgroundResource(R.drawable.btn_round)
            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = 130
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            params.setMargins(4, 4, 4, 4)
            b.layoutParams = params
            b.setOnClickListener { addPoints(value) }
            grid.addView(b)
        }
    }

    private fun confirmTurn() {
        val sum = currentTurnThrows.sum()
        val currentPlayerName = playersList?.get(currentPlayerIndex) ?: "Gracz"

        // 1. Logika odejmowania punktów
        if (scores[currentPlayerIndex] - sum >= 0) {
            scores[currentPlayerIndex] -= sum
        } else {
            Toast.makeText(this, "Fura! (Bust)", Toast.LENGTH_SHORT).show()
        }

        // 2. Sprawdzenie wygranej w tej turze
        if (scores[currentPlayerIndex] == 0 && !winners.contains(currentPlayerIndex)) {
            winners.add(currentPlayerIndex)
            // ZAPISZ ZWYCIĘZCĘ OD RAZU
            db.updatePlayerResult(currentGameId, currentPlayerName, 0, true)

            // Pokaż dialog wygranej
            showWinnerDialogAnimated(currentPlayerName)

            // Tryb ostatniej szansy dla 2 graczy
            if (numPlayers == 2 && winners.size == 1) {
                lastTurnMode = true
                Toast.makeText(this, "Ostatnia szansa dla rywala!", Toast.LENGTH_LONG).show()
            }
        }

        // 3. Obsługa końca gry
        val isGameOver = checkIsGameOver()

        if (isGameOver) {
            endGameSession()
        } else {
            // Kontynuuj grę - wyczyść rzuty, zmień gracza, odśwież UI
            currentTurnThrows.clear()
            moveToNextPlayer()
            updateUI()
        }
    }

    // Pomocnicza funkcja dla czystszego kodu
    private fun checkIsGameOver(): Boolean {
        // Wszyscy wygrali
        if (winners.size == numPlayers) return true

        // Tryb 2 graczy, ostatnia szansa.
        // Jeśli to była tura rywala (tego, który nie wygrał jako pierwszy) i właśnie ją skończył, to koniec gry.
        if (numPlayers == 2 && lastTurnMode) {
            // Skoro lastTurnMode jest true, to znaczy, że ktoś wygrał.
            // Jeśli aktualny gracz to ten sam, który wygrał, znaczy to, że rywal już skończył (albo spudłował, albo też zremisował).
            // Z uwagi na sposób działania kolejki, sprawdźmy: czy kolejny to rywal, który już rzucił w ostatniej szansie?

            // Uproszczona logika dla 2 graczy:
            // Skoro rzucał i nie trafił 0 (bo inaczej winners.size == 2), to kończymy grę.
            if (winners.size == 1 && !winners.contains(currentPlayerIndex)) {
                return true
            }
        }

        // Dla gier z 3 lub 4 graczami, możesz chcieć grać aż do ostatniego przegranego.
        // Ustalmy, że gra się kończy, gdy zostanie tylko jeden, który nie wygrał (czyli wszyscy inni już skończyli)
        if (numPlayers > 2 && winners.size == numPlayers - 1) return true

        return false
    }
    private fun showWinnerDialogAnimated(playerName: String) {
        val view = layoutInflater.inflate(R.layout.dialog_winner, null)
        val tvName = view.findViewById<TextView>(R.id.tvName)
        tvName.text = playerName

        // Używamy starszego konstruktora AlertDialog dla lepszej zgodności
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .create()

        dialog.show()

        val root = view.findViewById<LinearLayout>(R.id.winnerRoot)

        // Animacja kompatybilna z API 19
        root.alpha = 0f
        root.scaleX = 0.7f
        root.scaleY = 0.7f

        root.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(300)
            .setListener(null) // Reset listenera dla bezpieczeństwa na starszych systemach
            .start()

        // 🔥 Zamykanie po 3 sekundach (Handler jest bezpieczny dla KitKat)
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            } catch (e: Exception) {
            }
        }, 3000)
    }

    private fun moveToNextPlayer() {
        // Jeśli wszyscy wygrali lub rywal oddał ostatni rzut w trybie 2 graczy
        if (winners.size == numPlayers || (numPlayers == 2 && lastTurnMode && winners.size == 1 && !winners.contains(currentPlayerIndex))) {
            return
        }

        val startIndex = currentPlayerIndex
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % numPlayers
            // Pętla kręci się tak długo, aż znajdzie gracza, którego nie ma na liście zwycięzców
        } while (winners.contains(currentPlayerIndex) && currentPlayerIndex != startIndex)
    }

    private fun resetGame() {
        // 1. Czyścimy dane lokalne (opcjonalnie, bo finish() i tak zamknie aktywność)
        currentPlayerIndex = 0
        currentTurnThrows.clear()
        scores.clear()

        // 2. Zamykamy MainActivity i wracamy do MainMenuActivity
        finish()
    }

    private fun addMisses() {
        currentTurnThrows.clear() // 👈 usuwa stare rzuty

        repeat(MAX_THROWS) {
            currentTurnThrows.add(0)
        }

        updateUI()
    }

    private fun endGameSession() {
        Toast.makeText(this, "KONIEC GRY", Toast.LENGTH_LONG).show()

        // Zapisz ostateczne wyniki przegranych
        for (i in 0 until numPlayers) {
            if (!winners.contains(i)) {
                val name = playersList?.get(i) ?: "Gracz"
                // --- ZMIANA ---
                db.updatePlayerResult(currentGameId, name, scores[i], false)
            }
        }

        currentTurnThrows.clear()
        updateUI()

        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 2000)
    }
}