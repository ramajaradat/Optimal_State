package com.example.mental_state

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class UserSettingPage : AppCompatActivity() {
    private lateinit var userAccountSettings: Button
    private lateinit var userChangeTheme: Button
    private lateinit var userLogout: Button
    private lateinit var UserSettingBackButton: Button
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
    }
}
