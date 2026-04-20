package com.fitnessaicoach.app.ui.screens.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import kotlin.math.pow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fitnessaicoach.app.navigation.Screen
import com.fitnessaicoach.app.ui.common.UiState
import com.fitnessaicoach.app.ui.theme.*

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }

    // Staggered entrance
    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) { enter.animateTo(1f, tween(700, easing = FastOutSlowInEasing)) }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            viewModel.resetState()
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    val isLoading = uiState is UiState.Loading
    val errorMsg  = (uiState as? UiState.Error)?.message

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
    ) {
        AuthBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .padding(top = 72.dp, bottom = 40.dp)
                .alpha(enter.value),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // Brand mark
            Text(
                text = "FITNESS",
                color = Gold,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 6.sp,
                lineHeight = 42.sp,
            )
            Text(
                text = "AI COACH",
                color = TextPrimary,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 6.sp,
                lineHeight = 46.sp,
            )

            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(3.dp)
                    .background(Gold, RoundedCornerShape(2.dp))
            )

            Spacer(Modifier.height(40.dp))
            Text(
                text = "Bienvenido de vuelta",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Inicia sesión para continuar tu progreso",
                color = TextMuted,
                fontSize = 14.sp,
            )

            Spacer(Modifier.height(36.dp))

            // Email field
            AuthTextField(
                value        = email,
                onValueChange = { email = it },
                label        = "CORREO ELECTRÓNICO",
                keyboardType = KeyboardType.Email,
                imeAction    = ImeAction.Next,
                onNext       = { focusManager.moveFocus(FocusDirection.Down) },
                enabled      = !isLoading,
            )

            Spacer(Modifier.height(20.dp))

            // Password field
            AuthTextField(
                value         = password,
                onValueChange = { password = it },
                label         = "CONTRASEÑA",
                isPassword    = true,
                showPassword  = showPass,
                onTogglePass  = { showPass = !showPass },
                imeAction     = ImeAction.Done,
                onDone        = { focusManager.clearFocus(); viewModel.login(email, password) },
                enabled       = !isLoading,
            )

            // Error message
            if (errorMsg != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text  = errorMsg,
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

            // CTA button
            GoldButton(
                text      = if (isLoading) "Iniciando sesión..." else "Iniciar sesión",
                enabled   = !isLoading && email.isNotBlank() && password.isNotBlank(),
                loading   = isLoading,
                onClick   = { viewModel.login(email, password) },
            )

            Spacer(Modifier.height(24.dp))

            // Register link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text("¿Sin cuenta? ", color = TextMuted, fontSize = 14.sp)
                Text(
                    text     = "Regístrate",
                    color    = Gold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable {
                        viewModel.resetState()
                        navController.navigate(Screen.Register.route)
                    },
                )
            }
        }
    }
}

// ── Reusable composables ──────────────────────────────────────────────────────

@Composable
internal fun DiagonalAccent() {
    val gold = Gold
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .drawBehind {
                rotate(-20f, pivot = Offset(size.width * 0.7f, 0f)) {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(gold.copy(alpha = 0.12f), Color.Transparent),
                            start  = Offset(size.width * 0.4f, 0f),
                            end    = Offset(size.width * 1.2f, size.height),
                        ),
                    )
                }
                rotate(-20f, pivot = Offset(size.width * 0.7f, 0f)) {
                    drawRect(
                        color  = gold.copy(alpha = 0.35f),
                        topLeft = Offset(size.width * 0.72f, 0f),
                        size    = androidx.compose.ui.geometry.Size(2f, size.height),
                    )
                }
            },
    )
}

@Composable
internal fun AuthBackground() {
    val gold = Gold
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Radial gold glow top-right
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(gold.copy(alpha = 0.18f), Color.Transparent),
                center = Offset(size.width * 0.9f, size.height * 0.05f),
                radius = size.width * 0.65f,
            ),
            center = Offset(size.width * 0.9f, size.height * 0.05f),
            radius = size.width * 0.65f,
        )
        // Secondary glow bottom-left
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(gold.copy(alpha = 0.07f), Color.Transparent),
                center = Offset(size.width * 0.05f, size.height * 0.85f),
                radius = size.width * 0.45f,
            ),
            center = Offset(size.width * 0.05f, size.height * 0.85f),
            radius = size.width * 0.45f,
        )
        // Diagonal accent line
        val lineX = size.width * 0.78f
        rotate(-22f, pivot = Offset(lineX, 0f)) {
            drawRect(
                color = gold.copy(alpha = 0.28f),
                topLeft = Offset(lineX, 0f),
                size = androidx.compose.ui.geometry.Size(1.5f, size.height * 0.55f),
            )
            drawRect(
                color = gold.copy(alpha = 0.10f),
                topLeft = Offset(lineX + 14f, 0f),
                size = androidx.compose.ui.geometry.Size(1f, size.height * 0.40f),
            )
        }
        // Dot grid top-right quadrant
        val dotSpacing = 36f
        val dotRadius  = 1.2f
        val cols = (size.width  / dotSpacing).toInt()
        val rows = (size.height / dotSpacing).toInt()
        for (col in 0..cols) {
            for (row in 0..rows) {
                val x = col * dotSpacing
                val y = row * dotSpacing
                val distFromTopRight = kotlin.math.sqrt(
                    ((size.width - x) / size.width).toDouble().pow(2) +
                    (y / size.height).toDouble().pow(2)
                ).toFloat()
                val alpha = (0.12f - distFromTopRight * 0.10f).coerceIn(0f, 0.12f)
                if (alpha > 0.01f) {
                    drawCircle(color = gold.copy(alpha = alpha), radius = dotRadius, center = Offset(x, y))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AuthTextField(
    value         : String,
    onValueChange : (String) -> Unit,
    label         : String,
    isPassword    : Boolean = false,
    showPassword  : Boolean = false,
    onTogglePass  : (() -> Unit)? = null,
    keyboardType  : KeyboardType = KeyboardType.Text,
    imeAction     : ImeAction = ImeAction.Next,
    onNext        : (() -> Unit)? = null,
    onDone        : (() -> Unit)? = null,
    enabled       : Boolean = true,
) {
    val visual = if (isPassword && !showPassword) PasswordVisualTransformation() else VisualTransformation.None
    val gold = Gold

    Column {
        Text(
            text       = label,
            color      = TextMuted,
            fontSize   = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp,
        )
        Spacer(Modifier.height(6.dp))
        BasicTextField(
            value         = value,
            onValueChange = onValueChange,
            enabled       = enabled,
            singleLine    = true,
            visualTransformation = visual,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = KeyboardActions(
                onNext = { onNext?.invoke() },
                onDone = { onDone?.invoke() },
            ),
            textStyle = TextStyle(
                color = TextPrimary,
                fontSize = 16.sp,
            ),
            decorationBox = { inner ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            // bottom border
                            val y = size.height
                            drawLine(
                                color       = if (value.isNotEmpty()) gold else Color(0xFF3A3A3A),
                                start       = Offset(0f, y),
                                end         = Offset(size.width, y),
                                strokeWidth = if (value.isNotEmpty()) 2f else 1f,
                            )
                        }
                        .padding(bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(
                                text  = if (isPassword) "••••••••" else "ejemplo@correo.com",
                                color = TextMuted.copy(alpha = 0.4f),
                                fontSize = 16.sp,
                            )
                        }
                        inner()
                    }
                    if (isPassword && onTogglePass != null) {
                        Text(
                            text     = if (showPassword) "OCULTAR" else "VER",
                            color    = Gold.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier
                                .clickable { onTogglePass() }
                                .padding(start = 8.dp),
                        )
                    }
                }
            },
        )
    }
}

@Composable
internal fun GoldButton(
    text    : String,
    enabled : Boolean,
    loading : Boolean,
    onClick : () -> Unit,
) {
    val gold = Gold
    Button(
        onClick  = onClick,
        enabled  = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .drawBehind {
                if (enabled) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(gold.copy(alpha = 0.25f), Color.Transparent),
                            center = Offset(size.width / 2, size.height),
                            radius = size.width * 0.6f,
                        ),
                    )
                }
            },
        shape  = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor         = Gold,
            contentColor           = Color(0xFF0F0F0F),
            disabledContainerColor = Color(0xFF2A2A2A),
            disabledContentColor   = TextMuted,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier  = Modifier.size(20.dp),
                color     = Color(0xFF0F0F0F),
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text       = text,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                textAlign  = TextAlign.Center,
            )
        }
    }
}
