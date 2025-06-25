package com.example.aisisstant

import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MoodLogActivity : AppCompatActivity() {

    private lateinit var moodRadioGroup: RadioGroup
    private lateinit var submitButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_log)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://aisisstant-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

        moodRadioGroup = findViewById(R.id.moodRadioGroup)
        submitButton = findViewById(R.id.submitMoodButton)

        submitButton.setOnClickListener {
            val selectedMoodId = moodRadioGroup.checkedRadioButtonId
            val selectedMood = findViewById<RadioButton>(selectedMoodId)?.text.toString()

            if (selectedMood.isNotEmpty()) {
                saveMoodToDatabase(selectedMood)
                saveMoodLocally(selectedMood)
                Toast.makeText(this, "Mood logged: $selectedMood", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Please select a mood", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveMoodLocally(mood: String) {
        val sharedPreferences = getSharedPreferences("MoodLog", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("lastMood", mood)
        editor.apply()
    }

    private fun saveMoodToDatabase(mood: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val moodId = database.child("users").child(userId).child("moods").push().key

            if (moodId != null) {
                val moodData = mapOf(
                    "id" to moodId,
                    "mood" to mood,
                    "timestamp" to System.currentTimeMillis()
                )
                database.child("users").child(userId).child("moods").child(moodId).setValue(moodData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "✅ Mood saved to database!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "❌ Failed to save mood: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(this, "❌ Failed to generate mood ID", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "❌ User not logged in!", Toast.LENGTH_SHORT).show()
        }
    }
}
