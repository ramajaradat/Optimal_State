package com.example.mental_state

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProviderHomePage : AppCompatActivity() {
    private lateinit var provideraddClientButton: Button
    private lateinit var providerremoveClientButton: Button
    private lateinit var providerViewclientButton: Button
    private lateinit var providersettingsButton: Button
    private lateinit var tvwelcomeprovider:TextView // Updated ID
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_provider_home_page)

        // Initialize buttons
        provideraddClientButton = findViewById(R.id.provideraddClientButton)
        providerremoveClientButton = findViewById(R.id.providerremoveClientButton)
        providerViewclientButton = findViewById(R.id.providerViewclientButton)
        providersettingsButton = findViewById(R.id.providersettingsButton)
        tvwelcomeprovider = findViewById(R.id.tvwelcomeprovider) // Updated reference

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Fetch and display the user's first name
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val userRef = database.getReference("users").child(uid).child("firstName")
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val firstName = snapshot.getValue(String::class.java)
                    val formattedName = firstName?.replaceFirstChar { it.uppercase() } ?: ""
                    tvwelcomeprovider.text = "Welcome $formattedName" // Updated TextView usage
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle errors if necessary
                }
            })
        }

        // Navigate to AddClients page
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