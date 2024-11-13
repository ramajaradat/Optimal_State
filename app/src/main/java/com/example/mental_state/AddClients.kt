package com.example.mental_state

import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.mentalstate.Model.UserInformation
import com.example.mentalstate.util.CustomToast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddClients : AppCompatActivity() {
    private lateinit var backButton: Button
    private lateinit var emailEditText: EditText
    private lateinit var addClientButton: Button
    private lateinit var database: FirebaseDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_clintes)
        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance()
        emailEditText = findViewById(R.id.emailEditText)
        addClientButton = findViewById(R.id.addClientButton)
        backButton = findViewById(R.id.backButton)

        backButton.setOnClickListener {
            val intent = Intent(this, ProviderHomePage::class.java)
            startActivity(intent)
            finish()
        }

        addClientButton.setOnClickListener {
            val email = emailEditText.text.toString().trim() // Move email assignment here

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.error = "Please enter a valid email address"
                emailEditText.requestFocus()
                return@setOnClickListener
            }

            val databaseRef = FirebaseDatabase.getInstance().getReference("users")
            databaseRef.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(
                                this@AddClients,
                                "Email already exists. Please try again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val userInformation = UserInformation(
                                firstName = "",
                                lastName = "",
                                dob = "",
                                email = email,
                                password = "",
                                isprovider = "no"
                            )
                            addUserToFirebase(userInformation)
                        }
                    }

                    override fun onCancelled(p0: DatabaseError) {
                        Toast.makeText(
                            this@AddClients,"Database error: ${p0.message}",Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun addUserToFirebase(userInformation: UserInformation) {
        val databaseRef = database.getReference("users")
        val userId = databaseRef.push().key

        if (userId != null) {
            databaseRef.child(userId).setValue(userInformation)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        CustomToast.createToast(this@AddClients, "Client added successfully!", false)
                    } else {
                        CustomToast.createToast(this@AddClients, "Failed to add client. Please try again.", true)
                    }
                }
        }
    }
}
