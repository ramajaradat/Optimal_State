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
        // Initialize UI
        initialize()
        // Get and show the user firstname on homepage
        showUserName()
        //setup Button
        setupButton()
    }
    private fun initialize(){
        provideraddClientButton = findViewById(R.id.provideraddClientButton)
        providerremoveClientButton = findViewById(R.id.providerremoveClientButton)
        providerViewclientButton = findViewById(R.id.providerViewclientButton)
        providersettingsButton = findViewById(R.id.providersettingsButton)
        tvwelcomeprovider = findViewById(R.id.tvwelcomeprovider)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
    }
    private fun showUserName(){
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
    }
    private fun setupButton(){
        provideraddClientButton.setOnClickListener {
            val intent = Intent(this@ProviderHomePage, ProviderAddClients::class.java)
            startActivity(intent)
        }

        providerremoveClientButton.setOnClickListener {
            val intent = Intent(this@ProviderHomePage, ProviderRemoveClients::class.java)
            startActivity(intent)
        }

        providerViewclientButton.setOnClickListener {
            val intent = Intent(this@ProviderHomePage, ProviderCurrentClients::class.java)
            startActivity(intent)
        }

        providersettingsButton.setOnClickListener {
            val intent = Intent(this@ProviderHomePage, ProviderSettings::class.java)
            startActivity(intent)
        }
    }

}