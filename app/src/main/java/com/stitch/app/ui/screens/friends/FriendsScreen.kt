package com.stitch.app.ui.screens.friends

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.stitch.app.ui.navigation.Screen
import com.stitch.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    navController: NavController,
    viewModel: FriendsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Accountability Circle",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = StitchTextPrimary
                        )
                        Text(
                            text = "${state.friends.size} Focus Allies Connected",
                            fontSize = 11.sp,
                            color = StitchTextTertiary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.AddFriend.route) }) {
                        Icon(
                            imageVector = Icons.Rounded.PersonAdd,
                            contentDescription = "Add Friend",
                            tint = StitchPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StitchDarkBg)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = StitchDarkBg
    ) { paddingValues ->
        if (state.friends.isEmpty()) {
            // Clean, non-AI empty state with real action
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
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(StitchPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Group,
                            contentDescription = null,
                            tint = StitchPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Text(
                        text = "No Focus Allies Yet",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchTextPrimary
                    )

                    Text(
                        text = "Screen time discipline works 3x better when holding friends accountable. Share your invite code or WhatsApp link to connect with your study buddy.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = StitchTextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = { navController.navigate(Screen.AddFriend.route) },
                        colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = null,
                            tint = StitchDarkBg,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Share Invite Code / WhatsApp",
                            color = StitchDarkBg,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
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
                item {
                    // Mutual Authority Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = StitchCardBg),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StitchSecondary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Security,
                                contentDescription = null,
                                tint = StitchSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Mutual Authority Active",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StitchSecondary
                                )
                                Text(
                                    text = "${state.friends.size} buddies hold mutual remote app block authority",
                                    fontSize = 11.sp,
                                    color = StitchTextSecondary
                                )
                            }
                        }
                    }
                }

                items(state.friends, key = { it.friendshipId }) { friend ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = StitchCardBg),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(StitchSurfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = friend.displayName.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = StitchPrimary
                                        )
                                        if (friend.isOnline) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(StitchSecondary)
                                                    .align(Alignment.BottomEnd)
                                                    .border(2.dp, StitchCardBg, CircleShape)
                                            )
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = friend.displayName,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StitchTextPrimary
                                        )
                                        Text(
                                            text = if (friend.isOnline) "🟢 Online • ${friend.currentActivity}" else "⚪ Offline",
                                            fontSize = 11.sp,
                                            color = if (friend.isOnline) StitchSecondary else StitchTextTertiary
                                        )
                                    }
                                }

                                Row(
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
                                        text = "${friend.currentStreak}d",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = StitchAccent
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.sendNudge(friend) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(vertical = 8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorderActive)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.TouchApp,
                                        contentDescription = null,
                                        tint = StitchPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Send Nudge", color = StitchTextPrimary, fontSize = 12.sp)
                                }

                                Button(
                                    onClick = { navController.navigate(Screen.CreatePact.route) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = StitchPrimaryDim),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lock,
                                        contentDescription = null,
                                        tint = StitchTextPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("New Pact", color = StitchTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
