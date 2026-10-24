package com.stitch.app.data.model

import android.graphics.drawable.Drawable

data class AppUsageInfo(
    val packageName: String = "",
    val appName: String = "",
    val usageMinutes: Long = 0L,
    val category: String = "Other", // "Social", "Entertainment", "Productivity", "Utility", "Other"
    val icon: Drawable? = null
) {
    fun formattedUsage(): String {
        val hours = usageMinutes / 60
        val mins = usageMinutes % 60
        return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
    }
}

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable? = null,
    val category: String = "Other"
)
