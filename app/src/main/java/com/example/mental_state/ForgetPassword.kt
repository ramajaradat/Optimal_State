package com.example.mental_state

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ForgetPassword : AppCompatActivity() {
    private lateinit var emailtorestpass:EditText
    private lateinit var restpassbutton:Button
    private lateinit var BackrestButton:Button
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forget_password)

        emailtorestpass=findViewById(R.id.emailtorestpass)
        restpassbutton=findViewById(R.id.restpassbutton)
        BackrestButton=findViewById(R.id.BackrestButton)
        firebaseAuth = FirebaseAuth.getInstance()

        BackrestButton.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        // Set button click listener
        restpassbutton.setOnClickListener {
            val email = emailtorestpass.text.toString().trim()

            if (email.isEmpty()) {
                emailtorestpass.error = "Please enter your email"
                emailtorestpass.requestFocus()
            } else {
                sendPasswordResetEmail(email)
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
}