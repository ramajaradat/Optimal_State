package com.example.mental_state

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class UserAccountSetting : AppCompatActivity() {
    private lateinit var UserProfilInfoButton:Button
    private lateinit var UserChangePassButton: Button
    private lateinit var UserChangAccountBackButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_account_setting)
        //Initialize UI
        initializeUI()
        //SetUp Bttons
        setupButton()
    }
    private fun initializeUI(){
        UserProfilInfoButton=findViewById(R.id.UserProfilInfoButton)
        UserChangePassButton=findViewById(R.id.UserChangePassButton)
        UserChangAccountBackButton=findViewById(R.id.UserChangAccountBackButton)
    }
    private fun setupButton(){
        UserProfilInfoButton.setOnClickListener {
            val intent = Intent(this@UserAccountSetting, UserChangeProfileInfo::class.java)
            startActivity(intent)
        }

        UserChangePassButton.setOnClickListener {
            val intent = Intent(this@UserAccountSetting, UserChangePass::class.java)
            startActivity(intent)
        }
        UserChangAccountBackButton.setOnClickListener {
            val intent = Intent(this@UserAccountSetting, UserSettingPage::class.java)
            startActivity(intent)
        }
    }
}