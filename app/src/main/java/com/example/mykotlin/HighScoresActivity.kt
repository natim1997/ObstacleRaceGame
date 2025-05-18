package com.example.mykotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class HighScoresActivity : AppCompatActivity(),
    ScoresTableFragment.OnScoreClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_high_scores)
    }

    override fun onScoreSelected(entry: ScoreEntry) {
        val mapFrag = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as? MapScoresFragment
        mapFrag?.showLocation(entry.lat, entry.lng)
    }
}
