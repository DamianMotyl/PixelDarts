package com.emdez.pixeldarts

import android.os.Bundle
import android.widget.*
import android.view.ViewGroup
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var scores = mutableListOf(301) // Zmienione na 301
    private var currentPlayerIndex = 0
    private var numPlayers = 1
    private var currentTurnThrows = mutableListOf<Int>()
    private val MAX_THROWS = 3

    // Nowa zmienna dla mnożnika
    private var activeMultiplier = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupGameButtons()
        generatePointsGrid()
        updateUI()
    }

    private fun setupGameButtons() {
        findViewById<Button>(R.id.btnConfirmTurn).setOnClickListener { confirmTurn() }
        findViewById<Button>(R.id.btnMiss).setOnClickListener { addPoints(0) }

        // Wybór liczby graczy
        findViewById<Button>(R.id.btnP1).setOnClickListener { resetGame(1) }
        findViewById<Button>(R.id.btnP2).setOnClickListener { resetGame(2) }
        findViewById<Button>(R.id.btnP3).setOnClickListener { resetGame(3) }
        findViewById<Button>(R.id.btnP4).setOnClickListener { resetGame(4) }

        // Obsługa przycisków Double i Triple
        val btnD = findViewById<Button>(R.id.btnDouble)
        val btnT = findViewById<Button>(R.id.btnTriple)

        btnD.setOnClickListener {
            activeMultiplier = 2
            btnD.setBackgroundColor(Color.YELLOW)
            btnT.setBackgroundColor(Color.LTGRAY)
        }
        btnT.setOnClickListener {
            activeMultiplier = 3
            btnT.setBackgroundColor(Color.YELLOW)
            btnD.setBackgroundColor(Color.LTGRAY)
        }
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

    private fun resetMultipliers() {
        activeMultiplier = 1
        findViewById<Button>(R.id.btnDouble).setBackgroundColor(Color.LTGRAY)
        findViewById<Button>(R.id.btnTriple).setBackgroundColor(Color.LTGRAY)
    }

    private fun updateUI() {
        val tvScore = findViewById<TextView>(R.id.tvScore)
        val tvPlayer = findViewById<TextView>(R.id.tvCurrentPlayer)
        val tvTurn = findViewById<TextView>(R.id.tvTurnCurrent)
        val layoutAllScores = findViewById<LinearLayout>(R.id.layoutAllScores)

        tvPlayer.text = "Gracz ${currentPlayerIndex + 1}"
        tvScore.text = scores[currentPlayerIndex].toString()

        val rzutyText = currentTurnThrows.joinToString(" | ")
        tvTurn.text = "Tura: $rzutyText (Suma: ${currentTurnThrows.sum()})"

        layoutAllScores.removeAllViews()
        for (i in 0 until numPlayers) {
            val tv = TextView(this)
            tv.text = " P${i+1}: ${scores[i]} "
            tv.textSize = 18f
            tv.setPadding(15, 10, 15, 10)
            if (i == currentPlayerIndex) {
                tv.setTextColor(Color.RED)
                tv.setTypeface(null, Typeface.BOLD)
            }
            layoutAllScores.addView(tv)
        }

        findViewById<Button>(R.id.btnConfirmTurn).isEnabled = currentTurnThrows.isNotEmpty()
    }

    private fun generatePointsGrid() {
        val grid = findViewById<GridLayout>(R.id.gridPoints)
        grid.removeAllViews()
        val pointsValues = (0..20).toList() + listOf(25, 50)

        for (value in pointsValues) {
            val b = Button(this)
            b.text = value.toString()
            val params = GridLayout.LayoutParams()
            params.width = 0
            params.height = 130
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            params.setMargins(2, 2, 2, 2)
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

    private fun resetGame(p: Int) {
        numPlayers = p
        currentPlayerIndex = 0
        scores = MutableList(p) { 301 }
        currentTurnThrows.clear()
        resetMultipliers()
        updateUI()
    }
}