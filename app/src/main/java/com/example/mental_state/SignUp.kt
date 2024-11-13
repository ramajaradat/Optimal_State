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
    private lateinit var txfirstname: EditText
    private lateinit var txlastname: EditText
    private lateinit var editTextDate: EditText
    private lateinit var yescheckbox: CheckBox
    private lateinit var nocheckbox: CheckBox
    private lateinit var txEmail: EditText
    private lateinit var txPassword: EditText
    private lateinit var signinButton: Button
    private lateinit var back1Button: Button
    private lateinit var tvprovider: TextView
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        txfirstname = findViewById(R.id.txfirstname)
        txlastname = findViewById(R.id.txlastname)
        editTextDate = findViewById(R.id.editTextDate)
        yescheckbox = findViewById(R.id.yescheckbox)
        nocheckbox = findViewById(R.id.nocheckbox)
        txEmail = findViewById(R.id.txEmail)
        txPassword = findViewById(R.id.txPassword)
        signinButton = findViewById(R.id.signupButton)
        back1Button = findViewById(R.id.back1Button)
        tvprovider = findViewById(R.id.tvprovider)

        mFirebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()

        yescheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                nocheckbox.isChecked = false // Uncheck "No" if "Yes" is checked
            }
        }

        nocheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                yescheckbox.isChecked = false // Uncheck "Yes" if "No" is checked
            }
        }
        back1Button.setOnClickListener {
            val intent = Intent(this@SignUp, Login::class.java)
            startActivity(intent)
        }

        signinButton.setOnClickListener {
            val firstname = txfirstname.text.toString().trim()
            val lastname = txlastname.text.toString().trim()
            val dob = editTextDate.text.toString().trim()
            val email = txEmail.text.toString().trim()
            val pass = txPassword.text.toString().trim()
            val providerStatus = if (yescheckbox.isChecked) "yes" else "no"

            // Validate names contain only alphabetic characters
            val nameRegex = Regex("^[a-zA-Z]+$")
            if (!nameRegex.matches(firstname)) {
                txfirstname.error = "Firstname should contain only letters"
                txfirstname.requestFocus()
                return@setOnClickListener
            }

            if (!nameRegex.matches(lastname)) {
                txlastname.error = "Lastname should contain only letters"
                txlastname.requestFocus()
                return@setOnClickListener
            }

            // Validate date of birth format (dd/MM/yyyy or d/M/yyyy)
            val dobRegex = Regex("^(\\d{1,2})/(\\d{1,2})/(\\d{4})$")
            if (!dobRegex.matches(dob)) {
                editTextDate.error = "Date of Birth should be in dd/MM/yyyy or d/M/yyyy format"
                editTextDate.requestFocus()
                return@setOnClickListener
            }

            // Validate email format
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                txEmail.error = "Please enter a valid email address"
                txEmail.requestFocus()
                return@setOnClickListener
            }



            // Validate password length and complexity
            if (pass.length < 8 || !Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>]).+$").containsMatchIn(pass)) {
                txPassword.error = "Password must be at least 8 characters, with one uppercase, one lowercase letter, and one symbol"
                txPassword.requestFocus()
                return@setOnClickListener
            }

            // Check if only one checkbox is selected
            if (!yescheckbox.isChecked && !nocheckbox.isChecked) {
                tvprovider.error = "Please select the type of your account"
                tvprovider.requestFocus()
                return@setOnClickListener
            }
            // Check if email already exists in Firebase Realtime Database
            val databaseRef = FirebaseDatabase.getInstance().getReference("users")
            databaseRef.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {

                            Toast.makeText(this@SignUp, "Email already Exist Please try again", Toast.LENGTH_SHORT).show()
                        }

           else {

                        mFirebaseAuth.createUserWithEmailAndPassword(email, pass)
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
                                        email,
                                        pass
                                    )
                                    val uid = task.result?.user?.uid
                                    firebaseDatabase.getReference("users").child(uid.toString()).setValue(userInformation)
                                        .addOnSuccessListener {
                                            val intent = Intent(this@SignUp, Login::class.java)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                            startActivity(intent)
                                        }
                                        .addOnFailureListener {
                                            CustomToast.createToast(this@SignUp, "Error occurred while saving user information!", true)
                                        }
                                }
                            }
                    }
                }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@SignUp, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }

                })
    }
}}
