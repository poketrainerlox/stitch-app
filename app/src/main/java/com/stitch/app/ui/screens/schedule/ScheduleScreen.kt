package com.stitch.app.ui.screens.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.stitch.app.data.model.FocusSchedule
import com.stitch.app.ui.theme.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    navController: NavController,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateDialog by remember { mutableStateOf(false) }

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
                        Text("Calendar & Routines", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = StitchTextPrimary)
                        Text("Auto-scheduled screen time shields", fontSize = 11.sp, color = StitchTextTertiary)
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add Schedule", tint = StitchPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StitchDarkBg)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = StitchPrimary,
                contentColor = StitchDarkBg
            ) {
                Icon(imageVector = Icons.Rounded.Event, contentDescription = "Add Schedule")
            }
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
            // Week Strip Calendar
            item {
                WeekStripCalendar()
            }

            // AI Smart Routine Auto-Schedule Hero Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = StitchCardBgElevated),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StitchPrimary.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(StitchPrimary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        tint = StitchPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "Smart Routine Auto-Schedule",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StitchTextPrimary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StitchSecondary.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "INTELLIGENCE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StitchSecondary
                                )
                            }
                        }

                        Text(
                            text = "Auto-detects typical peak distraction hours (Afternoon 2–4 PM, Night Sleep Vault) and configures automatic focus locks without manual setup.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StitchTextSecondary
                        )

                        Button(
                            onClick = { viewModel.applySmartRoutines() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Bolt,
                                contentDescription = null,
                                tint = StitchDarkBg,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Auto-Apply Smart Routines",
                                color = StitchDarkBg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE ROUTINES (${state.schedules.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchTextTertiary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Auto-locks engage automatically",
                        fontSize = 11.sp,
                        color = StitchTextSecondary
                    )
                }
            }

            if (state.schedules.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = StitchCardBg),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarToday,
                                contentDescription = null,
                                tint = StitchTextTertiary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "No Scheduled Routines Yet",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = StitchTextPrimary
                            )
                            Text(
                                text = "Tap 'Auto-Apply Smart Routines' above or tap '+' to add custom study and sleep blocks.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = StitchTextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(state.schedules, key = { it.id }) { schedule ->
                    ScheduleCard(
                        schedule = schedule,
                        onToggle = { viewModel.toggleSchedule(schedule.id, it) },
                        onDelete = { viewModel.deleteSchedule(schedule.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Create Custom Schedule Dialog
        if (showCreateDialog) {
            CreateScheduleDialog(
                onDismiss = { showCreateDialog = false },
                onConfirm = { title, cat, sHour, sMin, eHour, eMin, days, apps, names ->
                    viewModel.addCustomSchedule(title, cat, sHour, sMin, eHour, eMin, days, apps, names)
                    showCreateDialog = false
                }
            )
        }
    }
}

@Composable
fun WeekStripCalendar() {
    val cal = Calendar.getInstance()
    val todayDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)

    val dayLabels = listOf(
        2 to "Mon", 3 to "Tue", 4 to "Wed", 5 to "Thu", 6 to "Fri", 7 to "Sat", 1 to "Sun"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = StitchCardBg),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            dayLabels.forEach { (dayCode, name) ->
                val isToday = dayCode == todayDayOfWeek
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isToday) StitchPrimary.copy(alpha = 0.15f) else androidx.compose.ui.graphics.Color.Transparent)
                        .border(
                            1.dp,
                            if (isToday) StitchPrimary.copy(alpha = 0.5f) else androidx.compose.ui.graphics.Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = name,
                        fontSize = 11.sp,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                        color = if (isToday) StitchPrimary else StitchTextTertiary
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isToday) StitchSecondary else StitchBorder)
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduleCard(
    schedule: FocusSchedule,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val isRunningNow = schedule.isCurrentlyActive()

    val categoryColor = when (schedule.category) {
        "Sleep" -> StitchMediaColor
        "Deep Work" -> StitchSecondary
        else -> StitchPrimary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = StitchCardBg),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isRunningNow) StitchSecondary.copy(alpha = 0.5f) else StitchBorder
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
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isRunningNow) StitchSecondary else categoryColor)
                    )
                    Text(
                        text = schedule.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchTextPrimary
                    )
                }

                Switch(
                    checked = schedule.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = StitchDarkBg,
                        checkedTrackColor = StitchPrimary,
                        uncheckedThumbColor = StitchTextTertiary,
                        uncheckedTrackColor = StitchSurfaceVariant
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = StitchPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = schedule.formattedTimeRange(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StitchTextPrimary
                    )
                }

                Text(
                    text = "•",
                    color = StitchTextTertiary
                )

                Text(
                    text = schedule.formattedDays(),
                    fontSize = 12.sp,
                    color = StitchTextSecondary
                )
            }

            if (schedule.autoScheduleReason.isNotBlank()) {
                Text(
                    text = "💡 ${schedule.autoScheduleReason}",
                    fontSize = 11.sp,
                    color = StitchTextSecondary,
                    lineHeight = 15.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${schedule.blockedAppNames.size} apps shielded",
                    fontSize = 11.sp,
                    color = StitchTextTertiary
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Delete",
                        tint = StitchError.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CreateScheduleDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, Int, Int, Int, List<Int>, List<String>, List<String>) -> Unit
) {
    var title by remember { mutableStateOf("Study Focus Block") }
    var category by remember { mutableStateOf("Study") }
    var startHour by remember { mutableIntStateOf(14) }
    var endHour by remember { mutableIntStateOf(16) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StitchSurface,
        title = {
            Text("Create Scheduled Focus Block", fontWeight = FontWeight.Bold, color = StitchTextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Block Title", color = StitchTextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StitchPrimary,
                        unfocusedBorderColor = StitchBorder,
                        focusedTextColor = StitchTextPrimary,
                        unfocusedTextColor = StitchTextPrimary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("Study", "Deep Work", "Sleep").forEach { cat ->
                        val selected = category == cat
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) StitchPrimary else StitchCardBg)
                                .clickable { category = cat }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat,
                                color = if (selected) StitchDarkBg else StitchTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Time Range:", fontSize = 12.sp, color = StitchTextSecondary)
                    Text("$startHour:00 – $endHour:00", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StitchPrimary)
                }

                Slider(
                    value = startHour.toFloat(),
                    onValueChange = { startHour = it.toInt(); if (endHour <= startHour) endHour = startHour + 1 },
                    valueRange = 0f..23f,
                    steps = 22,
                    colors = SliderDefaults.colors(thumbColor = StitchPrimary, activeTrackColor = StitchPrimary)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        title,
                        category,
                        startHour,
                        0,
                        endHour,
                        0,
                        listOf(2, 3, 4, 5, 6), // Weekdays
                        listOf("com.instagram.android", "com.zhiliaoapp.musically", "com.google.android.youtube"),
                        listOf("Instagram", "TikTok", "YouTube")
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = StitchPrimary)
            ) {
                Text("Schedule Block", color = StitchDarkBg, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = StitchTextPrimary)
            }
        }
    )
}
