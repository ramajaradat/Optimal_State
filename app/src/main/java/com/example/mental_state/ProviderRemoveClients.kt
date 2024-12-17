package com.example.mental_state

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RadioButton
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ProviderRemoveClients : AppCompatActivity() {
    private lateinit var clientsTableLayout: TableLayout
    private lateinit var database: FirebaseDatabase
    private var selectedRadioButton: RadioButton? = null
    private lateinit var backButton: Button
    private lateinit var confirmButton: Button
    private lateinit var selectedClientName: String
    private lateinit var mFirebaseAuth: FirebaseAuth


    // Assuming the current provider's email is stored in shared preferences or passed to this activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_provider_remove_clients)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance()
        clientsTableLayout = findViewById(R.id.clientsTableLayout)
        backButton = findViewById(R.id.btnBack)
        confirmButton = findViewById(R.id.btnConfirm)
        mFirebaseAuth = FirebaseAuth.getInstance()


        // Format current provider email to match the Firebase structure
        val firebaseProvider = mFirebaseAuth.currentUser
        val providerEmail = firebaseProvider?.email.toString()
        val formattedProviderEmail = providerEmail.replace(".", "_").replace("@", "_")


        backButton.setOnClickListener {
            val intent = Intent(this, ProviderHomePage::class.java)
            startActivity(intent)
            finish()
        }

        // Confirm button click listener
        confirmButton.setOnClickListener {
            if (selectedRadioButton == null) {
                Toast.makeText(this, "Please select a client to remove", Toast.LENGTH_SHORT).show()
            } else {
                selectedClientName = selectedRadioButton?.tag.toString()
                deleteClientFromFirebase(selectedClientName,formattedProviderEmail)
            }
        }

        // Load clients associated with the current provider
        loadClientsFromFirebase(formattedProviderEmail)
    }

    private fun loadClientsFromFirebase(formattedProviderEmail:String) {
        val providerRef = database.reference.child("Providers")
        val usersRef = database.reference.child("users")
        // Reference to the current provider's clients
        val clientsRef = providerRef.child(formattedProviderEmail).child("clients")

        clientsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    clientsTableLayout.removeAllViews()
                    addTableHeader()

                    var rowIndex = 0

                    // Iterate over each email in the 'clients' node
                    for (clientSnapshot in snapshot.children) {
                        val clientEmail = clientSnapshot.getValue(String::class.java) ?: ""
                        Log.d("ClientEmail", "Client email: $clientEmail")

                        // Query the 'users' node for details about the client
                        usersRef.orderByChild("email").equalTo(clientEmail)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(userSnapshot: DataSnapshot) {
                                    if (userSnapshot.exists()) {
                                        for (user in userSnapshot.children) {
                                            val firstName = user.child("firstName").getValue(String::class.java) ?: ""
                                            val lastName = user.child("lastName").getValue(String::class.java) ?: ""

                                            val fullName = if (firstName.isNotEmpty() || lastName.isNotEmpty()) {
                                                "$firstName $lastName"
                                            } else {
                                                clientEmail // If no names, show the email
                                            }

                                            // Add the client row to the table
                                            addClientRow(fullName, clientEmail, rowIndex)
                                            rowIndex++
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("FirebaseError", "Error fetching user data: ${error.message}")
                                }
                            })
                    }
                } else {

                    Toast.makeText(this@ProviderRemoveClients, "No clients found for this provider", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProviderRemoveClients, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addTableHeader() {
        val headerRow = TableRow(this)

        val nameHeader = TextView(this).apply {
            text = "Client"
            textSize = 20f
            setPadding(16, 16, 16, 16)
            setTextColor(resources.getColor(android.R.color.black))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val actionHeader = TextView(this).apply {
            text = "Remove"
            textSize = 20f
            setPadding(16, 16, 16, 16)
            setTextColor(resources.getColor(android.R.color.black))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        headerRow.addView(nameHeader)
        headerRow.addView(actionHeader)
        clientsTableLayout.addView(headerRow)
    }

    private fun addClientRow(fullName: String, email: String, index: Int) {
        val tableRow = TableRow(this).apply {
            setPadding(8, 8, 8, 8)
            setBackgroundColor(if (index % 2 == 0) 0xFFF0E0B0.toInt() else 0xFFFFFFFF.toInt())
        }

        val clientNameTextView = TextView(this).apply {
            text = fullName
            textSize = 18f
            setPadding(16, 16, 16, 16)
            setTextColor(0xFF000000.toInt())
        }

        val clientRadioButton = RadioButton(this).apply {
            tag = email
            setOnClickListener {
                selectedRadioButton?.isChecked = false
                selectedRadioButton = this
            }
        }

        tableRow.addView(clientNameTextView)
        tableRow.addView(clientRadioButton)
        clientsTableLayout.addView(tableRow)
    }

    private fun deleteClientFromFirebase(email: String,formattedProviderEmail:String) {
        val providerRef = database.reference.child("Providers")
        val clientsRef = providerRef.child(formattedProviderEmail).child("clients")
        clientsRef.orderByValue().equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (clientSnapshot in snapshot.children) {
                        clientSnapshot.ref.removeValue()
                    }
                    Toast.makeText(
                        this@ProviderRemoveClients,
                        "Client removed from provider's list",
                        Toast.LENGTH_SHORT
                    ).show()
                    recreate()

                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@ProviderRemoveClients,
                        "Error: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}