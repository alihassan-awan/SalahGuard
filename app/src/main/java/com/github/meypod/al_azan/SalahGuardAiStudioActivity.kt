package com.github.meypod.al_azan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.navigation.AppNavigation
import com.example.ui.theme.SalahGuardTheme

/** Onboarding permission request. Location permission is requested on LocationDetectionScreen. */
class SalahGuardAiStudioActivity : ComponentActivity() {
    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Permissions can be changed in Android Settings; UI shows diagnostics. */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SalahGuardPrayerScheduler.reschedule(this)
        setContent { SalahGuardTheme { AppNavigation() } }
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
