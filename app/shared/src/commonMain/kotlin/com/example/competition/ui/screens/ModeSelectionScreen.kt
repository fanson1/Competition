package com.example.competition.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.competition.api.ApiClient
import com.example.competition.repository.AppMode
import com.example.competition.repository.ModeRouter
import com.example.competition.ui.theme.CorrectGreen
import com.example.competition.ui.theme.Gold
import com.example.competition.ui.theme.QuizTheme
import competition.app.shared.generated.resources.Res
import competition.app.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ModeSelectionScreen(
    onComplete: () -> Unit = {}
) {
    var showServerUrlDialog by remember { mutableStateOf(false) }

    QuizTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0D1B3E), Color(0xFF1A2980))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(Res.string.welcome_title),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.welcome_subtitle),
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = stringResource(Res.string.welcome_select_mode),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Online mode card
                var selectedMode by remember { mutableStateOf<AppMode?>(null) }
                ModeOptionCard(
                    title = stringResource(Res.string.mode_online),
                    description = stringResource(Res.string.welcome_online_desc),
                    isSelected = selectedMode == AppMode.ONLINE,
                    accentColor = CorrectGreen,
                    onClick = { selectedMode = AppMode.ONLINE }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Offline mode card
                ModeOptionCard(
                    title = stringResource(Res.string.mode_offline),
                    description = stringResource(Res.string.welcome_offline_desc),
                    isSelected = selectedMode == AppMode.OFFLINE,
                    accentColor = Color.White.copy(alpha = 0.6f),
                    onClick = { selectedMode = AppMode.OFFLINE }
                )
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        val mode = selectedMode ?: return@Button
                        if (mode == AppMode.ONLINE) {
                            showServerUrlDialog = true
                        } else {
                            ModeRouter.switch(AppMode.OFFLINE)
                            ModeRouter.markModeSelected()
                            onComplete()
                        }
                    },
                    enabled = selectedMode != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold)
                ) {
                    Text(
                        text = stringResource(Res.string.welcome_confirm),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D1B3E)
                    )
                }
            }
        }

        if (showServerUrlDialog) {
            var url by remember { mutableStateOf(ApiClient.getBaseUrl()) }
            AlertDialog(
                onDismissRequest = { showServerUrlDialog = false },
                containerColor = Color(0xFF1A2980),
                title = { Text(stringResource(Res.string.mode_server_url), color = Color.White) },
                text = {
                    Column {
                        Text(
                            text = stringResource(Res.string.mode_server_hint),
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = url,
                            onValueChange = { url = it },
                            label = { Text(stringResource(Res.string.mode_server_url)) },
                            placeholder = { Text("http://localhost:8080") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Gold,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = Gold,
                                unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (url.isNotBlank()) {
                                ApiClient.configure(url)
                            }
                            showServerUrlDialog = false
                            ModeRouter.switch(AppMode.ONLINE)
                            ModeRouter.markModeSelected()
                            onComplete()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold)
                    ) {
                        Text(stringResource(Res.string.profile_save), color = Color(0xFF0D1B3E))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showServerUrlDialog = false }) {
                        Text(stringResource(Res.string.profile_cancel), color = Color.White)
                    }
                }
            )
        }
    }
}

@Composable
private fun ModeOptionCard(
    title: String,
    description: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) accentColor.copy(alpha = 0.15f)
                else Color.White.copy(alpha = 0.06f),
        border = if (isSelected) BorderStroke(2.dp, accentColor) else null
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) accentColor else Color.White.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Text(
                        text = "✓",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) accentColor else Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}
