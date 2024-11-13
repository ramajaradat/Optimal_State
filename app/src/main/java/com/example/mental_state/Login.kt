package com.example.mental_state


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.mentalstate.Model.UserInformation
import com.example.mentalstate.util.CustomToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Login : AppCompatActivity() {

    private lateinit var signupbutton: Button
    private lateinit var loginbutton: Button
    private lateinit var tvusername: EditText
    private lateinit var tvpassword: EditText
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        // Initialize Firebase Auth and Database
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()

        signupbutton = findViewById(R.id.signupbutton)
        loginbutton = findViewById(R.id.loginbutton)
        tvusername = findViewById(R.id.tvusername)
        tvpassword = findViewById(R.id.tvpassword)

        signupbutton.setOnClickListener {
            val intent = Intent(this@Login, SignUp::class.java)
            startActivity(intent)
        }

        // Handle login button click
        loginbutton.setOnClickListener {
            val email: String = tvusername.text.toString()
            val pass: String = tvpassword.text.toString()

            if (email.isEmpty()) {
                tvusername.error = "Please provide your email"
                tvusername.requestFocus()
            } else if (pass.isEmpty()) {
                tvpassword.error = "Please provide your password"
                tvpassword.requestFocus()
            } else {
                firebaseAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this@Login) { task ->
                        if (task.isSuccessful) {
                            val firebaseUser = firebaseAuth.currentUser
                            if (firebaseUser != null) {
                                moveToMainActivity(firebaseUser)
                            }
                        } else {
                            Toast.makeText(
                                this@Login,
                                "Login error. Please Check your email and password.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }


        }
    }

    private fun moveToMainActivity(user: FirebaseUser) {
        val userRef = firebaseDatabase.getReference("users").child(user.uid)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userInformation = snapshot.getValue(UserInformation::class.java)
                if (userInformation != null) {
                    val name: String = "${userInformation.firstName} ${userInformation.lastName}"
                    val usertype=userInformation.isprovider
                    val intent = if (usertype=="yes") {
                        Intent(this@Login, ProviderHomePage::class.java)
                    } else {
                        Intent(this@Login, UserHomePage::class.java)
                    }

                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.putExtra("name", name)
                    startActivity(intent)
                    finish()

                    Toast.makeText(this@Login, "Login Successful.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@Login, "User information not found.", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@Login,
                    "Database error: ${databaseError.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

}