package com.example.mykotlin

import android.animation.*
import android.os.*
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var imgCar: ImageView
    private lateinit var btnLeft: ImageButton
    private lateinit var btnRight: ImageButton
    private lateinit var gameRoot: FrameLayout
    private lateinit var heart1: ImageView
    private lateinit var heart2: ImageView
    private lateinit var heart3: ImageView
    private lateinit var tvScore: TextView
    private lateinit var btnStart: Button

    private var score = 0
    private var lives = 3
    private var currentLane = 1
    private var laneWidth = 0
    private var screenWidth = 0

    private val handler = Handler(Looper.getMainLooper())
    private var gameRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imgCar = findViewById(R.id.img_car)
        btnLeft = findViewById(R.id.btn_left)
        btnRight = findViewById(R.id.btn_right)
        gameRoot = findViewById(R.id.game_root)
        heart1 = findViewById(R.id.heart1)
        heart2 = findViewById(R.id.heart2)
        heart3 = findViewById(R.id.heart3)
        tvScore = findViewById(R.id.tv_score)
        btnStart = findViewById(R.id.btn_start)

        gameRoot.post {
            screenWidth = gameRoot.width
            laneWidth = screenWidth / 3
            setCarPosition()
        }

        btnLeft.setOnClickListener {
            if (currentLane > 0) {
                currentLane--
                setCarPosition()
            }
        }

        btnRight.setOnClickListener {
            if (currentLane < 2) {
                currentLane++
                setCarPosition()
            }
        }

        btnStart.setOnClickListener {
            btnStart.visibility = View.GONE
            gameRunning = true
            startSpawningObstacles()
        }
    }

    private fun setCarPosition() {
        imgCar.x = (laneWidth * currentLane + laneWidth / 2 - imgCar.width / 2).toFloat()
    }

    private fun startSpawningObstacles() {
        handler.post(object : Runnable {
            override fun run() {
                if (lives > 0 && gameRunning) {
                    spawnObstacles()
                    val delay = (1000L - score).coerceAtLeast(400L) // מעלה קצב לפי ניקוד
                    handler.postDelayed(this, delay)
                }
            }
        })
    }

    private fun spawnObstacles() {
        if (laneWidth == 0 || lives == 0) return

        val lanes = listOf(0, 1, 2).shuffled()
        val numObstacles = if (Random.nextFloat() < 0.25f) 2 else 1  // 25% סיכוי ל-2 מכשולים

        for (i in 0 until numObstacles) {
            val lane = lanes[i]
            val xPosition = laneWidth * lane + laneWidth / 2 - 32

            val stone = ImageView(this).apply {
                setImageResource(R.drawable.stone)
                layoutParams = FrameLayout.LayoutParams(64, 64)
                x = xPosition.toFloat()
                y = 0f
                tag = lane
            }

            gameRoot.addView(stone)
            animateObstacle(stone)
        }
    }

    private fun animateObstacle(obstacle: ImageView) {
        val screenHeight = gameRoot.height

        val animator = ValueAnimator.ofFloat(0f, screenHeight.toFloat()).apply {
            duration = 3000
            addUpdateListener { animation ->
                val y = animation.animatedValue as Float
                obstacle.translationY = y

                val obstacleLane = obstacle.tag as Int
                val didCollide = obstacleLane == currentLane &&
                        y + obstacle.height >= imgCar.y &&
                        y <= imgCar.y + imgCar.height &&
                        obstacle.alpha == 1f

                if (didCollide) {
                    lives--
                    updateHearts()
                    showHitEffect()
                    obstacle.alpha = 0f
                    if (lives == 0) endGame()
                }
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (obstacle.alpha == 1f) {
                        score += 10
                        tvScore.text = getString(R.string.score_template, score)
                    }
                    gameRoot.removeView(obstacle)
                }
            })
        }

        animator.start()
    }

    private fun updateHearts() {
        when (lives) {
            2 -> heart3.visibility = View.INVISIBLE
            1 -> heart2.visibility = View.INVISIBLE
            0 -> heart1.visibility = View.INVISIBLE
        }
    }

    private fun showHitEffect() {
        val flashCount = 3
        val flashDuration = 100L
        fun flash(times: Int) {
            if (times <= 0) return
            imgCar.animate().alpha(0.3f).setDuration(flashDuration).withEndAction {
                imgCar.animate().alpha(1f).setDuration(flashDuration).withEndAction {
                    flash(times - 1)
                }.start()
            }.start()
        }
        flash(flashCount)
    }

    private fun endGame() {
        gameRunning = false
        AlertDialog.Builder(this)
            .setTitle("Game Over")
            .setMessage("Your score: $score")
            .setPositiveButton("Restart") { _, _ -> recreate() }
            .setCancelable(false)
            .show()
    }
}
