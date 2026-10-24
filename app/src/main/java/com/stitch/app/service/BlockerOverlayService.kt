package com.stitch.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.stitch.app.ui.screens.overlay.BlockerOverlayActivity

class BlockerOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val packageName = intent?.getStringExtra(EXTRA_PACKAGE) ?: ""
        val appName = intent?.getStringExtra(EXTRA_APP_NAME) ?: "Restricted App"
        val guardianName = intent?.getStringExtra(EXTRA_GUARDIAN) ?: "Focus Guardian"
        val pactId = intent?.getStringExtra(EXTRA_PACT_ID) ?: ""
        val remainingSeconds = intent?.getLongExtra(EXTRA_REMAINING_SECONDS, 1800L) ?: 1800L

        // Trigger overlay activity for full compose rich interaction
        val activityIntent = Intent(this, BlockerOverlayActivity::class.java).apply {
            putExtra(BlockerOverlayActivity.EXTRA_BLOCKED_PACKAGE, packageName)
            putExtra(BlockerOverlayActivity.EXTRA_BLOCKED_APP_NAME, appName)
            putExtra(BlockerOverlayActivity.EXTRA_GUARDIAN_NAME, guardianName)
            putExtra(BlockerOverlayActivity.EXTRA_PACT_ID, pactId)
            putExtra(BlockerOverlayActivity.EXTRA_REMAINING_SECONDS, remainingSeconds)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(activityIntent)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
    }

    private fun removeOverlay() {
        overlayView?.let {
            windowManager?.removeView(it)
            overlayView = null
        }
    }

    companion object {
        const val EXTRA_PACKAGE = "extra_package"
        const val EXTRA_APP_NAME = "extra_app_name"
        const val EXTRA_GUARDIAN = "extra_guardian"
        const val EXTRA_PACT_ID = "extra_pact_id"
        const val EXTRA_REMAINING_SECONDS = "extra_remaining_seconds"

        fun showLock(
            context: Context,
            packageName: String,
            appName: String,
            guardianName: String,
            pactId: String,
            remainingSeconds: Long
        ) {
            val intent = Intent(context, BlockerOverlayActivity::class.java).apply {
                putExtra(BlockerOverlayActivity.EXTRA_BLOCKED_PACKAGE, packageName)
                putExtra(BlockerOverlayActivity.EXTRA_BLOCKED_APP_NAME, appName)
                putExtra(BlockerOverlayActivity.EXTRA_GUARDIAN_NAME, guardianName)
                putExtra(BlockerOverlayActivity.EXTRA_PACT_ID, pactId)
                putExtra(BlockerOverlayActivity.EXTRA_REMAINING_SECONDS, remainingSeconds)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(intent)
        }
    }
}
