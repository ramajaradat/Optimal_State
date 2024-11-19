package com.example.mental_state


import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mental_state.Model.UserHistory
import com.google.firebase.database.*

class currentclients : AppCompatActivity() {

    private lateinit var clientsTableLayout: TableLayout
    private lateinit var database: FirebaseDatabase
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_currentclients)

        // Use android.R.id.content to reference the root view directly
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance()
        clientsTableLayout = findViewById(R.id.clientsTableLayout)
        backButton = findViewById(R.id.btnBack)

        backButton.setOnClickListener {
            val intent = Intent(this, ProviderHomePage::class.java)
            startActivity(intent)
            finish()
        }

        // Load client data from Firebase
        loadClientData()
    }

    private fun loadClientData() {
        val userHistoryRef = database.reference.child("UserHistory")

        userHistoryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    clientsTableLayout.removeAllViews()
                    addTableHeader()

                    for (userHistorySnapshot in snapshot.children) {
                        // Loop through each user's history
                        for (historySnapshot in userHistorySnapshot.children) {
                            // Retrieve the fields from Firebase
                            val email = historySnapshot.child("email").getValue(String::class.java) ?: ""
                            val day = historySnapshot.child("day").getValue(Int::class.java) ?: 0
                            val month = historySnapshot.child("month").getValue(Int::class.java) ?: 0
                            val year = historySnapshot.child("year").getValue(Int::class.java) ?: 0
                            val time = historySnapshot.child("time").getValue(String::class.java) ?: ""
                            val status = historySnapshot.child("status").getValue(String::class.java) ?: ""

                            // Format date
                            val date = "$day/$month/$year"

                            // Add client data to the table
                            addClientRow(email, date, time, status)

                            // Log the retrieved data for debugging
                            Log.d("currentclients", "Email: $email, Date: $date, Time: $time, Status: $status")
                        }
                    }
                } else {
                    Toast.makeText(this@currentclients, "No client history found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@currentclients, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addTableHeader() {
        val headerRow = TableRow(this)

        val headers = listOf("Email", "Date", "Time", "Status")
        headers.forEach { headerText ->
            val header = TextView(this).apply {
                text = headerText
                textSize = 15f
                setPadding(10, 10, 10, 10)
                setTextColor(Color.BLACK)
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(0xFF000000.toInt())
            }
            headerRow.addView(header)
        }

        clientsTableLayout.addView(headerRow)
    }

    private fun addClientRow(email: String, date: String, time: String, status: String) {
        val tableRow = TableRow(this).apply {
            setPadding(8, 8, 8, 8)
        }

        // Email Column
        val emailTextView = TextView(this).apply {
            text = email
            textSize = 15f
            setPadding(10, 10, 10, 10)
            setTextColor(0xFF000000.toInt())
        }

        // Date Column
        val dateTextView = TextView(this).apply {
            text = date
            textSize = 12f
            setPadding(10, 10, 10, 10)
            setTextColor(0xFF000000.toInt())
        }

        // Time Column
        val timeTextView = TextView(this).apply {
            text = time
            textSize = 12f
            setPadding(10, 10, 10, 10)
            setTextColor(0xFF000000.toInt())
        }

        // Status Column
        val statusTextView = TextView(this).apply {
            text = status // Display status directly
            textSize = 12f
            setPadding(10, 10, 10, 10)
            setTextColor(0xFF000000.toInt())

        }

        // Add views to the row
        tableRow.addView(emailTextView)
        tableRow.addView(dateTextView)
        tableRow.addView(timeTextView)
        tableRow.addView(statusTextView)

        // Add row to the table layout
        clientsTableLayout.addView(tableRow)
    }
}
