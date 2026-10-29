package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.AccessTime
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
import com.example.data.model.PrayerType
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun EditPrayerTimesScreen(
    onNavigateBack: () -> Unit,
    onSaveTimes: () -> Unit,
    onQuickSwitchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fajrTime by remember { mutableStateOf("5:20 AM") }
    var zuhrTime by remember { mutableStateOf("1:30 PM") }
    var asrTime by remember { mutableStateOf("4:15 PM") }
    var maghribTime by remember { mutableStateOf("5:52 PM") }
    var ishaTime by remember { mutableStateOf("7:10 PM") }

    // Dialog state for editing a time
    var activeEditingPrayer by remember { mutableStateOf<String?>(null) }
    var activeEditingTime by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = SalahDarkBg,
        topBar = {
            SalahGuardTopBar(
                title = "Set Prayer Times",
                onBackClick = onNavigateBack,
                onQuickSwitchClick = onQuickSwitchClick
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SalahDarkBg)
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                SalahPrimaryButton(
                    text = "Save Times",
                    onClick = onSaveTimes,
                    testTag = "save_times_button"
                )
            }
        },
        modifier = modifier.testTag("edit_prayer_times_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 5 Editable Rows (matches mockup)
            EditablePrayerRow(
                prayerType = PrayerType.FAJR,
                time = fajrTime,
                onTimeClick = {
                    activeEditingPrayer = "Fajr"
                    activeEditingTime = fajrTime
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            EditablePrayerRow(
                prayerType = PrayerType.ZUHR,
                time = zuhrTime,
                onTimeClick = {
                    activeEditingPrayer = "Zuhr"
                    activeEditingTime = zuhrTime
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            EditablePrayerRow(
                prayerType = PrayerType.ASR,
                time = asrTime,
                onTimeClick = {
                    activeEditingPrayer = "Asr"
                    activeEditingTime = asrTime
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            EditablePrayerRow(
                prayerType = PrayerType.MAGHRIB,
                time = maghribTime,
                onTimeClick = {
                    activeEditingPrayer = "Maghrib"
                    activeEditingTime = maghribTime
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            EditablePrayerRow(
                prayerType = PrayerType.ISHA,
                time = ishaTime,
                onTimeClick = {
                    activeEditingPrayer = "Isha"
                    activeEditingTime = ishaTime
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Info note card at bottom (matches mockup)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF263F46), RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                color = SalahCardBg
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = SalahGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "These times will be used for daily reminders and alarms.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = SalahTextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Time Edit Dialog
        if (activeEditingPrayer != null) {
            AlertDialog(
                onDismissRequest = { activeEditingPrayer = null },
                containerColor = SalahDarkSurface,
                title = {
                    Text(
                        text = "Edit ${activeEditingPrayer} Time",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = SalahTextWhite,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                text = {
                    OutlinedTextField(
                        value = activeEditingTime,
                        onValueChange = { activeEditingTime = it },
                        label = { Text("Time (e.g. 5:20 AM)", color = SalahTextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SalahTextWhite,
                            unfocusedTextColor = SalahTextWhite,
                            focusedBorderColor = SalahEmerald,
                            unfocusedBorderColor = SalahCardBorder
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            when (activeEditingPrayer) {
                                "Fajr" -> fajrTime = activeEditingTime
                                "Zuhr" -> zuhrTime = activeEditingTime
                                "Asr" -> asrTime = activeEditingTime
                                "Maghrib" -> maghribTime = activeEditingTime
                                "Isha" -> ishaTime = activeEditingTime
                            }
                            activeEditingPrayer = null
                        }
                    ) {
                        Text("Apply", color = SalahEmerald)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activeEditingPrayer = null }) {
                        Text("Cancel", color = SalahTextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun EditablePrayerRow(
    prayerType: PrayerType,
    time: String,
    onTimeClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, SalahCardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onTimeClick),
        shape = RoundedCornerShape(14.dp),
        color = SalahCardBg
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PrayerIcon(
                type = prayerType,
                tint = if (prayerType == PrayerType.ISHA) Color(0xFF90CDF4) else SalahGold,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = prayerType.displayName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = SalahTextWhite,
                    fontSize = 16.sp
                ),
                modifier = Modifier.weight(1f)
            )

            // Editable Time Chip
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0C1D22),
                border = androidx.compose.foundation.BorderStroke(1.dp, SalahCardBorder)
            ) {
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = SalahTextWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = Icons.Outlined.AccessTime,
                contentDescription = "Edit Time",
                tint = SalahTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
