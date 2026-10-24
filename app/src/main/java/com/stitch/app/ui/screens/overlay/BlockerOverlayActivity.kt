package com.stitch.app.ui.screens.overlay

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stitch.app.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class BlockerOverlayActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appPackage = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE) ?: ""
        val appName = intent.getStringExtra(EXTRA_BLOCKED_APP_NAME) ?: "Restricted App"
        val guardianName = intent.getStringExtra(EXTRA_GUARDIAN_NAME) ?: "Focus Guardian"
        val pactId = intent.getStringExtra(EXTRA_PACT_ID) ?: ""
        val remainingSeconds = intent.getLongExtra(EXTRA_REMAINING_SECONDS, 1800L)

        setContent {
            StitchTheme {
                BlockerOverlayScreen(
                    blockedAppName = appName,
                    guardianName = guardianName,
                    initialRemainingSeconds = remainingSeconds,
                    onCloseApp = {
                        // Go home
                        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_HOME)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(homeIntent)
                        finish()
                    },
                    onRequestPass = {
                        // Triggers pass request
                    },
                    onSurrender = {
                        finish()
                    }
                )
            }
        }
    }

    override fun onBackPressed() {
        // Enforce focus: back button goes to Android Home screen, not back to the restricted app!
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "extra_blocked_package"
        const val EXTRA_BLOCKED_APP_NAME = "extra_blocked_app_name"
        const val EXTRA_GUARDIAN_NAME = "extra_guardian_name"
        const val EXTRA_PACT_ID = "extra_pact_id"
        const val EXTRA_REMAINING_SECONDS = "extra_remaining_seconds"
    }
}

@Composable
fun BlockerOverlayScreen(
    blockedAppName: String,
    guardianName: String,
    initialRemainingSeconds: Long,
    onCloseApp: () -> Unit,
    onRequestPass: () -> Unit,
    onSurrender: () -> Unit
) {
    var secondsLeft by remember { mutableLongStateOf(initialRemainingSeconds) }
    var passRequested by remember { mutableStateOf(false) }
    var showSurrenderConfirm by remember { mutableStateOf(false) }
    var pushupChallengeActive by remember { mutableStateOf(false) }
    var pushupCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft--
        }
    }

    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StitchDarkBg.copy(alpha = 0.96f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Pill indicator
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(StitchError.copy(alpha = 0.15f))
                    .border(1.dp, StitchError.copy(alpha = 0.4f), RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(StitchError)
                )
                Text(
                    text = "App Shielded: $blockedAppName",
                    color = StitchError,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Big Lock Icon with ring
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(StitchCardBg)
                    .border(2.dp, StitchPrimary.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = StitchPrimary,
                    modifier = Modifier.size(52.dp)
                )
            }

            // Headline
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Focus Lock in Progress",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = StitchTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Locked by $guardianName to protect your goals.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StitchTextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            // Countdown Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StitchSurfaceVariant),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LOCK EXPIRES IN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchTextTertiary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = timeFormatted,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shield,
                            contentDescription = null,
                            tint = StitchSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Mutual Accountability Active",
                            fontSize = 12.sp,
                            color = StitchSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Push-up penalty challenge mode
            if (pushupChallengeActive) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = StitchCardBgElevated),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchSecondary.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "💪 Physical Proof Challenge",
                            fontWeight = FontWeight.Bold,
                            color = StitchSecondary,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Complete 25 pushups to earn an emergency 5-min unlock without breaking your streak.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "$pushupCount / 25 Reps",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchTextPrimary
                        )
                        LinearProgressIndicator(
                            progress = { pushupCount / 25f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = StitchSecondary,
                            trackColor = StitchBorder
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    if (pushupCount < 25) pushupCount += 5
                                    if (pushupCount >= 25) {
                                        // Unlocked!
                                        onCloseApp()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StitchSecondary)
                            ) {
                                Text("+5 Push-ups Done", color = StitchDarkBg, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary: Back to Work / Close
                Button(
                    onClick = onCloseApp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = null,
                        tint = StitchDarkBg
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Put Phone Down & Focus",
                        color = StitchDarkBg,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Secondary: Request 5-min Emergency Pass
                OutlinedButton(
                    onClick = {
                        passRequested = true
                        onRequestPass()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorderActive)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = null,
                        tint = StitchTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (passRequested) "Pass Requested from $guardianName..." else "Ask $guardianName for 5-Min Pass",
                        color = StitchTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Alternative: Physical Challenge
                TextButton(
                    onClick = { pushupChallengeActive = !pushupChallengeActive },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FitnessCenter,
                        contentDescription = null,
                        tint = StitchSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Do Push-up Challenge to Unlock",
                        color = StitchSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Danger: Surrender
                TextButton(
                    onClick = { showSurrenderConfirm = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Surrender & Break Focus Pact",
                        color = StitchError.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Surrender confirmation dialog
        if (showSurrenderConfirm) {
            AlertDialog(
                onDismissRequest = { showSurrenderConfirm = false },
                containerColor = StitchSurface,
                title = {
                    Text("Break Focus Pact?", color = StitchTextPrimary, fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(
                        "Surrendering will notify $guardianName, reset your focus streak to 0, and record a discipline infraction.",
                        color = StitchTextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSurrenderConfirm = false
                            onSurrender()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StitchError)
                    ) {
                        Text("Break Pact", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showSurrenderConfirm = false }) {
                        Text("Keep Focusing", color = StitchTextPrimary)
                    }
                }
            )
        }
    }
}
