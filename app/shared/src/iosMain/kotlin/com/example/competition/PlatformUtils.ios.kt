package com.example.competition

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual object PlatformUtils {
    actual fun currentTimeMillis(): Long {
        return (NSDate().timeIntervalSince1970 * 1000).toLong()
    }
    actual fun defaultBaseUrl(): String = "http://localhost:8080"
}
