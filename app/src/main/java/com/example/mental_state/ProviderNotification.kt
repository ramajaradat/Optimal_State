package com.example.mental_state

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.log

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
                                getClientsUid(clientEmail)
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
    //get user uid
   private fun getClientsUid(email: String) {
        val userInfoRef = database.getReference("users")

        userInfoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    val userEmail = userSnapshot.child("email").getValue(String::class.java)
                    if (userEmail != null && userEmail.equals(email, ignoreCase = true)) {
                        checkClientStatus(userSnapshot.key.toString(),email)
                        return
                    }
                }
                tvNotifications.text ="Email not has UID"
            }

            override fun onCancelled(error: DatabaseError) {
                tvNotifications.text ="Error fetching UID: ${error.message}"
            }
        })
    }
    // Check the latest status of a client that hasn't passed 24 hours
    private fun checkClientStatus(clientuid: String, clientEmail:String) {
        val clientRef = database.getReference("UserHistory").child(clientuid)

        clientRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val lastStatusSnapshot = snapshot.children.firstOrNull()
                    if (lastStatusSnapshot != null) {

                        val status = lastStatusSnapshot.child("status").getValue(String::class.java) ?: ""
                        val day = lastStatusSnapshot.child("day").getValue(Int::class.java) ?: 0
                        val month = lastStatusSnapshot.child("month").getValue(Int::class.java) ?: 0
                        val year = lastStatusSnapshot.child("year").getValue(Int::class.java) ?: 0
                        val time = lastStatusSnapshot.child("time").getValue(String::class.java) ?: "12:00 AM"
                        val date="$day-$month-$year $time"
                        val notificationMessage = "Client: $clientEmail, \nStatus: $status \nDate: $date\n\n"
                        notificationsList.add(notificationMessage)
                        updateNotificationsTextView()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProviderNotification, "Error fetching client status: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
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
