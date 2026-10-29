package com.example.navigation

sealed class Screen(val route: String, val title: String) {
    object Splash : Screen("splash", "Splash Screen")
    object Home : Screen("home", "Home Dashboard")
    object TimesSetup : Screen("times_setup", "Prayer Times")
    object LocationDetection : Screen("location_detection", "Location Detection")
    object EditTimes : Screen("edit_times", "Edit Prayer Times")
    object AlarmScreen : Screen("alarm_screen", "Alarm Screen")
    object ConfirmPrayer : Screen("confirm_prayer", "Confirm Prayer")
    object NotPrayedReason : Screen("not_prayed_reason", "Not Prayed Reason")
    object PrayerHistory : Screen("prayer_history", "Prayer History")
    object WeeklyReport : Screen("weekly_report", "Weekly Report")
    object Settings : Screen("settings", "Settings")
    object TestAlarm : Screen("test_alarm", "Test Alarm")
}

val ALL_12_SCREENS = listOf(
    Screen.Splash,
    Screen.Home,
    Screen.TimesSetup,
    Screen.LocationDetection,
    Screen.EditTimes,
    Screen.AlarmScreen,
    Screen.ConfirmPrayer,
    Screen.NotPrayedReason,
    Screen.PrayerHistory,
    Screen.WeeklyReport,
    Screen.Settings,
    Screen.TestAlarm
)
