package com.example.competition.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.competition.api.ApiClient
import com.example.competition.model.AvatarEmoji
import com.example.competition.model.User
import com.example.competition.model.UserProfile
import com.example.competition.presentation.profile.ProfileEffect
import com.example.competition.presentation.profile.ProfileIntent
import com.example.competition.presentation.profile.ProfileSyncError
import com.example.competition.presentation.profile.ProfileViewModel
import com.example.competition.repository.AppMode
import com.example.competition.repository.ModeRouter
import com.example.competition.ui.MviEffectCollector
import com.example.competition.ui.components.AnimatedCount
import com.example.competition.ui.components.GlassCard
import com.example.competition.ui.components.QuizChip
import com.example.competition.ui.components.QuizPrimaryButton
import com.example.competition.ui.components.QuizSecondaryButton
import com.example.competition.ui.components.ScreenBackground
import com.example.competition.ui.components.ScreenHeader
import com.example.competition.ui.components.StatItem
import com.example.competition.ui.rememberViewModel
import com.example.competition.ui.theme.*
import org.jetbrains.compose.resources.stringResource
import competition.app.shared.generated.resources.Res
import competition.app.shared.generated.resources.*

@Composable
fun ProfileScreen(
    user: User,
    profile: UserProfile?,
    onLogout: () -> Unit,
    onBack: () -> Unit,
    onProfileUpdated: () -> Unit = {}
) {
    val viewModel = rememberViewModel { ProfileViewModel() }
    val state by viewModel.state.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    var showServerUrlDialog by remember { mutableStateOf(false) }
    var passwordResult by remember { mutableStateOf<Result<Unit>?>(null) }

    val currentMode by ModeRouter.currentMode.collectAsState()
    val modeNeedLoginText = stringResource(Res.string.mode_need_login)

    MviEffectCollector(viewModel) { effect ->
        when (effect) {
            ProfileEffect.ProfileUpdated -> onProfileUpdated()
            ProfileEffect.Logout -> onLogout()
            ProfileEffect.Back -> onBack()
            is ProfileEffect.ChangePasswordResult -> passwordResult = effect.result
        }
    }

    ScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            ScreenHeader(
                backLabel = stringResource(Res.string.profile_back),
                title = stringResource(Res.string.profile_title),
                onBack = { viewModel.dispatch(ProfileIntent.Back) }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Avatar and name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(116.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        QuizPalette.Gold.copy(alpha = 0.35f),
                                        QuizPalette.Gold.copy(alpha = 0f)
                                    )
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        QuizPalette.Gold.copy(alpha = 0.95f),
                                        QuizPalette.GoldDeep
                                    )
                                )
                            )
                            .border(3.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                            .clickable { showAvatarDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.avatarEmoji,
                            fontSize = 40.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = user.nickname,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "@${user.username}",
                    fontSize = 13.sp,
                    color = QuizPalette.TextMuted
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (profile != null) {
                    QuizChip(
                        text = stringResource(Res.string.home_score_display, profile.totalScore),
                        color = QuizPalette.Gold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Stats
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = QuizRadii.lg,
                contentPadding = PaddingValues(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(stringResource(Res.string.profile_total_score), "${profile?.totalScore ?: 0}", QuizPalette.Gold)
                    StatItem(stringResource(Res.string.profile_max_level), "${profile?.maxLevel ?: 0}", QuizPalette.Success)
                    StatItem(stringResource(Res.string.profile_games_played), "${profile?.totalGamesPlayed ?: 0}", Info)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(stringResource(Res.string.profile_correct_count), "${profile?.totalCorrectCount ?: 0}", TimerOrange)
                    StatItem(stringResource(Res.string.profile_streak), "${profile?.maxStreak ?: 0}", VioletAccent)
                    StatItem(stringResource(Res.string.profile_completed_levels), "${profile?.completedLevels?.size ?: 0}", QuizPalette.Success)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Completed levels
            if (profile?.completedLevels?.isNotEmpty() == true) {
                Text(
                    text = stringResource(Res.string.profile_completed_section),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    profile.completedLevels.sorted().forEach { level ->
                        QuizChip(
                            text = stringResource(Res.string.profile_level_badge, level),
                            color = QuizPalette.Success,
                            emoji = "✓"
                        )
                    }
                }
            }

            // Mode switch section
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        if (currentMode == AppMode.ONLINE) Res.string.mode_online
                        else Res.string.mode_offline
                    ),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (currentMode == AppMode.ONLINE) QuizPalette.Success else Color.White.copy(alpha = 0.7f)
                )
                TextButton(onClick = { showServerUrlDialog = true }) {
                    Text(
                        text = stringResource(Res.string.mode_server_url),
                        fontSize = 11.sp,
                        color = QuizPalette.TextMuted
                    )
                }
            }
            GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(Res.string.mode_current, if (currentMode == AppMode.ONLINE)
                                stringResource(Res.string.mode_online)
                            else
                                stringResource(Res.string.mode_offline)),
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (currentMode == AppMode.ONLINE)
                                stringResource(Res.string.mode_switch_to_offline)
                            else
                                stringResource(Res.string.mode_switch_to_online),
                            fontSize = 12.sp,
                            color = QuizPalette.TextMuted
                        )
                    }
                    Switch(
                        checked = currentMode == AppMode.ONLINE,
                        onCheckedChange = { online -> viewModel.dispatch(ProfileIntent.ToggleOnline(online)) },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = QuizPalette.Success,
                            checkedThumbColor = Color.White,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.2f),
                            uncheckedThumbColor = QuizPalette.TextSecondary
                        )
                    )
                }

                if (state.isSyncing) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = QuizPalette.Gold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(Res.string.mode_syncing),
                            fontSize = 12.sp,
                            color = QuizPalette.TextMuted
                        )
                    }
                }

                val syncErrorMessage = when (val error = state.syncError) {
                    ProfileSyncError.NeedLogin -> modeNeedLoginText
                    is ProfileSyncError.Message -> error.text
                    null -> null
                }
                if (syncErrorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = syncErrorMessage,
                        fontSize = 12.sp,
                        color = Danger
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuizSecondaryButton(
                    text = stringResource(Res.string.profile_edit_button),
                    onClick = { showEditDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )

                QuizSecondaryButton(
                    text = stringResource(Res.string.profile_change_password),
                    onClick = { showPasswordDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )

                QuizSecondaryButton(
                    text = stringResource(Res.string.profile_logout),
                    onClick = { viewModel.dispatch(ProfileIntent.Logout) },
                    modifier = Modifier.fillMaxWidth(),
                    textColor = Danger
                )
            }
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            user = user,
            onDismiss = { showEditDialog = false },
            onSave = { nickname ->
                viewModel.dispatch(ProfileIntent.UpdateNickname(nickname))
                showEditDialog = false
            }
        )
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onChange = { old, new ->
                viewModel.dispatch(ProfileIntent.ChangePassword(old, new))
            },
            result = passwordResult,
            onResultConsumed = { passwordResult = null },
            onPasswordChanged = {
                showPasswordDialog = false
                viewModel.dispatch(ProfileIntent.Logout)
            }
        )
    }

    if (showAvatarDialog) {
        AvatarSelectionDialog(
            onDismiss = { showAvatarDialog = false },
            onSelect = { emoji ->
                viewModel.dispatch(ProfileIntent.UpdateAvatar(emoji))
                showAvatarDialog = false
            }
        )
    }

    if (showServerUrlDialog) {
        ServerUrlDialog(
            onDismiss = { showServerUrlDialog = false }
        )
    }
}

@Composable
private fun EditProfileDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var nickname by remember { mutableStateOf(user.nickname) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A2980),
        title = { Text(stringResource(Res.string.profile_edit_title), color = Color.White) },
        text = {
            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text(stringResource(Res.string.profile_nickname)) },
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
        },
        confirmButton = {
            Button(
                onClick = { onSave(nickname) },
                colors = ButtonDefaults.buttonColors(containerColor = Gold)
            ) {
                Text(stringResource(Res.string.profile_save), color = DeepBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.profile_cancel), color = Color.White)
            }
        }
    )
}

@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onChange: (String, String) -> Unit,
    result: Result<Unit>?,
    onResultConsumed: () -> Unit,
    onPasswordChanged: () -> Unit = {}
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var showSuccess by remember { mutableStateOf(false) }
    val changeFailed = stringResource(Res.string.profile_change_failed)
    val focusManager = LocalFocusManager.current
    val oldPasswordFocusRequester = remember { FocusRequester() }
    val newPasswordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }

    LaunchedEffect(result) {
        if (result != null) {
            result.onSuccess { showSuccess = true }
                .onFailure { error = it.message ?: changeFailed }
            onResultConsumed()
        }
    }

    if (showSuccess) {
        AlertDialog(
            onDismissRequest = onPasswordChanged,
            containerColor = Color(0xFF1A2980),
            title = { Text(stringResource(Res.string.profile_password_changed), color = CorrectGreen) },
            text = {
                Text(
                    text = stringResource(Res.string.profile_password_changed_desc),
                    color = Color.White
                )
            },
            confirmButton = {
                Button(
                    onClick = onPasswordChanged,
                    colors = ButtonDefaults.buttonColors(containerColor = Gold)
                ) {
                    Text(stringResource(Res.string.profile_confirm), color = DeepBlue)
                }
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A2980),
        title = { Text(stringResource(Res.string.profile_change_password_title), color = Color.White) },
        text = {
            Column {
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it; error = "" },
                    label = { Text(stringResource(Res.string.profile_old_password)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(oldPasswordFocusRequester),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { newPasswordFocusRequester.requestFocus() },
                        onDone = { newPasswordFocusRequester.requestFocus() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Gold,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it; error = "" },
                    label = { Text(stringResource(Res.string.profile_new_password)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(newPasswordFocusRequester),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { confirmPasswordFocusRequester.requestFocus() },
                        onDone = { confirmPasswordFocusRequester.requestFocus() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Gold,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; error = "" },
                    label = { Text(stringResource(Res.string.profile_confirm_new_password)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(confirmPasswordFocusRequester),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Gold,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                    )
                )
                if (error.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error, color = WrongRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            val passwordMismatch = stringResource(Res.string.profile_password_mismatch)
            Button(
                onClick = {
                    if (newPassword != confirmPassword) {
                        error = passwordMismatch
                    } else {
                        error = ""
                        onChange(oldPassword, newPassword)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Gold)
            ) {
                Text(stringResource(Res.string.profile_confirm_button), color = DeepBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.profile_cancel), color = Color.White)
            }
        }
    )
}

@Composable
fun ServerUrlDialog(
    onDismiss: () -> Unit
) {
    val currentUrl = remember { ApiClient.getBaseUrl() }
    var url by remember { mutableStateOf(currentUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
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
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Gold)
            ) {
                Text(stringResource(Res.string.profile_save), color = DeepBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.profile_cancel), color = Color.White)
            }
        }
    )
}

@Composable
private fun AvatarSelectionDialog(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A2980),
        title = { Text(stringResource(Res.string.profile_select_avatar), color = Color.White) },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AvatarEmoji.entries.toList()) { avatar ->
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Gold.copy(alpha = 0.1f))
                            .clickable { onSelect(avatar.emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = avatar.emoji, fontSize = 28.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.profile_cancel), color = Color.White)
            }
        }
    )
}
