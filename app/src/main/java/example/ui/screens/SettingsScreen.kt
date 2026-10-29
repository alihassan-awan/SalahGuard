package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToTimes: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToTestAlarm: () -> Unit,
    onQuickSwitchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var adhanSound by remember { mutableStateOf("Default Adhan") }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var fullScreenAlarmEnabled by remember { mutableStateOf(true) }
    var snoozeDuration by remember { mutableStateOf("10 minutes") }
    var liveLocationGps by remember { mutableStateOf(true) }
    var detectedLocation by remember { mutableStateOf("Multan, Pakistan") }
    var calculationMethod by remember { mutableStateOf("Muslim World League (MWL)") }

    var adhanMenuExpanded by remember { mutableStateOf(false) }
    var snoozeMenuExpanded by remember { mutableStateOf(false) }

    val adhanOptions = listOf("Default Adhan", "Makkah Adhan", "Madinah Adhan", "Al-Aqsa Adhan", "Gentle Chime")
    val snoozeOptions = listOf("5 minutes", "10 minutes", "15 minutes")

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = SalahDarkBg,
        topBar = {
            SalahGuardTopBar(
                title = "Settings",
                onBackClick = onNavigateBack,
                onQuickSwitchClick = onQuickSwitchClick
            )
        },
        bottomBar = {
            SalahGuardBottomBar(
                currentTab = "settings",
                onTabSelected = { tab ->
                    when (tab) {
                        "home" -> onNavigateToHome()
                        "times" -> onNavigateToTimes()
                        "history" -> onNavigateToHistory()
                    }
                }
            )
        },
        modifier = modifier.testTag("settings_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // SECTION 1: Notifications & Alarm
            Text(
                text = "Notifications & Alarm",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = SalahTextSecondary,
                    fontSize = 13.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SalahCardBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = SalahCardBg
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    // Adhan Sound
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { adhanMenuExpanded = true }
                                .padding(vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Adhan Sound",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = SalahTextWhite,
                                    fontSize = 15.sp
                                )
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = adhanSound,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = SalahTextSecondary,
                                        fontSize = 13.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = SalahTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = adhanMenuExpanded,
                            onDismissRequest = { adhanMenuExpanded = false },
                            modifier = Modifier.background(SalahDarkSurface)
                        ) {
                            adhanOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt, color = SalahTextWhite) },
                                    onClick = {
                                        adhanSound = opt
                                        adhanMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = SalahCardBorder.copy(alpha = 0.5f), thickness = 0.8.dp)

                    // Vibration Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Vibration",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = SalahTextWhite,
                                fontSize = 15.sp
                            )
                        )
                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = { vibrationEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SalahTextWhite,
                                checkedTrackColor = SalahEmerald,
                                uncheckedThumbColor = SalahTextSecondary,
                                uncheckedTrackColor = SalahCardBorder
                            ),
                            modifier = Modifier.testTag("vibration_switch")
                        )
                    }

                    HorizontalDivider(color = SalahCardBorder.copy(alpha = 0.5f), thickness = 0.8.dp)

                    // Full Screen Alarm Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Full Screen Alarm",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = SalahTextWhite,
                                fontSize = 15.sp
                            )
                        )
                        Switch(
                            checked = fullScreenAlarmEnabled,
                            onCheckedChange = { fullScreenAlarmEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SalahTextWhite,
                                checkedTrackColor = SalahEmerald,
                                uncheckedThumbColor = SalahTextSecondary,
                                uncheckedTrackColor = SalahCardBorder
                            ),
                            modifier = Modifier.testTag("fullscreen_alarm_switch")
                        )
                    }

                    HorizontalDivider(color = SalahCardBorder.copy(alpha = 0.5f), thickness = 0.8.dp)

                    // Snooze Duration
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { snoozeMenuExpanded = true }
                                .padding(vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Snooze Duration",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = SalahTextWhite,
                                    fontSize = 15.sp
                                )
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = snoozeDuration,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = SalahTextSecondary,
                                        fontSize = 13.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = SalahTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = snoozeMenuExpanded,
                            onDismissRequest = { snoozeMenuExpanded = false },
                            modifier = Modifier.background(SalahDarkSurface)
                        ) {
                            snoozeOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt, color = SalahTextWhite) },
                                    onClick = {
                                        snoozeDuration = opt
                                        snoozeMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // SECTION 2: Location & Calculation
            Text(
                text = "Location & Calculation",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = SalahTextSecondary,
                    fontSize = 13.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SalahCardBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = SalahCardBg
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    // Live Location (GPS)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Live Location (GPS)",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = SalahTextWhite,
                                    fontSize = 15.sp
                                )
                            )
                        }
                        Switch(
                            checked = liveLocationGps,
                            onCheckedChange = { liveLocationGps = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SalahTextWhite,
                                checkedTrackColor = SalahEmerald,
                                uncheckedThumbColor = SalahTextSecondary,
                                uncheckedTrackColor = SalahCardBorder
                            )
                        )
                    }

                    HorizontalDivider(color = SalahCardBorder.copy(alpha = 0.5f), thickness = 0.8.dp)

                    // Detected Location
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = SalahEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Detected Location",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = SalahTextWhite,
                                    fontSize = 15.sp
                                )
                            )
                        }
                        Text(
                            text = detectedLocation,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = SalahTextSecondary,
                                fontSize = 13.sp
                            )
                        )
                    }

                    HorizontalDivider(color = SalahCardBorder.copy(alpha = 0.5f), thickness = 0.8.dp)

                    // Calculation Method
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateToTimes)
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mosque,
                                contentDescription = null,
                                tint = SalahGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Calculation Method",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = SalahTextWhite,
                                        fontSize = 15.sp
                                    )
                                )
                                Text(
                                    text = calculationMethod,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = SalahTextSecondary,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Edit Method",
                            tint = SalahTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Developer Screen 12 Shortcut Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onNavigateToTestAlarm)
                    .border(1.dp, SalahGold.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                color = SalahCardBg
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Build,
                            contentDescription = null,
                            tint = SalahGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Test Alarm (Developer)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SalahTextWhite,
                                    fontSize = 15.sp
                                )
                            )
                            Text(
                                text = "Trigger 60s / Immediate Alarm Test",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SalahTextSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = SalahGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
