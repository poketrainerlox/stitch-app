package com.stitch.app.ui.screens.pacts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.stitch.app.data.model.Pact
import com.stitch.app.ui.navigation.Screen
import com.stitch.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PactsScreen(
    navController: NavController,
    viewModel: PactsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mutual Focus Pacts", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = StitchTextPrimary)
                        Text("Screen time agreements locked by friends", fontSize = 11.sp, color = StitchTextTertiary)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.CreatePact.route) }) {
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = "Create Pact", tint = StitchPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StitchDarkBg)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.CreatePact.route) },
                containerColor = StitchPrimary,
                contentColor = StitchDarkBg
            ) {
                Icon(imageVector = Icons.Rounded.LockOpen, contentDescription = "New Pact")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = StitchDarkBg
    ) { paddingValues ->
        if (state.pacts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(StitchPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LockClock,
                            contentDescription = null,
                            tint = StitchPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "No Active Focus Pacts",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchTextPrimary
                    )

                    Text(
                        text = "Seal a focus pact with a friend. Pick distracting apps to block and lock your device for deep study or work.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = StitchTextSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Button(
                        onClick = { navController.navigate(Screen.CreatePact.route) },
                        colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Create Your First Pact", color = StitchDarkBg, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val activePacts = state.pacts.filter { it.isCurrentlyActive() }
                val pastPacts = state.pacts.filter { !it.isCurrentlyActive() }

                if (activePacts.isNotEmpty()) {
                    item {
                        Text(
                            text = "ACTIVE PACTS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchTextTertiary,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(activePacts, key = { it.id }) { pact ->
                        PactCard(
                            pact = pact,
                            currentUid = state.currentUid,
                            onApprovePass = { viewModel.approveEmergencyPass(pact.id, true) },
                            onDenyPass = { viewModel.approveEmergencyPass(pact.id, false) },
                            onSurrender = { viewModel.surrenderPact(pact.id) }
                        )
                    }
                }

                if (pastPacts.isNotEmpty()) {
                    item {
                        Text(
                            text = "PAST PACTS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchTextTertiary,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                        )
                    }

                    items(pastPacts, key = { it.id }) { pact ->
                        PactCard(
                            pact = pact,
                            currentUid = state.currentUid,
                            onApprovePass = {},
                            onDenyPass = {},
                            onSurrender = {}
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }
}

@Composable
fun PactCard(
    pact: Pact,
    currentUid: String,
    onApprovePass: () -> Unit,
    onDenyPass: () -> Unit,
    onSurrender: () -> Unit
) {
    val isActive = pact.isCurrentlyActive()
    val isGuardian = pact.isGuardian(currentUid)
    val remainingSecs = pact.remainingSeconds()
    val mins = remainingSecs / 60
    val secs = remainingSecs % 60

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = StitchCardBg),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) StitchSecondary.copy(alpha = 0.5f) else StitchBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isActive) StitchSecondary else StitchTextTertiary)
                    )
                    Text(
                        text = pact.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchTextPrimary
                    )
                }

                Text(
                    text = if (isActive) String.format("%02d:%02d left", mins, secs) else pact.status.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) StitchSecondary else StitchTextTertiary
                )
            }

            Text(
                text = if (isGuardian) "You are Guardian for ${pact.creatorName}" else "Guarded by ${pact.partnerName}",
                fontSize = 12.sp,
                color = StitchTextSecondary
            )

            // Blocked apps chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                pact.blockedAppNames.take(4).forEach { name ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StitchSurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = name, fontSize = 11.sp, color = StitchTextSecondary)
                    }
                }
            }

            // Emergency Pass Pending Approval (Shown to Guardian)
            if (isActive && isGuardian && pact.emergencyPassRequested) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = StitchCardBgElevated),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchAccent)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "⏱️ ${pact.creatorName} requested a 5-min Emergency Pass",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchAccent
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onApprovePass,
                                colors = ButtonDefaults.buttonColors(containerColor = StitchSecondary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Approve 5m", color = StitchDarkBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = onDenyPass,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Deny", color = StitchError, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
