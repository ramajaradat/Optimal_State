package com.example.mental_state


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class ProviderSettings : AppCompatActivity() {
    private lateinit var AccountSettingButton: Button
    private lateinit var ChangeThemeButton: Button
    private lateinit var Logoutbutton: Button
    private lateinit var BackButton: Button
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
    }
}