package com.simats.naturepulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.simats.naturepulse.ui.nav.NaturePulseNavHost
import com.simats.naturepulse.ui.theme.NaturePulseTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        // Allow our composables to draw behind the status bar and navigation bar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContent {
            NaturePulseTheme(darkTheme = false) {
                NaturePulseNavHost()
            }
        }
    }
}