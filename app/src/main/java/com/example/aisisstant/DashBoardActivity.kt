package com.example.aisisstant
import com.example.aisisstant.HomeFragment
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView


class DashBoardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Load default fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.dashboardFragmentContainer, HomeFragment())
            .commit()

        val navView = findViewById<BottomNavigationView>(R.id.dashboardBottomNav)
        navView.setOnItemSelectedListener {
            val selectedFragment = when (it.itemId) {
                R.id.nav_home -> HomeFragment()
                else -> HomeFragment()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.dashboardFragmentContainer, selectedFragment)
                .commit()

            true
        }
    }
}
