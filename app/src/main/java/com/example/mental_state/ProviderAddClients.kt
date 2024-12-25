package com.example.mental_state

import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProviderAddClients : AppCompatActivity() {
    private lateinit var ProviderBackAddClientButton: Button
    private lateinit var ProviderEnterClientEmail: EditText
    private lateinit var ProviderAddClientButton: Button
    private lateinit var database: FirebaseDatabase
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var ClientNameShow: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_provider_add_clintes)
        // Initialize UI
        initiaizeUI()
        //Setup Button
        setupButton()
    }

    private fun initiaizeUI(){
        database = FirebaseDatabase.getInstance()
        ProviderEnterClientEmail = findViewById(R.id.ProviderEnterClientEmail)
        ProviderAddClientButton = findViewById(R.id.ProviderAddClientButton)
        ProviderBackAddClientButton = findViewById(R.id.ProviderBackAddClientButton)
        mFirebaseAuth = FirebaseAuth.getInstance()
        ClientNameShow = findViewById(R.id.ClientNameShow)
    }
    private fun setupButton(){
        ProviderBackAddClientButton.setOnClickListener {
            val intent = Intent(this, ProviderHomePage::class.java)
            startActivity(intent)
            finish()
        }

        ProviderAddClientButton.setOnClickListener {
            val email = ProviderEnterClientEmail.text.toString().lowercase().trim()

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                ProviderEnterClientEmail.error = "Please enter a valid email address"
                ProviderEnterClientEmail.requestFocus()
                return@setOnClickListener
            }

            val databaseRef = FirebaseDatabase.getInstance().getReference("users")
            databaseRef.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.exists()) {
                            Toast.makeText(this@ProviderAddClients, "Email does not exist. Please try again.", Toast.LENGTH_SHORT).show()
                            ClientNameShow.text = null
                        } else {
                            val firebaseProvider = mFirebaseAuth.currentUser
                            val providerEmail = firebaseProvider?.email.toString()
                            val formattedProviderEmail = providerEmail.replace(".", "_").replace("@", "_")

                            checkIfUserIsProvider(email) { isProvider ->
                                if (isProvider) {
                                    Toast.makeText(this@ProviderAddClients, "Client is  provider You cant add it.", Toast.LENGTH_SHORT).show()
                                    ClientNameShow.text = null

                                } else {
                                    checkIfClientAlreadyAdded( email) { clientAlreadyAdded ->
                                        if (clientAlreadyAdded) {
                                            Toast.makeText(this@ProviderAddClients, "Client is already added.", Toast.LENGTH_SHORT).show()
                                            ClientNameShow.text = null

                                        } else {
                                            showClientName(formattedProviderEmail, email)

                                        }
                                    }
                                }
                            }
                        }
                    }

                    override fun onCancelled(p0: DatabaseError) {
                        Toast.makeText(this@ProviderAddClients, "Database error: ${p0.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
    // Function to check if the client is already added to the provider's dataset
    private fun checkIfClientAlreadyAdded( clientEmail: String, callback: (Boolean) -> Unit) {
        val providersRef = database.getReference("Providers")

        providersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (providerSnapshot in snapshot.children) {
                        val clientsSnapshot = providerSnapshot.child("clients")

                        val clientEmails = clientsSnapshot.children.mapNotNull { it.getValue(String::class.java) }
                        if (clientEmails.contains(clientEmail)) {
                            callback(true)
                            return
                        }
                    }
                    callback(false)
                } else {
                    callback(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Database error: ${error.message}")
                callback(false)
            }
        })
    }
    //function to show Client FullName
    private fun showClientName(providerEmail: String, clientEmail: String) {
        val userinfo= database.reference
        userinfo.child("users")
            .orderByChild("email")
            .equalTo(clientEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { userSnapshot ->
                        val firstName = userSnapshot.child("firstName").getValue(String::class.java) ?: "N/A"
                        val lastName = userSnapshot.child("lastName").getValue(String::class.java) ?: "N/A"
                        val fullName = "$firstName $lastName"

                        ClientNameShow.text = fullName

                        saveClientEmailOnProviderDataset(clientEmail,providerEmail)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(applicationContext, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })

    }
    //Check if the email entered for provider
    private fun checkIfUserIsProvider(clientEmail: String, callback: (Boolean) -> Unit) {
        val userInfoRef = database.reference.child("users")
        userInfoRef.orderByChild("email").equalTo(clientEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userSnapshot = snapshot.children.first()
                        val providerStatus = userSnapshot.child("isprovider").getValue(String::class.java)
                        callback(providerStatus == "yes")
                    } else {
                        callback(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(applicationContext, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    callback(false)
                }
            })
    }
    //save the client email which is added to provider realtime dataset
    private fun saveClientEmailOnProviderDataset(clientEmail:String,providerEmail:String){
        val providerRef = database.reference.child("Providers").child(providerEmail).child("clients")

        providerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val clientEmails = snapshot.children.mapNotNull { it.getValue(String::class.java) }.toMutableList()

                if (!clientEmails.contains(clientEmail)) {
                    clientEmails.add(clientEmail)

                    providerRef.setValue(clientEmails)
                        .addOnSuccessListener {
                            Toast.makeText(applicationContext, "Client added successfully!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(applicationContext, "Failed to add client: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(applicationContext, "Client already added.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

