package com.example.aisisstant

import HomeFragment
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class DashBoardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        supportFragmentManager.beginTransaction()
            .replace(R.id.dashboardFragmentContainer, HomeFragment())
            .commit()
    }
}
