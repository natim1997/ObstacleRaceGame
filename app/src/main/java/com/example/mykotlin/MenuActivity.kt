package com.example.mykotlin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val btnSensor     = findViewById<Button>(R.id.btnSensor)
        val btnButtons    = findViewById<Button>(R.id.btnButtons)
        val btnHighScores = findViewById<Button>(R.id.btnHighScores)

        btnSensor.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                putExtra("CONTROL_MODE", "SENSOR")
            })
        }
        btnButtons.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply {
                putExtra("CONTROL_MODE", "BUTTON")
            })
        }
        btnHighScores.setOnClickListener {
            startActivity(Intent(this, HighScoresActivity::class.java))
        }
    }
}
