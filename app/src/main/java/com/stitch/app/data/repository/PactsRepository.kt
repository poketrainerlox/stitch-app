package com.stitch.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.stitch.app.data.model.Pact
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PactsRepository @Inject constructor() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun observeUserPacts(): Flow<List<Pact>> = callbackFlow {
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val query = firestore.collection("pacts")
            .where(
                Filter.or(
                    Filter.equalTo("creatorUid", currentUid),
                    Filter.equalTo("partnerUid", currentUid)
                )
            )

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val pacts = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Pact::class.java)?.copy(id = doc.id)
            }?.sortedByDescending { it.createdAt } ?: emptyList()

            trySend(pacts)
        }

        awaitClose { registration.remove() }
    }

    suspend fun createPact(
        partnerUid: String,
        partnerName: String,
        title: String,
        blockedApps: List<String>,
        blockedAppNames: List<String>,
        durationMinutes: Int,
        penaltyType: String = "PUSHUPS",
        penaltyAmount: Int = 25
    ): Result<String> {
        val currentUid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        return try {
            val userDoc = firestore.collection("users").document(currentUid).get().await()
            val creatorName = userDoc.getString("displayName") ?: "Ward"

            val startTime = System.currentTimeMillis()
            val endTime = startTime + (durationMinutes * 60 * 1000L)

            val pact = Pact(
                id = "",
                creatorUid = currentUid,
                partnerUid = partnerUid,
                creatorName = creatorName,
                partnerName = partnerName,
                title = title.ifBlank { "Focus Lock ($durationMinutes m)" },
                blockedApps = blockedApps,
                blockedAppNames = blockedAppNames,
                durationMinutes = durationMinutes,
                penaltyType = penaltyType,
                penaltyAmount = penaltyAmount,
                startTime = startTime,
                endTime = endTime,
                status = "active", // Immediately locks for immediate accountability
                acceptedAt = startTime,
                createdAt = startTime
            )

            val docRef = firestore.collection("pacts").add(pact).await()

            // Notify partner (Guardian)
            firestore.collection("notifications").add(
                mapOf(
                    "recipientUid" to partnerUid,
                    "title" to "New Accountability Pact Active! 🔒",
                    "message" to "$creatorName locked ${blockedAppNames.joinToString(", ")} for $durationMinutes mins. You are their Guardian.",
                    "timestamp" to System.currentTimeMillis(),
                    "read" to false
                )
            )

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordBreach(pactId: String): Result<Unit> {
        return try {
            val pactDoc = firestore.collection("pacts").document(pactId).get().await()
            val pact = pactDoc.toObject(Pact::class.java)

            firestore.collection("pacts").document(pactId).update(
                mapOf(
                    "breachCount" to FieldValue.increment(1),
                    "lastBreachTime" to System.currentTimeMillis()
                )
            ).await()

            // Alert guardian
            if (pact != null) {
                firestore.collection("notifications").add(
                    mapOf(
                        "recipientUid" to pact.partnerUid,
                        "title" to "⚠️ Pact Breach Alert!",
                        "message" to "${pact.creatorName} attempted to open a restricted app during active lock!",
                        "timestamp" to System.currentTimeMillis(),
                        "read" to false
                    )
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun requestEmergencyPass(pactId: String, reason: String): Result<Unit> {
        return try {
            val pactDoc = firestore.collection("pacts").document(pactId).get().await()
            val pact = pactDoc.toObject(Pact::class.java)

            firestore.collection("pacts").document(pactId).update(
                mapOf(
                    "emergencyPassRequested" to true,
                    "emergencyPassMinutes" to 5
                )
            ).await()

            if (pact != null) {
                firestore.collection("notifications").add(
                    mapOf(
                        "recipientUid" to pact.partnerUid,
                        "title" to "Emergency 5-Min Pass Request ⏱️",
                        "message" to "${pact.creatorName} asks for 5 min pass: \"$reason\"",
                        "timestamp" to System.currentTimeMillis(),
                        "read" to false
                    )
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun approveEmergencyPass(pactId: String, approved: Boolean): Result<Unit> {
        return try {
            val updates = if (approved) {
                mapOf(
                    "emergencyPassApproved" to true,
                    "emergencyPassRequested" to false,
                    "endTime" to FieldValue.increment(5 * 60 * 1000L) // extend sprint by 5 mins to compensate
                )
            } else {
                mapOf(
                    "emergencyPassApproved" to false,
                    "emergencyPassRequested" to false
                )
            }
            firestore.collection("pacts").document(pactId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun surrenderPact(pactId: String): Result<Unit> {
        val currentUid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        return try {
            firestore.collection("pacts").document(pactId).update(
                mapOf(
                    "status" to "surrendered"
                )
            ).await()

            // Reset streak or mark surrender on user
            firestore.collection("users").document(currentUid).update(
                mapOf(
                    "currentStreak" to 0
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completePact(pactId: String): Result<Unit> {
        val currentUid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
        return try {
            firestore.collection("pacts").document(pactId).update(
                mapOf(
                    "status" to "completed"
                )
            ).await()

            // Increment streak & total completed pacts
            firestore.collection("users").document(currentUid).update(
                mapOf(
                    "currentStreak" to FieldValue.increment(1),
                    "totalPactsCompleted" to FieldValue.increment(1)
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
