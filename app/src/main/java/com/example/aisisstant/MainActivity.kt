package com.example.aisisstant

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.aisisstant.CalendarActivity
import com.example.aisisstant.DashBoardActivity
import com.example.aisisstant.LoginActivity
import com.example.aisisstant.MoodLogActivity
import com.example.aisisstant.TaskInputActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            startActivity(Intent(this, DashBoardActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}
