package com.example.competition

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.example.competition.data.UserManager

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Prevent the system from adding a translucent scrim to the navigation
        // bar so the app's edge-to-edge background shows through it.
        window.isNavigationBarContrastEnforced = false

        PlatformHolder.init(AndroidPlatform(this))
        UserManager.init()

        setContent {
            App()
        }
    }
}
