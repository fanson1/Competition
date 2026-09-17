package com.example.competition.ui

import androidx.compose.runtime.Composable

@Composable
actual fun AppBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No system back gesture on desktop; in-app back buttons are used instead.
}
