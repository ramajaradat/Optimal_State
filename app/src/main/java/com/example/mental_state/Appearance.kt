package com.example.mental_state

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import android.content.res.Configuration

class Appearance : AppCompatActivity() {
    private lateinit var Themebutton: Button
    private lateinit var FontSizebutton: Button
    private lateinit var back3Button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_appearance)

        Themebutton = findViewById(R.id.Themebutton)
        FontSizebutton = findViewById(R.id.FontSizebutton)
        back3Button = findViewById(R.id.back3Button)

        // Check if the app is in dark mode or light mode
        val isDarkMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        // Change button text colors based on the theme (light/dark)
        if (isDarkMode) {
            // Set text color to white for dark mode
            Themebutton.setTextColor(ContextCompat.getColor(this, R.color.white))
            FontSizebutton.setTextColor(ContextCompat.getColor(this, R.color.white))
            back3Button.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            // Set text color to black for light mode
            Themebutton.setTextColor(ContextCompat.getColor(this, R.color.black))
            FontSizebutton.setTextColor(ContextCompat.getColor(this, R.color.black))
            back3Button.setTextColor(ContextCompat.getColor(this, R.color.black))
        }

        // Set button click listeners
        Themebutton.setOnClickListener {
            val intent = Intent(this, ChangeTheme::class.java)
            startActivity(intent)
        }
        FontSizebutton.setOnClickListener {
            val intent = Intent(this, UserHomePage::class.java)
            startActivity(intent)
        }
        back3Button.setOnClickListener {
            val intent = Intent(this, Appearance::class.java)
            startActivity(intent)
        }
    }
}
