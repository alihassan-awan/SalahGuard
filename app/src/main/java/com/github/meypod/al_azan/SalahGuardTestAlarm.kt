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
import android.media.MediaPlayer
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

/** Temporary developer test path: intentionally separate from actual prayer records. */
object SalahGuardTestAlarm {
    private const val CHANNEL_ID = "salahguard_test_alarm_v1"
    private const val ACTION = "com.github.meypod.al_azan.TEST_ALARM"

    fun schedule(context: Context): String {
        val manager = context.getSystemService(AlarmManager::class.java)
        val intent = Intent(context, SalahGuardTestAlarmReceiver::class.java).setAction(ACTION)
        val pending = PendingIntent.getBroadcast(
            context, 90501, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val trigger = SystemClock.elapsedRealtime() + 60_000L
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !manager.canScheduleExactAlarms()) {
            manager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, trigger, pending)
            return "Test scheduled in approximately 60 seconds (exact alarm permission not granted)"
        }
        manager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, trigger, pending)
        return "Test alarm scheduled for 60 seconds from now"
    }

    internal fun show(context: Context) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        Log.i("SalahGuardTestAlarm",
            "notification requested; canUseFullScreenIntent=" +
                (Build.VERSION.SDK_INT < 34 || notificationManager.canUseFullScreenIntent()) +
                ", notificationsEnabled=" + notificationManager.areNotificationsEnabled())
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "SalahGuard test alarms", NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Temporary 60-second alarm checks"
                setSound(
                    sound,
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build(),
                )
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return
        val launch = PendingIntent.getActivity(
            context, 90502, Intent(context, SalahGuardAccountabilityActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("SalahGuard — Prayer Alarm Test")
            .setContentText("Tap to open accountability screen. No real prayers will be recorded.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(launch)
            .setFullScreenIntent(launch, true)
            .setOngoing(true)
            .build()
        notificationManager.notify(90501, notification)
        Log.i("SalahGuardTestAlarm", "test alarm notification posted")
    }
}

class SalahGuardTestAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != "com.github.meypod.al_azan.TEST_ALARM") return
        SalahGuardTestAlarm.show(context)
        // Temporary smoke test: play the ALARM audio stream independently from
        // notification-channel sound configuration. Stop automatically after 8 seconds.
        val pending = goAsync()
        Thread {
            var player: MediaPlayer? = null
            try {
                val alarmUri = RingtoneManager.getActualDefaultRingtoneUri(
                    context, RingtoneManager.TYPE_ALARM
                ) ?: Settings.System.DEFAULT_ALARM_ALERT_URI
                player = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    setDataSource(context, alarmUri)
                    isLooping = true
                    prepare()
                    start()
                }
                Thread.sleep(8_000)
            } catch (e: Exception) {
                Log.e("SalahGuardTestAlarm", "Test audio failed", e)
            } finally {
                runCatching { player?.stop() }
                player?.release()
                pending.finish()
            }
        }.start()
    }
}
