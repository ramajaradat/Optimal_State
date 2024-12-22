package com.example.mental_state

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.mental_state.Model.UserHistory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class User_Take_Assesment : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var redCard: LinearLayout
    private lateinit var blueCard: LinearLayout
    private lateinit var goldCard: LinearLayout
    private lateinit var whiteCard: LinearLayout
    private lateinit var AssesmentBackButton: Button
    private lateinit var AssesmentsubmitButton: Button

    //Counts of checked CheckBoxes for each card status
    private var redCheckNum = 0
    private var blueCheckNum = 0
    private var goldCheckNum = 0
    private var whiteCheckNum = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_take_assesment)
        //initialize UI & Firebase
        initializeUI()
        //set up user buttons click
        setupButtonClick()
        // Get the emotional states from Dataset on Firestore
        getEmotionalStates()

    }

    private fun initializeUI(){
        firestore = FirebaseFirestore.getInstance()
        redCard = findViewById(R.id.redCard)
        blueCard = findViewById(R.id.blueCard)
        goldCard = findViewById(R.id.goldCard)
        whiteCard = findViewById(R.id.whiteCard)
        AssesmentBackButton = findViewById(R.id.AssesmentBackButton)
        AssesmentsubmitButton = findViewById(R.id.AssesmentsubmitButton)
    }
    private fun setupButtonClick() {

        AssesmentBackButton.setOnClickListener {
            val intent = Intent(this@User_Take_Assesment, UserHomePage::class.java)
            startActivity(intent)
        }
        AssesmentsubmitButton.setOnClickListener {
            setupAssesmentSubmitButton()
        }
    }
    private fun setupAssesmentSubmitButton() {
        val redCheckNum = getCheckedCount(redCard)
        val blueCheckNum = getCheckedCount(blueCard)
        val goldCheckNum = getCheckedCount(goldCard)
        val whiteCheckNum = getCheckedCount(whiteCard)


        val statuses = mutableListOf<String>()
        if(redCheckNum>= 2 || blueCheckNum>= 2||goldCheckNum>= 2||whiteCheckNum>= 2 ) {
            if (redCheckNum >= 2) statuses.add("Red")
            if (blueCheckNum >= 2) statuses.add("Blue")
            if (goldCheckNum >= 2) statuses.add("Gold")
            if (whiteCheckNum >= 2) statuses.add("White")
        }

        else {
            if (redCheckNum == 1) statuses.add("Red")
            if (blueCheckNum == 1) statuses.add("Blue")
            if (goldCheckNum == 1) statuses.add("Gold")
            if (whiteCheckNum == 1) statuses.add("White")
        }


        val status = statuses.joinToString(", ")

        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: "Unknown User"

        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(calendar.time)

        val userHistory = UserHistory(
            email = userEmail,
            status = status,
            day = day,
            month = month,
            year = year,
            time = time
        )

        val database = FirebaseDatabase.getInstance()
        val userHistoryRef = database.getReference("UserHistory")
            .child(FirebaseAuth.getInstance().currentUser?.uid ?: "UnknownUID")

        userHistoryRef.push().setValue(userHistory)
            .addOnSuccessListener {
                Log.d("Firebase", "User history successfully written!")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed to write user history", e)
            }
        val intent = Intent(this@User_Take_Assesment, UserExercise::class.java)
        startActivity(intent)
    }
    private fun getEmotionalStates() {
        firestore.collection("Emotional-States")
            .document("s4Unsfhaf6UagJhptLrr")
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val redItems = document.get("Red") as? List<String> ?: emptyList()
                    val blueItems = document.get("Blue") as? List<String> ?: emptyList()
                    val goldItems = document.get("Gold") as? List<String> ?: emptyList()
                    val whiteItems = document.get("White") as? List<String> ?: emptyList()


                    addCheckBoxes(redItems, redCard)
                    addCheckBoxes(blueItems, blueCard)
                    addCheckBoxes(goldItems, goldCard)
                    addCheckBoxes(whiteItems, whiteCard)
                } else {
                    Toast.makeText(this, "No such document", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Error getting documents: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
    private fun addCheckBoxes(items: List<String>, cardLayout: LinearLayout) {
        cardLayout.removeAllViews()

        for (item in items) {
            val checkBox = CheckBox(this)
            checkBox.text = item
            checkBox.setTextColor(getColor(android.R.color.black))
            checkBox.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            cardLayout.addView(checkBox)
        }
    }
    // function to calculate the number of CheckBoxes was checked
    private fun getCheckedCount(cardLayout: LinearLayout): Int {
        var count = 0
        for (i in 0 until cardLayout.childCount) {
            val checkBox = cardLayout.getChildAt(i) as? CheckBox
            if (checkBox?.isChecked == true) {
                count++
            }
        }
        return count
    }
}
