package com.stitch.app.data.model

data class Pact(
    val id: String = "",
    val creatorUid: String = "",
    val partnerUid: String = "",
    val creatorName: String = "",
    val partnerName: String = "",
    val title: String = "Focus Sprint",
    val blockedApps: List<String> = emptyList(),
    val blockedAppNames: List<String> = emptyList(),
    val durationMinutes: Int = 45,
    val penaltyType: String = "PUSHUPS", // "PUSHUPS" or "STREAK_RESET"
    val penaltyAmount: Int = 25, // 25 pushups
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val status: String = "pending", // "pending" | "active" | "completed" | "surrendered" | "rejected"
    val breachCount: Int = 0,
    val lastBreachTime: Long = 0L,
    val emergencyPassRequested: Boolean = false,
    val emergencyPassApproved: Boolean = false,
    val emergencyPassMinutes: Int = 0,
    val acceptedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun isGuardian(currentUid: String): Boolean = currentUid == partnerUid
    fun isWard(currentUid: String): Boolean = currentUid == creatorUid
    fun isCurrentlyActive(): Boolean {
        val now = System.currentTimeMillis()
        return status == "active" && now in startTime..endTime
    }
    fun remainingSeconds(): Long {
        val now = System.currentTimeMillis()
        return if (endTime > now) (endTime - now) / 1000 else 0L
    }
}
