package com.example.mental_state

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.mentalstate.Model.UserInformation
import com.example.mentalstate.util.CustomToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar


class SignUp : AppCompatActivity() {

    private lateinit var firstnameinput: EditText
    private lateinit var lastnameinput: EditText
    private lateinit var birthdaydateinput: EditText
    private lateinit var yesbox: CheckBox
    private lateinit var nobox: CheckBox
    private lateinit var Emailinput: EditText
    private lateinit var passwordinput: EditText
    private lateinit var signinButton: Button
    private lateinit var signupbackbutton: Button
    private lateinit var providershow: TextView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        //initializeUI&Firebase
        initializeUI()
        //set up user buttons click
        setupButtonClick()
    }

    private fun initializeUI(){
        firstnameinput = findViewById(R.id.firstnameinput)
        lastnameinput = findViewById(R.id.lastnameinput)
        birthdaydateinput = findViewById(R.id.birthdaydateinput)
        yesbox = findViewById(R.id.yesbox)
        nobox = findViewById(R.id.nobox)
        Emailinput = findViewById(R.id.Emailinput)
        passwordinput = findViewById(R.id.passwordinput)
        signinButton = findViewById(R.id.signupButton)
        signupbackbutton = findViewById(R.id.signupbackbutton)
        providershow = findViewById(R.id.providershow)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
    }
    private fun setupButtonClick() {
        yesbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                nobox.isChecked = false
            }
        }
        nobox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                yesbox.isChecked = false
            }
        }
        signupbackbutton.setOnClickListener {
            val intent = Intent(this@SignUp, Login::class.java)
            startActivity(intent)
        }
        signinButton.setOnClickListener {

            val firstname = firstnameinput.text.toString().trim()
            val lastname = lastnameinput.text.toString().trim()
            val dob = birthdaydateinput.text.toString().trim()
            val email = Emailinput.text.toString().lowercase().trim()
            val pass = passwordinput.text.toString().trim()
            val providerStatus = if (yesbox.isChecked) "yes" else "no"

            val nameRegex = Regex("^[a-zA-Z]+$")
            if (!nameRegex.matches(firstname)) {
                firstnameinput.error = "Firstname should contain only letters"
                firstnameinput.requestFocus()
                return@setOnClickListener
            }

            if (!nameRegex.matches(lastname)) {
                lastnameinput.error = "Lastname should contain only letters"
                lastnameinput.requestFocus()
                return@setOnClickListener
            }

            val dobRegex = Regex("^(\\d{1,2})/(\\d{1,2})/(\\d{4})$")
            if (!dobRegex.matches(dob)) {
                birthdaydateinput.error = "Date of Birth should be in dd/MM/yyyy or d/M/yyyy format"
                birthdaydateinput.requestFocus()
                return@setOnClickListener
            }
            val matchResult = dobRegex.find(dob)
            val (dayString, monthString, yearString) = matchResult?.destructured ?: return@setOnClickListener
            val day = dayString.toInt()
            val month = monthString.toInt()
            val year = yearString.toInt()

            if (month !in 1..12) {
                birthdaydateinput.error = "Month should be between 1 and 12"
                birthdaydateinput.requestFocus()
                return@setOnClickListener
            }

            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            if (year !in 1900..currentYear) {
                birthdaydateinput.error = "Year should be between 1900 and $currentYear"
                birthdaydateinput.requestFocus()
                return@setOnClickListener
            }

            val isLeapYear = (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0))

            val maxDaysInMonth = when (month) {
                1, 3, 5, 7, 8, 10, 12 -> 31
                4, 6, 9, 11 -> 30
                2 -> if (isLeapYear) 29 else 28
                else -> 0
            }

            if (day !in 1..maxDaysInMonth) {
                birthdaydateinput.error = "Day should be valid for the given month"
                birthdaydateinput.requestFocus()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Emailinput.error = "Please enter a valid email address"
                Emailinput.requestFocus()
                return@setOnClickListener
            }


            if (pass.length < 8 || !Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>]).+$").containsMatchIn(
                    pass
                )
            ) {
                passwordinput.error =
                    "Password must be at least 8 characters, with one uppercase, one lowercase letter, and one symbol"
                passwordinput.requestFocus()
                return@setOnClickListener
            }

            if (!yesbox.isChecked && !nobox.isChecked) {
                providershow.error = "Please select the type of your account"
                providershow.requestFocus()
                return@setOnClickListener
            }
            handelSignUpButton(email, pass, firstname, lastname, dob, providerStatus)
        }

    }

    private fun handelSignUpButton(email: String,pass: String,firstname: String,lastname: String,dob: String, providerStatus: String){
        val databaseRef = FirebaseDatabase.getInstance().getReference("users")
        databaseRef.orderByChild("email").equalTo(email.lowercase())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {

                        Toast.makeText(
                            this@SignUp,
                            "Email already Exist Please try again",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {

                        firebaseAuth.createUserWithEmailAndPassword(email.lowercase(), pass)
                            .addOnCompleteListener(this@SignUp) { task ->
                                if (!task.isSuccessful) {
                                    CustomToast.createToast(
                                        this@SignUp,
                                        "Sign Up Unsuccessful, Please Try Again! " + task.exception?.message,
                                        true
                                    )

                                } else {
                                    val userInformation = UserInformation(
                                        firstname,
                                        lastname,
                                        dob,
                                        providerStatus,
                                        email.lowercase()
                                    )
                                    val uid = task.result?.user?.uid
                                    firebaseDatabase.getReference("users").child(uid.toString())
                                        .setValue(userInformation)
                                        .addOnSuccessListener {
                                            val intent = Intent(this@SignUp, Login::class.java)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                            startActivity(intent)
                                        }
                                        .addOnFailureListener {
                                            CustomToast.createToast(
                                                this@SignUp,
                                                "Error occurred while saving user information!",
                                                true
                                            )
                                        }
                                }
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SignUp, "Error: ${error.message}", Toast.LENGTH_SHORT)
                        .show()
                }

            })
    }


}
