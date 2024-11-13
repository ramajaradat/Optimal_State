package com.example.mental_state

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ChangeEmail : AppCompatActivity() {
    private lateinit var currentEmailField: TextView
    private lateinit var newEmailField: EditText
    private lateinit var currentPasswordField: EditText
    private lateinit var email_edit_button: Button
    private lateinit var email_cancle_button: Button
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_email)

        currentEmailField = findViewById(R.id.currentemail)
        newEmailField = findViewById(R.id.newemail)
        currentPasswordField = findViewById(R.id.currentPassword)
        email_edit_button = findViewById(R.id.email_edit_button)
        email_cancle_button = findViewById(R.id.email_cancle_button)

        mFirebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()

        email_cancle_button.setOnClickListener {
            val intent = Intent(this@ChangeEmail, AccountSetting::class.java)
            startActivity(intent)
        }

        val currentUser = mFirebaseAuth.currentUser
        currentUser?.let {
            // Populate current email field with user's existing email
            currentEmailField.setText(it.email)

            email_edit_button.setOnClickListener {
                val currentEmail = currentEmailField.text.toString().trim()
                val newEmail = newEmailField.text.toString().trim()
                val password = currentPasswordField.text.toString().trim()

                // Validate new email format
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                    newEmailField.error = "Please enter a valid email address"
                    newEmailField.requestFocus()
                    return@setOnClickListener
                }

                // Ensure password is entered
                if (password.isEmpty()) {
                    currentPasswordField.error = "Please enter your current password"
                    currentPasswordField.requestFocus()
                    return@setOnClickListener
                }

                // Reauthenticate user with current email and password
                val credential = EmailAuthProvider.getCredential(currentEmail, password)
                currentUser.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        // Proceed with updating email in Firebase Authentication
                        currentUser.updateEmail(newEmail).addOnCompleteListener { emailUpdateTask ->
                            if (emailUpdateTask.isSuccessful) {
                                // Update email in Firebase Realtime Database
                                val userRef = firebaseDatabase.reference.child("users").child(currentUser.uid)
                                userRef.child("email").setValue(newEmail)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Email updated successfully in Firebase and Realtime Database.", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { dbError ->
                                        Toast.makeText(this, "Failed to update email in Realtime Database: ${dbError.message}", Toast.LENGTH_SHORT).show()
                                        Log.e("ChangeEmail", "Database Error: ${dbError.message}")
                                    }
                            } else {
                                // Log and show email update error
                                Toast.makeText(this, "Failed to update email in Firebase: ${emailUpdateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                Log.e("ChangeEmail", "Auth Email Update Error: ${emailUpdateTask.exception?.message}")
                            }
                        }
                    } else {
                        // Log and show reauthentication error
                        Toast.makeText(this, "Reauthentication failed: ${reauthTask.exception?.message}", Toast.LENGTH_LONG).show()
                        Log.e("ChangeEmail", "Reauthentication Error: ${reauthTask.exception?.message}")
                    }
                }
            }
        } ?: Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()


    }
}
