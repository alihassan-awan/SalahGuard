package com.example.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Splash.route

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showScreenSwitcher by remember { mutableStateOf(false) }

    val openSwitcher = { showScreenSwitcher = true }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route
        ) {
            // 1. Splash Screen
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // 2. Home Dashboard
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToTimes = { navController.navigate(Screen.TimesSetup.route) },
                    onNavigateToHistory = { navController.navigate(Screen.PrayerHistory.route) },
                    onNavigateToAlarm = { navController.navigate(Screen.AlarmScreen.route) },
                    onNavigateToConfirm = { navController.navigate(Screen.ConfirmPrayer.route) },
                    onQuickSwitchClick = openSwitcher
                )
            }

            // 3. Prayer Times Setup
            composable(Screen.TimesSetup.route) {
                PrayerTimesSetupScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onDetectLocationClick = { navController.navigate(Screen.LocationDetection.route) },
                    onEditTimesClick = { navController.navigate(Screen.EditTimes.route) },
                    onSaved = { navController.navigate(Screen.Home.route) },
                    onQuickSwitchClick = openSwitcher
                )
            }

            // 4. Location Detection
            composable(Screen.LocationDetection.route) {
                LocationDetectionScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onContinue = { navController.navigate(Screen.TimesSetup.route) },
                    onQuickSwitchClick = openSwitcher
                )
            }

            // 5. Edit Prayer Times
            composable(Screen.EditTimes.route) {
                EditPrayerTimesScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSaveTimes = { navController.navigate(Screen.TimesSetup.route) },
                    onQuickSwitchClick = openSwitcher
                )
            }

            // 6. Alarm Screen (Full Screen)
            composable(Screen.AlarmScreen.route) {
                AlarmScreen(
                    onCloseClick = { navController.popBackStack() },
                    onPrayedClick = { navController.navigate(Screen.ConfirmPrayer.route) },
                    onSnoozeClick = { navController.navigate(Screen.Home.route) },
                    onNotPrayedClick = { navController.navigate(Screen.NotPrayedReason.route) },
                    onViewPrayerTimeClick = { navController.navigate(Screen.TimesSetup.route) }
                )
            }

            // 7. Prayed Verification
            composable(Screen.ConfirmPrayer.route) {
                PrayedVerificationScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onConfirmPrayed = { navController.navigate(Screen.PrayerHistory.route) },
                    onQuickSwitchClick = openSwitcher
                )
            }

            // 8. Not Prayed Reason
            composable(Screen.NotPrayedReason.route) {
                NotPrayedReasonScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSubmitMissedReason = { navController.navigate(Screen.PrayerHistory.route) },
                    onQuickSwitchClick = openSwitcher
                )
            }

            // 9. Prayer History (Daily)
            composable(Screen.PrayerHistory.route) {
                PrayerHistoryScreen(
                    onNavigateToHome = { navController.navigate(Screen.Home.route) },
                    onNavigateToTimes = { navController.navigate(Screen.TimesSetup.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToReports = { navController.navigate(Screen.WeeklyReport.route) },
                    onQuickSwitchClick = openSwitcher
                )
            }

            // 10. Reports (Weekly/Monthly)
            composable(Screen.WeeklyReport.route) {
                WeeklyReportScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToHome = { navController.navigate(Screen.Home.route) },
                    onNavigateToTimes = { navController.navigate(Screen.TimesSetup.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onQuickSwitchClick = openSwitcher
                )
            }

            // 11. Settings
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToHome = { navController.navigate(Screen.Home.route) },
                    onNavigateToTimes = { navController.navigate(Screen.TimesSetup.route) },
                    onNavigateToHistory = { navController.navigate(Screen.PrayerHistory.route) },
                    onNavigateToTestAlarm = { navController.navigate(Screen.TestAlarm.route) },
                    onQuickSwitchClick = openSwitcher
                )
            }

            // 12. Test Alarm (Developer)
            composable(Screen.TestAlarm.route) {
                TestAlarmScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onTriggerAlarmNow = { navController.navigate(Screen.AlarmScreen.route) },
                    onQuickSwitchClick = openSwitcher
                )
            }
        }

        // Quick 12-Screen Navigator Bottom Sheet
        if (showScreenSwitcher) {
            ModalBottomSheet(
                onDismissRequest = { showScreenSwitcher = false },
                sheetState = sheetState,
                containerColor = SalahDarkSurface,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .size(width = 40.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(SalahCardBorder)
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "All 12 Screens",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SalahTextWhite
                                )
                            )
                            Text(
                                text = "Directly preview any reference mockup screen",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SalahTextSecondary
                                )
                            )
                        }
                        IconButton(
                            onClick = {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    showScreenSwitcher = false
                                }
                            }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = SalahTextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(ALL_12_SCREENS) { index, screen ->
                            val isSelected = currentRoute == screen.route
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                                            showScreenSwitcher = false
                                            navController.navigate(screen.route) {
                                                launchSingleTop = true
                                            }
                                        }
                                    },
                                color = if (isSelected) SalahCardHover else SalahCardBg,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, SalahGold) else null,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${index + 1}.",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) SalahGold else SalahTextSecondary
                                            ),
                                            modifier = Modifier.width(28.dp)
                                        )
                                        Text(
                                            text = screen.title,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) SalahTextWhite else SalahTextSecondary
                                            )
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Active",
                                            tint = SalahGold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
