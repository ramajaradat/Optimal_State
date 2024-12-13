package com.example.mental_state

import android.content.Intent
import android.os.Bundle
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class User_history_screen : AppCompatActivity() {

    private lateinit var yearSpinner: Spinner
    private lateinit var monthSpinner: Spinner
    private lateinit var daySpinner: Spinner
    private lateinit var TableLayout: TableLayout
    private lateinit var userHistoryBackButton: Button
    private var isYearSpinnerInitialized = false
    private var ismonthSpinnernerInitialized = false
    private var isDaySpinnerInitialized = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_history_screen)

        // Initialize UI
        yearSpinner = findViewById(R.id.yearspin)
        monthSpinner = findViewById(R.id.monthSpinner)
        daySpinner = findViewById(R.id.dayspin)
        TableLayout = findViewById(R.id.TableLayout)
        userHistoryBackButton = findViewById(R.id.userHistoryBackButton)

        //Setup Spinner
        setupSpinners()
        //set up button click
        setupButton()
    }

    private fun setupButton() {
        userHistoryBackButton.setOnClickListener {
            val intent = Intent(this@User_history_screen, UserHomePage::class.java)
            startActivity(intent)
        }
    }

    private fun setupSpinners() {
        val calendar = Calendar.getInstance()
        val thisYear = calendar.get(Calendar.YEAR)
        val thisMonth = calendar.get(Calendar.MONTH) + 1
        val thisDay = calendar.get(Calendar.DAY_OF_MONTH)

        val years = (1900..thisYear).map { it.toString() }
        val months = (1..12).map { it.toString() }
        val days = (1..31).map { it.toString() }

        setupSpinner(yearSpinner, years, thisYear.toString()) {
            if (isYearSpinnerInitialized) loadUserHistory() else isYearSpinnerInitialized = true
        }

        setupSpinner(monthSpinner, months, thisMonth.toString()) {
            if (ismonthSpinnernerInitialized) loadUserHistory() else ismonthSpinnernerInitialized =
                true
        }

        setupSpinner(daySpinner, days, thisDay.toString()) {
            if (isDaySpinnerInitialized) loadUserHistory() else isDaySpinnerInitialized = true
        }

        //Auto load history for current day
        loadUserHistory()
    }

    private fun setupSpinner(
        spinner: Spinner,
        items: List<String>,
        defaultValue: String,
        onSelectionChange: () -> Unit
    ) {
        // Create an ArrayAdapter
        val adapter =
            object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items) {
                override fun getView(
                    position: Int,
                    convertView: View?,
                    parent: android.view.ViewGroup
                ): View {
                    val view = super.getView(position, convertView, parent)
                    (view as TextView).setTextColor(0xFF000000.toInt())
                    return view
                }

                override fun getDropDownView(
                    position: Int,
                    convertView: View?,
                    parent: android.view.ViewGroup
                ): View {
                    return super.getDropDownView(position, convertView, parent)
                }
            }

        // Apply  adapter to  spinner
        spinner.adapter = adapter
        spinner.setSelection(items.indexOf(defaultValue))

        // Set up the item
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                onSelectionChange()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }


    private fun loadUserHistory() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            addErrorRow("Please log in to view history")
            return
        }

        clearTable()

        val selectedYear = yearSpinner.selectedItem.toString()
        val selectedMonth = monthSpinner.selectedItem.toString().toInt()
        val selectedDay = daySpinner.selectedItem.toString().toInt()


        val userUid = currentUser.uid
        val userHistoryRef =
            FirebaseDatabase.getInstance().getReference("UserHistory").child(userUid)

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
            addErrorRow("Failed to load history")
        }
    }


    private fun clearTable() {
        for (i in TableLayout.childCount - 1 downTo 1) {
            TableLayout.removeViewAt(i)
        }
    }

    private fun addTableRow(time: String, status: String) {
        val tableRow = TableRow(this).apply {
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            setPadding(5, 5, 5, 5)
            setBackgroundColor(if (TableLayout.childCount % 2 == 0) 0xFFEEEEEE.toInt() else 0xFFFFFFFF.toInt())
        }

        val timeView = createTableCell(time)
        val statusView = createTableCell(status)

        tableRow.addView(timeView)
        tableRow.addView(statusView)
        TableLayout.addView(tableRow)
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
        addMessageRow("No records found for selected date")
    }

    private fun addErrorRow(message: String) {
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
            TableLayout.addView(tableRow)
        }
    }
}