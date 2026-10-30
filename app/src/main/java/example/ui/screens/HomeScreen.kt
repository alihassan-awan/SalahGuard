package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PrayerScheduleItem
import com.example.data.model.PrayerStatus
import com.example.data.model.PrayerType
import com.example.data.model.SalahGuardStore
import com.example.data.model.DailyHadith
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToTimes: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAlarm: () -> Unit,
    onNavigateToConfirm: () -> Unit,
    onQuickSwitchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var isHadeethVisible by remember { mutableStateOf(true) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart || value == SwipeToDismissBoxValue.StartToEnd) {
                isHadeethVisible = false
                true
            } else {
                false
            }
        }
    )

    val context = LocalContext.current
    var dailyHadith by remember { mutableStateOf(DailyHadith.current(context)) }
    LaunchedEffect(Unit) {
        while (true) {
            DailyHadith.refresh(context) { dailyHadith = it }
            kotlinx.coroutines.delay(30 * 60 * 1000L)
        }
    }
    var secondsTick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            secondsTick++
        }
    }
    val timeNow = Calendar.getInstance()
    val currentSeconds = timeNow.get(Calendar.HOUR_OF_DAY) * 3600 +
        timeNow.get(Calendar.MINUTE) * 60 + timeNow.get(Calendar.SECOND)
    val types = listOf(PrayerType.FAJR, PrayerType.ZUHR, PrayerType.ASR, PrayerType.MAGHRIB, PrayerType.ISHA)
    val values = SalahGuardStore.names.map { SalahGuardStore.get24(context, it) }
    val seconds = values.map { v ->
        val pair = v.split(":")
        pair[0].toInt() * 3600 + pair[1].toInt() * 60
    }
    val nextIndex = seconds.indices.minByOrNull { (seconds[it] - currentSeconds + 86400) % 86400 } ?: 0
    val countdownSeconds = (seconds[nextIndex] - currentSeconds + 86400) % 86400
    val countdownFormatted = "%02d:%02d:%02d".format(Locale.US,
        countdownSeconds / 3600, (countdownSeconds % 3600) / 60, countdownSeconds % 60)
    val prayers = types.mapIndexed { i, type ->
        PrayerScheduleItem(type,
            time = SalahGuardStore.display(values[i], SalahGuardStore.is24(context)),
            status = when (context.getSharedPreferences("salahguard_v03", android.content.Context.MODE_PRIVATE)
                .getString("status_${SalahGuardStore.today()}_${SalahGuardStore.names[i]}", "Unconfirmed")) {
                "Completed" -> PrayerStatus.PRAYED
                "NotPrayed" -> PrayerStatus.NOT_PRAYED
                else -> PrayerStatus.PENDING
            },
            isNext = i == nextIndex)
    }
    val isScheduleConfigured = context.getSharedPreferences("salahguard_v03", android.content.Context.MODE_PRIVATE)
        .getBoolean("manual_schedule_enabled", false)
    val nextPrayerName = if (isScheduleConfigured) SalahGuardStore.names[nextIndex] else "Set prayer times"
    val nextPrayerTime = if (isScheduleConfigured) SalahGuardStore.display(values[nextIndex], SalahGuardStore.is24(context)) else "--:--"

    Scaffold(
        containerColor = SalahDarkBg,
        topBar = {
            SalahGuardTopBar(
                title = "SalahGuard",
                onSettingsClick = onNavigateToSettings,
                onQuickSwitchClick = onQuickSwitchClick
            )
        },
        bottomBar = {
            SalahGuardBottomBar(
                currentTab = "home",
                onTabSelected = { tab ->
                    when (tab) {
                        "times" -> onNavigateToTimes()
                        "history" -> onNavigateToHistory()
                        "settings" -> onNavigateToSettings()
                    }
                }
            )
        },
        modifier = modifier.testTag("home_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            if (!isScheduleConfigured) {
                Surface(
                    color = SalahCardBg,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onNavigateToTimes)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Prayer times not configured", color = SalahGold, fontWeight = FontWeight.Bold)
                        Text("Set your actual city and prayer timetable before relying on alerts. Tap to set up.",
                            color = SalahTextWhite, fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            // Gregorian & Islamic Date Header
            Text(
                text = SimpleDateFormat("EEE, dd MMM yyyy", Locale.ENGLISH).format(timeNow.time),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SalahTextWhite,
                    fontSize = 17.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Manual timetable • " + SalahGuardStore.city(context),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = SalahTextSecondary,
                    fontSize = 13.sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // DAILY HADEETH CARD (Swipe-to-dismiss)
            AnimatedVisibility(
                visible = isHadeethVisible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        val color = when (dismissState.targetValue) {
                            SwipeToDismissBoxValue.EndToStart,
                            SwipeToDismissBoxValue.StartToEnd -> SalahRed.copy(alpha = 0.25f)
                            else -> Color.Transparent
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .background(color)
                                .padding(horizontal = 20.dp),
                            contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                                Alignment.CenterStart
                            } else {
                                Alignment.CenterEnd
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Dismiss",
                                tint = SalahRed,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("daily_hadeeth_card")
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFF26444D), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF0F262D)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF143039),
                                            Color(0xFF0C2026)
                                        )
                                    )
                                )
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(SalahGold.copy(alpha = 0.15f))
                                                .border(1.dp, SalahGold.copy(alpha = 0.35f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.FormatQuote,
                                                contentDescription = null,
                                                tint = SalahGold,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Daily Hadeeth",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = SalahGold,
                                                fontSize = 12.sp,
                                                letterSpacing = 0.5.sp
                                            )
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Swipe to dismiss",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = SalahTextSecondary.copy(alpha = 0.7f),
                                                fontSize = 10.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = { isHadeethVisible = false },
                                            modifier = Modifier.size(22.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Dismiss Hadeeth",
                                                tint = SalahTextSecondary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "“" + dailyHadith.first + "”",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = SalahTextWhite,
                                        fontStyle = FontStyle.Italic,
                                        fontSize = 13.5.sp,
                                        lineHeight = 19.sp
                                    )
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "— " + dailyHadith.second,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SalahEmeraldLight,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (!isHadeethVisible) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { isHadeethVisible = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = SalahGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Show Daily Hadeeth",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SalahGold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, Color(0xFF1F4852), RoundedCornerShape(20.dp))
                    .clickable(onClick = onNavigateToAlarm)
                    .testTag("next_prayer_card"),
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0F2E36),
                                    Color(0xFF0A1E24),
                                    Color(0xFF08161A)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        // "Next Prayer" Gold Label
                        Text(
                            text = "Next Prayer",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = SalahGoldLight,
                                fontSize = 13.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Glowing Sun Icon
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(SalahGold.copy(alpha = 0.15f))
                                    .border(1.dp, SalahGold.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WbSunny,
                                    contentDescription = "Zuhr Sun",
                                    tint = SalahGoldLight,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = nextPrayerName,
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 26.sp,
                                        color = SalahTextWhite
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isScheduleConfigured) countdownFormatted else "--:--:--",
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 30.sp,
                                        letterSpacing = 1.sp,
                                        color = SalahTextWhite
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = nextPrayerTime,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = SalahTextSecondary,
                                        fontSize = 14.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // PRAYER LIST CARDS (5 rows in dark container)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SalahCardBorder, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                color = SalahCardBg
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    prayers.forEachIndexed { index, prayer ->
                        HomePrayerRow(
                            prayer = prayer,
                            onClick = {
                                if (prayer.isNext) {
                                    onNavigateToAlarm()
                                } else {
                                    onNavigateToConfirm()
                                }
                            }
                        )
                        if (index < prayers.size - 1) {
                            HorizontalDivider(
                                color = SalahCardBorder.copy(alpha = 0.5f),
                                thickness = 0.8.dp,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HomePrayerRow(
    prayer: PrayerScheduleItem,
    onClick: () -> Unit
) {
    val isHighlighted = prayer.isNext

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isHighlighted) {
                    Modifier
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SalahGold.copy(alpha = 0.08f))
                        .border(1.dp, SalahGold.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Prayer Icon
        PrayerIcon(
            type = prayer.type,
            tint = if (prayer.type == PrayerType.ISHA) Color(0xFF90CDF4) else SalahGold,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        // Prayer Name
        Text(
            text = prayer.name,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
                color = SalahTextWhite,
                fontSize = 16.sp
            ),
            modifier = Modifier.weight(1f)
        )

        // Prayer Time
        Text(
            text = prayer.time,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (prayer.status == PrayerStatus.PRAYED) FontWeight.Bold else FontWeight.Normal,
                color = if (prayer.status == PrayerStatus.PRAYED) SalahEmeraldLight else SalahTextWhite,
                fontSize = 15.sp
            )
        )

        Spacer(modifier = Modifier.width(14.dp))

        // Right-side Status Indicator
        when (prayer.status) {
            PrayerStatus.PRAYED -> {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(SalahEmerald),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = SalahTextWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            PrayerStatus.PENDING -> {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, SalahGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = "Next",
                        tint = SalahGold,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, SalahTextMuted, CircleShape)
                )
            }
        }
    }
}
