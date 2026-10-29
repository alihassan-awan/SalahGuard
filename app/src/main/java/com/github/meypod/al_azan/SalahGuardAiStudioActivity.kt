package com.github.meypod.al_azan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.navigation.AppNavigation
import com.example.ui.theme.SalahGuardTheme

/** UI migration entry point. Existing Al-Azan services and SalahGuard alarm code remain available. */
class SalahGuardAiStudioActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SalahGuardTheme { AppNavigation() } }
    }
}
