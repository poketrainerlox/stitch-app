package com.stitch.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.stitch.app.data.model.Friendship
import com.stitch.app.data.model.StitchUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendsRepository @Inject constructor() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun observeFriendships(): Flow<List<Friendship>> = callbackFlow {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        // Query friendships where user is user1 or user2
        val query = firestore.collection("friendships")
            .where(
                Filter.or(
                    Filter.equalTo("user1", currentUid),
                    Filter.equalTo("user2", currentUid)
                )
            )

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { it.toObject(Friendship::class.java)?.copy(id = it.id) } ?: emptyList()
            trySend(list)
        }

        awaitClose { registration.remove() }
    }

    suspend fun getUserProfile(uid: String): StitchUser? {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            doc.toObject(StitchUser::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun observeUserProfile(uid: String): Flow<StitchUser?> = callbackFlow {
        val registration = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, _ ->
                val user = snapshot?.toObject(StitchUser::class.java)
                trySend(user)
            }
        awaitClose { registration.remove() }
    }

    suspend fun addFriendByCode(code: String): Result<String> {
        val currentUid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        val normalizedCode = code.trim().uppercase()

        return try {
            // 1. Look up code
            val codeDoc = firestore.collection("inviteCodes").document(normalizedCode).get().await()
            if (!codeDoc.exists()) {
                return Result.failure(Exception("Invite code not found. Please verify with your friend."))
            }

            val targetUid = codeDoc.getString("uid") ?: return Result.failure(Exception("Invalid invite code data"))
            if (targetUid == currentUid) {
                return Result.failure(Exception("You cannot add yourself as a friend!"))
            }

            // 2. Check if friendship already exists
            val existing = firestore.collection("friendships")
                .where(
                    Filter.or(
                        Filter.and(Filter.equalTo("user1", currentUid), Filter.equalTo("user2", targetUid)),
                        Filter.and(Filter.equalTo("user1", targetUid), Filter.equalTo("user2", currentUid))
                    )
                ).get().await()

            if (!existing.isEmpty) {
                return Result.failure(Exception("Friend request or connection already exists."))
            }

            // 3. Fetch both user names
            val currentUserDoc = firestore.collection("users").document(currentUid).get().await()
            val targetUserDoc = firestore.collection("users").document(targetUid).get().await()

            val currentName = currentUserDoc.getString("displayName") ?: "Friend"
            val targetName = targetUserDoc.getString("displayName") ?: "Friend"
            val currentAvatar = currentUserDoc.getString("avatarUrl") ?: ""
            val targetAvatar = targetUserDoc.getString("avatarUrl") ?: ""

            // 4. Create friendship document
            val friendship = Friendship(
                id = "",
                user1 = currentUid,
                user2 = targetUid,
                user1Name = currentName,
                user2Name = targetName,
                user1Avatar = currentAvatar,
                user2Avatar = targetAvatar,
                status = "accepted", // Auto-accept via direct code link for seamless buddying!
                requestedBy = currentUid,
                createdAt = System.currentTimeMillis()
            )

            val newDocRef = firestore.collection("friendships").add(friendship).await()
            // Send in-app notification to target user
            sendNotificationToUser(
                targetUid,
                "New Focus Ally!",
                "$currentName connected with you via invite code. You can now hold each other accountable."
            )

            Result.success("Connected with $targetName!")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendNudge(friendUid: String, friendName: String): Result<Unit> {
        val currentUid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        return try {
            val user = getUserProfile(currentUid)
            val senderName = user?.displayName ?: "Your buddy"
            sendNotificationToUser(
                friendUid,
                "Focus Nudge from $senderName! ⚡",
                "$senderName says: Put the phone down and get back to your focus session!"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun enforceEmergencyLock(friendUid: String, friendName: String, durationMinutes: Int = 30): Result<Unit> {
        val currentUid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        return try {
            val user = getUserProfile(currentUid)
            val senderName = user?.displayName ?: "Guardian"
            sendNotificationToUser(
                friendUid,
                "Disciplinary Lock Enforced! 🔒",
                "$senderName has enforced an emergency ${durationMinutes}m focus lockdown on your device."
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFriendship(friendshipId: String): Result<Unit> {
        return try {
            firestore.collection("friendships").document(friendshipId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun sendNotificationToUser(targetUid: String, title: String, message: String) {
        try {
            firestore.collection("notifications").add(
                mapOf(
                    "recipientUid" to targetUid,
                    "title" to title,
                    "message" to message,
                    "timestamp" to System.currentTimeMillis(),
                    "read" to false
                )
            ).await()
        } catch (_: Exception) {}
    }
}
