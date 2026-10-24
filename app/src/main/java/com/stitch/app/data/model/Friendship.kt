package com.stitch.app.data.model

data class Friendship(
    val id: String = "",
    val user1: String = "",
    val user2: String = "",
    val user1Name: String = "",
    val user2Name: String = "",
    val user1Avatar: String = "",
    val user2Avatar: String = "",
    val status: String = "pending", // "pending" | "accepted" | "rejected"
    val requestedBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getFriendUid(currentUid: String): String = if (user1 == currentUid) user2 else user1
    fun getFriendName(currentUid: String): String = if (user1 == currentUid) user2Name else user1Name
    fun getFriendAvatar(currentUid: String): String = if (user1 == currentUid) user2Avatar else user1Avatar
}
