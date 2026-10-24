package com.stitch.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.stitch.app.data.model.Pact
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
class PactsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository
) {

    private val prefs: SharedPreferences = context.getSharedPreferences("stitch_pacts_prefs", Context.MODE_PRIVATE)

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun observeUserPacts(): Flow<List<Pact>> = callbackFlow {
        val currentUid = authRepository.currentUid
        if (currentUid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val fbUid = auth.currentUser?.uid
        if (fbUid != null) {
            val query = firestore.collection("pacts")
                .where(
                    Filter.or(
                        Filter.equalTo("creatorUid", fbUid),
                        Filter.equalTo("partnerUid", fbUid)
                    )
                )

            val registration = try {
                query.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(getLocalPacts())
                        return@addSnapshotListener
                    }
                    val pacts = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Pact::class.java)?.copy(id = doc.id)
                    }?.sortedByDescending { it.createdAt } ?: emptyList()

                    saveLocalPacts(pacts)
                    trySend(pacts)
                }
            } catch (_: Exception) {
                trySend(getLocalPacts())
                null
            }

            awaitClose { registration?.remove() }
        } else {
            trySend(getLocalPacts())
            awaitClose {}
        }
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
        val currentUid = authRepository.currentUid
        if (currentUid.isBlank()) return Result.failure(Exception("Not logged in"))

        val creatorName = authRepository.getLocalUser()?.displayName ?: "Me"
        val startTime = System.currentTimeMillis()
        val endTime = startTime + (durationMinutes * 60 * 1000L)
        val pactId = UUID.randomUUID().toString()

        val pact = Pact(
            id = pactId,
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
            status = "active",
            acceptedAt = startTime,
            createdAt = startTime
        )

        // Try Firebase Cloud save
        try {
            firestore.collection("pacts").document(pactId).set(pact).await()
            firestore.collection("notifications").add(
                mapOf(
                    "recipientUid" to partnerUid,
                    "title" to "New Accountability Pact Active! 🔒",
                    "message" to "$creatorName locked ${blockedAppNames.joinToString(", ")} for $durationMinutes mins. You are their Guardian.",
                    "timestamp" to System.currentTimeMillis(),
                    "read" to false
                )
            )
        } catch (_: Exception) {}

        // Always save locally so it works offline/locally 100%
        addLocalPact(pact)
        return Result.success(pactId)
    }

    suspend fun recordBreach(pactId: String): Result<Unit> {
        try {
            firestore.collection("pacts").document(pactId).update(
                mapOf(
                    "breachCount" to FieldValue.increment(1),
                    "lastBreachTime" to System.currentTimeMillis()
                )
            ).await()
        } catch (_: Exception) {}
        return Result.success(Unit)
    }

    suspend fun requestEmergencyPass(pactId: String, reason: String): Result<Unit> {
        try {
            firestore.collection("pacts").document(pactId).update(
                mapOf(
                    "emergencyPassRequested" to true,
                    "emergencyPassMinutes" to 5
                )
            ).await()
        } catch (_: Exception) {}
        return Result.success(Unit)
    }

    suspend fun approveEmergencyPass(pactId: String, approved: Boolean): Result<Unit> {
        try {
            val updates = if (approved) {
                mapOf(
                    "emergencyPassApproved" to true,
                    "emergencyPassRequested" to false,
                    "endTime" to FieldValue.increment(5 * 60 * 1000L)
                )
            } else {
                mapOf(
                    "emergencyPassApproved" to false,
                    "emergencyPassRequested" to false
                )
            }
            firestore.collection("pacts").document(pactId).update(updates).await()
        } catch (_: Exception) {}

        val pacts = getLocalPacts().map {
            if (it.id == pactId) {
                it.copy(
                    emergencyPassApproved = approved,
                    emergencyPassRequested = false,
                    endTime = if (approved) it.endTime + (5 * 60 * 1000L) else it.endTime
                )
            } else it
        }
        saveLocalPacts(pacts)
        return Result.success(Unit)
    }

    suspend fun surrenderPact(pactId: String): Result<Unit> {
        try {
            firestore.collection("pacts").document(pactId).update("status", "surrendered").await()
        } catch (_: Exception) {}

        val pacts = getLocalPacts().map {
            if (it.id == pactId) it.copy(status = "surrendered") else it
        }
        saveLocalPacts(pacts)
        return Result.success(Unit)
    }

    suspend fun completePact(pactId: String): Result<Unit> {
        try {
            firestore.collection("pacts").document(pactId).update("status", "completed").await()
        } catch (_: Exception) {}

        val pacts = getLocalPacts().map {
            if (it.id == pactId) it.copy(status = "completed") else it
        }
        saveLocalPacts(pacts)
        return Result.success(Unit)
    }

    private fun getLocalPacts(): List<Pact> {
        val jsonStr = prefs.getString("local_pacts", "[]") ?: "[]"
        val list = mutableListOf<Pact>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val blockedApps = mutableListOf<String>()
                val blockedAppNames = mutableListOf<String>()

                val bApps = obj.optJSONArray("blockedApps")
                if (bApps != null) {
                    for (j in 0 until bApps.length()) blockedApps.add(bApps.getString(j))
                }
                val bNames = obj.optJSONArray("blockedAppNames")
                if (bNames != null) {
                    for (j in 0 until bNames.length()) blockedAppNames.add(bNames.getString(j))
                }

                list.add(
                    Pact(
                        id = obj.optString("id"),
                        creatorUid = obj.optString("creatorUid"),
                        partnerUid = obj.optString("partnerUid"),
                        creatorName = obj.optString("creatorName"),
                        partnerName = obj.optString("partnerName"),
                        title = obj.optString("title"),
                        blockedApps = blockedApps,
                        blockedAppNames = blockedAppNames,
                        durationMinutes = obj.optInt("durationMinutes", 45),
                        startTime = obj.optLong("startTime"),
                        endTime = obj.optLong("endTime"),
                        status = obj.optString("status", "active"),
                        emergencyPassRequested = obj.optBoolean("emergencyPassRequested", false),
                        emergencyPassApproved = obj.optBoolean("emergencyPassApproved", false)
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun saveLocalPacts(pacts: List<Pact>) {
        val arr = JSONArray()
        for (p in pacts) {
            val obj = JSONObject().apply {
                put("id", p.id)
                put("creatorUid", p.creatorUid)
                put("partnerUid", p.partnerUid)
                put("creatorName", p.creatorName)
                put("partnerName", p.partnerName)
                put("title", p.title)
                put("blockedApps", JSONArray(p.blockedApps))
                put("blockedAppNames", JSONArray(p.blockedAppNames))
                put("durationMinutes", p.durationMinutes)
                put("startTime", p.startTime)
                put("endTime", p.endTime)
                put("status", p.status)
                put("emergencyPassRequested", p.emergencyPassRequested)
                put("emergencyPassApproved", p.emergencyPassApproved)
            }
            arr.put(obj)
        }
        prefs.edit().putString("local_pacts", arr.toString()).apply()
    }

    private fun addLocalPact(pact: Pact) {
        val current = getLocalPacts().toMutableList()
        current.removeAll { it.id == pact.id }
        current.add(0, pact)
        saveLocalPacts(current)
    }
}
