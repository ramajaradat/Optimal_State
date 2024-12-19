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

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProviderRemoveClients : AppCompatActivity() {
    private lateinit var clientsShowToremoveTableLayout: TableLayout
    private lateinit var database: FirebaseDatabase
    private var selectedRadioButton: RadioButton? = null
    private lateinit var ProviderBackRemoveClientButton: Button
    private lateinit var ProviderApplyRemoveClientButton: Button
    private lateinit var selectedClientName: String
    private lateinit var mFirebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_provider_remove_clients)
        //initialize Ui
        initializeUI()

        val firebaseProvider = mFirebaseAuth.currentUser
        val providerEmail = firebaseProvider?.email.toString()
        val formattedProviderEmail = providerEmail.replace(".", "_").replace("@", "_")


        //setup Button
        setupButton(formattedProviderEmail)
        //load clients from provider database
        loadClientsFromFirebase(formattedProviderEmail)
    }

    private fun initializeUI(){
        database = FirebaseDatabase.getInstance()
        clientsShowToremoveTableLayout = findViewById(R.id.clientsShowToremoveTableLayout)
        ProviderBackRemoveClientButton = findViewById(R.id.btnBack)
        ProviderApplyRemoveClientButton = findViewById(R.id.btnConfirm)
        mFirebaseAuth = FirebaseAuth.getInstance()
    }
    private fun setupButton(formattedProviderEmail:String){
        ProviderBackRemoveClientButton.setOnClickListener {
            val intent = Intent(this, ProviderHomePage::class.java)
            startActivity(intent)
            finish()
        }

        ProviderApplyRemoveClientButton.setOnClickListener {
            if (selectedRadioButton == null) {
                Toast.makeText(this, "Please select a client to remove", Toast.LENGTH_SHORT).show()
            } else {
                selectedClientName = selectedRadioButton?.tag.toString()
                deleteClientFromFirebase(selectedClientName,formattedProviderEmail)
            }
        }
    }
    private fun loadClientsFromFirebase(formattedProviderEmail:String) {
        val providerRef = database.reference.child("Providers")
        val usersRef = database.reference.child("users")
        val clientsRef = providerRef.child(formattedProviderEmail).child("clients")

        clientsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    clientsShowToremoveTableLayout.removeAllViews()
                    addTableHeader()

                    var rowIndex = 0

                    for (clientSnapshot in snapshot.children) {
                        val clientEmail = clientSnapshot.getValue(String::class.java) ?: ""
                        Log.d("ClientEmail", "Client email: $clientEmail")

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
                                                clientEmail
                                            }

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
        clientsShowToremoveTableLayout.addView(headerRow)
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
        clientsShowToremoveTableLayout.addView(tableRow)
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