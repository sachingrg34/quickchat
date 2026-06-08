package com.example.chatapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_layout)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirm = findViewById<EditText>(R.id.etConfirm)
        val btnSignup = findViewById<Button>(R.id.btnSignup)
        val txtStrength = findViewById<TextView>(R.id.txtStrength)

        btnBack.setOnClickListener { finish() }


        etPassword.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                txtStrength.text = checkPasswordStrength(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        btnSignup.setOnClickListener {
            val fullname = etFullName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            val confirm = etConfirm.text.toString().trim()


            if (fullname.isEmpty() || phone.isEmpty() || email.isEmpty() ||
                pass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != confirm) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (checkPasswordStrength(pass) != "Strong Password ✔") {
                Toast.makeText(this, "Weak password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Toast.makeText(this, "Signup failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        return@addOnCompleteListener
                    }


                    val uid = FirebaseAuth.getInstance().currentUser!!.uid
                    val user = UserModel(fullname, email, phone)

                    FirebaseDatabase.getInstance().getReference("users")
                        .child(uid)
                        .setValue(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Signup Successful", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to save user: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
        }
    }

    private fun checkPasswordStrength(password: String): String {
        val upper = Regex(".*[A-Z].*")
        val lower = Regex(".*[a-z].*")
        val digit = Regex(".*[0-9].*")
        val special = Regex(".*[@#\$%^&+=!].*")

        return when {
            password.length < 8 -> "Too short (min 8 characters)"
            !upper.containsMatchIn(password) -> "Add uppercase letter"
            !lower.containsMatchIn(password) -> "Add lowercase letter"
            !digit.containsMatchIn(password) -> "Add number"
            !special.containsMatchIn(password) -> "Add special character"
            else -> "Strong Password ✔"
        }
    }
}
