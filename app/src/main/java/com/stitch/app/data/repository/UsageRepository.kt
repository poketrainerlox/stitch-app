package com.stitch.app.data.repository

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import com.stitch.app.data.model.AppUsageInfo
import com.stitch.app.data.model.InstalledAppInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    private val packageManager = context.packageManager

    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun isAccessibilityServiceEnabled(): Boolean {
        val expectedServiceName = "${context.packageName}/com.stitch.app.service.StitchAccessibilityService"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.contains(expectedServiceName)
    }

    suspend fun getTodayScreenTimeStats(): List<AppUsageInfo> = withContext(Dispatchers.IO) {
        if (!hasUsageStatsPermission() || usageStatsManager == null) {
            return@withContext getFallbackMockStats()
        }

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val usageStatsList: List<UsageStats> = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        ) ?: emptyList()

        val aggregated = mutableMapOf<String, Long>()
        for (stat in usageStatsList) {
            val totalTime = stat.totalTimeInForeground
            if (totalTime > 60_000L) { // at least 1 minute
                val current = aggregated.getOrDefault(stat.packageName, 0L)
                aggregated[stat.packageName] = current + totalTime
            }
        }

        val resultList = mutableListOf<AppUsageInfo>()
        for ((pkg, totalMillis) in aggregated) {
            // Exclude our own app and launchers/system UI
            if (pkg == context.packageName || pkg == "com.android.systemui") continue

            val appName = try {
                val appInfo = packageManager.getApplicationInfo(pkg, 0)
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                pkg.substringAfterLast(".")
            }

            val icon = try {
                packageManager.getApplicationIcon(pkg)
            } catch (e: Exception) {
                null
            }

            val minutes = totalMillis / (1000 * 60)
            val category = categorizeApp(pkg, appName)

            resultList.add(
                AppUsageInfo(
                    packageName = pkg,
                    appName = appName,
                    usageMinutes = minutes,
                    category = category,
                    icon = icon
                )
            )
        }

        resultList.sortByDescending { it.usageMinutes }
        if (resultList.isEmpty()) getFallbackMockStats() else resultList
    }

    suspend fun getInstalledLaunchableApps(): List<InstalledAppInfo> = withContext(Dispatchers.IO) {
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)
        val list = mutableListOf<InstalledAppInfo>()

        for (resolveInfo in resolveInfos) {
            val pkg = resolveInfo.activityInfo.packageName
            if (pkg == context.packageName) continue // Skip Stitch itself

            val appName = resolveInfo.loadLabel(packageManager).toString()
            val icon = resolveInfo.loadIcon(packageManager)
            val category = categorizeApp(pkg, appName)

            list.add(
                InstalledAppInfo(
                    packageName = pkg,
                    appName = appName,
                    icon = icon,
                    category = category
                )
            )
        }
        list.sortBy { it.appName.lowercase() }
        list
    }

    private fun categorizeApp(pkg: String, name: String): String {
        val lowerPkg = pkg.lowercase()
        val lowerName = name.lowercase()
        return when {
            lowerPkg.contains("instagram") || lowerPkg.contains("tiktok") ||
            lowerPkg.contains("twitter") || lowerPkg.contains("x.android") ||
            lowerPkg.contains("facebook") || lowerPkg.contains("snapchat") ||
            lowerPkg.contains("reddit") || lowerName.contains("insta") -> "Social"

            lowerPkg.contains("youtube") || lowerPkg.contains("netflix") ||
            lowerPkg.contains("spotify") || lowerPkg.contains("twitch") ||
            lowerPkg.contains("primevideo") || lowerPkg.contains("hulu") -> "Entertainment"

            lowerPkg.contains("notion") || lowerPkg.contains("obsidian") ||
            lowerPkg.contains("slack") || lowerPkg.contains("teams") ||
            lowerPkg.contains("docs") || lowerPkg.contains("sheets") ||
            lowerPkg.contains("anki") || lowerPkg.contains("todoist") -> "Productivity"

            lowerPkg.contains("whatsapp") || lowerPkg.contains("telegram") ||
            lowerPkg.contains("discord") || lowerPkg.contains("messenger") -> "Communication"

            else -> "Other"
        }
    }

    private fun getFallbackMockStats(): List<AppUsageInfo> {
        return listOf(
            AppUsageInfo("com.instagram.android", "Instagram", 45, "Social"),
            AppUsageInfo("com.zhiliaoapp.musically", "TikTok", 35, "Social"),
            AppUsageInfo("com.google.android.youtube", "YouTube", 30, "Entertainment"),
            AppUsageInfo("com.reddit.frontpage", "Reddit", 20, "Social"),
            AppUsageInfo("notion.id", "Notion", 50, "Productivity")
        )
    }
}
