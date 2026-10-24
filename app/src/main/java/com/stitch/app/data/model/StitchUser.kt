package com.stitch.app.data.model

data class StitchUser(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val inviteCode: String = "",
    val fcmToken: String = "",
    val dailyGoalMinutes: Int = 240,
    val currentStreak: Int = 0,
    val totalPactsCompleted: Int = 0,
    val isOnline: Boolean = false,
    val lastSeen: Long = 0L,
    val currentActivity: String = "Idle",
    val createdAt: Long = System.currentTimeMillis()
)
