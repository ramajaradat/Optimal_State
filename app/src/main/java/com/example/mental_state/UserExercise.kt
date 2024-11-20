package com.example.mental_state

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore

class UserExercise : AppCompatActivity() {

    private lateinit var otherspinner: Spinner
    private lateinit var foodspinner: Spinner
    private lateinit var breathingspinner: Spinner
    private lateinit var videospinner: Spinner
    private lateinit var exercisesbackbutton:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_exercise)

        // Initialize spinners
        otherspinner = findViewById(R.id.otherspinner)
        foodspinner = findViewById(R.id.foodspinner)
        breathingspinner = findViewById(R.id.breathingspinner)
        videospinner = findViewById(R.id.videospinner)
        exercisesbackbutton=findViewById(R.id.exercisesbackbutton)

        exercisesbackbutton.setOnClickListener {
            val intent = Intent(this@UserExercise, UserHomePage::class.java)
            startActivity(intent)
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userUid = currentUser.uid
            val userHistoryRef = FirebaseDatabase.getInstance().getReference("UserHistory").child(userUid)

            // Query to get the last entry's status
            userHistoryRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val lastStatus = snapshot.children.iterator().next().child("status").getValue(String::class.java)
                        Toast.makeText(this@UserExercise, "Last status: $lastStatus", Toast.LENGTH_SHORT).show()
                        val statuses = lastStatus?.split(",")?.map { it.trim() } ?: emptyList()

                        // Fetch data from Firestore based on the statuses
                        fetchDataFromFirestore(statuses)
                    } else {
                        Toast.makeText(this@UserExercise, "No status history available for the current user.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@UserExercise, "Error retrieving user history: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun fetchDataFromFirestore(statuses: List<String>) {
        val db = FirebaseFirestore.getInstance()

        // To store data to populate the spinners
        val breathingExercises = mutableListOf<String>()
        val foods = mutableListOf<String>()
        val otherExercises = mutableListOf<String>()
        val recommendedVideos = mutableListOf<String>()

        // A counter to track the number of completed Firestore requests
        var completedRequests = 0

        // Define a map for the status to Firestore document IDs
        val statusToDocId = mapOf(
            "Red" to "HgVJpyoUYLZgMfq1TMM9",
            "Blue" to "IeQGuaztpUb0iY1YPiwX",
            "Gold" to "Bs7i9lI3nMr0xUEdZnMF",
            "White" to "iEUt0GdunvMBjfyTOGI9"
        )

        // Initialize a counter to track completed status fetches
        var statusProcessedCount = 0

        // Iterate through each status and fetch data from Firestore
        statuses.forEach { status ->
            val docId = statusToDocId[status]
            if (docId != null) {
                val statusDocRef = db.collection(status).document(docId)  // Get document by ID
                statusDocRef.get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        try {
                            // Safely cast the fields to Lists of Strings
                            val breathingList = document.get("Breath") as? List<String> ?: emptyList()
                            val foodList = document.get("Food") as? List<String> ?: emptyList()
                            val otherExerciseList = document.get("Other") as? List<String> ?: emptyList()
                            val videoList = document.get("Video") as? List<String> ?: emptyList()

                            // Add each element from the arrays to the respective lists
                            breathingExercises.addAll(breathingList)
                            foods.addAll(foodList)
                            otherExercises.addAll(otherExerciseList)
                            recommendedVideos.addAll(videoList)

                            // Log the retrieved data for debugging
                            Log.d("UserExercise", "Status: $status")
                            Log.d("UserExercise", "Breathing: $breathingList")
                            Log.d("UserExercise", "Foods: $foodList")
                            Log.d("UserExercise", "Other: $otherExerciseList")
                            Log.d("UserExercise", "Recommended: $videoList")
                        } catch (e: Exception) {
                            Log.e("UserExercise", "Error processing document: ${e.message}")
                        }
                    } else {
                        Log.e("UserExercise", "Document does not exist for status: $status")
                    }

                    // Increment the counter when each document fetch is completed
                    statusProcessedCount++

                    // If all requests are completed, update the spinners
                    if (statusProcessedCount == statuses.size) {
                        Log.d("UserExercise", "All statuses processed, updating spinners.")
                        runOnUiThread {
                            updateSpinners(breathingExercises, foods, otherExercises, recommendedVideos)
                        }
                    }
                }.addOnFailureListener { e ->
                    Log.e("UserExercise", "Error fetching document for status $status: ${e.message}")
                    statusProcessedCount++ // Still increment to prevent hanging if one request fails
                }
            } else {
                Log.e("UserExercise", "No Firestore document found for status: $status")
            }
        }
    }

    private fun updateSpinners(breathingExercises: List<String>, foods: List<String>, otherExercises: List<String>, recommendedVideos: List<String>) {
        // Log the final lists to ensure they have been populated correctly
        Log.d("UserExercise", "Final Breathing List: $breathingExercises")
        Log.d("UserExercise", "Final Foods List: $foods")
        Log.d("UserExercise", "Final Other Exercises List: $otherExercises")
        Log.d("UserExercise", "Final Recommended Videos List: $recommendedVideos")

        // Set initial values
        val initialBreathingValue = "Breathing Exercises"
        val initialFoodValue = "Recommended Foods"
        val initialOtherValue = "Other Exercises"
        val initialVideoValue = "Watch Video"

        // Adapter for Breathing Exercise Spinner
        val breathingAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf(initialBreathingValue) + breathingExercises)
        breathingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        breathingspinner.adapter = breathingAdapter
        breathingspinner.setSelection(0)  // Set initial value as first item
        // Set the initial value displayed in white
        breathingspinner.setSelection(0)
        breathingspinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) { // Initial value
                    (view as? TextView)?.setTextColor(Color.WHITE)
                } else {
                    (view as? TextView)?.setTextColor(Color.BLACK) // Default color for other items
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        })

        // Adapter for Foods Recommended Spinner
        val foodAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf(initialFoodValue) + foods)
        foodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        foodspinner.adapter = foodAdapter
        foodspinner.setSelection(0)  // Set initial value as first item
        // Set the initial value displayed in white
        foodspinner.setSelection(0)
        foodspinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) { // Initial value
                    (view as? TextView)?.setTextColor(Color.WHITE)
                } else {
                    (view as? TextView)?.setTextColor(Color.BLACK) // Default color for other items
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        })

        // Adapter for Other Exercise Spinner
        val otherAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf(initialOtherValue) + otherExercises)
        otherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        otherspinner.adapter = otherAdapter
        otherspinner.setSelection(0)  // Set initial value as first item
        // Set the initial value displayed in white
        otherspinner.setSelection(0)
        otherspinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) { // Initial value
                    (view as? TextView)?.setTextColor(Color.WHITE)
                } else {
                    (view as? TextView)?.setTextColor(Color.BLACK) // Default color for other items
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        })

        // Adapter for Recommended Video Spinner
        val videoAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf(initialVideoValue) + recommendedVideos)
        videoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        videospinner.adapter = videoAdapter
        videospinner.setSelection(0)
        // Set the initial value displayed in white
        videospinner.setSelection(0)
        videospinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) { // Initial value
                    (view as? TextView)?.setTextColor(Color.WHITE)
                } else {
                    (view as? TextView)?.setTextColor(Color.BLACK) // Default color for other items
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        })// Set initial value as first item
    }



}
