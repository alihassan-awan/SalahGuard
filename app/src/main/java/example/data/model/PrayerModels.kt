package com.example.data.model

enum class PrayerType(
    val displayName: String,
    val defaultTime: String,
    val iconType: String
) {
    FAJR("Fajr", "5:20 AM", "sunrise"),
    ZUHR("Zuhr", "1:30 PM", "sun"),
    ASR("Asr", "4:15 PM", "cloud_sun"),
    MAGHRIB("Maghrib", "5:52 PM", "sunset"),
    ISHA("Isha", "7:10 PM", "moon")
}

enum class PrayerStatus {
    PRAYED,
    PENDING,
    NOT_PRAYED,
    SNOOZED
}

data class PrayerScheduleItem(
    val type: PrayerType,
    val name: String = type.displayName,
    val time: String = type.defaultTime,
    val status: PrayerStatus = PrayerStatus.NOT_PRAYED,
    val isNext: Boolean = false,
    val actualPrayedTime: String? = null
)

data class DayHistory(
    val dateDisplay: String = "Thu, 29 Oct 2026",
    val islamicDate: String = "6 Jumada al-Awwal 1448",
    val prayers: List<PrayerScheduleItem> = listOf(
        PrayerScheduleItem(PrayerType.FAJR, time = "5:20 AM", status = PrayerStatus.PRAYED, actualPrayedTime = "5:28 AM"),
        PrayerScheduleItem(PrayerType.ZUHR, time = "1:30 PM", status = PrayerStatus.PENDING, isNext = true),
        PrayerScheduleItem(PrayerType.ASR, time = "4:15 PM", status = PrayerStatus.NOT_PRAYED),
        PrayerScheduleItem(PrayerType.MAGHRIB, time = "5:52 PM", status = PrayerStatus.NOT_PRAYED),
        PrayerScheduleItem(PrayerType.ISHA, time = "7:10 PM", status = PrayerStatus.NOT_PRAYED)
    )
)

data class WeeklyReportData(
    val dateRange: String = "26 Oct – 1 Nov 2026",
    val completionPercentage: Int = 60,
    val completedCount: Int = 15,
    val totalCount: Int = 25,
    val prayedCount: Int = 15,
    val snoozedCount: Int = 3,
    val missedCount: Int = 5,
    val pendingCount: Int = 2,
    val mostMissedPrayer: String = "Asr",
    val mostMissedCount: Int = 3
)

data class SalahSettings(
    val liveLocationGps: Boolean = true,
    val detectedLocation: String = "Multan, Pakistan",
    val calculationMethod: String = "Muslim World League (MWL)",
    val asrMethod: String = "Standard (Shafi'i, Maliki, Hanbali)",
    val timeFormat24Hour: Boolean = false,
    val adhanSound: String = "Default Adhan",
    val vibration: Boolean = true,
    val fullScreenAlarm: Boolean = true,
    val snoozeDuration: String = "10 minutes"
)
