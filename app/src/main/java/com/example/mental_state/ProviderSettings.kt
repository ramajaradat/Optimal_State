package com.example.mental_state


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ProviderSettings : AppCompatActivity() {
    private lateinit var AccountSettingButton: Button
    private lateinit var NotiButton: Button
    private lateinit var Logoutbutton: Button
    private lateinit var BackButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_provider_settings)
        AccountSettingButton = findViewById(R.id.AccountSettingButton)
        NotiButton = findViewById(R.id.NotiButton)
        Logoutbutton = findViewById(R.id.Logoutbutton)
        BackButton = findViewById(R.id.BackButton)


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
        NotiButton.setOnClickListener {
            val intent = Intent(this@ProviderSettings, ProviderChangeTheme::class.java)
            startActivity(intent)
        }



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets


        }

    }
}