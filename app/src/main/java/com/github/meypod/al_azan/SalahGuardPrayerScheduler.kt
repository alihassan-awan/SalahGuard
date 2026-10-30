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
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.data.model.SalahGuardStore
import java.util.Calendar

/** Schedules the five user-entered daily times. GPS-based calculation is NOT active yet. */
object SalahGuardPrayerScheduler {
    const val ACTION = "com.alihassanawan.salahguard.PRAYER_DUE"
    private const val CHANNEL = "salahguard_prayer_v1"
    fun reschedule(context: Context) {
        val alarm = context.getSystemService(AlarmManager::class.java)
        SalahGuardStore.names.forEachIndexed { index, name ->
            val intent = Intent(context, SalahGuardPrayerReceiver::class.java).setAction(ACTION)
                .putExtra("prayer", name)
            val pending = PendingIntent.getBroadcast(context, 94000 + index, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            alarm.cancel(pending)
            val values = SalahGuardStore.get24(context, name).split(":")
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, values[0].toInt())
                set(Calendar.MINUTE, values[1].toInt())
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
            }
            schedule(alarm, target.timeInMillis, pending)
        }
    }
    private fun schedule(alarm: AlarmManager, time: Long, pending: PendingIntent) {
        if (Build.VERSION.SDK_INT >= 31 && !alarm.canScheduleExactAlarms())
            alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pending)
        else alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pending)
    }
    fun snooze(context: Context, name: String) {
        val alarm = context.getSystemService(AlarmManager::class.java)
        val index = SalahGuardStore.names.indexOf(name)
        if (index < 0) return
        val intent = Intent(context, SalahGuardPrayerReceiver::class.java).setAction(ACTION)
            .putExtra("prayer", name).putExtra("snoozed", true)
        val pending = PendingIntent.getBroadcast(context, 95000 + index, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        schedule(alarm, System.currentTimeMillis() + 10 * 60_000L, pending)
    }
    internal fun notifyPrayer(context: Context, name: String) {
        val notifications = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) {
            val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            notifications.createNotificationChannel(NotificationChannel(CHANNEL, "Daily prayer alarms",
                NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(sound, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build())
                enableVibration(true)
            })
        }
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED) return
        val launch = PendingIntent.getActivity(context, 96000 + SalahGuardStore.names.indexOf(name),
            Intent(context, SalahGuardRealPrayerActivity::class.java)
                .putExtra("prayer", name).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        notifications.notify(97000 + SalahGuardStore.names.indexOf(name),
            NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("$name prayer time")
                .setContentText("It is time to pray. Tap to open SalahGuard.")
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setFullScreenIntent(launch, true)
                .setContentIntent(launch).setAutoCancel(true).build())
    }
}
class SalahGuardPrayerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != SalahGuardPrayerScheduler.ACTION) return
        val prayer = intent.getStringExtra("prayer") ?: return
        if (prayer !in SalahGuardStore.names) return
        // A snoozed occurrence must not reset the five daily schedules.
        if (!intent.getBooleanExtra("snoozed", false))
            SalahGuardPrayerScheduler.reschedule(context)
        SalahGuardPrayerScheduler.notifyPrayer(context, prayer)
    }
}
class SalahGuardPrayerRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action in listOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED,
                Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED))
            SalahGuardPrayerScheduler.reschedule(context)
    }
}
