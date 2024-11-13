package com.example.mental_state


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class UserHomePage : AppCompatActivity() {
    private lateinit var userassesmentButton: Button
    private lateinit var usercurentExersisesButton: Button
    private lateinit var userviewhistoryButton: Button
    private lateinit var usersettingsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_home_page)

        userassesmentButton = findViewById(R.id.userassesmentButton)
        usercurentExersisesButton = findViewById(R.id.usercurentExersisesButton)
        userviewhistoryButton = findViewById(R.id.userviewhistoryButton)
        usersettingsButton = findViewById(R.id.usersettingsButton)

        userassesmentButton.setOnClickListener {
            val intent = Intent(this@UserHomePage, Take_Assesment::class.java)
            startActivity(intent)
        }

        usercurentExersisesButton.setOnClickListener {
            val intent = Intent(this@UserHomePage, UserExercise::class.java)
            startActivity(intent)
        }
        userviewhistoryButton.setOnClickListener {
            val intent = Intent(this@UserHomePage, User_history_screen::class.java)
            startActivity(intent)
        }
        usersettingsButton.setOnClickListener {
            val intent = Intent(this@UserHomePage, SettingUserPage::class.java)
            startActivity(intent)
        }



    }
}