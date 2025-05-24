package com.example.healthcalculator

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val beginnerButton = findViewById<Button>(R.id.beginnerButton)
        val intermediateButton = findViewById<Button>(R.id.intermediateButton)

        beginnerButton.setOnClickListener {
            val intent = Intent(this, BeginnerActivity::class.java)
            startActivity(intent)
        }

        intermediateButton.setOnClickListener {
            val intent = Intent(this, IntermediateActivity::class.java)
            startActivity(intent)
        }
    }
}