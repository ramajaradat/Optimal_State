package com.example.mental_state

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.mentalstate.Model.UserInformation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Login : AppCompatActivity() {
    //initializeUI&Firebase

    private lateinit var usersignup: Button
    private lateinit var userlogin: Button
    private lateinit var usernameinput: EditText
    private lateinit var passwordinput: EditText
    private lateinit var userforgetpass: TextView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        usersignup = findViewById(R.id.usersignup)
        userlogin = findViewById(R.id.userlogin)
        usernameinput = findViewById(R.id.usernameinput)
        passwordinput = findViewById(R.id.passwordinput)
        userforgetpass = findViewById(R.id.userforgetpass)

        setupButtonClick()

    }

    //set up user buttons click
    private fun setupButtonClick() {
        // set up forget password button click
        userforgetpass.setOnClickListener {
            val intent = Intent(this@Login, ForgetPassword::class.java)
            startActivity(intent)
        }
        //set up signup button click
        usersignup.setOnClickListener {
            val intent = Intent(this@Login, SignUp::class.java)
            startActivity(intent)
        }
        // set up login button click
        userlogin.setOnClickListener {
            handelLoginButton()
        }
    }

    private fun handelLoginButton() {
        val email: String = usernameinput.text.toString()
        val pass: String = passwordinput.text.toString()

        if (email.isEmpty()) {
            usernameinput.error = "Please Enter your email"
            usernameinput.requestFocus()
        } else if (pass.isEmpty()) {
            passwordinput.error = "Please Enter your password"
            passwordinput.requestFocus()
        } else {
            firebaseAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this@Login) { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = firebaseAuth.currentUser
                        if (firebaseUser != null) {
                            moveToLoginPage(firebaseUser)
                        }
                    } else {
                        Toast.makeText(
                            this@Login,
                            "Login error. Please Check your Email and password.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun moveToLoginPage(user: FirebaseUser) {
        val userRef = firebaseDatabase.getReference("users").child(user.uid)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("ShowToast")
            override fun onDataChange(snapshot: DataSnapshot) {
                val userInformation = snapshot.getValue(UserInformation::class.java)
                val usertype = userInformation?.isprovider

                val name: String = "${userInformation?.firstName} ${userInformation?.lastName}"

                val intent = if (usertype == "yes") {
                    Intent(this@Login, ProviderHomePage::class.java)
                } else {
                    Intent(this@Login, UserHomePage::class.java)
                }

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.putExtra("name", name)
                startActivity(intent)
                finish()

                Toast.makeText(this@Login, "Login Successful.", Toast.LENGTH_SHORT).show()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@Login, "Database error: ${databaseError.message}", Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

}