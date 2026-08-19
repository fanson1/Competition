package com.example.competition.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.competition.api.ApiClient
import com.example.competition.presentation.mode.ModeSelectionEffect
import com.example.competition.presentation.mode.ModeSelectionIntent
import com.example.competition.presentation.mode.ModeSelectionViewModel
import com.example.competition.repository.AppMode
import com.example.competition.ui.MviEffectCollector
import com.example.competition.ui.components.PressableCard
import com.example.competition.ui.components.QuizPrimaryButton
import com.example.competition.ui.components.ScreenBackground
import com.example.competition.ui.components.rememberPulseScale
import com.example.competition.ui.rememberViewModel
import com.example.competition.ui.theme.*
import competition.app.shared.generated.resources.Res
import competition.app.shared.generated.resources.app_logo
import competition.app.shared.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ModeSelectionScreen(
    onComplete: () -> Unit = {}
) {
    val viewModel = rememberViewModel { ModeSelectionViewModel() }
    val state by viewModel.state.collectAsState()
    var showServerUrlDialog by remember { mutableStateOf(false) }
    val pulseScale = rememberPulseScale()

    MviEffectCollector(viewModel) { effect ->
        when (effect) {
            ModeSelectionEffect.Complete -> onComplete()
        }
    }

    QuizTheme {
        ScreenBackground(contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(Res.drawable.app_logo),
                    contentDescription = stringResource(Res.string.home_logo_description),
                    modifier = Modifier
                        .size(96.dp)
                        .scale(pulseScale),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = stringResource(Res.string.welcome_title),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = QuizPalette.Gold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(Res.string.welcome_subtitle),
                    fontSize = 15.sp,
                    color = QuizPalette.TextSecondary
                )
                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = stringResource(Res.string.welcome_select_mode),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = QuizPalette.TextPrimary
                )
                Spacer(modifier = Modifier.height(18.dp))

                ModeOptionCard(
                    emoji = "🛜",
                    title = stringResource(Res.string.mode_online),
                    description = stringResource(Res.string.welcome_online_desc),
                    isSelected = state.selectedMode == AppMode.ONLINE,
                    accentColor = QuizPalette.Success,
                    onClick = { viewModel.dispatch(ModeSelectionIntent.SelectMode(AppMode.ONLINE)) }
                )
                Spacer(modifier = Modifier.height(14.dp))

                ModeOptionCard(
                    emoji = "✈️",
                    title = stringResource(Res.string.mode_offline),
                    description = stringResource(Res.string.welcome_offline_desc),
                    isSelected = state.selectedMode == AppMode.OFFLINE,
                    accentColor = QuizPalette.TextSecondary,
                    onClick = { viewModel.dispatch(ModeSelectionIntent.SelectMode(AppMode.OFFLINE)) }
                )
                Spacer(modifier = Modifier.height(30.dp))

                QuizPrimaryButton(
                    text = stringResource(Res.string.welcome_confirm),
                    onClick = {
                        val mode = state.selectedMode ?: return@QuizPrimaryButton
                        if (mode == AppMode.ONLINE) {
                            showServerUrlDialog = true
                        } else {
                            viewModel.dispatch(ModeSelectionIntent.Confirm)
                        }
                    },
                    enabled = state.selectedMode != null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (showServerUrlDialog) {
            var url by remember { mutableStateOf(ApiClient.getBaseUrl()) }
            AlertDialog(
                onDismissRequest = { showServerUrlDialog = false },
                containerColor = QuizPalette.NightMid,
                title = { Text(stringResource(Res.string.mode_server_url), color = Color.White) },
                text = {
                    Column {
                        Text(
                            text = stringResource(Res.string.mode_server_hint),
                            fontSize = 13.sp,
                            color = QuizPalette.TextSecondary
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
                                focusedBorderColor = QuizPalette.Gold,
                                unfocusedBorderColor = QuizPalette.GlassBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = QuizPalette.Gold,
                                unfocusedLabelColor = QuizPalette.TextSecondary
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (url.isNotBlank()) {
                                ApiClient.configure(url)
                            }
                            showServerUrlDialog = false
                            viewModel.dispatch(ModeSelectionIntent.Confirm)
                        }
                    ) {
                        Text(
                            text = stringResource(Res.string.profile_save),
                            color = QuizPalette.Gold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showServerUrlDialog = false }) {
                        Text(stringResource(Res.string.profile_cancel), color = QuizPalette.TextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun ModeOptionCard(
    emoji: String,
    title: String,
    description: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    PressableCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = QuizRadii.lg,
        color = if (isSelected) accentColor.copy(alpha = 0.12f) else QuizPalette.Glass,
        borderColor = if (isSelected) accentColor.copy(alpha = 0.7f) else QuizPalette.GlassBorder,
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                accentColor.copy(alpha = 0.35f),
                                accentColor.copy(alpha = 0.12f)
                            )
                        )
                    )
                    .border(1.dp, accentColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) accentColor else Color.White
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = QuizPalette.TextMuted
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) accentColor
                        else Color.White.copy(alpha = 0.08f)
                    )
                    .border(1.dp, QuizPalette.GlassBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Text(
                        text = "✓",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = QuizPalette.NightDeep
                    )
                }
            }
        }
    }
}
