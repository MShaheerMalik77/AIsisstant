package com.example.aisisstant

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class TaskInputActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_input)

        val inputField = findViewById<EditText>(R.id.taskInput)
        val saveBtn = findViewById<Button>(R.id.saveTaskButton)
        val resultView = findViewById<TextView>(R.id.parsedTaskResult)

        // Initialize Firebase Database and Auth
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        saveBtn.setOnClickListener {
            val rawText = inputField.text.toString()
            val parsedTask = NLPTaskParser.parse(rawText)

            // Update the UI
            resultView.text = "📌 ${parsedTask.title}\n📅 ${parsedTask.date}"

            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userId = currentUser.uid
                val taskId = database.child("users").child(userId).child("tasks").push().key

                if (taskId != null) {
                    val task = Task(
                        id = taskId,
                        title = parsedTask.title,
                        date = parsedTask.date,
                        category = parsedTask.category
                    )
                    database.child("users").child(userId).child("tasks").child(taskId).setValue(task)
                        .addOnSuccessListener {
                            resultView.append("\n✅ Task Saved!")
                        }
                        .addOnFailureListener { exception ->
                            resultView.append("\n❌ Failed to Save Task: ${exception.message}")
                        }
                }
            } else {
                resultView.append("\n❌ User not logged in!")
            }
        }
    }
}
