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
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProviderChangepassword : AppCompatActivity() {

    private lateinit var tvEmailtext: TextView
    private lateinit var currentpass: EditText
    private lateinit var newpassedite: EditText
    private lateinit var confirmnewpassedite: EditText
    private lateinit var pass_edit_button: Button
    private lateinit var pass_cancle_button: Button
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_provider_change_password)
        //initialize Ui
        initializeUI()
        // Display the current user email
        useremail()
        //setup button
        setupButton()
    }
    private fun initializeUI(){
        tvEmailtext = findViewById(R.id.tvEmailtext)
        currentpass = findViewById(R.id.currentpass)
        newpassedite = findViewById(R.id.newpassedite)
        confirmnewpassedite = findViewById(R.id.confirmnewpassedite)
        pass_edit_button = findViewById(R.id.pass_edit_button)
        pass_cancle_button = findViewById(R.id.pass_cancle_button)
        mFirebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
    }
    private fun useremail(){
        val currentUser = mFirebaseAuth.currentUser
        if (currentUser != null) {
            tvEmailtext.text = currentUser.email
        } else {
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show()
            return
        }
    }
    private fun setupButton(){
        pass_cancle_button.setOnClickListener {
            val intent = Intent(this@ProviderChangepassword, ProviderAccountSetting::class.java)
            startActivity(intent)
        }

        pass_edit_button.setOnClickListener {
            val currentPassword = currentpass.text.toString()
            val newPassword = newpassedite.text.toString()
            val confirmNewPassword = confirmnewpassedite.text.toString()

            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword.length < 8 || !Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>]).+$").containsMatchIn(newPassword)) {
                Toast.makeText(this, "New password must be at least 8 characters, with an uppercase letter, lowercase letter, and a special character.", Toast.LENGTH_SHORT).show()
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

            val credential = EmailAuthProvider.getCredential(user.email ?: "", currentPassword)

            user.reauthenticate(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user.updatePassword(newPassword).addOnCompleteListener { passwordUpdateTask ->
                        if (passwordUpdateTask.isSuccessful) {
                            Toast.makeText(this, "Password updated successfully.", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@ProviderChangepassword, ProviderAccountSetting::class.java)
                            startActivity(intent)

                        } else {
                            Toast.makeText(this, "Failed to update password in Firebase Authentication.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Current password is incorrect.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
