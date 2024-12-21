package com.example.mental_state

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Locale
import android.widget.Toast
import java.util.Calendar


class UserNotification : AppCompatActivity() {

    private lateinit var tvNotifications: TextView
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_notification)

        // initialize UI
        initializeUI()
        // setup back button
        setupBackButton()
        // Check and display notifications
        checkAndShowNotification()
    }

    private fun initializeUI(){
        tvNotifications = findViewById(R.id.tvNotifications)
        backButton = findViewById(R.id.backbutton)
    }
    private fun setupBackButton(){
        backButton.setOnClickListener {
            val intent = Intent(this@UserNotification, UserSettingPage::class.java)
            startActivity(intent)
        }
    }
    private fun checkAndShowNotification() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            showToast("Please log in to check notifications.")
            return
        }

        val userUid = currentUser.uid
        val userHistoryRef = FirebaseDatabase.getInstance().getReference("UserHistory").child(userUid)

        userHistoryRef.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    var latestDay: Int? = null
                    var latestMonth: Int? = null
                    var latestYear: Int? = null
                    var latestTime: String? = null
                    var latestStatus: String? = null

                    for (child in snapshot.children) {
                        val day = child.child("day").getValue(Int::class.java)
                        val month = child.child("month").getValue(Int::class.java)
                        val year = child.child("year").getValue(Int::class.java)
                        val time = child.child("time").getValue(String::class.java)
                        val status = child.child("status").getValue(String::class.java)

                        Log.d("UserHistory", "Found history entry: $day/$month/$year $time $status")

                        if (day != null && month != null && year != null && time != null && status != null) {
                            latestDay = day
                            latestMonth = month
                            latestYear = year
                            latestTime = time
                            latestStatus = status
                        }
                    }

                    if (latestDay != null && latestMonth != null && latestYear != null && latestTime != null && latestStatus != null) {
                        handleStatusUpdate(latestDay, latestMonth, latestYear, latestTime, latestStatus)
                    } else {
                        tvNotifications.text = "Incomplete data in user history."
                    }
                } else {
                    tvNotifications.text = "No user history found."
                }
            }
            .addOnFailureListener {
                showToast("Failed to retrieve user history.")
            }
    }
    private fun handleStatusUpdate(day: Int, month: Int, year: Int, time: String, status: String) {
        try {
            val dateTimeString = "$year-$month-$day $time"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
            val lastTime = dateFormat.parse(dateTimeString)
            val currentTime = Calendar.getInstance().time

            if (lastTime != null) {
                val timeDifference = currentTime.time - lastTime.time

                if (timeDifference > 24 * 60 * 60 * 1000) {
                    tvNotifications.text = "24 hours have passed since your last status update."
                } else {
                    tvNotifications.text = "Your status is up to date: $status"
                }
            } else {
                tvNotifications.text = "Invalid date format in status update."
            }
        } catch (e: Exception) {
            tvNotifications.text = "Error processing the status update."
            showToast("An error occurred while checking the status: ${e.message}")
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
