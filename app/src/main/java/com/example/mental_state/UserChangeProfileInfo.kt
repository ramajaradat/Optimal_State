package com.example.mental_state

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar


class UserChangeProfileInfo : AppCompatActivity() {
    private lateinit var UserFirstNameChange: EditText
    private lateinit var UserSecondNameChange: EditText
    private lateinit var UserBirthdayEdite: EditText
    private lateinit var UserApplyProfileChangeButton: Button
    private lateinit var UserCancelChangeProfileChange: Button
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_change_profile_info)
        //Initialize UI&Firrebase
        initializeUI()
        // Get the current user UID and load user data
        loadCurrentUserData()
        //setup Button
        setupButton()


    }

    private fun initializeUI(){
        UserFirstNameChange = findViewById(R.id.UserFirstNameChange)
        UserSecondNameChange = findViewById(R.id.UserSecondNameChange)
        UserBirthdayEdite = findViewById(R.id.UserBirthdayEdite)
        UserApplyProfileChangeButton = findViewById(R.id.UserApplyProfileChangeButton)
        UserCancelChangeProfileChange = findViewById(R.id.UserCancelChangeProfileChange)
        mFirebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
    }

    private fun loadCurrentUserData() {
        val currentUser = mFirebaseAuth.currentUser
        if (currentUser != null) {
            val userUid = currentUser.uid

            // Reference to the specific user's data
            val userRef = firebaseDatabase.reference.child("users").child(userUid)

            // Load user data into the EditText fields
            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // Set the original data into EditText fields
                    UserFirstNameChange.setText(
                        snapshot.child("firstName").getValue(String::class.java) ?: ""
                    )
                    UserSecondNameChange.setText(
                        snapshot.child("lastName").getValue(String::class.java) ?: ""
                    )
                    UserBirthdayEdite.setText(
                        snapshot.child("dob").getValue(String::class.java) ?: ""
                    )
                } else {
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupButton() {
        // Cancel button action
        UserCancelChangeProfileChange.setOnClickListener {
            val intent = Intent(this@UserChangeProfileInfo, UserAccountSetting::class.java)
            startActivity(intent)
        }
        UserApplyProfileChangeButton.setOnClickListener {
            val firstname = UserFirstNameChange.text.toString().trim()
            val lastname = UserSecondNameChange.text.toString().trim()
            val dob = UserBirthdayEdite.text.toString().trim()

            val nameRegex = Regex("^[a-zA-Z]+$")
            if (!nameRegex.matches(firstname)) {
                UserFirstNameChange.error = "Firstname should contain only letters"
                UserFirstNameChange.requestFocus()
                return@setOnClickListener
            }

            if (!nameRegex.matches(lastname)) {
                UserSecondNameChange.error = "Lastname should contain only letters"
                UserSecondNameChange.requestFocus()
                return@setOnClickListener
            }

            val dobRegex = Regex("^(\\d{1,2})/(\\d{1,2})/(\\d{4})$")
            if (!dobRegex.matches(dob)) {
                UserBirthdayEdite.error = "Date of Birth should be in dd/MM/yyyy or d/M/yyyy format"
                UserBirthdayEdite.requestFocus()
                return@setOnClickListener
            }
            val matchResult = dobRegex.find(dob)
            val (dayString, monthString, yearString) = matchResult?.destructured ?: return@setOnClickListener
            //test the date valid or not
            val day = dayString.toInt()
            val month = monthString.toInt()
            val year = yearString.toInt()

            if (month !in 1..12) {
                UserBirthdayEdite.error = "Month should be between 1 and 12"
                UserBirthdayEdite.requestFocus()
                return@setOnClickListener
            }

            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            if (year !in 1900..currentYear) {
                UserBirthdayEdite.error = "Year should be between 1900 and $currentYear"
                UserBirthdayEdite.requestFocus()
                return@setOnClickListener
            }

            val isLeapYear = (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0))

            val maxDaysInMonth = when (month) {
                1, 3, 5, 7, 8, 10, 12 -> 31
                4, 6, 9, 11 -> 30
                2 -> if (isLeapYear) 29 else 28 // February
                else -> 0
            }

            if (day !in 1..maxDaysInMonth) {
                UserBirthdayEdite.error = "Day should be valid for the given month"
                UserBirthdayEdite.requestFocus()
                return@setOnClickListener
            }

            // Get the current user UID to update data
            val currentUser = mFirebaseAuth.currentUser
            if (currentUser != null) {
                val userUid = currentUser.uid

                // Reference to the user's data
                val userRef = firebaseDatabase.reference.child("users").child(userUid)

                // Update the fields in Firebase
                userRef.child("firstName").setValue(firstname)
                userRef.child("lastName").setValue(lastname)
                userRef.child("dob").setValue(dob).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Update successful, navigate to UserAccountSetting or show a success message
                        startActivity(
                            Intent(
                                this@UserChangeProfileInfo,
                                UserAccountSetting::class.java
                            )
                        )
                    } else {
                        // Handle errors
                        Toast.makeText(this, "Failed to update. Try again!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } else {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            }

        }

    }

}