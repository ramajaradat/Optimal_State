package com.example.mental_state

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserHomePage : AppCompatActivity() {
    private lateinit var takeAssesmentButton: Button
    private lateinit var ExersisesButton: Button
    private lateinit var viewHistoryButton: Button
    private lateinit var userSettingsButton: Button
    private lateinit var userWelcomeShow: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_home_page)
        //initializeUI&Firebase
        initializeUI()
        //Display UserName in HomePage
        showuserName()
        //set up buttons click
        setupButtonClick()
    }
    private fun initializeUI(){
        takeAssesmentButton = findViewById(R.id.takeAssesmentButton)
        ExersisesButton = findViewById(R.id.ExersisesButton)
        viewHistoryButton = findViewById(R.id.viewHistoryButton)
        userSettingsButton = findViewById(R.id.userSettingsButton)
        userWelcomeShow = findViewById(R.id.userWelcomeShow)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()}
    private fun showuserName(){
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val userfirstname = database.getReference("users").child(uid).child("firstName")
            userfirstname.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val firstName = snapshot.getValue(String::class.java)
                    val formattedName = firstName?.replaceFirstChar { it.uppercase() } ?: ""
                    userWelcomeShow.text = "Welcome $formattedName"                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }
    private fun setupButtonClick(){
        takeAssesmentButton.setOnClickListener {
            val intent = Intent(this@UserHomePage, User_Take_Assesment::class.java)
            startActivity(intent)
        }
        ExersisesButton.setOnClickListener {
            val intent = Intent(this@UserHomePage, UserExercise::class.java)
            startActivity(intent)
        }
        viewHistoryButton.setOnClickListener {
            val intent = Intent(this@UserHomePage, User_history_screen::class.java)
            startActivity(intent)
        }
        userSettingsButton.setOnClickListener {
            val intent = Intent(this@UserHomePage, UserSettingPage::class.java)
            startActivity(intent)
        }
    }
}