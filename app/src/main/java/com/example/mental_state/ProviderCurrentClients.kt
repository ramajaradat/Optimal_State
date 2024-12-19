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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProviderCurrentClients : AppCompatActivity() {

    private lateinit var clientsStatusTableLayout: TableLayout
    private lateinit var database: FirebaseDatabase
    private lateinit var backShowClientStatusButton: Button
    private lateinit var mFirebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_provider_current_clients)
        // Initialize UI
        initializeUI()
        //setup button
        setupButton()

        val firebaseProvider = mFirebaseAuth.currentUser
        val providerEmail = firebaseProvider?.email.toString()
        val formattedProviderEmail = providerEmail.replace(".", "_").replace("@", "_")

        // Load client data from Firebase
        loadClientData(formattedProviderEmail)
    }

    private fun initializeUI(){
        database = FirebaseDatabase.getInstance()
        clientsStatusTableLayout = findViewById(R.id.clientsStatusTableLayout)
        backShowClientStatusButton = findViewById(R.id.btnBack)
        mFirebaseAuth = FirebaseAuth.getInstance()
    }
    private fun setupButton(){
        backShowClientStatusButton.setOnClickListener {
            val intent = Intent(this, ProviderHomePage::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun loadClientData(formattedProviderEmail:String) {
        val userHistoryRef = database.reference.child("UserHistory")
        val providerRef = database.reference.child("Providers")

        providerRef.child(formattedProviderEmail).child("clients").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(providerSnapshot: DataSnapshot) {
                val clientEmails = mutableSetOf<String>()
                for (clientSnapshot in providerSnapshot.children) {
                    val clientEmail = clientSnapshot.getValue(String::class.java) ?: ""
                    if (clientEmail.isNotEmpty()) {
                        clientEmails.add(clientEmail)
                    }
                }

                userHistoryRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        clientsStatusTableLayout.removeAllViews()
                        addTableHeader()

                        if (snapshot.exists()) {
                            var hasData = false

                            for (userHistorySnapshot in snapshot.children) {
                                for (historySnapshot in userHistorySnapshot.children) {
                                    val email = historySnapshot.child("email").getValue(String::class.java) ?: ""
                                    val day = historySnapshot.child("day").getValue(Int::class.java) ?: 0
                                    val month = historySnapshot.child("month").getValue(Int::class.java) ?: 0
                                    val year = historySnapshot.child("year").getValue(Int::class.java) ?: 0
                                    val time = historySnapshot.child("time").getValue(String::class.java) ?: ""
                                    val status = historySnapshot.child("status").getValue(String::class.java) ?: ""

                                    if (clientEmails.contains(email) && email.isNotEmpty()) {
                                        val date = "$day/$month/$year"
                                        addClientRow(email, date, time, status)
                                        hasData = true
                                    }
                                }
                            }

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
                setBackgroundColor(Color.parseColor("#FFB74D"))
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            }
            headerRow.addView(header)
        }

        clientsStatusTableLayout.addView(headerRow)
    }
    private fun addClientRow(email: String, date: String, time: String, status: String) {
        val tableRow = TableRow(this).apply {
            setPadding(4, 4, 4, 4)
        }

        val rowBackgroundColor = Color.parseColor("#F5F5F5")
        tableRow.setBackgroundColor(rowBackgroundColor)

        val columns = listOf(email, date, time, status)
        columns.forEach { columnText ->
            val textView = TextView(this).apply {
                text = columnText
                textSize = 16f
                setPadding(16, 16, 16, 16)
                setTextColor(Color.BLACK)
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            }
            tableRow.addView(textView)
        }

        clientsStatusTableLayout.addView(tableRow)

        val spacer = TableRow(this).apply {
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                10
            )
        }
        clientsStatusTableLayout.addView(spacer)
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
                span = 4
            }
            gravity = android.view.Gravity.CENTER
        }

        placeholderRow.addView(placeholder)
        clientsStatusTableLayout.addView(placeholderRow)
    }
}





