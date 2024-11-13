package com.example.mental_state

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class AccountSetting : AppCompatActivity() {
    private lateinit var ProfileInformationbutton:Button
    private lateinit var ChangeEmailbutton:Button
    private lateinit var Changepassbutton: Button
    private lateinit var backk2button: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_account_setting)
        ProfileInformationbutton=findViewById(R.id.ProfileInformationbutton)
        ChangeEmailbutton=findViewById(R.id.ChangeEmailbutton)
        Changepassbutton=findViewById(R.id.Changepassbutton)
        backk2button=findViewById(R.id.backk2button)

        ProfileInformationbutton.setOnClickListener {
            val intent = Intent(this@AccountSetting, ChangeInfo::class.java)
            startActivity(intent)
        }
        ChangeEmailbutton.setOnClickListener {
            val intent = Intent(this@AccountSetting, ChangeEmail::class.java)
            startActivity(intent)
        }
        Changepassbutton.setOnClickListener {
            val intent = Intent(this@AccountSetting, ChangePass::class.java)
            startActivity(intent)
        }
        backk2button.setOnClickListener {
            val intent = Intent(this@AccountSetting, UserHomePage::class.java)
            startActivity(intent)
        }
    }
}