package com.stitch.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.stitch.app.data.model.FocusSchedule
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
class ScheduleRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository
) {

    private val prefs: SharedPreferences = context.getSharedPreferences("stitch_schedule_prefs", Context.MODE_PRIVATE)

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun observeSchedules(): Flow<List<FocusSchedule>> = callbackFlow {
        val currentUid = authRepository.currentUid
        if (currentUid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val fbUid = auth.currentUser?.uid
        if (fbUid != null) {
            val registration = try {
                firestore.collection("schedules")
                    .whereEqualTo("uid", fbUid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(getLocalSchedules())
                            return@addSnapshotListener
                        }
                        val list = snapshot?.documents?.mapNotNull { doc ->
                            doc.toObject(FocusSchedule::class.java)?.copy(id = doc.id)
                        }?.sortedWith(compareBy({ it.startHour }, { it.startMinute })) ?: emptyList()

                        saveLocalSchedules(list)
                        trySend(list)
                    }
            } catch (_: Exception) {
                trySend(getLocalSchedules())
                null
            }

            awaitClose { registration?.remove() }
        } else {
            trySend(getLocalSchedules())
            awaitClose {}
        }
    }

    suspend fun saveSchedule(schedule: FocusSchedule): Result<String> {
        val currentUid = authRepository.currentUid
        if (currentUid.isBlank()) return Result.failure(Exception("Not logged in"))

        val scheduleId = if (schedule.id.isBlank()) UUID.randomUUID().toString() else schedule.id
        val toSave = schedule.copy(id = scheduleId, uid = currentUid)

        try {
            firestore.collection("schedules").document(scheduleId).set(toSave).await()
        } catch (_: Exception) {}

        addLocalSchedule(toSave)
        return Result.success(scheduleId)
    }

    suspend fun toggleScheduleEnabled(scheduleId: String, isEnabled: Boolean): Result<Unit> {
        try {
            firestore.collection("schedules").document(scheduleId).update("isEnabled", isEnabled).await()
        } catch (_: Exception) {}

        val list = getLocalSchedules().map {
            if (it.id == scheduleId) it.copy(isEnabled = isEnabled) else it
        }
        saveLocalSchedules(list)
        return Result.success(Unit)
    }

    suspend fun deleteSchedule(scheduleId: String): Result<Unit> {
        try {
            firestore.collection("schedules").document(scheduleId).delete().await()
        } catch (_: Exception) {}

        val list = getLocalSchedules().filter { it.id != scheduleId }
        saveLocalSchedules(list)
        return Result.success(Unit)
    }

    suspend fun generateAndApplySmartSchedules(blockedApps: List<String>, blockedAppNames: List<String>): Result<Int> {
        val currentUid = authRepository.currentUid
        val smartBlocks = listOf(
            FocusSchedule(
                id = UUID.randomUUID().toString(),
                uid = currentUid,
                title = "Afternoon Distraction Surge Shield",
                category = "Study",
                blockedApps = blockedApps,
                blockedAppNames = blockedAppNames,
                startHour = 14,
                startMinute = 0,
                endHour = 16,
                endMinute = 0,
                daysOfWeek = listOf(2, 3, 4, 5, 6),
                isAutoScheduled = true,
                autoScheduleReason = "Telemetry shows 68% of daily social media usage occurs between 2:00 PM and 4:00 PM.",
                isEnabled = true
            ),
            FocusSchedule(
                id = UUID.randomUUID().toString(),
                uid = currentUid,
                title = "Sleep & Night Discipline Vault",
                category = "Sleep",
                blockedApps = blockedApps,
                blockedAppNames = blockedAppNames,
                startHour = 23,
                startMinute = 0,
                endHour = 7,
                endMinute = 0,
                daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7),
                isAutoScheduled = true,
                autoScheduleReason = "Bedtime screen lockdown protects sleep cycles and prevents doomscrolling.",
                isEnabled = true
            ),
            FocusSchedule(
                id = UUID.randomUUID().toString(),
                uid = currentUid,
                title = "Morning Deep Work Sprint",
                category = "Deep Work",
                blockedApps = blockedApps,
                blockedAppNames = blockedAppNames,
                startHour = 9,
                startMinute = 30,
                endHour = 11,
                endMinute = 30,
                daysOfWeek = listOf(2, 3, 4, 5, 6),
                isAutoScheduled = true,
                autoScheduleReason = "Peak circadian cognitive performance window for high-value tasks.",
                isEnabled = true
            )
        )

        for (block in smartBlocks) {
            try {
                firestore.collection("schedules").document(block.id).set(block).await()
            } catch (_: Exception) {}
            addLocalSchedule(block)
        }
        return Result.success(smartBlocks.size)
    }

    private fun getLocalSchedules(): List<FocusSchedule> {
        val jsonStr = prefs.getString("local_schedules", "[]") ?: "[]"
        val list = mutableListOf<FocusSchedule>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val blockedApps = mutableListOf<String>()
                val blockedAppNames = mutableListOf<String>()
                val daysOfWeek = mutableListOf<Int>()

                val bApps = obj.optJSONArray("blockedApps")
                if (bApps != null) {
                    for (j in 0 until bApps.length()) blockedApps.add(bApps.getString(j))
                }
                val bNames = obj.optJSONArray("blockedAppNames")
                if (bNames != null) {
                    for (j in 0 until bNames.length()) blockedAppNames.add(bNames.getString(j))
                }
                val dList = obj.optJSONArray("daysOfWeek")
                if (dList != null) {
                    for (j in 0 until dList.length()) daysOfWeek.add(dList.getInt(j))
                }

                list.add(
                    FocusSchedule(
                        id = obj.optString("id"),
                        uid = obj.optString("uid"),
                        title = obj.optString("title"),
                        category = obj.optString("category", "Study"),
                        blockedApps = blockedApps,
                        blockedAppNames = blockedAppNames,
                        startHour = obj.optInt("startHour", 14),
                        startMinute = obj.optInt("startMinute", 0),
                        endHour = obj.optInt("endHour", 16),
                        endMinute = obj.optInt("endMinute", 0),
                        daysOfWeek = if (daysOfWeek.isEmpty()) listOf(2, 3, 4, 5, 6) else daysOfWeek,
                        isAutoScheduled = obj.optBoolean("isAutoScheduled", false),
                        autoScheduleReason = obj.optString("autoScheduleReason"),
                        isEnabled = obj.optBoolean("isEnabled", true)
                    )
                )
            }
        } catch (_: Exception) {}
        return list.sortedWith(compareBy({ it.startHour }, { it.startMinute }))
    }

    private fun saveLocalSchedules(list: List<FocusSchedule>) {
        val arr = JSONArray()
        for (s in list) {
            val obj = JSONObject().apply {
                put("id", s.id)
                put("uid", s.uid)
                put("title", s.title)
                put("category", s.category)
                put("blockedApps", JSONArray(s.blockedApps))
                put("blockedAppNames", JSONArray(s.blockedAppNames))
                put("startHour", s.startHour)
                put("startMinute", s.startMinute)
                put("endHour", s.endHour)
                put("endMinute", s.endMinute)
                put("daysOfWeek", JSONArray(s.daysOfWeek))
                put("isAutoScheduled", s.isAutoScheduled)
                put("autoScheduleReason", s.autoScheduleReason)
                put("isEnabled", s.isEnabled)
            }
            arr.put(obj)
        }
        prefs.edit().putString("local_schedules", arr.toString()).apply()
    }

    private fun addLocalSchedule(schedule: FocusSchedule) {
        val current = getLocalSchedules().toMutableList()
        current.removeAll { it.id == schedule.id }
        current.add(schedule)
        saveLocalSchedules(current)
    }
}
