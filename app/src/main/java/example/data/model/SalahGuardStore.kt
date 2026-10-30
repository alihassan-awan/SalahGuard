package com.example.data.model

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

/** Read-through bridge to the existing SalahGuard manual timetable preferences. */
object SalahGuardStore {
    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences("salahguard_v03", Context.MODE_PRIVATE)
    val names = listOf("Fajr", "Zuhr", "Asr", "Maghrib", "Isha")
    private val defaults = listOf("05:00", "13:30", "16:30", "18:00", "19:30")
    fun get24(context: Context, prayer: String): String {
        val index = names.indexOf(prayer).coerceAtLeast(0)
        return prefs(context).getString("time_$prayer", defaults[index]) ?: defaults[index]
    }
    fun set24(context: Context, prayer: String, time: String) {
        prefs(context).edit().putString("time_$prayer", time).apply()
    }
    fun display(time: String, format24: Boolean = false): String {
        val parts = time.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val min = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return if (format24) "%02d:%02d".format(Locale.US, hour, min)
        else "%d:%02d %s".format(Locale.US, if (hour % 12 == 0) 12 else hour % 12, min,
            if (hour < 12) "AM" else "PM")
    }
    fun parse(value: String): String? {
        val trimmed = value.trim()
        val twelve = Regex("""^(\d{1,2}):(\d{2})\s*(AM|PM)$""", RegexOption.IGNORE_CASE).matchEntire(trimmed)
        if (twelve != null) {
            val h = twelve.groupValues[1].toInt()
            val m = twelve.groupValues[2].toInt()
            if (h !in 1..12 || m !in 0..59) return null
            val hour = (h % 12) + if (twelve.groupValues[3].equals("PM", true)) 12 else 0
            return "%02d:%02d".format(Locale.US, hour, m)
        }
        val twentyFour = Regex("""^(\d{1,2}):(\d{2})$""").matchEntire(trimmed) ?: return null
        val h = twentyFour.groupValues[1].toInt()
        val m = twentyFour.groupValues[2].toInt()
        return if (h in 0..23 && m in 0..59) "%02d:%02d".format(Locale.US, h, m) else null
    }
    fun city(context: Context): String = prefs(context).getString("gps_city", null) ?: "Location not detected"
    fun setCity(context: Context, name: String, lat: Double, lon: Double) {
        prefs(context).edit().putString("gps_city", name)
            .putString("gps_coords", "$lat,$lon").apply()
    }
    fun gpsEnabled(context: Context) = prefs(context).getBoolean("live_location", false)
    fun setGpsEnabled(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean("live_location", enabled).apply()
    fun setting(context: Context, key: String, fallback: String): String =
        prefs(context).getString(key, fallback) ?: fallback
    fun setSetting(context: Context, key: String, value: String) =
        prefs(context).edit().putString(key, value).apply()
    fun is24(context: Context) = prefs(context).getBoolean("time_format_24", false)
    fun set24Format(context: Context, value: Boolean) =
        prefs(context).edit().putBoolean("time_format_24", value).apply()
    fun recordSnooze(context: Context, prayer: String) {
        val key = "snooze_${today()}_$prayer"
        val p = prefs(context)
        p.edit().putInt(key, p.getInt(key, 0) + 1).apply()
    }
    fun monthlyReport(context: Context): WeeklyReportData {
        val p = prefs(context)
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val days = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val completed = mutableMapOf<String, Int>()
        val missed = mutableMapOf<String, Int>()
        var snoozes = 0
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        for (day in 1..days) {
            val date = formatter.format(cal.time)
            names.forEach { name ->
                when (p.getString("status_${date}_$name", "Unconfirmed")) {
                    "Completed" -> completed[name] = (completed[name] ?: 0) + 1
                    "NotPrayed" -> missed[name] = (missed[name] ?: 0) + 1
                }
                snoozes += p.getInt("snooze_${date}_$name", 0)
            }
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        val prayed = completed.values.sum()
        val notPrayed = missed.values.sum()
        val total = days * names.size
        val most = missed.maxByOrNull { it.value }
        return WeeklyReportData(
            dateRange = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(Date()),
            completionPercentage = if (total == 0) 0 else prayed * 100 / total,
            completedCount = prayed, totalCount = total,
            prayedCount = prayed, snoozedCount = snoozes,
            missedCount = notPrayed, pendingCount = (total - prayed - notPrayed).coerceAtLeast(0),
            mostMissedPrayer = most?.key ?: "None recorded",
            mostMissedCount = most?.value ?: 0
        )
    }
    fun weeklyReport(context: Context): WeeklyReportData {
        val p = prefs(context)
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        // Monday-Sunday calendar week; future days are not counted as completed or missed.
        val daysSinceMonday = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
        calendar.add(Calendar.DAY_OF_YEAR, -daysSinceMonday)
        val start = calendar.time
        val end = Calendar.getInstance().apply {
            time = start
            add(Calendar.DAY_OF_YEAR, 6)
        }.time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        var prayed = 0
        var notPrayed = 0
        var snoozes = 0
        val perPrayer = names.associateWith { 0 }.toMutableMap()
        val todayDate = today()
        for (offset in 0..6) {
            val date = dateFormat.format(calendar.time)
            if (date <= todayDate) {
                for (name in names) {
                    when (p.getString("status_${date}_$name", "Unconfirmed")) {
                        "Completed" -> prayed++
                        "NotPrayed" -> {
                            notPrayed++
                            perPrayer[name] = (perPrayer[name] ?: 0) + 1
                        }
                    }
                    snoozes += p.getInt("snooze_${date}_$name", 0)
                }
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        val elapsed = daysSinceMonday + 1
        val total = elapsed * names.size
        val pending = (total - prayed - notPrayed).coerceAtLeast(0)
        val most = perPrayer.maxByOrNull { it.value }
        val range = SimpleDateFormat("d MMM", Locale.ENGLISH).format(start) +
            " – " + SimpleDateFormat("d MMM yyyy", Locale.ENGLISH).format(end)
        return WeeklyReportData(dateRange = range,
            completionPercentage = if (total == 0) 0 else prayed * 100 / total,
            completedCount = prayed, totalCount = total,
            prayedCount = prayed, snoozedCount = snoozes,
            missedCount = notPrayed, pendingCount = pending,
            mostMissedPrayer = if (most == null || most.value == 0) "None recorded" else most.key,
            mostMissedCount = most?.value ?: 0)
    }
    fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    fun verified(context: Context, prayer: String) =
        prefs(context).getString("status_${today()}_$prayer", "Unconfirmed") == "Completed"
}
