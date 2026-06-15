package com.havos.lubricerp.feature_reports.presentation.login

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.havos.lubricerp.core.ui.components.CollectEffect
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginRoute(
    onNavigateHome: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val context = LocalContext.current

    CollectEffect(effects = viewModel.effect) { effect ->
        when (effect) {
            LoginEffect.NavigateToHome -> onNavigateHome()
        }
    }

    LoginScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is LoginAction.UsernameChanged -> viewModel.onIntent(
                    LoginIntent.UsernameChanged(
                        action.value
                    )
                )

                is LoginAction.PasswordChanged -> viewModel.onIntent(
                    LoginIntent.PasswordChanged(
                        action.value
                    )
                )

                is LoginAction.RememberMeChanged -> viewModel.onIntent(
                    LoginIntent.RememberMeChanged(
                        action.value
                    )
                )

                LoginAction.Submit -> viewModel.onIntent(LoginIntent.Submit)
            }
        }
    )
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun LoginScreen(
    state: LoginUiState,
    onAction: (LoginAction) -> Unit,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val canSubmit = state.username.isNotBlank() &&
            state.password.isNotBlank() &&
            state.usernameError == null &&
            state.passwordError == null &&
            !state.isLoading
    val logoResId = remember {
        context.resources.getIdentifier("erp_logo", "drawable", context.packageName)
    }

    val colors = MaterialTheme.colorScheme

    // Press-scale animation for the submit button — adds a tactile, premium feel
    val buttonInteractionSource = remember { MutableInteractionSource() }
    val isButtonPressed by buttonInteractionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isButtonPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "submit_button_scale"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        colors.primary.copy(alpha = 0.10f),
                        colors.surface,
                        colors.tertiary.copy(alpha = 0.05f)
                    )
                )
            )
            .navigationBarsPadding()
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            }
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        val panelWidth = if (maxWidth >= 640.dp) 560.dp else maxWidth
        val logoWidth = if (maxWidth >= 640.dp) 210.dp else 170.dp

        // ── Decorative background blobs ──────────────────────────────────
        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.TopStart)
                .offset(x = (-50).dp, y = (-30).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            colors.primary.copy(alpha = 0.18f),
                            colors.primary.copy(alpha = 0f)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 70.dp, y = 70.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            colors.tertiary.copy(alpha = 0.14f),
                            colors.tertiary.copy(alpha = 0f)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 40.dp, y = (-80).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            colors.secondary.copy(alpha = 0.10f),
                            colors.secondary.copy(alpha = 0f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = panelWidth)
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (logoResId != 0) {
                Image(
                    painter = painterResource(id = logoResId),
                    contentDescription = "Goal ERP",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .widthIn(max = logoWidth),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(
                    text = "GOAL GP ERP",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.primary
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp),
                shape = RoundedCornerShape(24.dp),
                color = colors.surfaceContainerLow,
                tonalElevation = 3.dp,
                shadowElevation = 10.dp,
                border = BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            colors.primary.copy(alpha = 0.22f),
                            colors.outlineVariant.copy(alpha = 0.25f)
                        )
                    )
                ),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── Header ────────────────────────────────────────────
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            colors.primary.copy(alpha = 0.18f),
                                            colors.primary.copy(alpha = 0.06f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sign In",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                        Text(
                            text = "Please enter below details to access the dashboard",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // ── Username field ───────────────────────────────────
                    OutlinedTextField(
                        value = state.username,
                        onValueChange = { onAction(LoginAction.UsernameChanged(it)) },
                        label = { Text("Email or Phone") },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = null,
                                tint = if (state.usernameError != null) colors.error else colors.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = state.usernameError != null,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outlineVariant,
                            focusedLabelColor = colors.primary,
                            cursorColor = colors.primary,
                            focusedContainerColor = colors.surface,
                            unfocusedContainerColor = colors.surface.copy(alpha = 0.6f),
                            errorContainerColor = colors.errorContainer.copy(alpha = 0.12f)
                        ),
                        supportingText = {
                            AnimatedVisibility(
                                visible = state.usernameError != null,
                                enter = fadeIn(tween(150)) + expandVertically(tween(150)),
                                exit = fadeOut(tween(100)) + shrinkVertically(tween(100))
                            ) {
                                Text(
                                    text = state.usernameError.orEmpty(),
                                    color = colors.error
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                focusManager.moveFocus(FocusDirection.Down)
                            }
                        ),
                        singleLine = true
                    )

                    // ── Password field ────────────────────────────────────
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { onAction(LoginAction.PasswordChanged(it)) },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = if (state.passwordError != null) colors.error else colors.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = colors.onSurfaceVariant
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = state.passwordError != null,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outlineVariant,
                            focusedLabelColor = colors.primary,
                            cursorColor = colors.primary,
                            focusedContainerColor = colors.surface,
                            unfocusedContainerColor = colors.surface.copy(alpha = 0.6f),
                            errorContainerColor = colors.errorContainer.copy(alpha = 0.12f)
                        ),
                        supportingText = {
                            AnimatedVisibility(
                                visible = state.passwordError != null,
                                enter = fadeIn(tween(150)) + expandVertically(tween(150)),
                                exit = fadeOut(tween(100)) + shrinkVertically(tween(100))
                            ) {
                                Text(
                                    text = state.passwordError.orEmpty(),
                                    color = colors.error
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                onAction(LoginAction.Submit)
                            }
                        ),
                        singleLine = true
                    )

                    // ── Remember me ───────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onAction = { onAction(LoginAction.RememberMeChanged(!state.rememberMe)) }),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = state.rememberMe,
                            onCheckedChange = { onAction(LoginAction.RememberMeChanged(it)) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = colors.primary,
                                uncheckedColor = colors.outline,
                                checkmarkColor = colors.onPrimary
                            )
                        )
                        Text(
                            text = "Remember Me",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.onSurfaceVariant
                        )
                    }

                    // ── Error message banner ─────────────────────────────
                    AnimatedVisibility(
                        visible = state.errorMessage != null,
                        enter = fadeIn(tween(180)) + slideInVertically(tween(180)) { -it / 2 } + expandVertically(tween(180)),
                        exit = fadeOut(tween(120)) + shrinkVertically(tween(120))
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = colors.errorContainer,
                            tonalElevation = 0.dp
                        ) {
                            Text(
                                text = state.errorMessage.orEmpty(),
                                color = colors.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }

                    // ── Submit button ─────────────────────────────────────
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            onAction(LoginAction.Submit)
                        },
                        enabled = canSubmit,
                        shape = RoundedCornerShape(14.dp),
                        interactionSource = buttonInteractionSource,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary,
                            disabledContainerColor = colors.primary.copy(alpha = 0.35f),
                            disabledContentColor = colors.onPrimary.copy(alpha = 0.7f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 0.dp,
                            disabledElevation = 0.dp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .scale(buttonScale)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp,
                                color = colors.onPrimary
                            )
                        } else {
                            Text(
                                text = "Sign In",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Secured access · Goal GP ERP",
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.alpha(0.9f)
            )
        }
    }
}

/**
 * Small helper to make a Row clickable with a ripple while keeping the
 * function signature concise at the call-site.
 */
private fun Modifier.clickable(onAction: () -> Unit): Modifier = this.then(
    Modifier.pointerInput(Unit) {
        detectTapGestures { onAction() }
    }
)
