package com.example.mental_state

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class proaccountsetting : AppCompatActivity() {
    private lateinit var ProfileInformationbutton:Button

    private lateinit var Changepassbutton: Button
    private lateinit var backk2button: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_proaccountsetting)
        ProfileInformationbutton=findViewById(R.id.ProfileInformationbutton)

        Changepassbutton=findViewById(R.id.Changepassbutton)
        backk2button=findViewById(R.id.backk2button)

        backk2button.setOnClickListener {
            val intent = Intent(this@proaccountsetting, ProviderSettings::class.java)
            startActivity(intent)
        }
        ProfileInformationbutton.setOnClickListener {
            val intent = Intent(this@proaccountsetting, prochangeinfo::class.java)
            startActivity(intent)
        }
        Changepassbutton.setOnClickListener {
            val intent = Intent(this@proaccountsetting, prochangepass::class.java)
            startActivity(intent)
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}