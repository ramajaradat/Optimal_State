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

class Take_Assesment : AppCompatActivity() {

    //initializeUI&Firebase
    private lateinit var firestore: FirebaseFirestore
    private lateinit var redCard: LinearLayout
    private lateinit var blueCard: LinearLayout
    private lateinit var goldCard: LinearLayout
    private lateinit var whiteCard: LinearLayout
    private lateinit var AssesmentBackButton: Button
    private lateinit var AssesmentsubmitButton: Button

    //Counts of checked CheckBoxes for each card
    private var redCheckNum = 0
    private var blueCheckNum = 0
    private var goldCheckNum = 0
    private var whiteCheckNum = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_take_assesment)

        firestore = FirebaseFirestore.getInstance()
        redCard = findViewById(R.id.redCard)
        blueCard = findViewById(R.id.blueCard)
        goldCard = findViewById(R.id.goldCard)
        whiteCard = findViewById(R.id.whiteCard)
        AssesmentBackButton = findViewById(R.id.AssesmentBackButton)
        AssesmentsubmitButton = findViewById(R.id.AssesmentsubmitButton)

        // Get the emotional states from Dataset on Firestore
        fetchEmotionalStates()
        //set up user buttons click
        setupButtonClick()
    }

    private fun setupButtonClick() {

        AssesmentBackButton.setOnClickListener {
            val intent = Intent(this@Take_Assesment, UserHomePage::class.java)
            startActivity(intent)
        }
        // Set up the submit button click listener
        AssesmentsubmitButton.setOnClickListener {
            setupAssesmentsubmitButton()
        }
    }

    private fun setupAssesmentsubmitButton() {
        // Calculate the checked counts for each card layout
        val redCheckNum = getCheckedCount(redCard)
        val blueCheckNum = getCheckedCount(blueCard)
        val goldCheckNum = getCheckedCount(goldCard)
        val whiteCheckNum = getCheckedCount(whiteCard)

        // Log the counts for debugging
        Log.d("Take_Assessment", "Red Checked Count: $redCheckNum")
        Log.d("Take_Assessment", "Blue Checked Count: $blueCheckNum")
        Log.d("Take_Assessment", "Gold Checked Count: $goldCheckNum")
        Log.d("Take_Assessment", "White Checked Count: $whiteCheckNum")

        val statuses = mutableListOf<String>()

        // Check counts for each card if check equal or more 2 the save status
        if (redCheckNum >= 2) statuses.add("Red")
        if (blueCheckNum >= 2) statuses.add("Blue")
        if (goldCheckNum >= 2) statuses.add("Gold")
        if (whiteCheckNum >= 2) statuses.add("White")

        val status = statuses.joinToString(", ")

        // Get  current user’s email
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: "Unknown User"

        // Get the current date and time
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1  // Months are 0-based, so add 1
        val year = calendar.get(Calendar.YEAR)
        val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(calendar.time)

        // Create a UserHistory object
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

        // Push the UserHistory info to the database
        userHistoryRef.push().setValue(userHistory)
            .addOnSuccessListener {
                Log.d("Firebase", "User history successfully written!")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed to write user history", e)
            }
        val intent = Intent(this@Take_Assesment, UserExercise::class.java)
        startActivity(intent)
    }

    private fun fetchEmotionalStates() {
        firestore.collection("Emotional-States")
            .document("s4Unsfhaf6UagJhptLrr")
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val redItems = document.get("Red") as? List<String> ?: emptyList()
                    val blueItems = document.get("Blue") as? List<String> ?: emptyList()
                    val goldItems = document.get("Gold") as? List<String> ?: emptyList()
                    val whiteItems = document.get("White") as? List<String> ?: emptyList()

                    // Log the fetched items
                    Log.d("Take_Assesment", "Red Items: $redItems")
                    Log.d("Take_Assesment", "Blue Items: $blueItems")
                    Log.d("Take_Assesment", "Gold Items: $goldItems")
                    Log.d("Take_Assesment", "White Items: $whiteItems")

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
        // Clear any existing views to avoid duplicates
        cardLayout.removeAllViews()

        // Loop through each item and create a CheckBox
        for (item in items) {
            val checkBox = CheckBox(this)
            checkBox.text = item // Set the text for the checkbox
            checkBox.setTextColor(getColor(android.R.color.black))
            checkBox.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            // Add the CheckBox to the card layout
            cardLayout.addView(checkBox)
        }
    }

    // Function to calculate the checked CheckBoxes
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
