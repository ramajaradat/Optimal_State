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


class SignUp : AppCompatActivity() {
    //initializeUI&Firebase

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
        //set up user buttons click
        setupButtonClick()
    }

    private fun setupButtonClick() {
        //to allow user just check yes or no
        yesbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                nobox.isChecked = false // Uncheck "No" if "Yes" is checked
            }
        }

        nobox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                yesbox.isChecked = false // Uncheck "Yes" if "No" is checked
            }
        }
        //set up signup back buttons click
        signupbackbutton.setOnClickListener {
            val intent = Intent(this@SignUp, Login::class.java)
            startActivity(intent)
        }
        //set up signup buttons click
        signinButton.setOnClickListener {

            val firstname = firstnameinput.text.toString().trim()
            val lastname = lastnameinput.text.toString().trim()
            val dob = birthdaydateinput.text.toString().trim()
            val email = Emailinput.text.toString().trim()
            val pass = passwordinput.text.toString().trim()
            val providerStatus = if (yesbox.isChecked) "yes" else "no"

            // Validate names contain only alphabetic characters
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

            // format for date of birth  (dd/MM/yyyy or d/M/yyyy)
            val dobRegex = Regex("^(\\d{1,2})/(\\d{1,2})/(\\d{4})$")
            if (!dobRegex.matches(dob)) {
                birthdaydateinput.error = "Date of Birth should be in dd/MM/yyyy or d/M/yyyy format"
                birthdaydateinput.requestFocus()
                return@setOnClickListener
            }

            // Validate email
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Emailinput.error = "Please enter a valid email address"
                Emailinput.requestFocus()
                return@setOnClickListener
            }


            // Validate password
            if (pass.length < 8 || !Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>]).+$").containsMatchIn(
                    pass
                )
            ) {
                passwordinput.error =
                    "Password must be at least 8 characters, with one uppercase, one lowercase letter, and one symbol"
                passwordinput.requestFocus()
                return@setOnClickListener
            }

            // Check checkbox is selected
            if (!yesbox.isChecked && !nobox.isChecked) {
                providershow.error = "Please select the type of your account"
                providershow.requestFocus()
                return@setOnClickListener
            }
            handelSignUpButton(email, pass, firstname, lastname, dob, providerStatus)
        }

    }

    private fun handelSignUpButton(
        email: String,
        pass: String,
        firstname: String,
        lastname: String,
        dob: String,
        providerStatus: String,
    ) {
        // Check if email already exists in dataset
        val databaseRef = FirebaseDatabase.getInstance().getReference("users")
        databaseRef.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {

                        Toast.makeText(
                            this@SignUp,
                            "Email already Exist Please try again",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {

                        firebaseAuth.createUserWithEmailAndPassword(email, pass)
                            .addOnCompleteListener(this@SignUp) { task ->
                                if (!task.isSuccessful) {
                                    CustomToast.createToast(
                                        this@SignUp,
                                        "Sign Up Unsuccessful, Please Try Again! " + task.exception?.message,
                                        true
                                    )

                                } else {
                                    if (providerStatus.toString()=="yes") {
                                        // Format the email to be Firebase-compatible
                                        val formattedEmail =
                                            email.replace(".", "_").replace("@", "_")


                                        // Reference to the Firebase Realtime Database
                                        val database =
                                            FirebaseDatabase.getInstance().getReference("Providers")


                                        // Create an entry with an empty email column
                                        val providerData: MutableMap<String, Any> = HashMap()
                                        providerData["email"] = ""
                                        database.child(formattedEmail).setValue(providerData)
                                            .addOnSuccessListener { aVoid: Void? ->
                                                Toast.makeText(
                                                    this@SignUp,
                                                    "Provider dataset created successfully!",
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                            }
                                            .addOnFailureListener { e: Exception ->
                                                Toast.makeText(
                                                    this@SignUp,
                                                    "Failed to create dataset: ",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }

                                    val userInformation = UserInformation(
                                        firstname,
                                        lastname,
                                        dob,
                                        providerStatus,
                                        email,
                                        pass
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
    class ProviderModel {
        var email: String? = null

        constructor()

        constructor(email: String?) {
            this.email = email
        }
    }
}
