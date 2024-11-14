package com.example.mental_state

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class ChangeTheme : AppCompatActivity() {

    private lateinit var darkButton: Button
    private lateinit var lightButton: Button
    private var sharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_theme) // Ensure this XML file matches the one you provided

        // Initialize the buttons
        darkButton = findViewById(R.id.Darkbutton)
        lightButton = findViewById(R.id.Lightbutton)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("theme_prefs", MODE_PRIVATE)

        // Apply saved theme mode when the activity starts
        applySavedThemeMode()

        // Set up listeners for each button to change the theme
        darkButton.setOnClickListener {
            setThemeMode(AppCompatDelegate.MODE_NIGHT_YES) // Apply dark theme
        }

        lightButton.setOnClickListener {
            setThemeMode(AppCompatDelegate.MODE_NIGHT_NO) // Apply light theme
        }
    }

    // Function to set the theme mode
    private fun setThemeMode(mode: Int) {
        AppCompatDelegate.setDefaultNightMode(mode) // Change theme mode globally
        saveThemeMode(mode) // Save the selected theme mode
        recreate() // Recreate the activity to apply the theme immediately
    }

    // Function to save the selected theme mode to SharedPreferences
    private fun saveThemeMode(mode: Int) {
        sharedPreferences?.edit()?.putInt("theme_mode", mode)?.apply()
    }

    // Function to apply the saved theme mode when the app starts
    private fun applySavedThemeMode() {
        // Get the saved theme mode, default to follow system settings if none found
        val savedMode = sharedPreferences?.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        AppCompatDelegate.setDefaultNightMode(savedMode)
    }
}
