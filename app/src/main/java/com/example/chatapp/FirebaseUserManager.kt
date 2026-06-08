
package com.example.chatapp

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class FirebaseUserManager {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().getReference("users")

    fun registerUser(
        fullname: String,
        phone: String,
        email: String,
        password: String,
        callback: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    callback(false, task.exception?.message)
                    return@addOnCompleteListener
                }

                val userId = auth.currentUser!!.uid
                val userData = mapOf(
                    "fullname" to fullname,
                    "phone" to phone,
                    "email" to email
                )

                db.child(userId).setValue(userData)
                    .addOnSuccessListener { callback(true, null) }
                    .addOnFailureListener { callback(false, it.message) }
            }
    }

    fun loginUser(
        email: String,
        password: String,
        callback: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    callback(false, task.exception?.message)
                } else {
                    callback(true, null)
                }
            }
    }
}
