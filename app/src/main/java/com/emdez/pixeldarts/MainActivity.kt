package com.emdez.pixeldarts

import android.os.Bundle
import android.widget.*
import androidx.gridlayout.widget.GridLayout
import android.graphics.Color
import android.graphics.Typeface
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // NAJPIERW pobierz listę
        playersList = intent.getStringArrayListExtra("PLAYERS_LIST")

        // POTEM sprawdź i ustaw punkty
        if (!playersList.isNullOrEmpty()) {
            numPlayers = playersList!!.size
            setupInitialScores(numPlayers)
        } else {
            playersList = arrayListOf("Gracz 1")
            numPlayers = 1
            setupInitialScores(1)
        }

        setupGameButtons()
        generatePointsGrid()
        updateUI()
    }


    private fun setupInitialScores(count: Int) {
        scores = MutableList(count) { 301 }
        currentPlayerIndex = 0
        currentTurnThrows.clear()
    }

    private fun setupGameButtons() {
        findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btnConfirmTurn).setOnClickListener { confirmTurn() }
        findViewById<AppCompatButton>(R.id.btnMiss).setOnClickListener { addMisses() }
        findViewById<AppCompatButton>(R.id.btnUndo).setOnClickListener { undoLastThrow() }

        // Wybór liczby graczy
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
        val tvPlayer = findViewById<TextView>(R.id.tvCurrentPlayer)
        val tvTurn = findViewById<TextView>(R.id.tvTurnCurrent)
        val layoutAllScores = findViewById<LinearLayout>(R.id.layoutAllScores)

        tvPlayer.text = playersList?.get(currentPlayerIndex) ?: "Gracz"
        tvScore.text = scores[currentPlayerIndex].toString()

        val rzutyText = currentTurnThrows.joinToString(" | ")
        tvTurn.text = "Tura: $rzutyText (Suma: ${currentTurnThrows.sum()})"

        // Odświeżamy górny pasek wszystkich wyników
        layoutAllScores.removeAllViews()
        playersList?.forEachIndexed { i, name ->
            val tv = TextView(this)
            // Wyświetlamy imię i punkty
            tv.text = " $name: ${scores[i]} "
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
        if (scores[currentPlayerIndex] - sum >= 0) {
            scores[currentPlayerIndex] -= sum
        } else {
            Toast.makeText(this, "Fura! (Bust)", Toast.LENGTH_SHORT).show()
        }

        if (scores[currentPlayerIndex] == 0) {
            Toast.makeText(this, "BRAWO! WYGRANA!", Toast.LENGTH_LONG).show()
        }

        currentPlayerIndex = (currentPlayerIndex + 1) % numPlayers
        currentTurnThrows.clear()
        updateUI()
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
}