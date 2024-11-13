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
    private lateinit var userassesmentButton: Button
    private lateinit var usercurentExersisesButton: Button
    private lateinit var userviewhistoryButton: Button
    private lateinit var usersettingsButton: Button
    private lateinit var tvwelcomeuser: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_home_page)

        userassesmentButton = findViewById(R.id.userassesmentButton)
        usercurentExersisesButton = findViewById(R.id.usercurentExersisesButton)
        userviewhistoryButton = findViewById(R.id.userviewhistoryButton)
        usersettingsButton = findViewById(R.id.usersettingsButton)
        tvwelcomeuser = findViewById(R.id.tvwelcomeuser)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Fetch and display the user's first name
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val userRef = database.getReference("users").child(uid).child("firstName")
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val firstName = snapshot.getValue(String::class.java)
                    val formattedName = firstName?.replaceFirstChar { it.uppercase() } ?: ""
                    tvwelcomeuser.text = "Welcome $formattedName"                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }



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