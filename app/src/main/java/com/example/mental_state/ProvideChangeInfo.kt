package com.example.mental_state


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class ProvideChangeInfo : AppCompatActivity() {

    private lateinit var firstnameEdit: EditText
    private lateinit var lastnameEdit: EditText
    private lateinit var dateEdit: EditText
    private lateinit var setChangeButton: Button
    private lateinit var cancelChangeButton: Button
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_provier_change_info)

        // Initialize UI
        initializeUI()
        // Load current user data
        loadUserData()
        // Setup button
        setupButton()

    }

    private fun initializeUI(){
        firstnameEdit = findViewById(R.id.firstnameedite)
        lastnameEdit = findViewById(R.id.secondnameedit)
        dateEdit = findViewById(R.id.dateedit)
        setChangeButton = findViewById(R.id.setchangebutton)
        cancelChangeButton = findViewById(R.id.canclechangebutton)
        mFirebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
    }
    private fun setupButton(){
        cancelChangeButton.setOnClickListener {
            val intent = Intent(this@ProvideChangeInfo, ProviderAccountSetting::class.java)
            startActivity(intent)    }

        // Set up change button action
        setChangeButton.setOnClickListener {
            val firstname = firstnameEdit.text.toString().trim()
            val lastname = lastnameEdit.text.toString().trim()
            val dob = dateEdit.text.toString().trim()

            val nameRegex = Regex("^[a-zA-Z]+$")
            if (!nameRegex.matches(firstname)) {
                firstnameEdit.error = "Firstname should contain only letters"
                firstnameEdit.requestFocus()
                return@setOnClickListener
            }

            if (!nameRegex.matches(lastname)) {
                lastnameEdit.error = "Lastname should contain only letters"
                lastnameEdit.requestFocus()
                return@setOnClickListener
            }

            val dobRegex = Regex("^(\\d{1,2})/(\\d{1,2})/(\\d{4})$")
            if (!dobRegex.matches(dob)) {
                dateEdit.error = "Date of Birth should be in dd/MM/yyyy or d/M/yyyy format"
                dateEdit.requestFocus()
                return@setOnClickListener
            }
            val matchResult = dobRegex.find(dob)
            val (dayString, monthString, yearString) = matchResult?.destructured ?: return@setOnClickListener
            val day = dayString.toInt()
            val month = monthString.toInt()
            val year = yearString.toInt()

            if (month !in 1..12) {
                dateEdit.error = "Month should be between 1 and 12"
                dateEdit.requestFocus()
                return@setOnClickListener
            }

            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            if (year !in 1900..currentYear) {
                dateEdit.error = "Year should be between 1900 and $currentYear"
                dateEdit.requestFocus()
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
                dateEdit.error = "Day should be valid for the given month"
                dateEdit.requestFocus()
                return@setOnClickListener
            }

            val currentUser = mFirebaseAuth.currentUser
            if (currentUser != null) {
                val userUid = currentUser.uid

                val userRef = firebaseDatabase.reference.child("users").child(userUid)

                userRef.child("firstName").setValue(firstname)
                userRef.child("lastName").setValue(lastname)
                userRef.child("dob").setValue(dob).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        startActivity(
                            Intent(
                                this@ProvideChangeInfo,
                                ProviderAccountSetting::class.java
                            )
                        )
                    } else {
                        Toast.makeText(this, "Failed to update. Try again!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } else {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            }
        }}
    // Function to load user data from Firebase
    private fun loadUserData() {
        val currentUser = mFirebaseAuth.currentUser
        if (currentUser != null) {
            val userUid = currentUser.uid
            val userRef = firebaseDatabase.reference.child("users").child(userUid)

            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    firstnameEdit.setText(snapshot.child("firstName").getValue(String::class.java) ?: "")
                    lastnameEdit.setText(snapshot.child("lastName").getValue(String::class.java) ?: "")
                    dateEdit.setText(snapshot.child("dob").getValue(String::class.java) ?: "")
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

}
