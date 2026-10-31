package com.github.meypod.al_azan

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.data.model.SalahGuardStore
import com.example.data.model.SalahGuardPrayerCalculator
import java.util.Calendar

/** Schedules manually entered or GPS-calculated prayer times; each next-day trigger uses that date. */
object SalahGuardPrayerScheduler {
    const val ACTION = "com.alihassanawan.salahguard.PRAYER_DUE"
    private const val CHANNEL_PREFIX = "salahguard_prayer_v2_"
    fun reschedule(context: Context) {
        refreshCalculatedDay(context)
        if (!context.getSharedPreferences("salahguard_v03", Context.MODE_PRIVATE)
                .getBoolean("manual_schedule_enabled", false)) return
        SalahGuardStore.names.forEach { name -> schedulePrayer(context, name) }
    }
    private fun refreshCalculatedDay(context: Context) {
        val prefs = context.getSharedPreferences("salahguard_v03", Context.MODE_PRIVATE)
        if (prefs.getString("timetable_mode", "manual") == "calculated" &&
            prefs.getString("calculated_date", "") != SalahGuardStore.today()) {
            SalahGuardPrayerCalculator.apply(context)
        }
    }
    fun schedulePrayer(context: Context, name: String) {
        if (name !in SalahGuardStore.names) return
        val index = SalahGuardStore.names.indexOf(name)
        val alarm = context.getSystemService(AlarmManager::class.java)
        run {
            val now = System.currentTimeMillis()
            val todayValues = SalahGuardStore.get24(context, name).split(":")
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, todayValues[0].toInt())
                set(Calendar.MINUTE, todayValues[1].toInt())
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (target.timeInMillis <= now) {
                target.add(Calendar.DAY_OF_YEAR, 1)
                // Calculated schedules are date-dependent; tomorrow must not reuse today's hour.
                val prefs = context.getSharedPreferences("salahguard_v03", Context.MODE_PRIVATE)
                if (prefs.getString("timetable_mode", "manual") == "calculated") {
                    val tomorrow = java.time.LocalDate.now().plusDays(1)
                    val tomorrowValues = SalahGuardPrayerCalculator.calculate(context, tomorrow)
                        ?.get(name)?.split(":")
                    if (tomorrowValues != null && tomorrowValues.size == 2) {
                        target.set(Calendar.HOUR_OF_DAY, tomorrowValues[0].toInt())
                        target.set(Calendar.MINUTE, tomorrowValues[1].toInt())
                    }
                }
            }
            val prayerDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(target.time)
            val intent = Intent(context, SalahGuardPrayerReceiver::class.java).setAction(ACTION)
                .putExtra("prayer", name)
                .putExtra("prayer_date", prayerDate)
            val pending = PendingIntent.getBroadcast(context, 94000 + index, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            alarm.cancel(pending)
            schedule(alarm, target.timeInMillis, pending)
            Log.i("SalahGuardPrayer", "scheduled $name on $prayerDate at ${target.timeInMillis}")
        }
    }
    private fun schedule(alarm: AlarmManager, time: Long, pending: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= 31 && !alarm.canScheduleExactAlarms())
                alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pending)
            else alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pending)
        } catch (_: SecurityException) {
            alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pending)
        }
    }
    fun snooze(context: Context, name: String, prayerDate: String = SalahGuardStore.today()) {
        val alarm = context.getSystemService(AlarmManager::class.java)
        val index = SalahGuardStore.names.indexOf(name)
        if (index < 0) return
        val intent = Intent(context, SalahGuardPrayerReceiver::class.java).setAction(ACTION)
            .putExtra("prayer", name).putExtra("snoozed", true)
            .putExtra("prayer_date", prayerDate)
        val pending = PendingIntent.getBroadcast(context, 95000 + index, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val minutes = SalahGuardStore.setting(context, "alarm_snooze", "10 minutes").substringBefore(" ").toLongOrNull() ?: 10L
        schedule(alarm, System.currentTimeMillis() + minutes * 60_000L, pending)
    }
    internal fun notifyPrayer(context: Context, name: String, prayerDate: String) {
        val notifications = context.getSystemService(NotificationManager::class.java)
        val silent = SalahGuardStore.setting(context, "alarm_sound", "System alarm") == "Silent"
        val vibrate = SalahGuardStore.setting(context, "alarm_vibrate", "true") == "true"
        val fullScreen = SalahGuardStore.setting(context, "alarm_fullscreen", "true") == "true"
        val channel = CHANNEL_PREFIX + (if (silent) "silent" else "sound") +
            (if (vibrate) "_vibrate" else "_no_vibrate")
        if (Build.VERSION.SDK_INT >= 26) {
            val sound = if (silent) null else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            notifications.createNotificationChannel(NotificationChannel(channel, "Daily prayer alarms",
                NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(sound, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build())
                enableVibration(vibrate)
                if (vibrate) vibrationPattern = longArrayOf(0L, 350L, 250L, 350L)
            })
        }
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED) return
        val launch = PendingIntent.getActivity(context, 96000 + SalahGuardStore.names.indexOf(name),
            Intent(context, SalahGuardRealPrayerActivity::class.java)
                .putExtra("prayer", name)
                .putExtra("prayer_date", prayerDate)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        Log.i("SalahGuardPrayer", "notification requested for $name on $prayerDate; " +
            "fsiAllowed=" + (Build.VERSION.SDK_INT < 34 || notifications.canUseFullScreenIntent()))
        notifications.notify(97000 + SalahGuardStore.names.indexOf(name),
            NotificationCompat.Builder(context, channel)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("$name prayer time")
                .setContentText("It is time to pray. Tap to open SalahGuard.")
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setFullScreenIntent(launch, fullScreen)
                .setContentIntent(launch).setAutoCancel(true).build())
    }
}
class SalahGuardPrayerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != SalahGuardPrayerScheduler.ACTION) return
        val prayer = intent.getStringExtra("prayer") ?: return
        if (prayer !in SalahGuardStore.names) return
        // Snooze is a one-off notification, never a reason to change daily alarms.
        if (!intent.getBooleanExtra("snoozed", false)) {
            val prefs = context.getSharedPreferences("salahguard_v03", Context.MODE_PRIVATE)
            val needsNewDay = prefs.getString("timetable_mode", "manual") == "calculated" &&
                prefs.getString("calculated_date", "") != SalahGuardStore.today()
            if (needsNewDay) {
                // At midnight rollover the stored timetable and all next alarms must agree.
                SalahGuardPrayerScheduler.reschedule(context)
            } else {
                // Avoid canceling unrelated prayer alarms when a single prayer fires.
                SalahGuardPrayerScheduler.schedulePrayer(context, prayer)
            }
        }
        val prayerDate = intent.getStringExtra("prayer_date")
            ?.takeIf { it.matches(Regex("""\\d{4}-\\d{2}-\\d{2}""")) }
            ?: SalahGuardStore.today()
        Log.i("SalahGuardPrayer", "receiver fired: $prayer on $prayerDate; snoozed=" +
            intent.getBooleanExtra("snoozed", false))
        SalahGuardPrayerScheduler.notifyPrayer(context, prayer, prayerDate)
    }
}
class SalahGuardPrayerRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action in listOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED,
                Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED))
            SalahGuardPrayerScheduler.reschedule(context)
    }
}
