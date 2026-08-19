package com.example.competition.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.competition.presentation.login.LoginEffect
import com.example.competition.presentation.login.LoginError
import com.example.competition.presentation.login.LoginIntent
import com.example.competition.presentation.login.LoginMode
import com.example.competition.presentation.login.LoginViewModel
import com.example.competition.ui.MviEffectCollector
import com.example.competition.ui.components.DeepGradientColors
import com.example.competition.ui.components.GlassCard
import com.example.competition.ui.components.QuizPrimaryButton
import com.example.competition.ui.components.ScreenBackground
import com.example.competition.ui.components.rememberPulseScale
import com.example.competition.ui.rememberViewModel
import com.example.competition.ui.theme.*
import org.jetbrains.compose.resources.stringResource
import competition.app.shared.generated.resources.Res
import competition.app.shared.generated.resources.*

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val viewModel = rememberViewModel { LoginViewModel() }
    val state by viewModel.state.collectAsState()

    MviEffectCollector(viewModel) { effect ->
        when (effect) {
            LoginEffect.LoginSuccess -> onLoginSuccess()
        }
    }

    val passwordMismatchError = stringResource(Res.string.login_password_mismatch)

    val pulseScale = rememberPulseScale()
    val focusManager = LocalFocusManager.current
    val usernameFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }
    val nicknameFocusRequester = remember { FocusRequester() }

ScreenBackground(
        contentAlignment = Alignment.Center,
        colors = DeepGradientColors
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.login_title),
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = QuizPalette.Gold,
                modifier = Modifier.scale(pulseScale)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.login_subtitle),
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = QuizRadii.lg,
                contentPadding = PaddingValues(20.dp)
            ) {
                // Mode selector — sliding tab
                TabRow(
                    selectedTabIndex = if (state.mode == LoginMode.LOGIN) 0 else 1,
                    containerColor = Color.Transparent,
                    contentColor = QuizPalette.Gold,
                    modifier = Modifier.fillMaxWidth(),
                    indicator = {},
                    divider = {}
                ) {
                    Tab(
                        selected = state.mode == LoginMode.LOGIN,
                        onClick = { viewModel.dispatch(LoginIntent.SetMode(LoginMode.LOGIN)) },
                        text = {
                            Text(
                                text = stringResource(Res.string.login_tab),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (state.mode == LoginMode.LOGIN) QuizPalette.Gold
                                else Color.White.copy(alpha = 0.55f)
                            )
                        }
                    )
                    Tab(
                        selected = state.mode == LoginMode.REGISTER,
                        onClick = { viewModel.dispatch(LoginIntent.SetMode(LoginMode.REGISTER)) },
                        text = {
                            Text(
                                text = stringResource(Res.string.register_tab),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (state.mode == LoginMode.REGISTER) QuizPalette.Gold
                                else Color.White.copy(alpha = 0.55f)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Username
                OutlinedTextField(
                    value = state.username,
                    onValueChange = { viewModel.dispatch(LoginIntent.UpdateUsername(it)) },
                    label = { Text(stringResource(Res.string.login_username)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(usernameFocusRequester),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { passwordFocusRequester.requestFocus() },
                        onDone = { passwordFocusRequester.requestFocus() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = QuizPalette.Gold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                        focusedLabelColor = QuizPalette.Gold,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                        cursorColor = QuizPalette.Gold,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = QuizPalette.Glass,
                        unfocusedContainerColor = QuizPalette.Glass
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password
                OutlinedTextField(
                    value = state.password,
                    onValueChange = { viewModel.dispatch(LoginIntent.UpdatePassword(it)) },
                    label = { Text(stringResource(Res.string.login_password)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(passwordFocusRequester),
                    shape = RoundedCornerShape(14.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = if (state.mode == LoginMode.REGISTER) ImeAction.Next else ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { confirmPasswordFocusRequester.requestFocus() },
                        onDone = { focusManager.clearFocus() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = QuizPalette.Gold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                        focusedLabelColor = QuizPalette.Gold,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                        cursorColor = QuizPalette.Gold,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = QuizPalette.Glass,
                        unfocusedContainerColor = QuizPalette.Glass
                    ),
                    singleLine = true
                )

                if (state.mode == LoginMode.REGISTER) {
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = state.confirmPassword,
                        onValueChange = { viewModel.dispatch(LoginIntent.UpdateConfirmPassword(it)) },
                        label = { Text(stringResource(Res.string.login_confirm_password)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(confirmPasswordFocusRequester),
                        shape = RoundedCornerShape(14.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { nicknameFocusRequester.requestFocus() },
                            onDone = { nicknameFocusRequester.requestFocus() }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = QuizPalette.Gold,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                            focusedLabelColor = QuizPalette.Gold,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                            cursorColor = QuizPalette.Gold,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = QuizPalette.Glass,
                            unfocusedContainerColor = QuizPalette.Glass
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = state.nickname,
                        onValueChange = { viewModel.dispatch(LoginIntent.UpdateNickname(it)) },
                        label = { Text(stringResource(Res.string.login_nickname)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(nicknameFocusRequester),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = QuizPalette.Gold,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.25f),
                            focusedLabelColor = QuizPalette.Gold,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                            cursorColor = QuizPalette.Gold,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = QuizPalette.Glass,
                            unfocusedContainerColor = QuizPalette.Glass
                        ),
                        singleLine = true
                    )
                }

                val errorMessage = when (val error = state.error) {
                    LoginError.PasswordMismatch -> passwordMismatchError
                    is LoginError.Message -> error.text
                    null -> ""
                }
                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage,
                        color = Danger,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                QuizPrimaryButton(
                    text = if (state.mode == LoginMode.LOGIN)
                        stringResource(Res.string.login_button)
                    else
                        stringResource(Res.string.register_button),
                    onClick = { viewModel.dispatch(LoginIntent.Submit) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading,
                    content = if (state.isLoading) {
                        {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 8.dp),
                                color = QuizPalette.NightDeep,
                                strokeWidth = 2.dp
                            )
                        }
                    } else null
                )
            }
        }
    }
}
