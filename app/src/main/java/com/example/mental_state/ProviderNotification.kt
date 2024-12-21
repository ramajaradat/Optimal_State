package com.example.mental_state

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class ProviderNotification : AppCompatActivity() {

    private lateinit var tvNotifications: TextView
    private lateinit var backbutton:Button
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val notificationsList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Ensures edge-to-edge UI support
        setContentView(R.layout.activity_provider_notification)
        //initialize UI
        initisalizeUI()
        // Fetch provider's clients and check their latest status
        fetchClientsAndCheckStatus()
        // Set up back button click
        setupBackButton()
    }
    private fun initisalizeUI(){
        tvNotifications = findViewById(R.id.tvNotifications)
        backbutton = findViewById(R.id.backbutton)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

    }
    private fun setupBackButton(){
        backbutton.setOnClickListener {
            val intent = Intent(this@ProviderNotification, ProviderSettings::class.java)
            startActivity(intent)        }
    }
    // Fetch clients associated with the provider
    private fun fetchClientsAndCheckStatus() {
        val firebaseProvider = auth.currentUser
        val providerEmail1 = firebaseProvider?.email.toString()
        val formattedProviderEmail = providerEmail1.replace(".", "_").replace("@", "_")
        if (formattedProviderEmail != null) {
            val providerRef = database.getReference("Providers").child(formattedProviderEmail).child("clients")
            providerRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val clientEmails = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                        if (clientEmails.isNotEmpty()) {
                            clientEmails.forEach { clientEmail ->
                                checkClientStatus(clientEmail)
                            }
                        } else {
                            showNoClientsMessage()
                        }
                    } else {
                        showNoClientsMessage()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ProviderNotification, "Error fetching clients: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "Error: Provider email is null", Toast.LENGTH_SHORT).show()
        }
    }
    // Show a message if no clients are found
    private fun showNoClientsMessage() {
        tvNotifications.text = "No clients found for this provider."
    }
    // Check the latest status of a client that hasn't passed 24 hours
    private fun checkClientStatus(clientEmail: String) {
        val formattedEmail = clientEmail.replace(".", "_").replace("@", "_")
        val clientRef = database.getReference("UserHistory").child(formattedEmail)

        clientRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val lastStatusSnapshot = snapshot.children.firstOrNull()
                    if (lastStatusSnapshot != null) {
                        try {
                            val statusDate = lastStatusSnapshot.child("date").getValue(String::class.java) ?: ""
                            val statusTime = lastStatusSnapshot.child("time").getValue(String::class.java) ?: ""
                            val status = lastStatusSnapshot.child("status").getValue(String::class.java) ?: ""

                            val statusTimestamp = parseDateTimeToMillis(statusDate, statusTime)
                            val currentTimestamp = System.currentTimeMillis()

                            if (statusTimestamp != null && (currentTimestamp - statusTimestamp) <= 24 * 60 * 60 * 1000) {
                                val notificationMessage = "Client: $clientEmail, Status: $status"
                                notificationsList.add(notificationMessage)
                                updateNotificationsTextView()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@ProviderNotification, "Error parsing status data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProviderNotification, "Error fetching client status: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    // Parse date and time into a timestamp in milliseconds
    private fun parseDateTimeToMillis(date: String, time: String): Long? {
        return try {
            val dateParts = date.split("-").map { it.toInt() }
            val timeParts = time.split(":").map { it.toInt() }

            Calendar.getInstance().apply {
                set(Calendar.YEAR, dateParts[0])
                set(Calendar.MONTH, dateParts[1] - 1) // Month is 0-based
                set(Calendar.DAY_OF_MONTH, dateParts[2])
                set(Calendar.HOUR_OF_DAY, timeParts[0])
                set(Calendar.MINUTE, timeParts[1])
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        } catch (e: Exception) {
            null
        }
    }
    // Update the TextView with the list of notifications
    private fun updateNotificationsTextView() {
        val notifications = notificationsList.joinToString("\n")
        tvNotifications.text = if (notifications.isNotEmpty()) {
            notifications
        } else {
            "No recent status updates from clients."
        }
    }
}
