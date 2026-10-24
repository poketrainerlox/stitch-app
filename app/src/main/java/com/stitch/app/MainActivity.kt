package com.stitch.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.stitch.app.data.repository.AuthRepository
import com.stitch.app.data.repository.FriendsRepository
import com.stitch.app.data.repository.PactsRepository
import com.stitch.app.data.repository.UsageRepository
import com.stitch.app.service.UsageTrackingService
import com.stitch.app.ui.components.StitchBottomBar
import com.stitch.app.ui.navigation.Screen
import com.stitch.app.ui.navigation.StitchNavGraph
import com.stitch.app.ui.theme.StitchDarkBg
import com.stitch.app.ui.theme.StitchTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var friendsRepository: FriendsRepository

    @Inject
    lateinit var pactsRepository: PactsRepository

    @Inject
    lateinit var usageRepository: UsageRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start background vigilance monitoring service
        UsageTrackingService.startService(this)

        val currentUser = authRepository.currentFirebaseUser
        val startDestination = if (currentUser != null) Screen.Home.route else Screen.Login.route

        handleDeepLink(intent)

        setContent {
            StitchTheme {
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { StitchBottomBar(navController = navController) },
                    containerColor = StitchDarkBg
                ) { innerPadding ->
                    StitchNavGraph(
                        navController = navController,
                        paddingValues = innerPadding,
                        startDestination = startDestination,
                        authRepository = authRepository,
                        friendsRepository = friendsRepository,
                        pactsRepository = pactsRepository,
                        usageRepository = usageRepository
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val uri = intent?.data ?: return
        val code = uri.getQueryParameter("code")
        if (!code.isNullOrBlank()) {
            // If logged in, auto add or navigate to add friend
            val currentUid = authRepository.currentFirebaseUser?.uid
            if (currentUid != null) {
                // Background coroutine to add friend
                android.widget.Toast.makeText(this, "Connecting with invite code: $code", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }
}
