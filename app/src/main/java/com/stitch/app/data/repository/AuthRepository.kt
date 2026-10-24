package com.stitch.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.stitch.app.data.model.StitchUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val messaging: FirebaseMessaging by lazy { FirebaseMessaging.getInstance() }

    val currentFirebaseUser: FirebaseUser?
        get() = auth.currentUser

    fun getAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { authState ->
            trySend(authState.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signUpWithEmail(name: String, email: String, pass: String): Result<StitchUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = authResult.user?.uid ?: throw IllegalStateException("User creation failed")

            val inviteCode = generateUniqueInviteCode()
            val fcmToken = try { messaging.token.await() } catch (e: Exception) { "" }

            val user = StitchUser(
                uid = uid,
                displayName = name.ifBlank { email.substringBefore("@") },
                email = email,
                inviteCode = inviteCode,
                fcmToken = fcmToken,
                isOnline = true,
                lastSeen = System.currentTimeMillis()
            )

            // Save user profile in Firestore
            firestore.collection("users").document(uid).set(user).await()

            // Index invite code for easy friend finding
            firestore.collection("inviteCodes").document(inviteCode).set(
                mapOf("uid" to uid, "displayName" to user.displayName, "createdAt" to System.currentTimeMillis())
            ).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<StitchUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, pass).await()
            val uid = authResult.user?.uid ?: throw IllegalStateException("Sign in failed")

            val doc = firestore.collection("users").document(uid).get().await()
            val user = doc.toObject(StitchUser::class.java) ?: StitchUser(uid = uid, email = email)

            // Update online status & token
            val token = try { messaging.token.await() } catch (e: Exception) { user.fcmToken }
            firestore.collection("users").document(uid).update(
                mapOf("isOnline" to true, "lastSeen" to System.currentTimeMillis(), "fcmToken" to token)
            ).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): StitchUser? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            doc.toObject(StitchUser::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun observeCurrentUser(): Flow<StitchUser?> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val registration = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val user = snapshot?.toObject(StitchUser::class.java)
                trySend(user)
            }
        awaitClose { registration.remove() }
    }

    suspend fun updateProfile(name: String, dailyGoal: Int): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        return try {
            firestore.collection("users").document(uid).update(
                mapOf(
                    "displayName" to name,
                    "dailyGoalMinutes" to dailyGoal
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            try {
                firestore.collection("users").document(uid).update(
                    mapOf("isOnline" to false, "lastSeen" to System.currentTimeMillis())
                ).await()
            } catch (_: Exception) {}
        }
        auth.signOut()
    }

    private fun generateUniqueInviteCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val rnd = SecureRandom()
        return (1..6).map { chars[rnd.nextInt(chars.length)] }.joinToString("")
    }
}
