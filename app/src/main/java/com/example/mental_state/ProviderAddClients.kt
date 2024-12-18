package com.example.mental_state

import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProviderAddClients : AppCompatActivity() {
    private lateinit var BackAddClient: Button
    private lateinit var EnterUserEmail: EditText
    private lateinit var ApplyAddClinetButton: Button
    private lateinit var database: FirebaseDatabase
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var ClientNameShow: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_provider_add_clintes)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance()
        EnterUserEmail = findViewById(R.id.EnterUserEmail)
        ApplyAddClinetButton = findViewById(R.id.ApplyAddClinetButton)
        BackAddClient = findViewById(R.id.BackAddClient)
        mFirebaseAuth = FirebaseAuth.getInstance()
        ClientNameShow = findViewById(R.id.ClientNameShow)

        BackAddClient.setOnClickListener {
            val intent = Intent(this, ProviderHomePage::class.java)
            startActivity(intent)
            finish()
        }

        ApplyAddClinetButton.setOnClickListener {
            val email = EnterUserEmail.text.toString().trim()

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                EnterUserEmail.error = "Please enter a valid email address"
                EnterUserEmail.requestFocus()
                return@setOnClickListener
            }

            val databaseRef = FirebaseDatabase.getInstance().getReference("users")
            databaseRef.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.exists()) {
                            Toast.makeText(this@ProviderAddClients, "Email does not exist. Please try again.", Toast.LENGTH_SHORT).show()
                        } else {
                            val firebaseProvider = mFirebaseAuth.currentUser
                            val providerEmail = firebaseProvider?.email.toString()
                            val formattedProviderEmail = providerEmail.replace(".", "_").replace("@", "_")

                            // Check if the client has "provider" field set to "yes"
                            checkIfUserIsProvider(email) { isProvider ->
                                if (isProvider) {
                                    Toast.makeText(this@ProviderAddClients, "Client is  provider You cant add it.", Toast.LENGTH_SHORT).show()
                                } else {
                                    // Check if the client is already added
                                    checkIfClientAlreadyAdded(formattedProviderEmail, email) { clientAlreadyAdded ->
                                        if (clientAlreadyAdded) {
                                            Toast.makeText(this@ProviderAddClients, "Client is already added.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            saveClientToProvider(formattedProviderEmail, email)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    override fun onCancelled(p0: DatabaseError) {
                        Toast.makeText(this@ProviderAddClients, "Database error: ${p0.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    // Function to check if the client is already added to the provider's dataset
    private fun checkIfClientAlreadyAdded(providerEmail: String, clientEmail: String, callback: (Boolean) -> Unit) {
        val providerRef = database.reference.child("Providers").child(providerEmail).child("clients")
        providerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val clientEmails = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                    callback(clientEmails.contains(clientEmail))
                } else {
                    callback(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
        })
    }

    // Function to save client email to provider's dataset (handling multiple clients)
    private fun saveClientToProvider(providerEmail: String, clientEmail: String) {
        val userinfo= database.reference
        userinfo.child("users")
            .orderByChild("email")
            .equalTo(clientEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Directly retrieve firstName and lastName (assuming client exists)
                    snapshot.children.forEach { userSnapshot ->
                        val firstName = userSnapshot.child("firstName").getValue(String::class.java) ?: "N/A"
                        val lastName = userSnapshot.child("lastName").getValue(String::class.java) ?: "N/A"

                        // Combine firstName and lastName
                        val fullName = "$firstName $lastName"

                        // Find the TextView in the layout and set the full name
                        ClientNameShow.text = fullName
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                    Toast.makeText(applicationContext, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        val providerRef = database.reference.child("Providers").child(providerEmail).child("clients")

        // Get the current list of client emails
        providerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val clientEmails = snapshot.children.mapNotNull { it.getValue(String::class.java) }.toMutableList()

                // Add the new client email if not already in the list
                if (!clientEmails.contains(clientEmail)) {
                    clientEmails.add(clientEmail)

                    // Save the updated list of client emails
                    providerRef.setValue(clientEmails)
                        .addOnSuccessListener {
                            Toast.makeText(applicationContext, "Client added successfully!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(applicationContext, "Failed to add client: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(applicationContext, "Client already added.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun checkIfUserIsProvider(clientEmail: String, callback: (Boolean) -> Unit) {
        val userInfoRef = database.reference.child("users")
        userInfoRef.orderByChild("email").equalTo(clientEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userSnapshot = snapshot.children.first()
                        val providerStatus = userSnapshot.child("isprovider").getValue(String::class.java)
                        callback(providerStatus == "yes")
                    } else {
                        callback(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(applicationContext, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    callback(false)
                }
            })
    }
}

