package com.example.aisisstant

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SummarizerActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var summariesContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summarizer)

        val input = findViewById<EditText>(R.id.noteInput)
        val result = findViewById<TextView>(R.id.summaryOutput)
        val summarizeBtn = findViewById<Button>(R.id.summarizeBtn)
        summariesContainer = findViewById(R.id.summariesContainer)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://aisisstant-default-rtdb.firebaseio.com/").reference

        summarizeBtn.setOnClickListener {
            val rawText = input.text.toString()
            val bulletPoints = generateBulletSummary(rawText)

            val summaryText = bulletPoints.joinToString("\n• ", prefix = "• ")

            result.text = summaryText

            // Save to Firebase
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userId = currentUser.uid
                val summaryId = database.child("users").child(userId).child("summaries").push().key ?: return@setOnClickListener
                val summaryData = mapOf(
                    "id" to summaryId,
                    "summary" to summaryText,
                    "timestamp" to System.currentTimeMillis()
                )
                database.child("users").child(userId).child("summaries").child(summaryId).setValue(summaryData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Summary saved! 📄", Toast.LENGTH_SHORT).show()
                        // Refresh summaries
                        loadSummaries()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to save summary ❌", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please log in to save the summary.", Toast.LENGTH_SHORT).show()
            }
        }

        // Load previously saved summaries
        loadSummaries()
    }

    /**
     * Generate bullet points by removing connectors and splitting into short lines.
     */
    private fun generateBulletSummary(text: String): List<String> {
        val cleanedText = text.replace(Regex("\\b(and|but|so|or|because|although|however|meanwhile|therefore|furthermore|moreover|thus)\\b", RegexOption.IGNORE_CASE), "")
        val sentences = cleanedText.split(Regex("[.!?\\n]"))
            .map { it.trim() }
            .filter { it.length > 5 }

        return sentences.take(5)
    }

    /**
     * Load and display all saved summaries.
     */
    private fun loadSummaries() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        database.child("users").child(userId).child("summaries")
            .orderByChild("timestamp")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    summariesContainer.removeAllViews()
                    for (summarySnap in snapshot.children) {
                        val summary = summarySnap.child("summary").getValue(String::class.java)
                        if (!summary.isNullOrBlank()) {
                            val summaryView = TextView(this@SummarizerActivity).apply {
                                text = summary
                                textSize = 14f
                                setPadding(8, 8, 8, 8)
                            }
                            summariesContainer.addView(summaryView)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SummarizerActivity, "Failed to load summaries", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
