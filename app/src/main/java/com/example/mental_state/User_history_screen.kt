package com.example.mental_state

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
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

        // Create cells for time and status (these could be TextViews or other views)
        val timeView = createTableCell(time)
        val statusView = createTableCell(status)

        // Get the colored boxes based on the status
        val colorBoxes = getColorBoxes(status)

        // Add the time and status cells to the row
        tableRow.addView(timeView)
        tableRow.addView(statusView)

        // Add the colored boxes to the row
        for (box in colorBoxes) {
            tableRow.addView(box)
        }

        // Add the row to the TableLayout
        TableLayout.addView(tableRow)
    }


    fun createTableCell(text: String): View {
        // Create a cell for the table (this could be a TextView or any other type of view)
        val cell = TextView(this).apply {
            this.text = text
            setPadding(2, 2, 2, 2)
            textSize = 20f
            setTextColor(android.graphics.Color.BLACK)
            gravity = Gravity.CENTER
        }
        return cell
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
            setPadding(2, 2, 2, 2)
            text = message
            textSize = 20f
            if (isError) setTextColor(0xFFFF0000.toInt())
        }.also {
            tableRow.addView(it)
            TableLayout.addView(tableRow)
        }
    }
    fun getColorBoxes(status: String): List<View> {
        val colorBoxes = mutableListOf<View>()

        // Check the status and add the appropriate color boxes
        if ("Red" in status) {
            val redBox = createColoredBox(android.graphics.Color.RED)
            colorBoxes.add(redBox)
        }
        if ("Blue" in status) {
            val blueBox = createColoredBox(android.graphics.Color.BLUE)
            colorBoxes.add(blueBox)
        }
        if ("White" in status) {
            val whiteBox = createColoredBox(android.graphics.Color.WHITE)
            colorBoxes.add(whiteBox)
        }
        if ("Gold" in status) {
            val yellowBox = createColoredBox(android.graphics.Color.YELLOW)
            colorBoxes.add(yellowBox)
        }

        return colorBoxes // Return the list of color boxes
    }

    fun createColoredBox(color: Int): View {
        val box = View(this).apply {
            // Set the size of the box (adjust the size as needed)
            layoutParams = TableRow.LayoutParams(30, 30).apply {  // Increase size if 15x15 is too small
                setMargins(2, 4, 2, 4) // Set margins to space out the boxes
            }

            // Create a background for the square box using GradientDrawable
            val drawable = GradientDrawable().apply {
                setColor(color) // Set the color of the box
                setStroke(2, android.graphics.Color.BLACK) // Set black borders
                shape = GradientDrawable.RECTANGLE // Ensure the shape is a rectangle (square due to equal width and height)
            }

            // Set the background for the box
            background = drawable
        }
        return box
    }


}

