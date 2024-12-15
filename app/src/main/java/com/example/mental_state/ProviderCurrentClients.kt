package com.example.mental_state


import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.*

class ProviderCurrentClients : AppCompatActivity() {

    private lateinit var clientsTableLayout: TableLayout
    private lateinit var database: FirebaseDatabase
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_provider_current_clients)

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

        // Back button functionality
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
                // Clear table and add header
                clientsTableLayout.removeAllViews()
                addTableHeader()

                if (snapshot.exists()) {
                    var hasData = false

                    // Loop through users' history data
                    for (userHistorySnapshot in snapshot.children) {
                        for (historySnapshot in userHistorySnapshot.children) {
                            val email = historySnapshot.child("email").getValue(String::class.java) ?: ""
                            val day = historySnapshot.child("day").getValue(Int::class.java) ?: 0
                            val month = historySnapshot.child("month").getValue(Int::class.java) ?: 0
                            val year = historySnapshot.child("year").getValue(Int::class.java) ?: 0
                            val time = historySnapshot.child("time").getValue(String::class.java) ?: ""
                            val status = historySnapshot.child("status").getValue(String::class.java) ?: ""

                            if (email.isNotEmpty()) {
                                val date = "$day/$month/$year"
                                addClientRow(email, date, time, status)
                                hasData = true
                            }
                        }
                    }

                    // Show placeholder if no valid data exists
                    if (!hasData) {
                        showPlaceholderRow()
                    }
                } else {
                    showPlaceholderRow()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProviderCurrentClients, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addTableHeader() {
        val headerRow = TableRow(this)

        val headers = listOf("Email", "Date", "Time", "Status")
        headers.forEach { headerText ->
            val header = TextView(this).apply {
                text = headerText
                textSize = 16f
                setPadding(10, 10, 10, 10)
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.parseColor("#FFB74D")) // Purple header background
                setTypeface(null, android.graphics.Typeface.BOLD)
                // Ensures that columns are wide enough to display text
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            }
            headerRow.addView(header)
        }

        // Add header to the table layout
        clientsTableLayout.addView(headerRow)
    }

    private fun addClientRow(email: String, date: String, time: String, status: String) {
        val tableRow = TableRow(this).apply {
            setPadding(4, 4, 4, 4)
        }

        val rowBackgroundColor = Color.parseColor("#F5F5F5") // Light gray background
        tableRow.setBackgroundColor(rowBackgroundColor)

        // Create TextViews for each column with dynamic width adjustment
        val columns = listOf(email, date, time, status)
        columns.forEach { columnText ->
            val textView = TextView(this).apply {
                text = columnText
                textSize = 16f
                setPadding(16, 16, 16, 16)
                setTextColor(Color.BLACK)
                // Ensures that columns have even space distribution
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            }
            tableRow.addView(textView)
        }

        // Add row to the table layout
        clientsTableLayout.addView(tableRow)

        // Add spacing between rows
        val spacer = TableRow(this).apply {
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                10 // Spacer height
            )
        }
        clientsTableLayout.addView(spacer)
    }

    private fun showPlaceholderRow() {
        val placeholderRow = TableRow(this)

        val placeholder = TextView(this).apply {
            text = "No client data available"
            textSize = 16f
            setPadding(16, 16, 16, 16)
            setTextColor(Color.GRAY)
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            ).apply {
                span = 4 // Span across all columns
            }
            gravity = android.view.Gravity.CENTER
        }

        placeholderRow.addView(placeholder)
        clientsTableLayout.addView(placeholderRow)
    }
}