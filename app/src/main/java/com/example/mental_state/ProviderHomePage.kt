package com.example.mental_state

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class ProviderHomePage : AppCompatActivity() {
    private lateinit var provideraddClientButton: Button
    private lateinit var providerremoveClientButton: Button
    private lateinit var providerViewclientButton: Button
    private lateinit var providersettingsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_provider_home_page)

        // Initialize buttons
        provideraddClientButton = findViewById(R.id.provideraddClientButton)
        providerremoveClientButton = findViewById(R.id.providerremoveClientButton)
        providerViewclientButton = findViewById(R.id.providerViewclientButton)
        providersettingsButton = findViewById(R.id.providersettingsButton)

        // Navigate to AddClients page when clicking "Add Client" button
        provideraddClientButton.setOnClickListener {
            val intent = Intent(this@ProviderHomePage, AddClients::class.java)
            startActivity(intent)
        }
        providerremoveClientButton.setOnClickListener {
            val intent = Intent(this@ProviderHomePage, RemoveClients::class.java)
            startActivity(intent)
        }
        providerViewclientButton.setOnClickListener {
            val intent = Intent(this@ProviderHomePage, currentclients::class.java)
            startActivity(intent)
        }
        providersettingsButton.setOnClickListener {
            val intent = Intent(this@ProviderHomePage, ProviderSettings::class.java)
            startActivity(intent)
        }
    }
}
