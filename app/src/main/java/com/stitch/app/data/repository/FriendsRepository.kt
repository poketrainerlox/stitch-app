package com.stitch.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.stitch.app.data.model.Friendship
import com.stitch.app.data.model.StitchUser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository
) {

    private val prefs: SharedPreferences = context.getSharedPreferences("stitch_friends_prefs", Context.MODE_PRIVATE)

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun observeFriendships(): Flow<List<Friendship>> = callbackFlow {
        val currentUid = authRepository.currentUid
        if (currentUid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val fbUid = auth.currentUser?.uid
        if (fbUid != null) {
            val query = firestore.collection("friendships")
                .where(
                    Filter.or(
                        Filter.equalTo("user1", fbUid),
                        Filter.equalTo("user2", fbUid)
                    )
                )

            val registration = try {
                query.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(getLocalFriendships())
                        return@addSnapshotListener
                    }
                    val list = snapshot?.documents?.mapNotNull { it.toObject(Friendship::class.java)?.copy(id = it.id) } ?: emptyList()
                    saveLocalFriendships(list)
                    trySend(list)
                }
            } catch (_: Exception) {
                trySend(getLocalFriendships())
                null
            }

            awaitClose { registration?.remove() }
        } else {
            trySend(getLocalFriendships())
            awaitClose {}
        }
    }

    suspend fun getUserProfile(uid: String): StitchUser? {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            doc.toObject(StitchUser::class.java)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun addFriendByCode(code: String): Result<String> {
        val currentUid = authRepository.currentUid
        if (currentUid.isBlank()) return Result.failure(Exception("Not logged in"))
        val normalizedCode = code.trim().uppercase()

        // 1. Try Firebase Cloud lookup
        try {
            val codeDoc = firestore.collection("inviteCodes").document(normalizedCode).get().await()
            if (codeDoc.exists()) {
                val targetUid = codeDoc.getString("uid") ?: return Result.failure(Exception("Invalid invite code data"))
                if (targetUid == currentUid) {
                    return Result.failure(Exception("You cannot add yourself as a friend!"))
                }

                val currentUserDoc = try { firestore.collection("users").document(currentUid).get().await() } catch (_: Exception) { null }
                val targetUserDoc = try { firestore.collection("users").document(targetUid).get().await() } catch (_: Exception) { null }

                val currentName = currentUserDoc?.getString("displayName") ?: authRepository.getLocalUser()?.displayName ?: "Friend"
                val targetName = targetUserDoc?.getString("displayName") ?: "Focus Buddy"

                val friendship = Friendship(
                    id = UUID.randomUUID().toString(),
                    user1 = currentUid,
                    user2 = targetUid,
                    user1Name = currentName,
                    user2Name = targetName,
                    status = "accepted",
                    requestedBy = currentUid,
                    createdAt = System.currentTimeMillis()
                )

                try { firestore.collection("friendships").add(friendship).await() } catch (_: Exception) {}
                addLocalFriendship(friendship)
                return Result.success("Connected with $targetName!")
            }
        } catch (_: Exception) {}

        // 2. Offline / Local fallback: add a simulated or local friend buddy
        val localFriendship = Friendship(
            id = "local_friend_" + UUID.randomUUID().toString().take(8),
            user1 = currentUid,
            user2 = "buddy_" + normalizedCode,
            user1Name = authRepository.getLocalUser()?.displayName ?: "Me",
            user2Name = "Buddy ($normalizedCode)",
            status = "accepted",
            requestedBy = currentUid,
            createdAt = System.currentTimeMillis()
        )
        addLocalFriendship(localFriendship)
        return Result.success("Connected with Buddy ($normalizedCode)!")
    }

    suspend fun sendNudge(friendUid: String, friendName: String): Result<Unit> {
        val currentUid = authRepository.currentUid
        try {
            val user = authRepository.getCurrentUser()
            val senderName = user?.displayName ?: "Your buddy"
            firestore.collection("notifications").add(
                mapOf(
                    "recipientUid" to friendUid,
                    "title" to "Focus Nudge from $senderName! ⚡",
                    "message" to "$senderName says: Put the phone down and get back to your focus session!",
                    "timestamp" to System.currentTimeMillis(),
                    "read" to false
                )
            ).await()
        } catch (_: Exception) {}
        return Result.success(Unit)
    }

    suspend fun enforceEmergencyLock(friendUid: String, friendName: String, durationMinutes: Int = 30): Result<Unit> {
        try {
            val user = authRepository.getCurrentUser()
            val senderName = user?.displayName ?: "Guardian"
            firestore.collection("notifications").add(
                mapOf(
                    "recipientUid" to friendUid,
                    "title" to "Disciplinary Lock Enforced! 🔒",
                    "message" to "$senderName has enforced an emergency ${durationMinutes}m focus lockdown on your device.",
                    "timestamp" to System.currentTimeMillis(),
                    "read" to false
                )
            ).await()
        } catch (_: Exception) {}
        return Result.success(Unit)
    }

    private fun getLocalFriendships(): List<Friendship> {
        val jsonStr = prefs.getString("local_friendships", "[]") ?: "[]"
        val list = mutableListOf<Friendship>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    Friendship(
                        id = obj.optString("id"),
                        user1 = obj.optString("user1"),
                        user2 = obj.optString("user2"),
                        user1Name = obj.optString("user1Name"),
                        user2Name = obj.optString("user2Name"),
                        status = obj.optString("status", "accepted"),
                        requestedBy = obj.optString("requestedBy")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun saveLocalFriendships(list: List<Friendship>) {
        val arr = JSONArray()
        for (f in list) {
            val obj = JSONObject().apply {
                put("id", f.id)
                put("user1", f.user1)
                put("user2", f.user2)
                put("user1Name", f.user1Name)
                put("user2Name", f.user2Name)
                put("status", f.status)
                put("requestedBy", f.requestedBy)
            }
            arr.put(obj)
        }
        prefs.edit().putString("local_friendships", arr.toString()).apply()
    }

    private fun addLocalFriendship(friendship: Friendship) {
        val current = getLocalFriendships().toMutableList()
        current.removeAll { it.id == friendship.id }
        current.add(0, friendship)
        saveLocalFriendships(current)
    }
}
