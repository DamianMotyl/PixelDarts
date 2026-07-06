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

    private var scores = mutableListOf(301)
    private var playersList: ArrayList<String>? = null
    private var currentPlayerIndex = 0
    private var numPlayers = 1
    private var currentTurnThrows = mutableListOf<Int>()
    private val MAX_THROWS = 3
    private var activeMultiplier = 1
    private var gameMode = 301

    private val db by lazy { PlayerDatabaseHelper(this) }
    private var currentGameId: Long = -1

    private val winners = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playersList = intent.getStringArrayListExtra("PLAYERS_LIST")
        gameMode = intent.getIntExtra("GAME_MODE", 301)

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
        scores = MutableList(count) { gameMode }
        currentPlayerIndex = 0
        currentTurnThrows.clear()
        winners.clear()
    }

    private fun setupGameButtons() {
        findViewById<AppCompatButton>(R.id.btnConfirmTurn).setOnClickListener { confirmTurn() }
        findViewById<AppCompatButton>(R.id.btnMiss).setOnClickListener { addMisses() }
        findViewById<AppCompatButton>(R.id.btnUndo).setOnClickListener { undoLastThrow() }

        // 🎯 Odszukiwanie przycisku ulokowanego pod siatką w XML
        findViewById<Button>(R.id.btnStartingSet)?.setOnClickListener { addStartingSet() }

        findViewById<Button>(R.id.btnEndGame).setOnClickListener { showEndGameDialog() }

        val btnAddPlayer = findViewById<Button>(R.id.btnAddAnotherPlayer)
        btnAddPlayer.isEnabled = false
        btnAddPlayer.alpha = 0.5f

        val btnD = findViewById<Button>(R.id.btnDouble)
        val btnT = findViewById<Button>(R.id.btnTriple)

        btnD.setOnClickListener {
            if (activeMultiplier == 2) {
                activeMultiplier = 1
                btnD.setBackgroundResource(R.drawable.btn_blue)
            } else {
                activeMultiplier = 2
                btnD.setBackgroundResource(R.drawable.btn_yellow)
                btnT.setBackgroundResource(R.drawable.btn_blue)
            }
        }

        btnT.setOnClickListener {
            if (activeMultiplier == 3) {
                activeMultiplier = 1
                btnT.setBackgroundResource(R.drawable.btn_blue)
            } else {
                activeMultiplier = 3
                btnT.setBackgroundResource(R.drawable.btn_yellow)
                btnD.setBackgroundResource(R.drawable.btn_blue)
            }
        }
    }

    private fun addStartingSet() {
        if (currentTurnThrows.isEmpty()) {
            resetMultipliers()
            currentTurnThrows.add(25)
            currentTurnThrows.add(5)
            currentTurnThrows.add(1)
            updateUI()
        } else {
            Toast.makeText(this, "Zestaw startowy wymaga pustej tury!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEndGameDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Zakończyć grę?")
            .setMessage("Czy na pewno chcesz przerwać aktualną rozgrywkę i wrócić do menu?")
            .setPositiveButton("Tak") { _, _ -> resetGame() }
            .setNegativeButton("Nie", null)
            .show()
    }

    private fun addPoints(baseValue: Int) {
        if (currentTurnThrows.size < MAX_THROWS) {
            val finalValue = baseValue * activeMultiplier

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
        tvProjectedScore.text = ""

        val currentBaseScore = scores[currentPlayerIndex]
        val sumOfThrows = currentTurnThrows.sum()
        val projectedScore = currentBaseScore - sumOfThrows

        if (sumOfThrows > 0) {
            if (projectedScore < 0) {
                tvScore.text = "$currentBaseScore (FURA!)"
                tvScore.setTextColor(Color.RED)
            } else {
                tvScore.text = currentBaseScore.toString()
                if (currentBaseScore <= 160) {
                    tvProjectedScore.text = "($projectedScore)"
                }
                tvScore.setTextColor(Color.BLACK)
            }
        } else {
            tvScore.text = currentBaseScore.toString()
            tvScore.setTextColor(Color.BLACK)
        }

        val rzutyText = currentTurnThrows.joinToString(" | ")
        tvTurn.text = "Tura: $rzutyText (Suma: $sumOfThrows)"

        layoutAllScores.removeAllViews()
        playersList?.forEachIndexed { i, name ->
            val tv = TextView(this)
            val placeInGame = winners.indexOf(i) + 1

            tv.text = if (placeInGame > 0) {
                "🏅 $name: Miejsce $placeInGame"
            } else {
                "$name: ${scores[i]}"
            }
            tv.textSize = 18f
            tv.setPadding(10, 5, 10, 5)

            if (i == currentPlayerIndex) {
                tv.setTextColor(Color.parseColor("#1976D2"))
                tv.setTypeface(null, Typeface.BOLD)
            } else {
                tv.setTextColor(Color.BLACK)
            }
            layoutAllScores.addView(tv)
        }

        findViewById<Button>(R.id.btnConfirmTurn).isEnabled = currentTurnThrows.isNotEmpty()
    }

    private fun generatePointsGrid() {
        val grid = findViewById<GridLayout>(R.id.gridPoints)
        grid.removeAllViews()
        val pointsValues = (0..20).toList() + listOf(25, 50)

        // Generowanie wyłącznie czystych kafelków punktowych (0-50)
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

        if (scores[currentPlayerIndex] - sum >= 0) {
            scores[currentPlayerIndex] -= sum

            if (scores[currentPlayerIndex] == 0 && !winners.contains(currentPlayerIndex)) {
                winners.add(currentPlayerIndex)
                val assignedPlace = winners.size

                db.updatePlayerResult(currentGameId, currentPlayerName, 0, assignedPlace)

                if (assignedPlace == 1) {
                    showWinnerDialogAnimated(currentPlayerName)
                } else {
                    Toast.makeText(this, "$currentPlayerName zajmuje $assignedPlace miejsce!", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Fura! (Bust)", Toast.LENGTH_SHORT).show()
        }

        if (checkIsGameOver()) {
            endGameSession()
        } else {
            currentTurnThrows.clear()
            moveToNextPlayer()
            updateUI()
        }
    }

    private fun checkIsGameOver(): Boolean {
        if (winners.size == numPlayers) return true
        if (numPlayers > 1 && winners.size == numPlayers - 1) return true
        return false
    }

    private fun showWinnerDialogAnimated(playerName: String) {
        val view = layoutInflater.inflate(R.layout.dialog_winner, null)
        val tvName = view.findViewById<TextView>(R.id.tvName)
        tvName.text = playerName

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .create()

        dialog.show()

        val root = view.findViewById<LinearLayout>(R.id.winnerRoot)
        root.alpha = 0f
        root.scaleX = 0.7f
        root.scaleY = 0.7f

        root.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(300)
            .setListener(null)
            .start()

        Handler(Looper.getMainLooper()).postDelayed({
            try {
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            } catch (e: Exception) {}
        }, 3000)
    }

    private fun moveToNextPlayer() {
        if (checkIsGameOver()) return

        val startIndex = currentPlayerIndex
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % numPlayers
        } while (winners.contains(currentPlayerIndex) && currentPlayerIndex != startIndex)
    }

    private fun resetGame() {
        currentPlayerIndex = 0
        currentTurnThrows.clear()
        scores.clear()
        finish()
    }

    private fun addMisses() {
        currentTurnThrows.clear()
        repeat(MAX_THROWS) {
            currentTurnThrows.add(0)
        }
        updateUI()
    }

    private fun endGameSession() {
        Toast.makeText(this, "KONIEC GRY", Toast.LENGTH_LONG).show()

        for (i in 0 until numPlayers) {
            if (!winners.contains(i)) {
                val name = playersList?.get(i) ?: "Gracz"
                db.updatePlayerResult(currentGameId, name, scores[i], numPlayers)
            }
        }

        currentTurnThrows.clear()
        updateUI()

        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 2000)
    }
}