package com.example.mental_state

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class UserChangePass : AppCompatActivity() {

    private lateinit var UserEmailShow: TextView
    private lateinit var UserCurentPass: EditText
    private lateinit var UserNewPass: EditText
    private lateinit var ConfirmUserNewPass: EditText
    private lateinit var ApplyChangePassButton: Button
    private lateinit var CancelChangePassButton: Button
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_change_pass)
        //Initialize UI and Firebase
        initializeUI()
        // Show Current user email
        currentUserEmail()
        //user setup button
        setupButton()

    }

    private fun initializeUI(){
        UserEmailShow = findViewById(R.id.UserEmailShow)
        UserCurentPass = findViewById(R.id.UserCurentPass)
        UserNewPass = findViewById(R.id.UserNewPass)
        ConfirmUserNewPass = findViewById(R.id.ConfirmUserNewPass)
        ApplyChangePassButton = findViewById(R.id.ApplyChangePassButton)
        CancelChangePassButton = findViewById(R.id.CancelChangePassButton)
        mFirebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
    }

    private fun currentUserEmail() {
        val currentUser = mFirebaseAuth.currentUser
        if (currentUser != null) {
            UserEmailShow.text = currentUser.email
        } else {
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show()
            return
        }
    }

    private fun setupButton() {
        CancelChangePassButton.setOnClickListener {
            val intent = Intent(this@UserChangePass, UserAccountSetting::class.java)
            startActivity(intent)
        }
        ApplyChangePassButton.setOnClickListener {
            val UserCurentPassword = UserCurentPass.text.toString()
            val newPassword = UserNewPass.text.toString()
            val confirmNewPassword = ConfirmUserNewPass.text.toString()

            if (UserCurentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword.length < 8 || !Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>]).+$").containsMatchIn(
                    newPassword
                )
            ) {
                Toast.makeText(
                    this,
                    "New password must be at least 8 characters, with an uppercase letter, lowercase letter, and a special character.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (newPassword != confirmNewPassword) {
                Toast.makeText(this, "New passwords do not match.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = mFirebaseAuth.currentUser
            if (user == null) {
                Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val credential = EmailAuthProvider.getCredential(user.email ?: "", UserCurentPassword)

            user.reauthenticate(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user.updatePassword(newPassword).addOnCompleteListener { passwordUpdateTask ->
                        if (passwordUpdateTask.isSuccessful) {

                                        Toast.makeText(
                                            this,
                                            "Password updated successfully.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        val intent = Intent(
                                            this@UserChangePass,
                                            UserAccountSetting::class.java
                                        )
                                        startActivity(intent)

                        } else {
                            Toast.makeText(
                                this,
                                "Failed to update password in Firebase Authentication.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Current password is incorrect.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    }
}
