package com.example.trashify

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.trashify.ui.ClasificationActivity
import com.example.trashify.ui.LeaderboardActivity
import com.example.trashify.ui.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val actionbar = supportActionBar
        actionbar?.title = "Home"
        actionbar?.setDisplayHomeAsUpEnabled(true)

        val imageButton = findViewById<ImageButton>(R.id.imageButton)

        imageButton.setOnClickListener{
            val intent = Intent(this, ClasificationActivity::class.java)
            startActivity(intent)
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    handleHomeClick()
                    // Handle click on Home
                }
                R.id.navigation_leader_board -> {
                    handleLeaderboardClick()
                    // Handle click on Explore
                }
                R.id.navigation_profile -> {
                    handleProfileClick()
                    // Handle click on Profile
                }
            }
            true
        }
    }

    private fun handleProfileClick(){
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun handleLeaderboardClick(){
        val intent = Intent(this, LeaderboardActivity::class.java)
        startActivity(intent)
    }

    private fun handleHomeClick(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    companion object{
        const val TOKEN_INTENT_KEY = "TOKEN_KEY"
    }

}