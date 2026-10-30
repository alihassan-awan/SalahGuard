package com.example.data.model

import android.content.Context
import io.github.meypod.adhan_kotlin.CalculationMethod
import io.github.meypod.adhan_kotlin.Coordinates
import io.github.meypod.adhan_kotlin.data.DateComponents
import io.github.meypod.adhan_kotlin.Madhab
import io.github.meypod.adhan_kotlin.PrayerTimes
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Offline calculation. Never substitutes guessed times if GPS coordinates are absent. */
object SalahGuardPrayerCalculator {
    /**
     * Only a first-run suggestion, never an override of an explicitly saved school.
     * Local mosque/school choice remains authoritative for the user.
     */
    fun suggestedAsrMethod(city: String): String =
        if (city.contains("Pakistan", ignoreCase = true))
            "Hanafi"
        else "Standard (Shafi'i, Maliki, Hanbali)"

    /** Pure calculation entry point, independent of Android and testable on JVM. */
    fun calculateForCoordinates(
        coordinates: String?,
        methodName: String,
        asrMethod: String,
        date: LocalDate,
        zone: ZoneId
    ): Map<String, String>? {
        val parts = coordinates?.split(",") ?: return null
        val lat = parts.getOrNull(0)?.trim()?.toDoubleOrNull() ?: return null
        val lon = parts.getOrNull(1)?.trim()?.toDoubleOrNull() ?: return null
        if (!lat.isFinite() || !lon.isFinite() ||
            lat !in -90.0..90.0 || lon !in -180.0..180.0) return null
        val method = when (methodName) {
            "Islamic Society of North America (ISNA)" -> CalculationMethod.NORTH_AMERICA
            "Egyptian General Authority of Survey" -> CalculationMethod.EGYPTIAN
            "Umm al-Qura University, Makkah" -> CalculationMethod.UMM_AL_QURA
            "University of Islamic Sciences, Karachi" -> CalculationMethod.KARACHI
            else -> CalculationMethod.MUSLIM_WORLD_LEAGUE
        }
        val madhab = if (asrMethod == "Hanafi") Madhab.HANAFI else Madhab.SHAFI
        return try {
            val times = PrayerTimes(Coordinates(lat, lon),
                DateComponents(date.year, date.monthValue, date.dayOfMonth),
                method.parameters.copy(madhab = madhab))
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            fun time(instant: kotlin.time.Instant): String =
                Instant.ofEpochMilli(instant.toEpochMilliseconds()).atZone(zone).format(formatter)
            mapOf(
                "Fajr" to time(times.fajr),
                "Zuhr" to time(times.dhuhr),
                "Asr" to time(times.asr),
                "Maghrib" to time(times.maghrib),
                "Isha" to time(times.isha)
            )
        } catch (_: Exception) { null }
    }

    fun calculate(context: Context, date: LocalDate = LocalDate.now()): Map<String, String>? {
        val p = context.getSharedPreferences("salahguard_v03", Context.MODE_PRIVATE)
        return calculateForCoordinates(
            p.getString("gps_coords", null),
            p.getString("calculation_method", "") ?: "",
            p.getString("asr_method", "") ?: "",
            date, ZoneId.systemDefault()
        )
    }

    /**
     * Returns the next prayer occurrence as epoch milliseconds. Automatic mode uses
     * tomorrow's astronomical times after today's Isha; manual mode repeats fixed times.
     */
    fun nextPrayerOccurrence(
        now: java.time.ZonedDateTime,
        todayTimes: Map<String, String>,
        tomorrowTimes: Map<String, String>
    ): Pair<String, Long>? {
        fun at(day: java.time.LocalDate, name: String, time: String): Pair<String, Long>? {
            val parsed = try { java.time.LocalTime.parse(time) } catch (_: Exception) { return null }
            return name to day.atTime(parsed).atZone(now.zone).toInstant().toEpochMilli()
        }
        val today = SalahGuardStore.names.mapNotNull { name ->
            todayTimes[name]?.let { at(now.toLocalDate(), name, it) }
        }.filter { it.second > now.toInstant().toEpochMilli() }
        if (today.isNotEmpty()) return today.minByOrNull { it.second }
        return SalahGuardStore.names.mapNotNull { name ->
            tomorrowTimes[name]?.let { at(now.toLocalDate().plusDays(1), name, it) }
        }.minByOrNull { it.second }
    }

    fun canAutoApply(mode: String?, scheduleEnabled: Boolean): Boolean =
        mode != "manual" || !scheduleEnabled

    fun apply(context: Context): Boolean {
        val p = context.getSharedPreferences("salahguard_v03", Context.MODE_PRIVATE)
        if (!canAutoApply(p.getString("timetable_mode", "manual"),
                p.getBoolean("manual_schedule_enabled", false))) return false
        val times = calculate(context) ?: return false
        val editor = p.edit()
        times.forEach { (name, time) -> editor.putString("time_$name", time) }
        editor.putString("timetable_mode", "calculated")
            .putString("calculated_date", LocalDate.now().toString())
            .putBoolean("manual_schedule_enabled", true)
        return editor.commit()
    }
}
