package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Timer
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
import com.github.meypod.al_azan.SalahGuardTestAlarm
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TestAlarmScreen(
    onNavigateBack: () -> Unit,
    onTriggerAlarmNow: () -> Unit,
    onQuickSwitchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    var isCountdownActive by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableIntStateOf(60) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(isCountdownActive) {
        if (isCountdownActive) {
            secondsLeft = 60
            while (secondsLeft > 0 && isCountdownActive) {
                delay(1000)
                secondsLeft--
            }
            if (isCountdownActive && secondsLeft == 0) {
                isCountdownActive = false
            }
        }
    }

    Scaffold(
        containerColor = SalahDarkBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SalahGuardTopBar(
                title = "Test Alarm",
                onBackClick = onNavigateBack,
                onQuickSwitchClick = onQuickSwitchClick
            )
        },
        modifier = modifier.testTag("test_alarm_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Blue Info Card (matches mockup)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E3A8A).copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0F243A)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2563EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = SalahTextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "This is only for testing. Your actual prayer records will NOT be changed.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF93C5FD),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Card 1: Trigger in 60 seconds
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, SalahCardBorder, RoundedCornerShape(16.dp))
                    .clickable {
                        if (!isCountdownActive) {
                            val result = SalahGuardTestAlarm.schedule(context)
                            Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                            isCountdownActive = true
                        }
                        if (isCountdownActive) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Alarm scheduled to trigger in 60s")
                            }
                        }
                    }
                    .testTag("trigger_60s_card"),
                shape = RoundedCornerShape(16.dp),
                color = SalahCardBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF16323B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Timer,
                            contentDescription = null,
                            tint = SalahTextWhite,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isCountdownActive) "Triggering in ${secondsLeft}s..." else "Trigger in 60 seconds",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SalahTextWhite,
                                fontSize = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Test full alarm with sound",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SalahTextSecondary,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Card 2: Trigger now
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, SalahCardBorder, RoundedCornerShape(16.dp))
                    .clickable(onClick = {
                        SalahGuardTestAlarm.show(context)
                        onTriggerAlarmNow()
                    })
                    .testTag("trigger_now_card"),
                shape = RoundedCornerShape(16.dp),
                color = SalahCardBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF16323B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = SalahTextWhite,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Trigger now",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SalahTextWhite,
                                fontSize = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Test immediately",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SalahTextSecondary,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Card 3: Stop Test Alarm (Red outline / Pill)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.2.dp, Color(0xFFEF4444).copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                    .clickable {
                        isCountdownActive = false
                        scope.launch {
                            snackbarHostState.showSnackbar("Test alarm stopped")
                        }
                    }
                    .testTag("stop_test_alarm_button"),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1E1719)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = SalahTextWhite,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Stop Test Alarm",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444),
                            fontSize = 15.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom note card (matches mockup)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1B3842), RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                color = SalahCardBg
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = SalahTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Use this to test notification, sound, vibration and full-screen alarm.",
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
