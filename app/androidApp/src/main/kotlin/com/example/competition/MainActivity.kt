package com.example.competition

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.competition.data.UserManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        PlatformHolder.init(AndroidPlatform(this))
        UserManager.init()

        setContent {
            App()
        }
    }
}
