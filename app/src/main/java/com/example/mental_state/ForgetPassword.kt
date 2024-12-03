package com.example.mental_state

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ForgetPassword : AppCompatActivity() {
    private lateinit var emailtorestpass:EditText
    private lateinit var editTextTextPassword:EditText
    private lateinit var restpassbutton:Button
    private lateinit var BackrestButton:Button
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forget_password)

        emailtorestpass=findViewById(R.id.emailtorestpass)
        editTextTextPassword=findViewById(R.id.editTextTextPassword)
        restpassbutton=findViewById(R.id.restpassbutton)
        BackrestButton=findViewById(R.id.BackrestButton)
        firebaseAuth = FirebaseAuth.getInstance()

        BackrestButton.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        restpassbutton.setOnClickListener {
            val email = emailtorestpass.text.toString().trim()

            if (email.isEmpty()) {
                emailtorestpass.error = "Please enter your email"
                emailtorestpass.requestFocus()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailtorestpass.error = "Please enter a valid email address"
                emailtorestpass.requestFocus()
                return@setOnClickListener
            }

            checkEmailExistsInDatabase(email) { exists ->
                if (exists) {
                    sendPasswordResetEmail(email)
                    promptForNewPassword(email)
                } else {
                    emailtorestpass.error = "Email not found in the database"
                    emailtorestpass.requestFocus()
                }
            }
        }
    }

    private fun sendPasswordResetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset email sent", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun promptForNewPassword(email: String) {
        val newPassword = editTextTextPassword.text.toString().trim()

        // Update the new password in Firebase Authentication
        firebaseAuth.signInWithEmailAndPassword(email, newPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updatePasswordInDatabase(email, newPassword)
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updatePasswordInDatabase(email: String, newPassword: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("users")

        databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        child.ref.child("password").setValue(newPassword)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this@ForgetPassword, "Password updated in database", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this@ForgetPassword, "Failed to update password: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ForgetPassword, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkEmailExistsInDatabase(email: String, callback: (Boolean) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("users")

        databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(snapshot.exists())
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ForgetPassword, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
        })
    }
}