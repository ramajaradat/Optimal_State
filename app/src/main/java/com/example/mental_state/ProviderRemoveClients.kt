package com.example.mental_state

import android.content.Intent
import android.os.Bundle
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



        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContentView(R.layout.activity_provider_remove_clients)

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

            // Initialize Firebase Database
            database = FirebaseDatabase.getInstance()
            clientsTableLayout = findViewById(R.id.clientsTableLayout)
            backButton = findViewById(R.id.btnBack)
            confirmButton = findViewById(R.id.btnConfirm)

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
                    deleteClientFromFirebase(selectedClientName)
                }
            }

            // Load clients from Firebase
            loadClientsFromFirebase()
        }

        private fun loadClientsFromFirebase() {
            val usersRef = database.reference.child("users")

            usersRef.orderByChild("isprovider").equalTo("no")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            clientsTableLayout.removeAllViews()
                            addTableHeader()

                            var rowIndex = 0
                            for (userSnapshot in snapshot.children) {
                                val firstName = userSnapshot.child("firstName").getValue(String::class.java) ?: ""
                                val lastName = userSnapshot.child("lastName").getValue(String::class.java) ?: ""
                                val email = userSnapshot.child("email").getValue(String::class.java) ?: ""

                                val fullName = if (firstName.isEmpty() && lastName.isEmpty()) {
                                    email
                                } else {
                                    "$firstName $lastName"
                                }

                                addClientRow(fullName, email, rowIndex)
                                rowIndex++
                            }
                        } else {
                            Toast.makeText(this@ProviderRemoveClients, "No clients found", Toast.LENGTH_SHORT).show()
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

        private fun deleteClientFromFirebase(email: String) {
            val usersRef = database.reference.child("users")
            val userHistoryRef = database.reference.child("UserHistory")

            // Delete from 'users'
            usersRef.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (userSnapshot in snapshot.children) {
                            userSnapshot.ref.removeValue()
                        }
                        Toast.makeText(this@ProviderRemoveClients, "Client removed from users", Toast.LENGTH_SHORT).show()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@ProviderRemoveClients, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })

            // Delete from 'UserHistory'
            userHistoryRef.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (historySnapshot in snapshot.children) {
                            historySnapshot.ref.removeValue()
                        }
                        Toast.makeText(this@ProviderRemoveClients, "Client removed from UserHistory", Toast.LENGTH_SHORT).show()
                        recreate()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@ProviderRemoveClients, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
