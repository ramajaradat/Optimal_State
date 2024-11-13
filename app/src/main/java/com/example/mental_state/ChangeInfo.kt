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
import java.util.Date


class ChangeInfo : AppCompatActivity() {
    private lateinit var firstnameedite:EditText
    private lateinit var secondnameedit:EditText
    private lateinit var dateedit:EditText
    private lateinit var setchangebutton:Button
    private lateinit var canclechangebutton:Button
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_change_info)

        firstnameedite = findViewById(R.id.firstnameedite)
        secondnameedit = findViewById(R.id.secondnameedit)
        dateedit = findViewById(R.id.dateedit)
        setchangebutton = findViewById(R.id.setchangebutton)
        canclechangebutton = findViewById(R.id.canclechangebutton)

        mFirebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()

        // Get the current user UID and load their data
        val currentUser = mFirebaseAuth.currentUser
        if (currentUser != null) {
            val userUid = currentUser.uid

            // Reference to the specific user's data
            val userRef = firebaseDatabase.reference.child("users").child(userUid)

            // Load user data into the EditText fields
            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // Set the original data into EditText fields
                    firstnameedite.setText(snapshot.child("firstName").getValue(String::class.java) ?: "")
                    secondnameedit.setText(snapshot.child("lastName").getValue(String::class.java) ?: "")
                    dateedit.setText(snapshot.child("dob").getValue(String::class.java) ?: "")
                } else {
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }

        // Cancel button action
        canclechangebutton.setOnClickListener {
            val intent = Intent(this@ChangeInfo, AccountSetting::class.java)
            startActivity(intent)
        }

        // Set change button action
        setchangebutton.setOnClickListener {
            val firstname = firstnameedite.text.toString().trim()
            val lastname = secondnameedit.text.toString().trim()
            val dob = dateedit.text.toString().trim()

            val nameRegex = Regex("^[a-zA-Z]+$")
            if (!nameRegex.matches(firstname)) {
                firstnameedite.error = "Firstname should contain only letters"
                firstnameedite.requestFocus()
                return@setOnClickListener
            }

            if (!nameRegex.matches(lastname)) {
                secondnameedit.error = "Lastname should contain only letters"
                secondnameedit.requestFocus()
                return@setOnClickListener
            }

            val dobRegex = Regex("^(\\d{1,2})/(\\d{1,2})/(\\d{4})$")
            if (!dobRegex.matches(dob)) {
                dateedit.error = "Date of Birth should be in dd/MM/yyyy or d/M/yyyy format"
                dateedit.requestFocus()
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
                        // Update successful, navigate to AccountSetting or show a success message
                        startActivity(Intent(this@ChangeInfo, AccountSetting::class.java))
                    } else {
                        // Handle errors
                        Toast.makeText(this, "Failed to update. Try again!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            }
        }
    }
}