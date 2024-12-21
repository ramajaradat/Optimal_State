package com.example.mental_state

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserSettingPage : AppCompatActivity() {
    private lateinit var userAccountSettings: Button
    private lateinit var userChangeTheme: Button
    private lateinit var userLogout: Button
    private lateinit var UserSettingBackButton: Button
    private lateinit var UserDeleteAccount:Button
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_setting_page)
        //Initialize UI
        initializeUI()
        //User Button SetUp
        setupButton()
    }

    private fun initializeUI(){
        userAccountSettings = findViewById(R.id.userAccountSettings)
        userChangeTheme = findViewById(R.id.userChangeTheme)
        userLogout = findViewById(R.id.userLogout)
        UserSettingBackButton = findViewById(R.id.UserSettingBackButton)
        UserDeleteAccount=findViewById(R.id.UserDeleteAccount)
        mFirebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
    }
    private fun setupButton() {
        userLogout.setOnClickListener {
            val intent = Intent(this@UserSettingPage, Login::class.java)
            startActivity(intent)
        }

        UserSettingBackButton.setOnClickListener {
            val intent = Intent(this@UserSettingPage, UserHomePage::class.java)
            startActivity(intent)
        }

        userAccountSettings.setOnClickListener {
            val intent = Intent(this@UserSettingPage, UserAccountSetting::class.java)
            startActivity(intent)
        }

        userChangeTheme.setOnClickListener {
            val intent = Intent(this@UserSettingPage, UserChangeTheme::class.java)
            startActivity(intent)
        }

        UserDeleteAccount.setOnClickListener {
            deleteUserAccount()
        }
    }
    private fun deleteUserAccount(){
        val user = mFirebaseAuth.currentUser
        val userId = user?.uid
        val userEmail = user?.email

        val usersDataset = firebaseDatabase.getReference("users")
        val userHistoryDataset = firebaseDatabase.getReference("UserHistory")
        val providersDataset = firebaseDatabase.getReference("Providers")

        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                userId?.let { uid ->
                    // Step 1: Delete user data from the "users" dataset
                    usersDataset.child(uid).removeValue().addOnCompleteListener { task1 ->
                        if (task1.isSuccessful) {
                            Log.d("DeleteData", "User data deleted from 'users' dataset.")

                            // Step 2: Delete user data from the "UserHistory" dataset
                            userHistoryDataset.child(uid).removeValue().addOnCompleteListener { task2 ->
                                if (task2.isSuccessful) {
                                    Log.d("DeleteData", "User data deleted from 'UserHistory' dataset.")

                                    // Step 3: Delete user's email from the "Providers" dataset
                                    if (userEmail != null) {
                                        providersDataset.addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                var emailRemoved = false
                                                for (providerSnapshot in snapshot.children) {
                                                    val clientsRef = providerSnapshot.child("clients").ref
                                                    for (clientSnapshot in providerSnapshot.child("clients").children) {
                                                        val email = clientSnapshot.getValue(String::class.java)
                                                        if (email == userEmail) {
                                                            clientSnapshot.ref.removeValue()
                                                            emailRemoved = true
                                                            Log.d("DeleteData", "User email removed from 'Providers'.")
                                                            break
                                                        }
                                                    }
                                                    if (emailRemoved) break
                                                }

                                                // Step 4: Delete user account from Firebase Authentication
                                                user.delete().addOnCompleteListener { authTask ->
                                                    if (authTask.isSuccessful) {
                                                        Log.d("DeleteAccount", "User account deleted.")
                                                        // Navigate to the login or goodbye screen
                                                        val intent = Intent(this@UserSettingPage, Login::class.java)
                                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                        startActivity(intent)
                                                        finish()
                                                    } else {
                                                        Log.e("DeleteAccount", "Error deleting user account.", authTask.exception)
                                                    }
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                Log.e("DeleteData", "Error reading 'Providers' dataset.", error.toException())
                                            }
                                        })
                                    } else {
                                        Log.e("DeleteData", "User email is null. Cannot delete from 'Providers'.")
                                    }
                                } else {
                                    Log.e("DeleteData", "Error deleting data from 'UserHistory' dataset.", task2.exception)
                                }
                            }
                        } else {
                            Log.e("DeleteData", "Error deleting data from 'users' dataset.", task1.exception)
                        }
                    }
                } ?: run {
                    Log.e("DeleteData", "User ID is null. Cannot delete data.")
                    Toast.makeText(this, "Error: Unable to fetch user data.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()

    }
}
