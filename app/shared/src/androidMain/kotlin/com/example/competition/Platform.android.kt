package com.example.competition

import android.content.Context
import android.os.Build

class AndroidPlatform(val context: Context) : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override fun getUserDataPath(): String {
        return context.filesDir.absolutePath
    }
}
