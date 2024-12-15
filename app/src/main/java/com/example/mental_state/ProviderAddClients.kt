

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

class ProviderAddClients : AppCompatActivity() {
    private lateinit var BackAddClient: Button
    private lateinit var EnterUserEmail: EditText
    private lateinit var ApplyAddClinetButton: Button
    private lateinit var database: FirebaseDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_provider_add_clintes)
        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance()
        EnterUserEmail = findViewById(R.id.EnterUserEmail)
        ApplyAddClinetButton = findViewById(R.id.ApplyAddClinetButton)
        BackAddClient = findViewById(R.id.BackAddClient)

        BackAddClient.setOnClickListener {
            val intent = Intent(this, ProviderHomePage::class.java)
            startActivity(intent)
            finish()
        }

        ApplyAddClinetButton.setOnClickListener {
            val email = EnterUserEmail.text.toString().trim() // Move email assignment here

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                EnterUserEmail.error = "Please enter a valid email address"
                EnterUserEmail.requestFocus()
                return@setOnClickListener
            }

            val databaseRef = FirebaseDatabase.getInstance().getReference("users")
            databaseRef.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(
                                this@ProviderAddClients,
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
                            this@ProviderAddClients,"Database error: ${p0.message}",Toast.LENGTH_SHORT).show()
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
                        CustomToast.createToast(this@ProviderAddClients, "Client added successfully!", false)
                    } else {
                        CustomToast.createToast(this@ProviderAddClients, "Failed to add client. Please try again.", true)
                    }
                }
        }
    }
}
