package com.example.mykotlin

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.EditText
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
    private lateinit var tvDistance: TextView
    private lateinit var btnStart: Button

    private lateinit var hitSound: MediaPlayer

    private val numRows = 16
    private val numCols = 5
    private var gridCells = Array(numRows) { IntArray(numCols) }

    private var score = 0
    private var lives = 3
    private var distanceMeters = 0
    private var currentLane = numCols / 2

    private var spawnCounter = 0
    private val emptyRowsBetween = 2

    private val handler = Handler(Looper.getMainLooper())
    private var gameRunning = false

    private var sensorMode = false
    private lateinit var sensorManager: SensorManager
    private var accel: Sensor? = null
    private var canMoveBySensor = true
    private val sensorCooldownMs = 500L

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gridBoard   = findViewById(R.id.grid_board)
        imgCar      = findViewById(R.id.img_car)
        btnLeft     = findViewById(R.id.btn_left)
        btnRight    = findViewById(R.id.btn_right)
        heart1      = findViewById(R.id.heart1)
        heart2      = findViewById(R.id.heart2)
        heart3      = findViewById(R.id.heart3)
        tvScore     = findViewById(R.id.tv_score)
        tvDistance  = findViewById(R.id.tv_distance)
        btnStart    = findViewById(R.id.btn_start)

        hitSound = MediaPlayer.create(this, R.raw.hit)

        sensorMode = intent.getStringExtra("CONTROL_MODE") == "SENSOR"
        if (sensorMode) {
            btnLeft.visibility = View.GONE
            btnRight.visibility = View.GONE
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        }

        btnLeft.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_left_arrow))
        btnRight.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.ic_right_arrow))

        val padding = (4 * resources.displayMetrics.density).toInt()
        for (r in 0 until numRows) {
            for (c in 0 until numCols) {
                val cell = AppCompatImageView(this).apply {
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = 0; height = 0
                        columnSpec = GridLayout.spec(c, 1f)
                        rowSpec    = GridLayout.spec(r, 1f)
                    }
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    adjustViewBounds = true
                    setPadding(padding, padding, padding, padding)
                    alpha = 0f
                }
                gridBoard.addView(cell)
            }
        }

        gridBoard.post { updateCarPosition() }

        btnLeft.setOnClickListener {
            if (currentLane > 0) {
                currentLane--; updateCarPosition()
            }
        }
        btnRight.setOnClickListener {
            if (currentLane < numCols - 1) {
                currentLane++; updateCarPosition()
            }
        }

        btnStart.setOnClickListener {
            btnStart.visibility = View.GONE
            resetGame()
            gameRunning = true
            tick()
        }
    }

    override fun onResume() {
        super.onResume()
        if (sensorMode && accel != null) {
            sensorManager.registerListener(sensorListener, accel, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        if (sensorMode) {
            sensorManager.unregisterListener(sensorListener)
        }
    }

    private val sensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        override fun onSensorChanged(event: SensorEvent) {
            if (!canMoveBySensor) return
            val x = event.values[0]
            val threshold = 3f
            if (x > threshold && currentLane > 0) {
                currentLane--; updateCarPosition(); throttleSensor()
            } else if (x < -threshold && currentLane < numCols - 1) {
                currentLane++; updateCarPosition(); throttleSensor()
            }
        }
    }

    private fun throttleSensor() {
        canMoveBySensor = false
        handler.postDelayed({ canMoveBySensor = true }, sensorCooldownMs)
    }

    private fun updateCarPosition() {
        val laneWidth = gridBoard.width / numCols
        imgCar.x = (currentLane * laneWidth + laneWidth/2f - imgCar.width/2f)
    }

    private fun resetGame() {
        gridCells.forEach { it.fill(0) }
        score = 0; lives = 3; distanceMeters = 0
        currentLane = numCols/2; spawnCounter = 0

        tvScore.text    = getString(R.string.score_template, score)
        tvDistance.text = "$distanceMeters m"
        heart1.visibility = View.VISIBLE
        heart2.visibility = View.VISIBLE
        heart3.visibility = View.VISIBLE

        refreshGridUI()
        updateCarPosition()
    }

    private fun tick() {
        if (!gameRunning) return
        val rowH = gridBoard.height / numRows
        gridBoard.animate()
            .translationY(rowH.toFloat())
            .setDuration(400L)
            .setInterpolator(LinearInterpolator())
            .withEndAction {
                gridBoard.translationY = 0f
                for (r in numRows-1 downTo 1) gridCells[r] = gridCells[r-1].copyOf()
                gridCells[0] = IntArray(numCols)

                if (spawnCounter == 0) {
                    val lanes = (0 until numCols).shuffled()
                    val cnt = if (Random.nextFloat()<0.25f) 2 else 1
                    repeat(cnt) { i ->
                        val lane = lanes[i]
                        gridCells[0][lane] = if (Random.nextFloat()<0.2f) 2 else 1
                    }
                    spawnCounter = emptyRowsBetween
                } else spawnCounter--

                for (c in 0 until numCols) {
                    when (gridCells[numRows-1][c]) {
                        1 -> {
                            if (c == currentLane) {
                                if (!hitSound.isPlaying) hitSound.start()
                                lives--; updateHearts(); flashHit()
                            } else {
                                score += 10
                                tvScore.text = getString(R.string.score_template, score)
                            }
                        }
                        2 -> {
                            if (c == currentLane) {
                                score += 20
                                tvScore.text = getString(R.string.score_template, score)
                            }
                        }
                    }
                }

                distanceMeters++
                tvDistance.text = "$distanceMeters m"

                if (lives <= 0) { endGame(); return@withEndAction }

                refreshGridUI()
                handler.post { tick() }
            }
            .start()
    }

    private fun refreshGridUI() {
        var idx = 0
        for (r in 0 until numRows) for (c in 0 until numCols) {
            val cell = gridBoard.getChildAt(idx++) as AppCompatImageView
            when (gridCells[r][c]) {
                1 -> { cell.setImageResource(R.drawable.stone); cell.alpha = 1f }
                2 -> { cell.setImageResource(R.drawable.ic_coin); cell.alpha = 1f }
                else -> cell.alpha = 0f
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

    private fun flashHit() {
        fun f(times:Int) {
            if (times==0) return
            imgCar.animate().alpha(0.3f).setDuration(50)
                .withEndAction { imgCar.animate().alpha(1f).setDuration(50)
                    .withEndAction { f(times-1) }.start() }
                .start()
        }
        f(3)
    }

    private fun endGame() {
        gameRunning = false
        handler.removeCallbacksAndMessages(null)

        val input = EditText(this).apply { hint = "Enter your name" }
        AlertDialog.Builder(this)
            .setTitle("Game Over")
            .setMessage("Your score: $score\nEnter your name:")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val name = input.text.toString().ifBlank { "Anonymous" }
                ScoresRepository.save(
                    this,
                    ScoreEntry(score = score, name = name, lat = 0.0, lng = 0.0)
                )
                startActivity(Intent(this, MenuActivity::class.java))
                finish()
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        hitSound.release()
    }
}
