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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
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
import com.example.data.model.DayHistory
import com.example.data.model.SalahGuardStore
import com.example.data.model.PrayerStatus
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar
import java.util.Locale
import com.example.data.model.PrayerScheduleItem
import com.example.data.model.PrayerType
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun PrayerHistoryScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToTimes: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToReports: () -> Unit,
    onQuickSwitchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("salahguard_v03", android.content.Context.MODE_PRIVATE)
    var dayOffset by remember { mutableIntStateOf(0) }
    var selectedDetail by remember { mutableStateOf<String?>(null) }
    val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, dayOffset) }
    val day = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
    val dayPrayers = PrayerType.entries.map { type ->
        val status = prefs.getString("status_${day}_${type.displayName}", "Unconfirmed")
        PrayerScheduleItem(type,
            time = SalahGuardStore.display(SalahGuardStore.get24(context, type.displayName),
                SalahGuardStore.is24(context)),
            status = when(status) {
                "Completed" -> PrayerStatus.PRAYED
                "NotPrayed" -> PrayerStatus.NOT_PRAYED
                else -> PrayerStatus.PENDING
            })
    }
    val dayHistory = DayHistory(
        dateDisplay = SimpleDateFormat("EEE, dd MMM yyyy", Locale.ENGLISH).format(calendar.time),
        prayers = dayPrayers
    )
    val prayedCount = dayPrayers.count { it.status == PrayerStatus.PRAYED }
    val notPrayedCount = dayPrayers.count { it.status == PrayerStatus.NOT_PRAYED }
    val pendingCount = dayPrayers.count { it.status == PrayerStatus.PENDING }
    val snoozedCount = PrayerType.entries.sumOf { prefs.getInt("snooze_${day}_${it.displayName}", 0) }
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = SalahDarkBg,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SalahDarkBg)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { dayOffset-- },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Day",
                            tint = SalahGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (dayOffset == 0) "Today" else "Prayer History",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = SalahTextWhite,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = dayHistory.dateDisplay,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SalahTextSecondary,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onNavigateToReports,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "View Weekly Report",
                                tint = SalahGold,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        IconButton(
                            onClick = { if (dayOffset < 0) dayOffset++ },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Day",
                                tint = SalahGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            SalahGuardBottomBar(
                currentTab = "history",
                onTabSelected = { tab ->
                    when (tab) {
                        "home" -> onNavigateToHome()
                        "times" -> onNavigateToTimes()
                        "settings" -> onNavigateToSettings()
                    }
                }
            )
        },
        modifier = modifier.testTag("prayer_history_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // View Reports Header Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onNavigateToReports),
                color = SalahCardBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, SalahCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Switch to Weekly Report",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = SalahGold
                        )
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = SalahGold,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Prayer List Rows (matching mockup)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SalahCardBorder, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                color = SalahCardBg
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    dayHistory.prayers.forEachIndexed { index, prayer ->
                        HistoryPrayerRow(prayer = prayer, onClick = { selectedDetail = prayer.name })
                        if (index < dayHistory.prayers.size - 1) {
                            HorizontalDivider(
                                color = SalahCardBorder.copy(alpha = 0.5f),
                                thickness = 0.8.dp,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom Summary Metrics (1 Prayed, 0 Snoozed, 0 Missed, 4 Pending)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SalahCardBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = SalahCardBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HistoryMetricCol(count = prayedCount.toString(), label = "Prayed", color = SalahEmerald)
                    HistoryMetricCol(count = snoozedCount.toString(), label = "Snoozed", color = SalahOrange)
                    HistoryMetricCol(count = notPrayedCount.toString(), label = "Not prayed", color = SalahRed)
                    HistoryMetricCol(count = pendingCount.toString(), label = "Pending", color = SalahTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            if (selectedDetail != null) {
                val prayer = selectedDetail!!
                val status = prefs.getString("status_${day}_$prayer", "Unconfirmed")
                val detail = prefs.getString("prayed_detail_${day}_$prayer", null)
                val reason = prefs.getString("reason_${day}_$prayer", null)
                val snoozes = prefs.getInt("snooze_${day}_$prayer", 0)
                val timestamp = prefs.getLong("verified_at_${day}_$prayer", 0L)
                AlertDialog(
                    onDismissRequest = { selectedDetail = null },
                    title = { Text("$prayer • $day") },
                    text = {
                        Column {
                            Text("Status: " + (status ?: "Unconfirmed"))
                            if (detail != null) Text("How you prayed: $detail")
                            if (timestamp > 0L) Text("Confirmed at: " +
                                SimpleDateFormat("h:mm a", Locale.US).format(Date(timestamp)))
                            if (reason != null) Text("Explanation: $reason")
                            Text("Snoozed: $snoozes time(s)")
                            if (status == "Unconfirmed") Text("No completion has been recorded.")
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { selectedDetail = null }) { Text("Close") }
                    }
                )
            }
        }
    }
}

@Composable
private fun HistoryPrayerRow(
    prayer: PrayerScheduleItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PrayerIcon(
            type = prayer.type,
            tint = if (prayer.type == PrayerType.ISHA) Color(0xFF90CDF4) else SalahGold,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = prayer.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = SalahTextWhite,
                    fontSize = 16.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = prayer.time,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = SalahTextSecondary,
                    fontSize = 13.sp
                )
            )
        }

        StatusBadge(
            status = prayer.status,
            actualTime = prayer.actualPrayedTime
        )
    }
}

@Composable
private fun HistoryMetricCol(
    count: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = color
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = SalahTextSecondary,
                fontSize = 11.sp
            )
        )
    }
}
