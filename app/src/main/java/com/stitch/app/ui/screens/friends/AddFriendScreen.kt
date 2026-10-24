package com.stitch.app.ui.screens.friends

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.stitch.app.data.repository.AuthRepository
import com.stitch.app.data.repository.FriendsRepository
import com.stitch.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFriendScreen(
    navController: NavController,
    authRepository: AuthRepository,
    friendsRepository: FriendsRepository
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var userInviteCode by remember { mutableStateOf("...") }
    var enteredCode by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val user = authRepository.getCurrentUser()
        userInviteCode = user?.inviteCode ?: "STITCH1"
    }

    val shareText = "Hey! I'm using Stitch to control screen time and hold each other accountable. Add me using my invite code: $userInviteCode or tap https://stitch.app/join?code=$userInviteCode"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Focus Ally", fontWeight = FontWeight.Bold, color = StitchTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back", tint = StitchTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StitchDarkBg)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = StitchDarkBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Share Your Code Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StitchCardBg),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, StitchPrimary.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "YOUR UNIQUE INVITE CODE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchTextTertiary,
                        letterSpacing = 1.sp
                    )

                    // Big Code Box
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(StitchSurfaceVariant)
                            .border(1.dp, StitchPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Stitch Code", userInviteCode))
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Code $userInviteCode copied to clipboard!")
                                }
                            }
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = userInviteCode,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchPrimary,
                            letterSpacing = 4.sp
                        )
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = "Copy",
                            tint = StitchPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "Tap code to copy, or share directly via WhatsApp to invite friends into your accountability circle.",
                        fontSize = 12.sp,
                        color = StitchTextSecondary,
                        textAlign = TextAlign.Center
                    )

                    // Share button
                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Stitch Invite"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StitchSecondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = null,
                            tint = StitchDarkBg,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Share via WhatsApp / Messages",
                            color = StitchDarkBg,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Enter Friend's Code Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StitchCardBg),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "ENTER FRIEND'S CODE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchTextTertiary,
                        letterSpacing = 1.sp
                    )

                    OutlinedTextField(
                        value = enteredCode,
                        onValueChange = { if (it.length <= 8) enteredCode = it.uppercase() },
                        placeholder = { Text("e.g. K9X2P4", color = StitchTextTertiary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StitchPrimary,
                            unfocusedBorderColor = StitchBorder,
                            focusedTextColor = StitchTextPrimary,
                            unfocusedTextColor = StitchTextPrimary,
                            cursorColor = StitchPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            if (enteredCode.isBlank()) return@Button
                            isSubmitting = true
                            coroutineScope.launch {
                                val result = friendsRepository.addFriendByCode(enteredCode)
                                isSubmitting = false
                                if (result.isSuccess) {
                                    snackbarHostState.showSnackbar(result.getOrDefault("Connected!"))
                                    enteredCode = ""
                                    navController.popBackStack()
                                } else {
                                    snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: "Error connecting")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = enteredCode.isNotBlank() && !isSubmitting,
                        colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = StitchDarkBg,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Connect Focus Ally", color = StitchDarkBg, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
