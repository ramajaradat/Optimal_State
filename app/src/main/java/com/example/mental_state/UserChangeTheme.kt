package com.example.mental_state

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class UserChangeTheme : AppCompatActivity() {

    private lateinit var UserDarkThemeButton: Button
    private lateinit var UserLightThemeButton: Button
    private lateinit var UserBackChangeThemeButton:Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_change_theme)

        // Initialize UI
        initializeUI()
        //setup button
        setupbutton()
    }
    private fun initializeUI(){
        UserDarkThemeButton = findViewById(R.id.UserDarkThemeButton)
        UserLightThemeButton = findViewById(R.id.UserLightThemeButton)
        UserBackChangeThemeButton = findViewById(R.id.UserBackChangeThemeButton)
    }
    private fun setupbutton(){
        UserBackChangeThemeButton.setOnClickListener {
            val intent = Intent(this, UserSettingPage::class.java)
            startActivity(intent)
        }

        // Set up listeners for each button to change the theme
        UserDarkThemeButton.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            recreate() // Reload to show edit
            Toast.makeText(this@UserChangeTheme,"Theme will change now",Toast.LENGTH_SHORT).show()

        }

        UserLightThemeButton.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            recreate() // Reload to show edit
            Toast.makeText(this@UserChangeTheme,"Theme will change now",Toast.LENGTH_SHORT).show()
        }
    }
}
