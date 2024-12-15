package com.example.mental_state


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

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

        // Initialize views
        firstnameEdit = findViewById(R.id.firstnameedite)
        lastnameEdit = findViewById(R.id.secondnameedit)
        dateEdit = findViewById(R.id.dateedit)
        setChangeButton = findViewById(R.id.setchangebutton)
        cancelChangeButton = findViewById(R.id.canclechangebutton)

        // Initialize Firebase Authentication and Database
        mFirebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()

        // Load current user data
        loadUserData()

        // Set up cancel button action
        cancelChangeButton.setOnClickListener {
            navigateToAccountSettings()
        }

        // Set up change button action
        setChangeButton.setOnClickListener {
            updateUserData()
        }
    }

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
                    showToast("User data not found")
                }
            }.addOnFailureListener {
                showToast("Failed to load user data")
            }
        } else {
            showToast("User not authenticated")
        }
    }

    // Function to handle updating user data
    private fun updateUserData() {
        val firstname = firstnameEdit.text.toString().trim()
        val lastname = lastnameEdit.text.toString().trim()
        val dob = dateEdit.text.toString().trim()

        // Validate input fields
        if (!isValidName(firstname)) {
            firstnameEdit.error = "Firstname should contain only letters"
            firstnameEdit.requestFocus()
            return
        }

        if (!isValidName(lastname)) {
            lastnameEdit.error = "Lastname should contain only letters"
            lastnameEdit.requestFocus()
            return
        }

        if (!isValidDate(dob)) {
            dateEdit.error = "Date of Birth should be in dd/MM/yyyy or d/M/yyyy format"
            dateEdit.requestFocus()
            return
        }

        val currentUser = mFirebaseAuth.currentUser
        if (currentUser != null) {
            val userUid = currentUser.uid
            val userRef = firebaseDatabase.reference.child("users").child(userUid)

            // Update user data in Firebase
            val updates = mapOf(
                "firstName" to firstname,
                "lastName" to lastname,
                "dob" to dob
            )

            userRef.updateChildren(updates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Information updated successfully")
                    navigateToAccountSettings()
                } else {
                    showToast("Failed to update. Please try again.")
                }
            }
        } else {
            showToast("User not authenticated")
        }
    }

    // Helper function for name validation
    private fun isValidName(name: String): Boolean {
        val nameRegex = Regex("^[a-zA-Z]+$")
        return nameRegex.matches(name)
    }

    // Helper function for date validation
    private fun isValidDate(date: String): Boolean {
        val dobRegex = Regex("^(\\d{1,2})/(\\d{1,2})/(\\d{4})$")
        return dobRegex.matches(date)
    }

    // Helper function to show a toast message
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Function to navigate back to the account settings screen
    private fun navigateToAccountSettings() {
        startActivity(Intent(this, ProviderAccountSetting::class.java))
        finish()
    }
}
