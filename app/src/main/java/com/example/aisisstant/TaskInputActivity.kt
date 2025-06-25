package com.example.aisisstant

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        // Initialize Firebase Auth and Database
        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://aisisstant-default-rtdb.asia-southeast1.firebasedatabase.app/").reference



        saveBtn.setOnClickListener {
            val rawText = inputField.text.toString().trim()

            if (rawText.isEmpty()) {
                Toast.makeText(this, "Please enter a task description", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val parsedTask = ONNXTaskParser.parse(this, rawText)

            // Display result immediately
            resultView.text = """
    📌 Title: ${parsedTask.title}
    📅 Date: ${parsedTask.date}
    🔁 Recurs: ${parsedTask.recurrence}
    📂 Category: ${parsedTask.category}
""".trimIndent()


            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userId = currentUser.uid
                val taskId = database.child("users").child(userId).child("task").push().key

                if (taskId != null) {
                    val task = parsedTask.copy(id = taskId)
                    // ✅ Save under /users/{userId}/task/{taskId}
                    database.child("users").child(userId).child("task").child(taskId).setValue(task)
                        .addOnSuccessListener {
                            Toast.makeText(this, "✅ Task saved successfully!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "❌ Failed to save task: ${exception.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(this, "❌ Failed to generate task ID", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "❌ User not logged in!", Toast.LENGTH_SHORT).show()
            }


        }
    }
}
