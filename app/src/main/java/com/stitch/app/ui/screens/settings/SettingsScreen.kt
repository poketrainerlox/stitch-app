package com.stitch.app.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.stitch.app.data.model.StitchUser
import com.stitch.app.data.repository.AuthRepository
import com.stitch.app.data.repository.UsageRepository
import com.stitch.app.service.UsageTrackingService
import com.stitch.app.ui.navigation.Screen
import com.stitch.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    authRepository: AuthRepository,
    usageRepository: UsageRepository
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var user by remember { mutableStateOf<StitchUser?>(null) }
    var dailyGoalHours by remember { mutableFloatStateOf(4f) }
    var hasUsage by remember { mutableStateOf(false) }
    var hasOverlay by remember { mutableStateOf(false) }
    var hasAccessibility by remember { mutableStateOf(false) }
    var isForegroundActive by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val u = authRepository.getCurrentUser()
        user = u
        dailyGoalHours = (u?.dailyGoalMinutes ?: 240) / 60f
        hasUsage = usageRepository.hasUsageStatsPermission()
        hasOverlay = usageRepository.hasOverlayPermission()
        hasAccessibility = usageRepository.isAccessibilityServiceEnabled()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Profile", fontWeight = FontWeight.Bold, color = StitchTextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StitchDarkBg)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = StitchDarkBg
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = StitchCardBg),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(StitchPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (user?.displayName ?: "S").take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = StitchPrimary
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user?.displayName ?: "Focus Ally",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = StitchTextPrimary
                            )
                            Text(
                                text = user?.email ?: "",
                                fontSize = 12.sp,
                                color = StitchTextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StitchSurfaceVariant)
                                    .clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Stitch Code", user?.inviteCode ?: ""))
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Invite Code copied!")
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Code: ${user?.inviteCode ?: "STITCH1"}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StitchPrimary
                                )
                                Icon(
                                    imageVector = Icons.Rounded.ContentCopy,
                                    contentDescription = null,
                                    tint = StitchPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Daily Target Slider
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = StitchCardBg),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daily Screen Time Target",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = StitchTextPrimary
                            )
                            Text(
                                text = "${dailyGoalHours.toInt()}h 00m",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = StitchPrimary
                            )
                        }

                        Slider(
                            value = dailyGoalHours,
                            onValueChange = {
                                dailyGoalHours = it
                                coroutineScope.launch {
                                    authRepository.updateProfile(
                                        name = user?.displayName ?: "",
                                        dailyGoal = (dailyGoalHours * 60).toInt()
                                    )
                                }
                            },
                            valueRange = 1f..8f,
                            steps = 6,
                            colors = SliderDefaults.colors(thumbColor = StitchPrimary, activeTrackColor = StitchPrimary)
                        )
                    }
                }
            }

            // Permissions Checklist
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = StitchCardBg),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "SECURITY & ACCESS PERMISSIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchTextTertiary,
                            letterSpacing = 1.sp
                        )

                        // Usage Access
                        PermissionRow(
                            title = "App Usage Stats Access",
                            subtitle = "Required to track daily app screen time accurately",
                            isGranted = hasUsage,
                            onGrant = {
                                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                })
                            }
                        )

                        // Overlay Permission
                        PermissionRow(
                            title = "Display Over Other Apps",
                            subtitle = "Required to show focus lock screen when app is restricted",
                            isGranted = hasOverlay,
                            onGrant = {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                                context.startActivity(intent)
                            }
                        )

                        // Accessibility Service
                        PermissionRow(
                            title = "Stitch Focus Guard Accessibility",
                            subtitle = "Enables instant zero-lag app blocking without battery drain",
                            isGranted = hasAccessibility,
                            onGrant = {
                                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                })
                            }
                        )
                    }
                }
            }

            // Background Service Toggle
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = StitchCardBg),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Background Focus Guard Service",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = StitchTextPrimary
                            )
                            Text(
                                text = "Keeps monitoring active even when Stitch is closed",
                                fontSize = 12.sp,
                                color = StitchTextSecondary
                            )
                        }

                        Switch(
                            checked = isForegroundActive,
                            onCheckedChange = {
                                isForegroundActive = it
                                if (it) {
                                    UsageTrackingService.startService(context)
                                } else {
                                    UsageTrackingService.stopService(context)
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = StitchDarkBg, checkedTrackColor = StitchPrimary)
                        )
                    }
                }
            }

            // Sign out
            item {
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            authRepository.signOut()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchError.copy(alpha = 0.5f))
                ) {
                    Icon(imageVector = Icons.Rounded.Logout, contentDescription = null, tint = StitchError)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out", color = StitchError, fontWeight = FontWeight.Bold)
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun PermissionRow(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onGrant: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = StitchTextPrimary)
                if (isGranted) {
                    Icon(imageVector = Icons.Rounded.CheckCircle, contentDescription = null, tint = StitchSecondary, modifier = Modifier.size(16.dp))
                }
            }
            Text(text = subtitle, fontSize = 11.sp, color = StitchTextSecondary)
        }

        if (!isGranted) {
            Button(
                onClick = onGrant,
                colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Grant", color = StitchDarkBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
