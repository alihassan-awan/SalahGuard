package com.github.meypod.al_azan

import androidx.activity.ComponentActivity
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.data.model.SalahGuardStore
import com.example.ui.screens.AlarmScreen
import com.example.ui.screens.PrayedVerificationScreen
import com.example.ui.screens.NotPrayedReasonScreen
import com.example.ui.theme.SalahGuardTheme

/** Real prayer flow using the AI Studio UI; never used for developer test alarms. */
class SalahGuardRealPrayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 27) { setShowWhenLocked(true); setTurnScreenOn(true) }
        val prayer = intent.getStringExtra("prayer") ?: run { finish(); return }
        if (prayer !in SalahGuardStore.names) { finish(); return }
        val index = SalahGuardStore.names.indexOf(prayer)
        val date = intent.getStringExtra("prayer_date")
            ?.let { runCatching { java.time.LocalDate.parse(it) }.getOrNull()?.toString() }
            ?: SalahGuardStore.today()
        setContent {
            var page by remember { mutableStateOf("alarm") }
            SalahGuardTheme {
                when (page) {
                    "verify" -> PrayedVerificationScreen(
                        prayerName = prayer,
                        onNavigateBack = { page = "alarm" },
                        onQuickSwitchClick = {},
                        onConfirmPrayed = { detail ->
                            getSharedPreferences("salahguard_v03", MODE_PRIVATE).edit()
                                .putString("status_${date}_${prayer}", "Completed")
                                .putString("prayed_detail_${date}_${prayer}", detail)
                                .putLong("verified_at_${date}_${prayer}", System.currentTimeMillis())
                                .remove("reason_${date}_${prayer}")
                                .commit()
                            Toast.makeText(this, "نماز کی تصدیق محفوظ ہو گئی", Toast.LENGTH_LONG).show()
                            getSystemService(NotificationManager::class.java).cancel(97000 + index)
                            finish()
                        }
                    )
                    "reason" -> NotPrayedReasonScreen(
                        onNavigateBack = { page = "alarm" },
                        onQuickSwitchClick = {},
                        onSubmitMissedReason = { reason, explanation ->
                            getSharedPreferences("salahguard_v03", MODE_PRIVATE).edit()
                                .putString("status_${date}_${prayer}", "NotPrayed")
                                .putString("reason_${date}_${prayer}", "$reason: $explanation")
                                .remove("prayed_detail_${date}_${prayer}")
                                .remove("verified_at_${date}_${prayer}")
                                .commit()
                            Toast.makeText(this, "آپ کی وجہ محفوظ ہو گئی", Toast.LENGTH_LONG).show()
                            getSystemService(NotificationManager::class.java).cancel(97000 + index)
                            finish()
                        }
                    )
                    else -> AlarmScreen(
                        prayerName = prayer,
                        onCloseClick = { finish() },
                        onPrayedClick = { page = "verify" },
                        onNotPrayedClick = { page = "reason" },
                        onSnoozeClick = {
                            SalahGuardStore.recordSnooze(this, prayer, date)
                            SalahGuardPrayerScheduler.snooze(this, prayer, date)
                            finish()
                        },
                        onViewPrayerTimeClick = { finish() }
                    )
                }
            }
        }
    }
}
