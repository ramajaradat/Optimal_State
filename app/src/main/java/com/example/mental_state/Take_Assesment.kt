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

    private lateinit var firestore: FirebaseFirestore

    private lateinit var redCardLayout: LinearLayout
    private lateinit var blueCardLayout: LinearLayout
    private lateinit var goldCardLayout: LinearLayout
    private lateinit var whiteCardLayout: LinearLayout
    private lateinit var backButton: Button
    private lateinit var submitButton: Button

    // Variables to store the counts of checked CheckBoxes
    private var redCheckNum = 0
    private var blueCheckNum = 0
    private var goldCheckNum = 0
    private var whiteCheckNum = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_take_assesment)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Initialize the LinearLayouts for each card
        redCardLayout = findViewById(R.id.redCardLayout)
        blueCardLayout = findViewById(R.id.blueCardLayout)
        goldCardLayout = findViewById(R.id.goldCardLayout)
        whiteCardLayout = findViewById(R.id.whiteCardLayout)
        backButton = findViewById(R.id.backButton)
        submitButton = findViewById(R.id.submitButton)

        // Fetch the emotional states from Firestore
        fetchEmotionalStates()
        backButton.setOnClickListener {
            val intent = Intent(this@Take_Assesment, UserHomePage::class.java)
            startActivity(intent)
        }
        // Set up the submit button click listener
        submitButton.setOnClickListener {

            // Calculate the checked counts for each card layout
            val redCheckNum = getCheckedCount(redCardLayout)
            val blueCheckNum = getCheckedCount(blueCardLayout)
            val goldCheckNum = getCheckedCount(goldCardLayout)
            val whiteCheckNum = getCheckedCount(whiteCardLayout)

            // Log the counts for debugging
            Log.d("Take_Assessment", "Red Checked Count: $redCheckNum")
            Log.d("Take_Assessment", "Blue Checked Count: $blueCheckNum")
            Log.d("Take_Assessment", "Gold Checked Count: $goldCheckNum")
            Log.d("Take_Assessment", "White Checked Count: $whiteCheckNum")

            // Initialize a mutable list to store statuses
            val statuses = mutableListOf<String>()

            // Check counts and add to statuses if greater than 2
            if (redCheckNum >= 2) statuses.add("Red")
            if (blueCheckNum >= 2) statuses.add("Blue")
            if (goldCheckNum >= 2) statuses.add("Gold")
            if (whiteCheckNum >= 2) statuses.add("White")

            // Join the statuses into a single string
            val status = statuses.joinToString(", ")

            // Retrieve the current user’s email from Firebase Authentication
            val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: "Unknown User"

            // Get the current date and time in "9:00 PM" format
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

            // Reference to the Firebase real-time database
            val database = FirebaseDatabase.getInstance()
            val userHistoryRef = database.getReference("UserHistory").child(FirebaseAuth.getInstance().currentUser?.uid ?: "UnknownUID")

            // Push the UserHistory object to the database
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

                    addCheckBoxes(redItems, redCardLayout)
                    addCheckBoxes(blueItems, blueCardLayout)
                    addCheckBoxes(goldItems, goldCardLayout)
                    addCheckBoxes(whiteItems, whiteCardLayout)
                } else {
                    Toast.makeText(this, "No such document", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error getting documents: ${exception.message}", Toast.LENGTH_SHORT).show()
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
