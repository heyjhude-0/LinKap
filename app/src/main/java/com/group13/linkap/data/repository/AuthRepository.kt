package com.group13.linkap.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun signIn(
        email: String,
        password: String,
        onResult: (Result<FirebaseUser>) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { authResult ->

                val user = authResult.user

                if (user == null) {
                    auth.signOut()
                    onResult(
                        Result.failure(Exception("Unable to retrieve user account."))
                    )
                    return@addOnSuccessListener
                }

                firestore.collection("users")
                    .document(user.uid)
                    .get()
                    .addOnSuccessListener { document ->

                        val role = document.getString("role")
                        val isActive = document.getBoolean("isActive")

                        when {
                            !document.exists() -> {
                                auth.signOut()
                                onResult(
                                    Result.failure(
                                        Exception("Your account is not authorized for LinKap.")
                                    )
                                )
                            }

                            role != "BHW" -> {
                                auth.signOut()
                                onResult(
                                    Result.failure(
                                        Exception("This account does not have BHW access.")
                                    )
                                )
                            }

                            isActive != true -> {
                                auth.signOut()
                                onResult(
                                    Result.failure(
                                        Exception("Your account has been deactivated. Contact your administrator.")
                                    )
                                )
                            }

                            else -> {
                                onResult(Result.success(user))
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        auth.signOut()
                        onResult(Result.failure(exception))
                    }
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun signOut() {
        auth.signOut()
    }

    fun sendPasswordResetEmail(
        email: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        auth.sendPasswordResetEmail(email.trim())
            .addOnSuccessListener {
                onResult(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                onResult(Result.failure(exception))
            }
    }


    fun validateCurrentSession(
        onResult: (Result<FirebaseUser?>) -> Unit
    ) {
        val user = auth.currentUser

        // No existing Firebase session.
        if (user == null) {
            onResult(Result.success(null))
            return
        }

        firestore.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->

                val role = document.getString("role")
                val isActive = document.getBoolean("isActive")

                if (
                    document.exists() &&
                    role == "BHW" &&
                    isActive == true
                ) {
                    // Authorized BHW.
                    onResult(Result.success(user))
                } else {
                    // Invalid, unauthorized, or deactivated account.
                    auth.signOut()
                    onResult(Result.success(null))
                }
            }
            .addOnFailureListener { exception ->
                // Keep the session if validation fails due to a
                // network or Firestore error. Do not grant access yet.
                onResult(Result.failure(exception))
            }
    }

}
