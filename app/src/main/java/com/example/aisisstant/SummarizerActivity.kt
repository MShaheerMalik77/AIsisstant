package com.example.aisisstant

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.aisisstant.R

class SummarizerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summarizer)

        val input = findViewById<EditText>(R.id.noteInput)
        val result = findViewById<TextView>(R.id.summaryOutput)
        val summarizeBtn = findViewById<Button>(R.id.summarizeBtn)

        summarizeBtn.setOnClickListener {
            val lines = input.text.toString().split(". ", "\n")
            val topFive = lines.filter { it.trim().length > 10 }.take(5)
            result.text = topFive.joinToString("\n• ", prefix = "• ")
        }
    }
}

