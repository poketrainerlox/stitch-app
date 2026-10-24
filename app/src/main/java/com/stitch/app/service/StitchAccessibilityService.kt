package com.stitch.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.stitch.app.ui.screens.overlay.BlockerOverlayActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class StitchAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            // Don't intercept Stitch itself or system launcher
            if (packageName == applicationContext.packageName) return
            if (packageName == "com.android.systemui" || packageName == "com.google.android.apps.nexuslauncher") return

            // Check if this package is in active blocked apps memory
            if (activeBlockedPackages.contains(packageName)) {
                val appName = activeBlockedAppNames[packageName] ?: "Restricted App"
                val guardian = activeGuardianName

                val lockIntent = Intent(applicationContext, BlockerOverlayActivity::class.java).apply {
                    putExtra(BlockerOverlayActivity.EXTRA_BLOCKED_PACKAGE, packageName)
                    putExtra(BlockerOverlayActivity.EXTRA_BLOCKED_APP_NAME, appName)
                    putExtra(BlockerOverlayActivity.EXTRA_GUARDIAN_NAME, guardian)
                    putExtra(BlockerOverlayActivity.EXTRA_PACT_ID, activePactId)
                    putExtra(BlockerOverlayActivity.EXTRA_REMAINING_SECONDS, activeRemainingSeconds)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                startActivity(lockIntent)
            }
        }
    }

    override fun onInterrupt() {}

    companion object {
        var activeBlockedPackages: Set<String> = emptySet()
        var activeBlockedAppNames: Map<String, String> = emptyMap()
        var activeGuardianName: String = "Focus Guardian"
        var activePactId: String = ""
        var activeRemainingSeconds: Long = 1800L

        fun updateActivePactState(
            packages: Set<String>,
            appNames: Map<String, String>,
            guardian: String,
            pactId: String,
            remainingSeconds: Long
        ) {
            activeBlockedPackages = packages
            activeBlockedAppNames = appNames
            activeGuardianName = guardian
            activePactId = pactId
            activeRemainingSeconds = remainingSeconds
        }

        fun clearPactState() {
            activeBlockedPackages = emptySet()
            activeBlockedAppNames = emptyMap()
            activePactId = ""
        }
    }
}
