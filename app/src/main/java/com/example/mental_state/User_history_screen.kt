package com.example.mental_state

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mental_state.Model.UserHistory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar
import java.util.Locale

class User_history_screen : AppCompatActivity() {

    private lateinit var yearSpin: Spinner
    private lateinit var monthSpin: Spinner
    private lateinit var daySpin: Spinner
    private lateinit var tableLayout: TableLayout
    private val TAG = "UserHistoryScreen"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_history_screen)

        // Initialize UI components
        initializeViews()
        setupSpinners()

        // Initial load of data
        loadUserHistory()
    }

    private fun initializeViews() {
        yearSpin = findViewById(R.id.yearspin)
        monthSpin = findViewById(R.id.monthspin)
        daySpin = findViewById(R.id.dayspin)
        tableLayout = findViewById(R.id.tableLayout)
    }

    private fun setupSpinners() {
        val calendar = Calendar.getInstance()
        val thisYear = calendar.get(Calendar.YEAR)
        val thisMonth = calendar.get(Calendar.MONTH) + 1
        val thisDay = calendar.get(Calendar.DAY_OF_MONTH)

        // Setup Years Spinner
        val years = (1900..thisYear).map { it.toString() }
        setupSpinner(yearSpin, years, thisYear.toString())

        // Setup Months Spinner
        val months = (1..12).map { it.toString() }
        setupSpinner(monthSpin, months, thisMonth.toString())

        // Setup Days Spinner
        val days = (1..31).map { it.toString() }
        setupSpinner(daySpin, days, thisDay.toString())

        // Add listeners
        val spinnerListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                loadUserHistory()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        yearSpin.onItemSelectedListener = spinnerListener
        monthSpin.onItemSelectedListener = spinnerListener
        daySpin.onItemSelectedListener = spinnerListener
    }

    private fun setupSpinner(spinner: Spinner, items: List<String>, defaultValue: String) {
        ArrayAdapter(this, android.R.layout.simple_spinner_item, items).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            spinner.setSelection(items.indexOf(defaultValue))
        }
    }

    private fun loadUserHistory() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.e(TAG, "No user logged in")
            addErrorRow("Please log in to view history")
            return
        }

        // Clear existing table rows except header
        clearTable()

        val selectedYear = yearSpin.selectedItem.toString()
        val selectedMonth = monthSpin.selectedItem.toString().toInt()
        val selectedDay = daySpin.selectedItem.toString().toInt()

        Log.d(TAG, "Fetching history for Year: $selectedYear, Month: $selectedMonth, Day: $selectedDay")

        val userUid = currentUser.uid
        val userHistoryRef = FirebaseDatabase.getInstance().getReference("UserHistory").child(userUid)

        userHistoryRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                var foundRecords = false
                val records = snapshot.children.mapNotNull { dataSnapshot ->
                    dataSnapshot.getValue(UserHistory::class.java)
                }

                // Filter the records by the selected date
                val filteredRecords = records.filter { record ->
                    record.year == selectedYear.toInt() &&
                            record.month == selectedMonth &&
                            record.day == selectedDay
                }

                // Add rows to the table for each filtered record
                for (record in filteredRecords) {
                    addTableRow(record.time, record.status)
                    foundRecords = true
                }

                if (!foundRecords) {
                    addNoRecordsRow()
                }
            } else {
                Log.d(TAG, "No history found for the user")
                addNoRecordsRow()
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error fetching history", exception)
            addErrorRow("Failed to load history")
        }
    }

    private fun clearTable() {
        for (i in tableLayout.childCount - 1 downTo 1) {
            tableLayout.removeViewAt(i)
        }
    }

    private fun addTableRow(time: String, status: String) {
        val tableRow = TableRow(this).apply {
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            setPadding(5, 5, 5, 5)
            setBackgroundColor(if (tableLayout.childCount % 2 == 0) 0xFFEEEEEE.toInt() else 0xFFFFFFFF.toInt())
        }

        val timeView = createTableCell(time)
        val statusView = createTableCell(status)

        tableRow.addView(timeView)
        tableRow.addView(statusView)
        tableLayout.addView(tableRow)

        Log.d(TAG, "Added row - Time: $time, Status: $status")
    }

    private fun createTableCell(text: String): TextView {
        return TextView(this).apply {
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            gravity = Gravity.CENTER
            setPadding(5, 10, 5, 10)
            this.text = text
            textSize = 16f
        }
    }

    private fun addNoRecordsRow() {
        Log.d(TAG, "No records found for selected date")
        addMessageRow("No records found for selected date")
    }

    private fun addErrorRow(message: String) {
        Log.e(TAG, "Error: $message")
        addMessageRow(message, true)
    }

    private fun addMessageRow(message: String, isError: Boolean = false) {
        val tableRow = TableRow(this).apply {
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
        }

        TextView(this).apply {
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f)
            gravity = Gravity.CENTER
            setPadding(5, 20, 5, 20)
            text = message
            textSize = 16f
            if (isError) setTextColor(0xFFFF0000.toInt())
        }.also {
            tableRow.addView(it)
            tableLayout.addView(tableRow)
        }
    }
}