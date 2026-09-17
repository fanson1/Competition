package com.example.competition.ui

import androidx.compose.runtime.Composable

/**
 * Handles the platform "back" gesture/button.
 *
 * On Android this intercepts the system back press while [enabled]. On other
 * platforms it is a no-op (desktop/iOS expose their own in-app back buttons).
 */
@Composable
expect fun AppBackHandler(enabled: Boolean = true, onBack: () -> Unit)
