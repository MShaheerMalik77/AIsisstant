package com.example.aisisstant

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class VoiceTaskActivity : AppCompatActivity() {

    private val SPEECH_REQUEST_CODE = 0
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var resultText: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_task)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance("https://aisisstant-default-rtdb.asia-southeast1.firebasedatabase.app/").reference
        auth = FirebaseAuth.getInstance()

        // Bind views
        resultText = findViewById(R.id.voiceResultText)
        progressBar = findViewById(R.id.voiceProgressBar)
        val startButton = findViewById<Button>(R.id.startVoiceBtn)

        progressBar.visibility = ProgressBar.INVISIBLE

        startButton.setOnClickListener {
            startVoiceInput()
        }
    }

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your task...")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        try {
            progressBar.visibility = ProgressBar.VISIBLE
            startActivityForResult(intent, SPEECH_REQUEST_CODE)
        } catch (e: Exception) {
            progressBar.visibility = ProgressBar.INVISIBLE
            Toast.makeText(this, "Speech recognition not supported!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        progressBar.visibility = ProgressBar.INVISIBLE

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.firstOrNull()?.trim()

            if (!spokenText.isNullOrEmpty()) {
                handleVoiceInput(spokenText)
            } else {
                Toast.makeText(this, "Didn't catch that. Try again.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Voice input cancelled or failed.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleVoiceInput(spokenText: String) {
        val parsedTask = ONNXTaskParser.parse(this, spokenText) // ✅ include context

        resultText.text = "📌 Title: ${parsedTask.title}\n📅 Date: ${parsedTask.date}\n🏷 Category: ${parsedTask.category}"

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val taskId = database.child("users").child(userId).child("task").push().key

            if (taskId != null) {
                val taskToSave = parsedTask.copy(id = taskId)
                database.child("users").child(userId).child("task").child(taskId).setValue(taskToSave)
                    .addOnSuccessListener {
                        Toast.makeText(this, "✅ Task saved from voice input!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "❌ Failed to save task: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(this, "❌ Couldn't generate task ID", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "❌ You're not signed in", Toast.LENGTH_SHORT).show()
        }
    }
}
