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
    private lateinit var otherListSpinner: Spinner
    private lateinit var foodListSpinner: Spinner
    private lateinit var breathingListSpinner: Spinner
    private lateinit var videoListSpinner: Spinner
    private lateinit var backExersisesButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_exercise)
        // Initialize UI
        initializeUI()
        setupUserButton()
        //Get user last status
        getLastStatus()
    }

    private fun initializeUI(){
        otherListSpinner = findViewById(R.id.otherListSpinner)
        foodListSpinner = findViewById(R.id.foodListSpinner)
        breathingListSpinner = findViewById(R.id.breathingListSpinner)
        videoListSpinner = findViewById(R.id.videoListSpinner)
        backExersisesButton = findViewById(R.id.backExersisesButton)
        //Setup User Buttons
    }
    private fun setupUserButton() {
        backExersisesButton.setOnClickListener {
            val intent = Intent(this@UserExercise, UserHomePage::class.java)
            startActivity(intent)
        }
    }

    private fun getLastStatus() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userUid = currentUser.uid
            val userHistoryRef =
                FirebaseDatabase.getInstance().getReference("UserHistory").child(userUid)

            userHistoryRef.orderByKey().limitToLast(1)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val lastStatus = snapshot.children.iterator().next().child("status")
                                .getValue(String::class.java)
                            Toast.makeText(
                                this@UserExercise, "Last status: $lastStatus", Toast.LENGTH_SHORT
                            ).show()
                            val statuses = lastStatus?.split(",")?.map { it.trim() } ?: emptyList()

                            fetchDataFromFirestore(statuses)
                        } else {
                            Toast.makeText(
                                this@UserExercise,
                                "No status history available for the current user.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            this@UserExercise,
                            "Error retrieving user history: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }

    private fun fetchDataFromFirestore(statuses: List<String>) {
        val db = FirebaseFirestore.getInstance()
        val breathingExercises = mutableListOf<String>()
        val foods = mutableListOf<String>()
        val otherExercises = mutableListOf<String>()
        val recommendedVideos = mutableListOf<String>()

        val statusToDocId = mapOf(
            "Red" to "HgVJpyoUYLZgMfq1TMM9",
            "Blue" to "IeQGuaztpUb0iY1YPiwX",
            "Gold" to "Bs7i9lI3nMr0xUEdZnMF",
            "White" to "iEUt0GdunvMBjfyTOGI9"
        )

        var statusProcessedCount = 0

        statuses.forEach { status ->
            val docId = statusToDocId[status]
            if (docId != null) {
                val statusDocRef = db.collection(status).document(docId)
                statusDocRef.get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        try {
                            val breathingList =
                                document.get("Breath") as? List<String> ?: emptyList()
                            val foodList = document.get("Food") as? List<String> ?: emptyList()
                            val otherExerciseList =
                                document.get("Other") as? List<String> ?: emptyList()
                            val videoList = document.get("Video") as? List<String> ?: emptyList()

                            breathingExercises.addAll(breathingList)
                            foods.addAll(foodList)
                            otherExercises.addAll(otherExerciseList)
                            recommendedVideos.addAll(videoList)

                        } catch (e: Exception) {
                            Log.e("UserExercise", "Error processing document: ${e.message}")
                        }
                    } else {
                        Log.e("UserExercise", "Document does not exist for status: $status")
                    }

                    statusProcessedCount++

                    if (statusProcessedCount == statuses.size) {
                        Log.d("UserExercise", "All statuses processed, updating spinners.")
                        runOnUiThread {
                            updateSpinners(
                                breathingExercises, foods, otherExercises, recommendedVideos
                            )
                        }
                    }
                }.addOnFailureListener { e ->
                    Log.e(
                        "UserExercise", "Error fetching document for status $status: ${e.message}"
                    )
                    statusProcessedCount++
                }
            } else {
                Log.e("UserExercise", "No Firestore document found for status: $status")
            }
        }
    }

    private fun updateSpinners(
        breathingExercises: List<String>,
        foods: List<String>,
        otherExercises: List<String>,
        recommendedVideos: List<String>
    ) {
        val initialBreathingValue = "Breathing Exercises"
        val initialFoodValue = "Recommended Foods"
        val initialOtherValue = "Other Exercises"
        val initialVideoValue = "Watch Video"

        breathAdapter(initialBreathingValue, breathingExercises)
        foodsAdapter(initialFoodValue, foods)
        otherAdapter(initialOtherValue, otherExercises)
        videoAdapter(initialVideoValue, recommendedVideos)

    }

    private fun breathAdapter(initialBreathingValue: String, breathingExercises: List<String>) {
        val breathingAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf(initialBreathingValue) + breathingExercises
        )
        breathingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        breathingListSpinner.adapter = breathingAdapter
        breathingListSpinner.setSelection(0)
        breathingListSpinner.setSelection(0)
        breathingListSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                if (position == 0) {
                    (view as? TextView)?.setTextColor(Color.WHITE)
                } else {
                    (view as? TextView)?.setTextColor(Color.BLACK)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })

    }

    private fun foodsAdapter(initialFoodValue: String, foods: List<String>) {
        val foodAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, listOf(initialFoodValue) + foods
        )
        foodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        foodListSpinner.adapter = foodAdapter
        foodListSpinner.setSelection(0)
        foodListSpinner.setSelection(0)
        foodListSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                if (position == 0) {
                    (view as? TextView)?.setTextColor(Color.WHITE)
                } else {
                    (view as? TextView)?.setTextColor(Color.BLACK)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })

    }

    private fun otherAdapter(initialOtherValue: String, otherExercises: List<String>) {
        val otherAdapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, listOf(initialOtherValue) + otherExercises
        )
        otherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        otherListSpinner.adapter = otherAdapter
        otherListSpinner.setSelection(0)
        otherListSpinner.setSelection(0)
        otherListSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                if (position == 0) {
                    (view as? TextView)?.setTextColor(Color.WHITE)
                } else {
                    (view as? TextView)?.setTextColor(Color.BLACK)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })
    }

    private fun videoAdapter(initialVideoValue: String, recommendedVideos: List<String>) {
        val videoAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf(initialVideoValue) + recommendedVideos
        )
        videoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        videoListSpinner.adapter = videoAdapter
        videoListSpinner.setSelection(0)
        videoListSpinner.setSelection(0)
        videoListSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                if (position == 0) {
                    (view as? TextView)?.setTextColor(Color.WHITE)
                } else {
                    (view as? TextView)?.setTextColor(Color.BLACK)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })
    }


}
