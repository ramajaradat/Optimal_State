package com.example.mental_state

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SettingUserPage : AppCompatActivity() {
    private lateinit var AccountSettingButton: Button
    private lateinit var AppearanceButton: Button
    private lateinit var Logoutbutton: Button
    private lateinit var BackButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_setting_user_page)

        AccountSettingButton = findViewById(R.id.AccountSettingButton)
        AppearanceButton = findViewById(R.id.AppearanceButton)
        Logoutbutton = findViewById(R.id.Logoutbutton)
        BackButton = findViewById(R.id.BackButton)

        Logoutbutton.setOnClickListener {
            val intent = Intent(this@SettingUserPage, Login::class.java)
            startActivity(intent)
        }

        BackButton.setOnClickListener {
            val intent = Intent(this@SettingUserPage, UserHomePage::class.java)
            startActivity(intent)
        }

        AccountSettingButton.setOnClickListener {
            val intent = Intent(this@SettingUserPage, AccountSetting::class.java)
            startActivity(intent)
        }

        AppearanceButton.setOnClickListener {
            val intent = Intent(this@SettingUserPage, ChangeTheme::class.java)
            startActivity(intent)
        }

    }
}