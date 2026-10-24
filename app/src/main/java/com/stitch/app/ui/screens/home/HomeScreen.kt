package com.stitch.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.stitch.app.ui.components.AppUsageRow
import com.stitch.app.ui.components.PermissionBanner
import com.stitch.app.ui.components.ScreenTimeGauge
import com.stitch.app.ui.navigation.Screen
import com.stitch.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(StitchPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LockClock,
                                contentDescription = null,
                                tint = StitchPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Stitch",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = StitchTextPrimary
                            )
                            Text(
                                text = "Peer Screen Time Guard",
                                fontSize = 11.sp,
                                color = StitchTextTertiary
                            )
                        }
                    }
                },
                actions = {
                    // Streak Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(StitchAccent.copy(alpha = 0.15f))
                            .border(1.dp, StitchAccent.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = null,
                            tint = StitchAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${state.user?.currentStreak ?: 0}d",
                            color = StitchAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StitchDarkBg)
            )
        },
        containerColor = StitchDarkBg
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Permission check banner
            item {
                PermissionBanner(
                    hasUsageAccess = state.hasUsageAccess,
                    hasOverlayAccess = state.hasOverlayAccess,
                    hasAccessibilityAccess = state.hasAccessibilityAccess,
                    onRefresh = { viewModel.loadData() }
                )
            }

            // Active Guardian/Pact Card if currently locking
            if (state.activePact != null) {
                item {
                    val pact = state.activePact!!
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = StitchCardBg),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StitchSecondary.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(StitchSecondary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.VerifiedUser,
                                        contentDescription = null,
                                        tint = StitchSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "GUARDIAN ACTIVE",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StitchSecondary,
                                            letterSpacing = 0.5.sp
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(StitchSecondary)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Secured by ${pact.partnerName}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = StitchTextPrimary
                                    )
                                    Text(
                                        text = "${pact.blockedAppNames.size} apps shielded",
                                        fontSize = 12.sp,
                                        color = StitchTextSecondary
                                    )
                                }
                            }

                            Button(
                                onClick = { navController.navigate(Screen.Pacts.route) },
                                colors = ButtonDefaults.buttonColors(containerColor = StitchSecondary.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("View", color = StitchSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Screen Time Center Gauge
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = StitchCardBg),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ScreenTimeGauge(
                            usedMinutes = state.totalScreenTimeMinutes,
                            dailyGoalMinutes = state.dailyGoalMinutes
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Focus Streak",
                                    fontSize = 12.sp,
                                    color = StitchTextSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${state.user?.currentStreak ?: 0} Days 🔥",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StitchTextPrimary
                                )
                            }

                            Divider(
                                modifier = Modifier
                                    .height(36.dp)
                                    .width(1.dp),
                                color = StitchBorder
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Pacts Sealed",
                                    fontSize = 12.sp,
                                    color = StitchTextSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${state.user?.totalPactsCompleted ?: 0} Done 🎯",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StitchTextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Quick Actions: Instant Focus Lockdown
            item {
                Button(
                    onClick = { viewModel.engageInstantLockdown() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = null,
                        tint = StitchDarkBg
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (state.isLockdownEngaged) "Lockdown Engaged 🔒" else "Engage 25m Focus Lockdown",
                        color = StitchDarkBg,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // App Breakdown List
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = StitchCardBg),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "App Screen Time Breakdown",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = StitchTextPrimary
                            )
                            Text(
                                text = "Today",
                                fontSize = 12.sp,
                                color = StitchTextTertiary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val maxUsage = state.topApps.maxOfOrNull { it.usageMinutes } ?: 1L
                        if (state.topApps.isEmpty()) {
                            Text(
                                text = "No app usage detected yet today.",
                                fontSize = 12.sp,
                                color = StitchTextSecondary,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            state.topApps.forEach { appInfo ->
                                AppUsageRow(usageInfo = appInfo, maxMinutes = maxUsage)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
