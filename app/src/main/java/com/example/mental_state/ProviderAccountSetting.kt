package com.example.mental_state

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProviderAccountSetting : AppCompatActivity() {
    private lateinit var ProfileInformationbutton:Button
    private lateinit var Changepassbutton: Button
    private lateinit var BackChangeAccountButton: Button
    private lateinit var PrviderDelectAccountButton: Button
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_provider_account_setting)
        //initialize UI
        initializeUI()
        //setup Button
        setupButton()
    }
    private fun initializeUI(){
        ProfileInformationbutton=findViewById(R.id.ProfileInformationbutton)
        Changepassbutton=findViewById(R.id.Changepassbutton)
        BackChangeAccountButton=findViewById(R.id.BackChangeAccountButton)
        PrviderDelectAccountButton = findViewById(R.id.PrviderDelectAccountButton)
        mFirebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
    }
    private fun setupButton(){
        BackChangeAccountButton.setOnClickListener {
            val intent = Intent(this@ProviderAccountSetting, ProviderSettings::class.java)
            startActivity(intent)
        }
        ProfileInformationbutton.setOnClickListener {
            val intent = Intent(this@ProviderAccountSetting, ProvideChangeInfo::class.java)
            startActivity(intent)
        }
        Changepassbutton.setOnClickListener {
            val intent = Intent(this@ProviderAccountSetting, ProviderChangepassword::class.java)
            startActivity(intent)
        }
        PrviderDelectAccountButton.setOnClickListener {
            deleteProviderAccount()
        }
    }

    private fun deleteProviderAccount(){
        val user = mFirebaseAuth.currentUser
        val userId = user?.uid
        val userEmail = user?.email

        val providerDataSet = firebaseDatabase.getReference("Providers")

        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                userId?.let { uid ->
                    userEmail?.let { email ->
                        val formattedProviderEmail = email.replace(".", "_").replace("@", "_")

                        providerDataSet.child(formattedProviderEmail).removeValue().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("DeleteThirdDataset", "User data successfully deleted from the third dataset.")
                            } else {
                                Log.e("DeleteThirdDataset", "Error deleting data from third dataset.", task.exception)
                            }
                        }
                    }

                    val usersDataset = firebaseDatabase.getReference("users")

                    val userHistoryDataset = firebaseDatabase.getReference("UserHistory")

                    usersDataset.child(uid).removeValue().addOnCompleteListener { task1 ->
                        if (task1.isSuccessful) {
                            Log.d("DeleteData", "User data deleted from first database.")

                            userHistoryDataset.child(uid).removeValue().addOnCompleteListener { task2 ->
                                if (task2.isSuccessful) {
                                    Log.d("DeleteData", "User data deleted from second database.")

                                    user.delete().addOnCompleteListener { authTask ->
                                        if (authTask.isSuccessful) {
                                            Log.d("DeleteAccount", "User account deleted.")
                                            val intent = Intent(this, Login::class.java)
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            Log.e("DeleteAccount", "Error deleting user account.", authTask.exception)
                                        }
                                    }
                                } else {
                                    Log.e("DeleteData", "Error deleting data from second database.", task2.exception)
                                }
                            }
                        } else {
                            Log.e("DeleteData", "Error deleting data from first database.", task1.exception)
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