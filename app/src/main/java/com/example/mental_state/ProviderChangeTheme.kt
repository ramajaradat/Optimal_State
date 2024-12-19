package com.example.mental_state


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class ProviderChangeTheme : AppCompatActivity() {

    private lateinit var darkButton: Button
    private lateinit var lightButton: Button
    private lateinit var providerchangethemebackButton:Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_provider_change_theme)

        // Initialize UI
        initializeUI()
        //setup button
        setupButton()

    }
    private fun initializeUI(){
        darkButton = findViewById(R.id.Darkbutton)
        lightButton = findViewById(R.id.Lightbutton)
        providerchangethemebackButton = findViewById(R.id.providerchangethemebackButton)
    }
    private fun setupButton(){
        providerchangethemebackButton.setOnClickListener {
            val intent = Intent(this, ProviderSettings::class.java)
            startActivity(intent)
        }

        darkButton.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            recreate()
        }

        lightButton.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            recreate()
        }
    }
}