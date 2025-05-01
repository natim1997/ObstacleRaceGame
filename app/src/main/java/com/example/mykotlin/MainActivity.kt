package com.example.mykotlin

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.gridlayout.widget.GridLayout
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var gridBoard: GridLayout
    private lateinit var imgCar: ImageView
    private lateinit var btnLeft: AppCompatImageButton
    private lateinit var btnRight: AppCompatImageButton
    private lateinit var heart1: ImageView
    private lateinit var heart2: ImageView
    private lateinit var heart3: ImageView
    private lateinit var tvScore: TextView
    private lateinit var btnStart: Button

    private val numRows = 8
    private val numCols = 3
    private var gridCells = Array(numRows) { BooleanArray(numCols) }
    private var score = 0
    private var lives = 3
    private var currentLane = 1

    // Countdown to next spawn: 0 = spawn this tick
    private var spawnCounter = 0
    private val emptyRowsBetween = 2 // number of empty rows between spawns

    private val handler = Handler(Looper.getMainLooper())
    private var gameRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind views
        gridBoard = findViewById(R.id.grid_board)
        imgCar    = findViewById(R.id.img_car)
        btnLeft   = findViewById(R.id.btn_left)
        btnRight  = findViewById(R.id.btn_right)
        heart1    = findViewById(R.id.heart1)
        heart2    = findViewById(R.id.heart2)
        heart3    = findViewById(R.id.heart3)
        tvScore   = findViewById(R.id.tv_score)
        btnStart  = findViewById(R.id.btn_start)

        // Load arrow vectors
        btnLeft.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_left_arrow))
        btnRight.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_right_arrow))

        // Padding for smaller stones
        val padding = (10 * resources.displayMetrics.density).toInt()

        // Build grid cells
        for (r in 0 until numRows) {
            for (c in 0 until numCols) {
                val cell = AppCompatImageView(this).apply {
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = 0
                        height = 0
                        columnSpec = GridLayout.spec(c, 1f)
                        rowSpec    = GridLayout.spec(r, 1f)
                    }
                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                    adjustViewBounds = true
                    setPadding(padding, padding, padding, padding)
                    alpha = 0f
                }
                gridBoard.addView(cell)
            }
        }

        // Position car after layout
        gridBoard.post { updateCarPosition() }

        btnLeft.setOnClickListener {
            if (currentLane > 0) currentLane--
            updateCarPosition()
        }
        btnRight.setOnClickListener {
            if (currentLane < numCols - 1) currentLane++
            updateCarPosition()
        }

        btnStart.setOnClickListener {
            btnStart.visibility = View.GONE
            resetGame()
            gameRunning = true
            tick()
        }
    }

    private fun updateCarPosition() {
        val laneWidth = gridBoard.width / numCols
        imgCar.x = (currentLane * laneWidth + laneWidth / 2f - imgCar.width / 2f)
    }

    private fun resetGame() {
        gridCells.forEach { it.fill(false) }
        score = 0
        lives = 3
        currentLane = 1
        spawnCounter = 0
        tvScore.text = getString(R.string.score_template, score)
        heart1.visibility = View.VISIBLE
        heart2.visibility = View.VISIBLE
        heart3.visibility = View.VISIBLE
        refreshGridUI()
        updateCarPosition()
    }

    private fun tick() {
        if (!gameRunning) return

        // Compute rowHeight and keep constant speed
        val rowHeight = gridBoard.height / numRows
        val duration = 400L // constant 400ms per row

        gridBoard.animate()
            .translationY(rowHeight.toFloat())
            .setDuration(duration)
            .setInterpolator(LinearInterpolator())
            .withEndAction {
                gridBoard.translationY = 0f
                // Shift rows down
                for (r in numRows - 1 downTo 1) {
                    gridCells[r] = gridCells[r - 1].copyOf()
                }
                gridCells[0] = BooleanArray(numCols)

                // Spawn if counter == 0
                if (spawnCounter == 0) {
                    val lanes = (0 until numCols).shuffled()
                    val count = if (Random.nextFloat() < 0.25f) 2 else 1
                    repeat(count) { gridCells[0][lanes[it]] = true }
                    spawnCounter = emptyRowsBetween
                } else {
                    spawnCounter--
                }

                // Bottom row collision/score
                for (c in 0 until numCols) {
                    if (gridCells[numRows - 1][c]) {
                        if (c == currentLane) {
                            lives--
                            updateHearts()
                            showHitEffect()
                        } else {
                            score += 10
                            tvScore.text = getString(R.string.score_template, score)
                        }
                    }
                }
                if (lives <= 0) {
                    endGame()
                    return@withEndAction
                }

                // Refresh and loop
                refreshGridUI()
                handler.postDelayed({ tick() }, 0)
            }
            .start()
    }

    private fun refreshGridUI() {
        var idx = 0
        for (r in 0 until numRows) {
            for (c in 0 until numCols) {
                val cell = gridBoard.getChildAt(idx++) as AppCompatImageView
                if (gridCells[r][c]) {
                    cell.setImageResource(R.drawable.stone)
                    cell.alpha = 1f
                } else {
                    cell.alpha = 0f
                }
            }
        }
    }

    private fun updateHearts() {
        when (lives) {
            2 -> heart3.visibility = View.INVISIBLE
            1 -> heart2.visibility = View.INVISIBLE
            0 -> heart1.visibility = View.INVISIBLE
        }
    }

    private fun showHitEffect() {
        fun flash(times: Int) {
            if (times <= 0) return
            imgCar.animate().alpha(0.3f).setDuration(50L).withEndAction {
                imgCar.animate().alpha(1f).setDuration(50L).withEndAction {
                    flash(times - 1)
                }.start()
            }.start()
        }
        flash(3)
    }

    private fun endGame() {
        gameRunning = false
        handler.removeCallbacksAndMessages(null)
        AlertDialog.Builder(this)
            .setTitle("Game Over")
            .setMessage("Your score: $score")
            .setPositiveButton("Restart") { _, _ -> recreate() }
            .setCancelable(false)
            .show()
    }
}