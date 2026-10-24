package com.stitch.app.ui.screens.pacts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.navigation.NavController
import com.stitch.app.data.model.InstalledAppInfo
import com.stitch.app.data.repository.FriendsRepository
import com.stitch.app.data.repository.PactsRepository
import com.stitch.app.data.repository.UsageRepository
import com.stitch.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePactScreen(
    navController: NavController,
    pactsRepository: PactsRepository,
    friendsRepository: FriendsRepository,
    usageRepository: UsageRepository
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var title by remember { mutableStateOf("Study Focus Sprint") }
    var durationMinutes by remember { mutableIntStateOf(45) }
    var selectedApps by remember { mutableStateOf(setOf<String>()) }
    var appNamesMap by remember { mutableStateOf(mapOf<String, String>()) }
    var installedApps by remember { mutableStateOf(listOf<InstalledAppInfo>()) }
    var isSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val apps = usageRepository.getInstalledLaunchableApps()
        installedApps = apps

        // Default select typical social apps if installed
        val defaultSocial = apps.filter { it.category == "Social" || it.category == "Entertainment" }
            .map { it.packageName }
            .take(4)
        selectedApps = defaultSocial.toSet()
        appNamesMap = apps.associate { it.packageName to it.appName }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seal Focus Pact", fontWeight = FontWeight.Bold, color = StitchTextPrimary) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "PACT DETAILS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchTextTertiary,
                    letterSpacing = 1.sp
                )
            }

            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Sprint Title", color = StitchTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StitchPrimary,
                        unfocusedBorderColor = StitchBorder,
                        focusedTextColor = StitchTextPrimary,
                        unfocusedTextColor = StitchTextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Duration selector
            item {
                Text(
                    text = "DURATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StitchTextTertiary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(25 to "25m", 45 to "45m", 60 to "60m", 90 to "90m").forEach { (mins, label) ->
                        val selected = durationMinutes == mins
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) StitchPrimary else StitchCardBg)
                                .border(1.dp, if (selected) StitchPrimary else StitchBorder, RoundedCornerShape(10.dp))
                                .clickable { durationMinutes = mins }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) StitchDarkBg else StitchTextPrimary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Apps to block section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "APPS TO RESTRICT (${selectedApps.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchTextTertiary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Select all distracting apps",
                        fontSize = 11.sp,
                        color = StitchTextSecondary
                    )
                }
            }

            items(installedApps.take(15), key = { it.packageName }) { app ->
                val isSelected = selectedApps.contains(app.packageName)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedApps = if (isSelected) {
                                selectedApps - app.packageName
                            } else {
                                selectedApps + app.packageName
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = StitchCardBg),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) StitchPrimary.copy(alpha = 0.5f) else StitchBorder
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StitchSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = app.appName.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = StitchPrimary
                                )
                            }
                            Column {
                                Text(
                                    text = app.appName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = StitchTextPrimary
                                )
                                Text(
                                    text = app.category,
                                    fontSize = 11.sp,
                                    color = StitchTextTertiary
                                )
                            }
                        }

                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = {
                                selectedApps = if (it) selectedApps + app.packageName else selectedApps - app.packageName
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = StitchPrimary,
                                checkmarkColor = StitchDarkBg,
                                uncheckedColor = StitchBorder
                            )
                        )
                    }
                }
            }

            // Submit CTA
            item {
                Button(
                    onClick = {
                        if (selectedApps.isEmpty()) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Please select at least 1 app to restrict.")
                            }
                            return@Button
                        }
                        isSubmitting = true
                        coroutineScope.launch {
                            val blockedNames = selectedApps.map { appNamesMap[it] ?: it }
                            val res = pactsRepository.createPact(
                                partnerUid = "peer_guardian",
                                partnerName = "Focus Buddy",
                                title = title,
                                blockedApps = selectedApps.toList(),
                                blockedAppNames = blockedNames,
                                durationMinutes = durationMinutes
                            )
                            isSubmitting = false
                            if (res.isSuccess) {
                                navController.popBackStack()
                            } else {
                                snackbarHostState.showSnackbar(res.exceptionOrNull()?.message ?: "Failed to seal pact")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = StitchDarkBg, strokeWidth = 2.dp)
                    } else {
                        Icon(imageVector = Icons.Rounded.Lock, contentDescription = null, tint = StitchDarkBg)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lock Apps & Seal Pact ($durationMinutes min)",
                            color = StitchDarkBg,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
