package com.example.aisisstant
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.aisisstant.ChecklistGenerator
import com.example.aisisstant.R

class ChecklistActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checklist)

        val inputGoal = findViewById<EditText>(R.id.goalInput)
        val checklistView = findViewById<TextView>(R.id.generatedChecklist)
        val generateBtn = findViewById<Button>(R.id.generateChecklistBtn)

        generateBtn.setOnClickListener {
            val goal = inputGoal.text.toString()
            val steps = ChecklistGenerator.generate(goal)
            checklistView.text = steps.joinToString("\n- ")
        }
    }
}
