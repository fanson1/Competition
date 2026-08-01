package com.example.competition.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.competition.data.UserManager
import com.example.competition.model.AvatarEmoji
import com.example.competition.model.User
import com.example.competition.model.UserProfile
import com.example.competition.repository.AppMode
import com.example.competition.repository.ModeRouter
import com.example.competition.sync.SyncManager
import com.example.competition.ui.theme.*
import kotlinx.coroutines.launch
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
    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    var showServerUrlDialog by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }
    var syncError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val currentMode by ModeRouter.currentMode.collectAsState()
    val modeNeedLoginText = stringResource(Res.string.mode_need_login)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D1B3E), Color(0xFF1A2980))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.profile_back),
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.clickable { onBack() }
                )
                Text(
                    text = stringResource(Res.string.profile_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "",
                    modifier = Modifier.width(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Avatar and name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Gold.copy(alpha = 0.2f))
                        .clickable { showAvatarDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.avatarEmoji,
                        fontSize = 48.sp
                    )
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
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stats
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.08f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStatItem(stringResource(Res.string.profile_total_score), "${profile?.totalScore ?: 0}", Gold)
                        ProfileStatItem(stringResource(Res.string.profile_max_level), "${profile?.maxLevel ?: 0}", CorrectGreen)
                        ProfileStatItem(stringResource(Res.string.profile_games_played), "${profile?.totalGamesPlayed ?: 0}", LightBlue)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStatItem(stringResource(Res.string.profile_correct_count), "${profile?.totalCorrectCount ?: 0}", TimerOrange)
                        ProfileStatItem(stringResource(Res.string.profile_streak), "${profile?.maxStreak ?: 0}", Purple)
                        ProfileStatItem(stringResource(Res.string.profile_completed_levels), "${profile?.completedLevels?.size ?: 0}", CorrectGreen)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Completed levels
            if (profile?.completedLevels?.isNotEmpty() == true) {
                Text(
                    text = stringResource(Res.string.profile_completed_section),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    profile.completedLevels.sorted().forEach { level ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CorrectGreen.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = stringResource(Res.string.profile_level_badge, level),
                                fontSize = 12.sp,
                                color = CorrectGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
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
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (currentMode == AppMode.ONLINE) CorrectGreen else Color.White.copy(alpha = 0.6f)
                )
                TextButton(onClick = { showServerUrlDialog = true }) {
                    Text(
                        text = "服务器设置",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.08f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = currentMode == AppMode.ONLINE,
                            onCheckedChange = { online ->
                                if (online) {
                                    if (UserManager.isLoggedIn()) {
                                        scope.launch {
                                            isSyncing = true
                                            syncError = null
                                            val result = SyncManager.syncToOnline()
                                            result.onSuccess {
                                                ModeRouter.switch(AppMode.ONLINE)
                                            }.onFailure { e ->
                                                syncError = e.message
                                            }
                                            isSyncing = false
                                        }
                                    } else {
                                        syncError = modeNeedLoginText
                                    }
                                } else {
                                    ModeRouter.switch(AppMode.OFFLINE)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = CorrectGreen,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
                            )
                        )
                    }

                    if (isSyncing) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Gold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(Res.string.mode_syncing),
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }

                    if (syncError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = syncError!!,
                            fontSize = 12.sp,
                            color = WrongRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(stringResource(Res.string.profile_edit_button))
                }

                OutlinedButton(
                    onClick = { showPasswordDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(stringResource(Res.string.profile_change_password))
                }

                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WrongRed)
                ) {
                    Text(stringResource(Res.string.profile_logout), color = Color.White)
                }
            }
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            user = user,
            onDismiss = { showEditDialog = false },
                    onSave = { nickname ->
                        UserManager.updateProfile(nickname = nickname)
                        showEditDialog = false
                        onProfileUpdated()
                    }
        )
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onChange = { old, new -> UserManager.changePassword(old, new) },
            onPasswordChanged = { showPasswordDialog = false; onLogout() }
        )
    }

    if (showAvatarDialog) {
        AvatarSelectionDialog(
            onDismiss = { showAvatarDialog = false },
                    onSelect = { emoji ->
                        UserManager.updateProfile(avatarEmoji = emoji)
                        showAvatarDialog = false
                        onProfileUpdated()
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
private fun ProfileStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.6f)
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
    onChange: (String, String) -> Result<Unit>,
    onPasswordChanged: () -> Unit = {}
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var showSuccess by remember { mutableStateOf(false) }

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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
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
            val changeFailed = stringResource(Res.string.profile_change_failed)
            Button(
                onClick = {
                    if (newPassword != confirmPassword) {
                        error = passwordMismatch
                    } else {
                        val result = onChange(oldPassword, newPassword)
                        result.onSuccess { showSuccess = true }
                        result.onFailure { error = it.message ?: changeFailed }
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
