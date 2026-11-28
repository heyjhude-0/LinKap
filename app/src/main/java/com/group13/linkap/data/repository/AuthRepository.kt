package com.group13.linkap.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.group13.linkap.di.AppModule

class AuthRepository(
    private val context: Context? = null,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val sessionManager = context?.let { AppModule.provideUserSessionManager(it) }

    fun signIn(
        email: String,
        password: String,
        onResult: (Result<FirebaseUser>) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { authResult ->

                val user = authResult.user

                if (user == null) {
                    signOut()
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
                        val barangayId = document.getString("barangayId")?.trim().orEmpty()

                        when {
                            !document.exists() -> {
                                signOut()
                                onResult(
                                    Result.failure(
                                        Exception("Your account is not authorized for LinKap.")
                                    )
                                )
                            }

                            role != "BHW" -> {
                                signOut()
                                onResult(
                                    Result.failure(
                                        Exception("This account does not have BHW access.")
                                    )
                                )
                            }

                            isActive != true -> {
                                signOut()
                                onResult(
                                    Result.failure(
                                        Exception("Your account has been deactivated. Contact your administrator.")
                                    )
                                )
                            }

                            barangayId.isBlank() -> {
                                signOut()
                                onResult(
                                    Result.failure(
                                        Exception("Your account is missing an assigned barangay ID.")
                                    )
                                )
                            }

                            else -> {
                                sessionManager?.saveSession(user.uid, barangayId)
                                context?.let { performLegacyBackfillIfNeeded(it, barangayId) }
                                onResult(Result.success(user))
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        signOut()
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
        sessionManager?.clearSession()
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
            sessionManager?.clearSession()
            onResult(Result.success(null))
            return
        }

        firestore.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->

                val role = document.getString("role")
                val isActive = document.getBoolean("isActive")
                val barangayId = document.getString("barangayId")?.trim().orEmpty()

                if (
                    document.exists() &&
                    role == "BHW" &&
                    isActive == true &&
                    barangayId.isNotBlank()
                ) {
                    sessionManager?.saveSession(user.uid, barangayId)
                    context?.let { performLegacyBackfillIfNeeded(it, barangayId) }
                    onResult(Result.success(user))
                } else {
                    // Invalid, unauthorized, deactivated account, or missing barangayId.
                    signOut()
                    onResult(Result.success(null))
                }
            }
            .addOnFailureListener { exception ->
                // Check if this is a genuine network failure vs a security / authorization error.
                val isNetworkFailure = when (exception) {
                    is FirebaseFirestoreException -> exception.code == FirebaseFirestoreException.Code.UNAVAILABLE
                    else -> exception.message?.contains("network", ignoreCase = true) == true ||
                            exception.message?.contains("unavailable", ignoreCase = true) == true ||
                            exception.message?.contains("offline", ignoreCase = true) == true
                }

                if (isNetworkFailure && sessionManager != null) {
                    val cachedUid = sessionManager.getUid()
                    val cachedBarangayId = sessionManager.getBarangayId()

                    if (user.uid == cachedUid && cachedBarangayId.isNotBlank()) {
                        // Allow offline session validation for genuine network unavailability
                        onResult(Result.success(user))
                        return@addOnFailureListener
                    }
                }

                // If exception is not a network failure or cached session is invalid, do not grant access
                onResult(Result.failure(exception))
            }
    }

    private fun performLegacyBackfillIfNeeded(context: Context, verifiedBarangayId: String) {
        val manager = sessionManager ?: AppModule.provideUserSessionManager(context)
        if (!manager.isLegacyBackfilled() && verifiedBarangayId.isNotBlank()) {
            try {
                val db = AppModule.provideDatabase(context).openHelper.writableDatabase
                db.execSQL("UPDATE households SET barangayId = '$verifiedBarangayId' WHERE barangayId = '' OR barangayId IS NULL")
                db.execSQL("UPDATE residents SET barangayId = '$verifiedBarangayId' WHERE barangayId = '' OR barangayId IS NULL")
                db.execSQL("UPDATE visits SET barangayId = '$verifiedBarangayId' WHERE barangayId = '' OR barangayId IS NULL")
                db.execSQL("UPDATE follow_ups SET barangayId = '$verifiedBarangayId' WHERE barangayId = '' OR barangayId IS NULL")
                manager.setLegacyBackfilled()
            } catch (e: Exception) {
                // Ignore if migration 3_4 hasn't executed yet
            }
        }
    }
}
