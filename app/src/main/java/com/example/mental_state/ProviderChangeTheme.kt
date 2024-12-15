package com.example.mental_state


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class ProviderChangeTheme : AppCompatActivity() {

    private lateinit var darkButton: Button
    private lateinit var lightButton: Button
    private lateinit var back4button:Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_provider_change_theme) // Ensure this XML file matches the one you provided

        // Initialize the buttons
        darkButton = findViewById(R.id.Darkbutton)
        lightButton = findViewById(R.id.Lightbutton)
        back4button = findViewById(R.id.back4button)

        back4button.setOnClickListener {
            val intent = Intent(this, ProviderSettings::class.java)
            startActivity(intent)
        }

        // Set up listeners for each button to change the theme
        darkButton.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            recreate() // Recreate the activity to apply the theme immediately
        }

        lightButton.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            recreate() // Recreate the activity to apply the theme immediately
        }
    }
}