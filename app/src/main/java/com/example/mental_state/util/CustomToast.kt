package com.example.mentalstate.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.example.mental_state.R


object CustomToast {

    private const val ERROR_COLOR = "#830300"
    private const val SUCCESS_COLOR = "#149414"

    @SuppressLint("InflateParams")
    fun createToast(context: Context, message: String, error: Boolean) {
        // Inflate the custom layout
        val inflater = LayoutInflater.from(context)
        var view = inflater.inflate(R.layout.customtoast, null)

        // Find the TextView in the custom layout
        val toastTextView = view.findViewById<TextView>(R.id.textviewToast)

        // Apply formatting to the message
        val spannableString = SpannableString(message).apply {
            setSpan(StyleSpan(Typeface.BOLD_ITALIC), 0, length, 0)
        }

        // Set the spannable text to the TextView
        toastTextView.text = spannableString

        // Set text color based on error or not
        toastTextView.setTextColor(if (error) Color.parseColor(ERROR_COLOR) else Color.parseColor(SUCCESS_COLOR))

        // Create the Toast
        Toast(context).apply {
            duration = Toast.LENGTH_LONG
            show()
        }
    }
}

