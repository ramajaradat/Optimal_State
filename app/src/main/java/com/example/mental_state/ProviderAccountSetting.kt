package com.example.mental_state

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ProviderAccountSetting : AppCompatActivity() {
    private lateinit var ProfileInformationbutton:Button

    private lateinit var Changepassbutton: Button
    private lateinit var BackChangeAccountButton: Button
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
    }
}