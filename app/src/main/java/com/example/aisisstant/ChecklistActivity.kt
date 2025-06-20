package com.example.aisisstant

import android.os.Bundle
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChecklistActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var checklistLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checklist)

        checklistLayout = findViewById(R.id.checklistContainer)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(
            "https://aisisstant-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).reference

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val taskRef = database.child("users").child(userId).child("task")

            taskRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    checklistLayout.removeAllViews()

                    for (taskSnapshot in snapshot.children) {
                        val task = taskSnapshot.getValue(Task::class.java)
                        if (task != null) {

                            val card = LinearLayout(this@ChecklistActivity).apply {
                                orientation = LinearLayout.HORIZONTAL
                                setPadding(24, 24, 24, 24)
                                setBackgroundResource(android.R.drawable.dialog_holo_light_frame)
                                background?.mutate()?.alpha = 230
                                layoutParams = LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                ).apply {
                                    setMargins(0, 8, 0, 8)
                                }
                                background = ContextCompat.getDrawable(
                                    this@ChecklistActivity,
                                    R.drawable.task_card_bg
                                )
                            }

                            val checkbox = CheckBox(this@ChecklistActivity).apply {
                                isAllCaps = false
                                text = formatTaskText(task)
                                textSize = 16f
                                setPadding(8, 0, 0, 0)
                                setTextColor(
                                    ContextCompat.getColor(
                                        this@ChecklistActivity,
                                        android.R.color.black
                                    )
                                )
                                layoutParams = LinearLayout.LayoutParams(
                                    0,
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    1f
                                )
                                setOnCheckedChangeListener { _, isChecked ->
                                    if (isChecked) {
                                        taskRef.child(task.id).removeValue()
                                            .addOnSuccessListener {
                                                Toast.makeText(
                                                    this@ChecklistActivity,
                                                    "✅ Task completed and removed",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(
                                                    this@ChecklistActivity,
                                                    "❌ Failed to delete task",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                }
                            }

                            card.addView(checkbox)
                            checklistLayout.addView(card)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@ChecklistActivity,
                        "Failed to load tasks",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } else {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatTaskText(task: Task): String {
        val title = task.title.ifBlank { "Untitled Task" }
        val category = task.category.ifBlank { "General" }
        val recurrence = task.recurrence.ifBlank { "None" }
        val date = task.date.ifBlank { "Unspecified Date" }

        return "$title • $category • $recurrence • $date"
    }
}
