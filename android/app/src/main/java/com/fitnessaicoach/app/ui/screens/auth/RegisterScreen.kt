package com.fitnessaicoach.app.ui.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fitnessaicoach.app.navigation.Screen
import com.fitnessaicoach.app.ui.common.UiState
import com.fitnessaicoach.app.ui.theme.*

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState     by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm  by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }

    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) { enter.animateTo(1f, tween(700, easing = FastOutSlowInEasing)) }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            viewModel.resetState()
            navController.navigate(Screen.ProfileOnboarding.route) {
                popUpTo(Screen.Register.route) { inclusive = true }
            }
        }
    }

    val isLoading    = uiState is UiState.Loading
    val apiError     = (uiState as? UiState.Error)?.message
    val passNoMatch  = confirm.isNotEmpty() && password != confirm
    val canSubmit    = !isLoading && email.isNotBlank() && password.length >= 6 && password == confirm

    // Password strength
    val strength = when {
        password.length < 6  -> 0
        password.length < 10 -> 1
        password.any { !it.isLetterOrDigit() } -> 3
        else -> 2
    }
    val strengthColor = listOf(Color.Transparent, Danger, Warning, Success)
    val strengthLabel = listOf("", "Débil", "Media", "Fuerte")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .imePadding(),
    ) {
        AuthBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .padding(top = 72.dp, bottom = 40.dp)
                .alpha(enter.value),
        ) {
            // Back link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    viewModel.resetState()
                    navController.popBackStack()
                },
            ) {
                Text("← ", color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Volver", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "CREA TU",
                color = Gold,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 6.sp,
                lineHeight = 40.sp,
            )
            Text(
                text = "CUENTA",
                color = TextPrimary,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 6.sp,
                lineHeight = 44.sp,
            )

            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(3.dp)
                    .background(Gold, RoundedCornerShape(2.dp))
            )

            Spacer(Modifier.height(12.dp))
            Text(
                text = "Tu coach IA personalizado empieza aquí",
                color = TextMuted,
                fontSize = 14.sp,
            )

            Spacer(Modifier.height(36.dp))

            // Steps indicator
            StepsRow(currentStep = 1)

            Spacer(Modifier.height(32.dp))

            AuthTextField(
                value         = email,
                onValueChange = { email = it },
                label         = "CORREO ELECTRÓNICO",
                keyboardType  = KeyboardType.Email,
                imeAction     = ImeAction.Next,
                onNext        = { focusManager.moveFocus(FocusDirection.Down) },
                enabled       = !isLoading,
            )

            Spacer(Modifier.height(20.dp))

            AuthTextField(
                value         = password,
                onValueChange = { password = it },
                label         = "CONTRASEÑA",
                isPassword    = true,
                showPassword  = showPass,
                onTogglePass  = { showPass = !showPass },
                imeAction     = ImeAction.Next,
                onNext        = { focusManager.moveFocus(FocusDirection.Down) },
                enabled       = !isLoading,
            )

            // Password strength bar
            if (password.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(3) { i ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (i < strength) strengthColor[strength] else Color(0xFF2E2E2E))
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text     = strengthLabel[strength],
                        color    = strengthColor[strength],
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            AuthTextField(
                value         = confirm,
                onValueChange = { confirm = it },
                label         = "CONFIRMAR CONTRASEÑA",
                isPassword    = true,
                showPassword  = showPass,
                imeAction     = ImeAction.Done,
                onDone        = { focusManager.clearFocus(); if (canSubmit) viewModel.register(email, password) },
                enabled       = !isLoading,
            )

            if (passNoMatch) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Las contraseñas no coinciden",
                    color = Danger,
                    fontSize = 12.sp,
                )
            }

            if (apiError != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = apiError,
                    color = Danger,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Danger.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }

            Spacer(Modifier.height(36.dp))

            GoldButton(
                text    = if (isLoading) "Creando cuenta..." else "Crear cuenta",
                enabled = canSubmit,
                loading = isLoading,
                onClick = { viewModel.register(email, password) },
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("¿Ya tienes cuenta? ", color = TextMuted, fontSize = 14.sp)
                Text(
                    text = "Inicia sesión",
                    color = Gold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        viewModel.resetState()
                        navController.popBackStack()
                    },
                )
            }
        }
    }
}

@Composable
private fun StepsRow(currentStep: Int) {
    val steps = listOf("Cuenta", "Perfil", "Listo")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        steps.forEachIndexed { i, label ->
            val active = i + 1 == currentStep
            val done   = i + 1 < currentStep
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(when { done -> Gold; active -> Gold.copy(alpha = 0.2f); else -> Color(0xFF2E2E2E) })
                        .then(if (active) Modifier.border(1.5.dp, Gold, RoundedCornerShape(14.dp)) else Modifier),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text       = if (done) "✓" else "${i + 1}",
                        color      = when { done -> Color(0xFF0F0F0F); active -> Gold; else -> TextMuted },
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign  = TextAlign.Center,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = label,
                    color = if (active || done) Gold else TextMuted,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                )
            }
            if (i < steps.lastIndex) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .padding(bottom = 16.dp)
                        .background(if (done) Gold else Color(0xFF2E2E2E))
                )
            }
        }
    }
}
