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
import com.google.firebase.database.FirebaseDatabase

class ProviderSettings : AppCompatActivity() {
    private lateinit var AccountSettingButton: Button
    private lateinit var ChangeThemeButton: Button
    private lateinit var Logoutbutton: Button
    private lateinit var BackButton: Button
    private lateinit var ProviderSetNotificationButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_provider_settings)
        //initialize Ui
        initializeUI()
        //setup Button
        setupButton()
    }
    private fun initializeUI(){
        AccountSettingButton = findViewById(R.id.AccountSettingButton)
        ChangeThemeButton = findViewById(R.id.ChangeThemeButton)
        Logoutbutton = findViewById(R.id.Logoutbutton)
        BackButton = findViewById(R.id.BackButton)
        ProviderSetNotificationButton = findViewById(R.id.ProviderSetNotificationButton)
    }
    private fun setupButton(){
        Logoutbutton.setOnClickListener {
            val intent = Intent(this@ProviderSettings, Login::class.java)
            startActivity(intent)
        }

        BackButton.setOnClickListener {
            val intent = Intent(this@ProviderSettings, ProviderHomePage::class.java)
            startActivity(intent)
        }

        AccountSettingButton.setOnClickListener {
            val intent = Intent(this@ProviderSettings, ProviderAccountSetting::class.java)
            startActivity(intent)
        }
        ChangeThemeButton.setOnClickListener {
            val intent = Intent(this@ProviderSettings, ProviderChangeTheme::class.java)
            startActivity(intent)
        }
        ProviderSetNotificationButton.setOnClickListener {
            val intent = Intent(this@ProviderSettings, ProviderNotification::class.java)
            startActivity(intent)        }
    }
}