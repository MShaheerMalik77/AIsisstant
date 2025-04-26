package com.example.aisisstant

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.aisisstant.R
import androidx.fragment.app.Fragment
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MoodLogActivity : AppCompatActivity() {

    private lateinit var moodRadioGroup: RadioGroup
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_log)

        // Initialize the UI components
        moodRadioGroup = findViewById(R.id.moodRadioGroup)
        submitButton = findViewById(R.id.submitMoodButton)

        // Set the submit button's click listener
        submitButton.setOnClickListener {
            val selectedMoodId = moodRadioGroup.checkedRadioButtonId
            val selectedMood = findViewById<RadioButton>(selectedMoodId)?.text.toString()

            if (selectedMood.isNotEmpty()) {
                // Save the mood log (you could save it in a database or shared preferences)
                saveMoodLog(selectedMood)
                // Show a confirmation message
                Toast.makeText(this, "Mood logged: $selectedMood", Toast.LENGTH_SHORT).show()

                // Optionally, return to the previous activity or close the activity
                finish()
            } else {
                // If no mood is selected, show an error message
                Toast.makeText(this, "Please select a mood", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveMoodLog(mood: String) {
        // This method can be extended to save the mood in a database or shared preferences.
        // For now, it's a simple placeholder.
        // Example: Save the mood in SharedPreferences (you could replace this with a database)
        val sharedPreferences = getSharedPreferences("MoodLog", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("lastMood", mood)
        editor.apply()
    }
}
