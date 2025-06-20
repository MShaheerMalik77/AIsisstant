package com.example.aisisstant

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

// Import all your activities so navigation will be smooth
import com.example.aisisstant.CalendarActivity
import com.example.aisisstant.DashBoardActivity  // <-- CAREFUL! Name is DashBoardActivity based on your file list
import com.example.aisisstant.LoginActivity
import com.example.aisisstant.MoodLogActivity
import com.example.aisisstant.TaskInputActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            // User is already logged in -> Go to Dashboard
            startActivity(Intent(this, DashBoardActivity::class.java))
        } else {
            // User not logged in -> Go to Login
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}
