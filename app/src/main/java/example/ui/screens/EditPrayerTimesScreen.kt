package com.example.ui.screens

import android.app.TimePickerDialog
import android.content.Context
import java.util.Locale
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
import com.example.data.model.SalahGuardStore
import com.github.meypod.al_azan.SalahGuardPrayerScheduler
import androidx.compose.ui.platform.LocalContext
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun EditPrayerTimesScreen(
    onNavigateBack: () -> Unit,
    onSaveTimes: () -> Unit,
    onQuickSwitchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var fajrTime by remember { mutableStateOf(SalahGuardStore.display(SalahGuardStore.get24(context, "Fajr"), SalahGuardStore.is24(context))) }
    var zuhrTime by remember { mutableStateOf(SalahGuardStore.display(SalahGuardStore.get24(context, "Zuhr"), SalahGuardStore.is24(context))) }
    var asrTime by remember { mutableStateOf(SalahGuardStore.display(SalahGuardStore.get24(context, "Asr"), SalahGuardStore.is24(context))) }
    var maghribTime by remember { mutableStateOf(SalahGuardStore.display(SalahGuardStore.get24(context, "Maghrib"), SalahGuardStore.is24(context))) }
    var ishaTime by remember { mutableStateOf(SalahGuardStore.display(SalahGuardStore.get24(context, "Isha"), SalahGuardStore.is24(context))) }

    var timeError by remember { mutableStateOf(false) }
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
                    onClick = {
                        val values = listOf(fajrTime, zuhrTime, asrTime, maghribTime, ishaTime)
                        val parsed = values.map { SalahGuardStore.parse(it) }
                        if (parsed.any { it == null }) timeError = true
                        else {
                            SalahGuardStore.names.forEachIndexed { i, prayer -> SalahGuardStore.set24(context, prayer, parsed[i]!!) }
                            context.getSharedPreferences("salahguard_v03", android.content.Context.MODE_PRIVATE)
                                .edit().putBoolean("manual_schedule_enabled", true)
                                .putString("timetable_mode", "manual").apply()
                            SalahGuardPrayerScheduler.reschedule(context)
                            onSaveTimes()
                        }
                    },
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
            if (timeError) Text("Invalid time. Use 5:20 AM or 17:20.", color = SalahGold)
            // 5 Editable Rows (matches mockup)
            EditablePrayerRow(
                prayerType = PrayerType.FAJR,
                time = fajrTime,
                onTimeClick = {
                    showPrayerTimePicker(context, fajrTime) { fajrTime = it }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            EditablePrayerRow(
                prayerType = PrayerType.ZUHR,
                time = zuhrTime,
                onTimeClick = {
                    showPrayerTimePicker(context, zuhrTime) { zuhrTime = it }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            EditablePrayerRow(
                prayerType = PrayerType.ASR,
                time = asrTime,
                onTimeClick = {
                    showPrayerTimePicker(context, asrTime) { asrTime = it }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            EditablePrayerRow(
                prayerType = PrayerType.MAGHRIB,
                time = maghribTime,
                onTimeClick = {
                    showPrayerTimePicker(context, maghribTime) { maghribTime = it }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            EditablePrayerRow(
                prayerType = PrayerType.ISHA,
                time = ishaTime,
                onTimeClick = {
                    showPrayerTimePicker(context, ishaTime) { ishaTime = it }
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


    }
}

/**
 * Native clock picker: hour/minute dial plus AM/PM in 12-hour mode.
 * Values remain unchanged until the user confirms OK.
 */
private fun showPrayerTimePicker(context: Context, currentTime: String, onSelected: (String) -> Unit) {
    val time = SalahGuardStore.parse(currentTime) ?: "12:00"
    val parts = time.split(":")
    val hour = parts[0].toInt()
    val minute = parts[1].toInt()
    val is24Hour = SalahGuardStore.is24(context)
    TimePickerDialog(context, { _, chosenHour, chosenMinute ->
        val twentyFour = String.format(Locale.US, "%02d:%02d", chosenHour, chosenMinute)
        onSelected(SalahGuardStore.display(twentyFour, is24Hour))
    }, hour, minute, is24Hour).show()
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
