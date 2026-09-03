package com.example.data.auth

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object FirebaseAuthService {
    private const val TAG = "FirebaseAuthService"

    private var firebaseAuthInstance: FirebaseAuth? = null

    val isFirebaseInitialized: Boolean
        get() {
            return try {
                if (firebaseAuthInstance == null) {
                    firebaseAuthInstance = FirebaseAuth.getInstance()
                }
                firebaseAuthInstance != null
            } catch (e: Throwable) {
                Log.w(TAG, "Firebase Auth not initialized: ${e.message}")
                false
            }
        }

    private val auth: FirebaseAuth?
        get() = if (isFirebaseInitialized) firebaseAuthInstance else null

    private val _currentFirebaseUser = MutableStateFlow<FirebaseUser?>(null)
    val currentFirebaseUser: StateFlow<FirebaseUser?> = _currentFirebaseUser.asStateFlow()

    init {
        try {
            auth?.addAuthStateListener { fa ->
                _currentFirebaseUser.value = fa.currentUser
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Could not attach AuthStateListener: ${e.message}")
        }
    }

    suspend fun register(
        email: String,
        password: String,
        fullName: String
    ): Result<FirebaseUser> {
        val authClient = auth ?: return Result.failure(
            IllegalStateException("Firebase is not initialized. Connect google-services.json to enable cloud Firebase Auth.")
        )

        return try {
            val authResult = authClient.createUserWithEmailAndPassword(email.trim(), password).awaitTask()
            val user = authResult.user ?: throw IllegalStateException("Firebase user was null after creation.")

            // Set user display name
            try {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName.trim())
                    .build()
                user.updateProfile(profileUpdates).awaitTask()
            } catch (e: Exception) {
                Log.w(TAG, "Could not update user profile displayName", e)
            }

            // Send email verification
            try {
                user.sendEmailVerification().awaitTask()
            } catch (e: Exception) {
                Log.w(TAG, "Could not send verification email", e)
            }

            _currentFirebaseUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Firebase registration failed", e)
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        val authClient = auth ?: return Result.failure(
            IllegalStateException("Firebase is not initialized. Connect google-services.json to enable cloud Firebase Auth.")
        )

        return try {
            val authResult = authClient.signInWithEmailAndPassword(email.trim(), password).awaitTask()
            val user = authResult.user ?: throw IllegalStateException("Firebase user was null after sign in.")
            _currentFirebaseUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Firebase login failed", e)
            Result.failure(e)
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        val authClient = auth ?: return Result.failure(
            IllegalStateException("Firebase is not initialized. Connect google-services.json to enable cloud Firebase Auth.")
        )

        return try {
            authClient.sendPasswordResetEmail(email.trim()).awaitTask()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Firebase password reset failed", e)
            Result.failure(e)
        }
    }

    suspend fun resendVerificationEmail(): Result<Unit> {
        val user = auth?.currentUser ?: return Result.failure(
            IllegalStateException("No authenticated Firebase user found.")
        )

        return try {
            user.sendEmailVerification().awaitTask()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Resend verification email failed", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
            _currentFirebaseUser.value = null
        } catch (e: Throwable) {
            Log.w(TAG, "Sign out error", e)
        }
    }
}

// Suspend extension for any Google Play Services / Firebase Task without extra external dependencies
private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
        if (continuation.isActive) {
            continuation.resume(result)
        }
    }
    addOnFailureListener { exception ->
        if (continuation.isActive) {
            continuation.resumeWithException(exception)
        }
    }
    addOnCanceledListener {
        if (continuation.isActive) {
            continuation.cancel()
        }
    }
}
