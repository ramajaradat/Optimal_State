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
    private lateinit var UserCheckNotification:Button


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
        UserCheckNotification=findViewById(R.id.UserCheckNotification)
        userChangeTheme = findViewById(R.id.userChangeTheme)
        userLogout = findViewById(R.id.userLogout)
        UserSettingBackButton = findViewById(R.id.userSettingBackButton)

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
        UserCheckNotification.setOnClickListener{
            val intent = Intent(this@UserSettingPage, UserNotification::class.java)
            startActivity(intent)
        }

    }
}
