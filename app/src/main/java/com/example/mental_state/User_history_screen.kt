package com.example.mental_state

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
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
    private lateinit var backhistbutton: Button
    private val TAG = "UserHistoryScreen"
    private var isSpinnerInitialized = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_history_screen)

        // Initialize UI components
        initializeViews()
        setupSpinners()
        backhistbutton.setOnClickListener {
            val intent = Intent(this@User_history_screen, UserHomePage::class.java)
            startActivity(intent)
        }
    }


    private fun initializeViews() {
        yearSpin = findViewById(R.id.yearspin)
        monthSpin = findViewById(R.id.monthspin)
        daySpin = findViewById(R.id.dayspin)
        tableLayout = findViewById(R.id.tableLayout)
        backhistbutton = findViewById(R.id.backhistbutton)
    }

    private var isYearSpinnerInitialized = false
    private var isMonthSpinnerInitialized = false
    private var isDaySpinnerInitialized = false

    private fun setupSpinners() {
        val calendar = Calendar.getInstance()
        val thisYear = calendar.get(Calendar.YEAR)
        val thisMonth = calendar.get(Calendar.MONTH) + 1
        val thisDay = calendar.get(Calendar.DAY_OF_MONTH)

        val years = (1900..thisYear).map { it.toString() }
        val months = (1..12).map { it.toString() }
        val days = (1..31).map { it.toString() }

        setupSpinner(yearSpin, years, thisYear.toString()) {
            if (isYearSpinnerInitialized) loadUserHistory() else isYearSpinnerInitialized = true
        }

        setupSpinner(monthSpin, months, thisMonth.toString()) {
            if (isMonthSpinnerInitialized) loadUserHistory() else isMonthSpinnerInitialized = true
        }

        setupSpinner(daySpin, days, thisDay.toString()) {
            if (isDaySpinnerInitialized) loadUserHistory() else isDaySpinnerInitialized = true
        }

        // Automatically load the history for the current day after setting up the spinners
        loadUserHistory()
    }

    private fun setupSpinner(spinner: Spinner, items: List<String>, defaultValue: String, onSelectionChange: () -> Unit) {
        // Create an ArrayAdapter with custom item layout
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items) {
            override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                // Set the text color to black for the selected item
                (view as TextView).setTextColor(0xFF000000.toInt())
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                // You can leave the dropdown items' text color as is or set a different color if needed
                return super.getDropDownView(position, convertView, parent)
            }
        }

        // Apply the adapter to the spinner
        spinner.adapter = adapter
        spinner.setSelection(items.indexOf(defaultValue))

        // Set up the item selection listener
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                onSelectionChange()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }


    private var loadCalls = 0

    private fun loadUserHistory() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.e(TAG, "No user logged in")
            addErrorRow("Please log in to view history")
            return
        }

        clearTable()

        val selectedYear = yearSpin.selectedItem.toString()
        val selectedMonth = monthSpin.selectedItem.toString().toInt()
        val selectedDay = daySpin.selectedItem.toString().toInt()

        Log.d(TAG, "Fetching history for Year: $selectedYear, Month: $selectedMonth, Day: $selectedDay")

        val userUid = currentUser.uid
        val userHistoryRef = FirebaseDatabase.getInstance().getReference("UserHistory").child(userUid)

        userHistoryRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val uniqueRecords = mutableSetOf<UserHistory>()

                val records = snapshot.children.mapNotNull { dataSnapshot ->
                    dataSnapshot.getValue(UserHistory::class.java)
                }.filter { record ->
                    record.year == selectedYear.toInt() &&
                            record.month == selectedMonth &&
                            record.day == selectedDay
                }

                for (record in records) {
                    // Only add unique records
                    if (uniqueRecords.add(record)) {
                        addTableRow(record.time, record.status)
                    }
                }

                if (uniqueRecords.isEmpty()) addNoRecordsRow()
            } else {
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
        Log.d(TAG, "Adding row - Time: $time, Status: $status")
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
    }


    private fun createTableCell(text: String): TextView {
        return TextView(this).apply {
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
            gravity = Gravity.CENTER
            setPadding(5, 10, 5, 10)
            this.text = text
            textSize = 16f
            setTextColor(0xFF000000.toInt())
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