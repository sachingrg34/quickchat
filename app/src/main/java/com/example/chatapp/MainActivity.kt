package com.example.chatapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private val firebaseManager = FirebaseUserManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnSignup = findViewById<Button>(R.id.btnSignup)
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter email & password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if (!task.isSuccessful) {
                        val exception = task.exception
                        val msg = exception?.message ?: ""

                        val message = when (exception) {


                            is com.google.firebase.auth.FirebaseAuthInvalidUserException ->
                                "Email address doesn’t exist"


                            is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> {
                                if (msg.contains("format", ignoreCase = true) ||
                                    msg.contains("email", ignoreCase = true)) {
                                    "Badly formatted email address"
                                } else {
                                    "Incorrect password or email address"
                                }
                            }

                            else -> "Login failed: ${exception?.localizedMessage}"
                        }

                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        return@addOnCompleteListener
                    }


                    val userId = FirebaseAuth.getInstance().currentUser!!.uid
                    val db = FirebaseDatabase.getInstance().getReference("users")

                    db.child(userId).get().addOnSuccessListener { snapshot ->

                        if (!snapshot.exists()) {
                            Toast.makeText(this, "User profile not found in database", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }

                        val fullname = snapshot.child("fullname").value.toString()

                        val intent = Intent(this, DashboardActivity::class.java)
                        intent.putExtra("fullname", fullname)
                        intent.putExtra("email", email)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                }
        }




        btnSignup.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }


    }
}
