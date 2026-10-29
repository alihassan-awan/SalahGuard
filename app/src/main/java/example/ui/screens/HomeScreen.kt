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

    // Countdown timer state
    var countdownSeconds by remember { mutableIntStateOf(4585) } // 01:16:25
    LaunchedEffect(Unit) {
        while (countdownSeconds > 0) {
            kotlinx.coroutines.delay(1000)
            countdownSeconds--
        }
    }

    val hours = countdownSeconds / 3600
    val minutes = (countdownSeconds % 3600) / 60
    val seconds = countdownSeconds % 60
    val countdownFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    val prayers = remember {
        listOf(
            PrayerScheduleItem(PrayerType.FAJR, time = "5:20 AM", status = PrayerStatus.PRAYED),
            PrayerScheduleItem(PrayerType.ZUHR, time = "1:30 PM", status = PrayerStatus.PENDING, isNext = true),
            PrayerScheduleItem(PrayerType.ASR, time = "4:15 PM", status = PrayerStatus.NOT_PRAYED),
            PrayerScheduleItem(PrayerType.MAGHRIB, time = "5:52 PM", status = PrayerStatus.NOT_PRAYED),
            PrayerScheduleItem(PrayerType.ISHA, time = "7:10 PM", status = PrayerStatus.NOT_PRAYED)
        )
    }

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
            // Gregorian & Islamic Date Header
            Text(
                text = "Thu, 29 Oct 2026",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SalahTextWhite,
                    fontSize = 17.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "6 Jumada al-Awwal 1448",
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
                                    text = "“The most beloved deed to Allah is that which is done regularly, even if it is small.”",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = SalahTextWhite,
                                        fontStyle = FontStyle.Italic,
                                        fontSize = 13.5.sp,
                                        lineHeight = 19.sp
                                    )
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "— Sahih al-Bukhari 6464",
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
                                    text = "Zuhr",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 26.sp,
                                        color = SalahTextWhite
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = countdownFormatted,
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 30.sp,
                                        letterSpacing = 1.sp,
                                        color = SalahTextWhite
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "1:30 PM",
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
