package com.fitnessaicoach.app.ui.screens.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fitnessaicoach.app.navigation.Screen
import com.fitnessaicoach.app.data.network.TodaySnapshot
import com.fitnessaicoach.app.data.network.WeightPoint
import com.fitnessaicoach.app.data.network.WeeklySummary
import com.fitnessaicoach.app.ui.common.UiState
import com.fitnessaicoach.app.ui.screens.auth.DiagonalAccent
import com.fitnessaicoach.app.ui.screens.auth.GoldButton
import com.fitnessaicoach.app.ui.theme.Background
import com.fitnessaicoach.app.ui.theme.Border
import com.fitnessaicoach.app.ui.theme.Gold
import com.fitnessaicoach.app.ui.theme.Info
import com.fitnessaicoach.app.ui.theme.Success
import com.fitnessaicoach.app.ui.theme.Surface
import com.fitnessaicoach.app.ui.theme.Surface2
import com.fitnessaicoach.app.ui.theme.TextMuted
import com.fitnessaicoach.app.ui.theme.TextPrimary

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val dashboardState by viewModel.dashboardState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val submitState by viewModel.submitState.collectAsStateWithLifecycle()

    LaunchedEffect(submitState) {
        if (submitState is UiState.Success) {
            viewModel.clearSubmitState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
    ) {
        DiagonalAccent()

        when (val state = dashboardState) {
            UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Gold, strokeWidth = 2.dp)
                }
            }

            is UiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("No se pudo cargar el panel", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(state.message, color = TextMuted, fontSize = 14.sp)
                    Spacer(Modifier.height(20.dp))
                    GoldButton(
                        text = "Reintentar",
                        enabled = true,
                        loading = false,
                        onClick = viewModel::loadDashboard,
                    )
                }
            }

            is UiState.Success -> {
                DashboardContentView(
                    navController = navController,
                    content = state.data,
                    formState = formState,
                    submitState = submitState,
                    onStepsChange = viewModel::updateSteps,
                    onCaloriesChange = viewModel::updateCaloriesBurned,
                    onNotesChange = viewModel::updateNotes,
                    onSubmit = viewModel::submitTodayActivity,
                )
            }

            UiState.Idle -> Unit
        }
    }
}

@Composable
private fun DashboardContentView(
    navController: NavController,
    content: DashboardContent,
    formState: ActivityFormState,
    submitState: UiState<Unit>,
    onStepsChange: (String) -> Unit,
    onCaloriesChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        DashboardHeader()
        DashboardQuickActions(
            onLogWeight = { navController.navigate(Screen.LogWeight.route) },
            onLogActivity = { navController.navigate(Screen.LogActivity.route) },
        )
        WeightProgressCard(content.weightProgress)
        WeeklySummaryCard(content.weeklySummary)
        TodaySummarySection(content.todaySnapshot, content.weeklySummary)
        InlineActivityCard(
            formState = formState,
            submitState = submitState,
            onStepsChange = onStepsChange,
            onCaloriesChange = onCaloriesChange,
            onNotesChange = onNotesChange,
            onSubmit = onSubmit,
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun DashboardHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "PANEL",
                color = Gold,
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
            )
            Text(
                text = "Tu progreso diario de un vistazo.",
                color = TextMuted,
                fontSize = 14.sp,
            )
        }
        Text("T17", color = Gold.copy(alpha = 0.45f), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DashboardQuickActions(
    onLogWeight: () -> Unit,
    onLogActivity: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        QuickFab(
            label = "Peso",
            icon = { Icon(Icons.Filled.MonitorWeight, contentDescription = "Registrar peso") },
            onClick = onLogWeight,
            modifier = Modifier.weight(1f),
        )
        QuickFab(
            label = "Actividad",
            icon = { Icon(Icons.AutoMirrored.Filled.DirectionsRun, contentDescription = "Registrar actividad") },
            onClick = onLogActivity,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun QuickFab(
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        containerColor = Gold.copy(alpha = 0.14f),
        contentColor = Gold,
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            icon()
            Text(label, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun WeightProgressCard(points: List<WeightPoint>) {
    DashboardCard(title = "PROGRESO DE PESO", subtitle = "Ultimos registros") {
        if (points.isEmpty()) {
            EmptyState("Aun no hay pesos registrados para dibujar la evolucion.")
        } else {
            SimpleLineChart(points)
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(points.first().loggedAt.take(10), color = TextMuted, fontSize = 12.sp)
                Text(
                    "${points.last().weightKg.toPrettyNumber()} kg",
                    color = Gold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun WeeklySummaryCard(summary: WeeklySummary) {
    DashboardCard(title = "RESUMEN SEMANAL", subtitle = "Actividad de los ultimos 7 dias") {
        MiniBarChart(
            values = listOf(
                summary.avgSteps.toFloat(),
                summary.stepsTotal.toFloat() / 7f,
                summary.caloriesBurnedTotal.toFloat(),
            ),
            labels = listOf("Media", "Ritmo", "Kcal"),
        )
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatPill("Pasos", summary.stepsTotal.toString(), Modifier.weight(1f))
            StatPill("Dias", summary.daysLogged.toString(), Modifier.weight(1f))
        }
    }
}

@Composable
private fun TodaySummarySection(today: TodaySnapshot, weeklySummary: WeeklySummary) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("Pasos hoy", today.steps.toString(), "objetivo diario", Success, Modifier.weight(1f))
            MetricCard("Kcal hoy", today.caloriesBurned.toString(), "actividad", Info, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("Delta 7d", weeklySummary.weightDelta?.toPrettySigned() ?: "--", "peso", Gold, Modifier.weight(1f))
            MetricCard("Media", weeklySummary.avgSteps.toInt().toString(), "pasos/dia", Gold, Modifier.weight(1f))
        }
    }
}

@Composable
private fun InlineActivityCard(
    formState: ActivityFormState,
    submitState: UiState<Unit>,
    onStepsChange: (String) -> Unit,
    onCaloriesChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    DashboardCard(
        title = "REGISTRAR ACTIVIDAD",
        subtitle = "Hoy ${formState.date}",
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DashboardField(
                label = "Pasos",
                value = formState.steps,
                onValueChange = onStepsChange,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
            )
            DashboardField(
                label = "Calorias",
                value = formState.caloriesBurned,
                onValueChange = onCaloriesChange,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(12.dp))
        DashboardField(
            label = "Notas",
            value = formState.notes,
            onValueChange = onNotesChange,
            keyboardType = KeyboardType.Text,
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
        )
        Spacer(Modifier.height(12.dp))
        when (submitState) {
            is UiState.Error -> InlineBanner(submitState.message, false)
            is UiState.Success -> InlineBanner("Actividad guardada y panel actualizado.", true)
            else -> Unit
        }
        if (submitState is UiState.Error || submitState is UiState.Success) {
            Spacer(Modifier.height(12.dp))
        }
        GoldButton(
            text = if (submitState is UiState.Loading) "Guardando..." else "Guardar actividad de hoy",
            enabled = submitState !is UiState.Loading && formState.canSubmit(),
            loading = submitState is UiState.Loading,
            onClick = onSubmit,
        )
    }
}

@Composable
private fun DashboardCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(22.dp))
            .border(1.dp, Border, RoundedCornerShape(22.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp)
        Text(subtitle, color = TextMuted, fontSize = 13.sp)
        Spacer(Modifier.height(4.dp))
        content()
    }
}

@Composable
private fun DashboardField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    modifier: Modifier,
    singleLine: Boolean = true,
) {
    Column(modifier = modifier) {
        Text(label.uppercase(), color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 3,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Surface2,
                unfocusedContainerColor = Surface2,
                disabledContainerColor = Surface2,
                focusedBorderColor = Gold,
                unfocusedBorderColor = Border,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = Gold,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        )
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    caption: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(Surface2, RoundedCornerShape(18.dp))
            .border(1.dp, Border, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label.uppercase(), color = TextMuted, fontSize = 10.sp, letterSpacing = 1.sp)
        Text(value, color = accent, fontSize = 22.sp, fontWeight = FontWeight.Black)
        Text(caption, color = TextMuted, fontSize = 12.sp)
    }
}

@Composable
private fun StatPill(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Surface2, RoundedCornerShape(16.dp))
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = TextMuted, fontSize = 12.sp)
        Text(value, color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InlineBanner(message: String, isSuccess: Boolean) {
    val accent = if (isSuccess) Success else MaterialTheme.colorScheme.error
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(accent.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(message, color = accent, fontSize = 13.sp)
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface2, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Text(message, color = TextMuted, fontSize = 13.sp)
    }
}

@Composable
private fun SimpleLineChart(points: List<WeightPoint>) {
    val weights = points.map { it.weightKg.toFloat() }
    val min = weights.minOrNull() ?: 0f
    val max = weights.maxOrNull() ?: min
    val span = (max - min).takeIf { it > 0f } ?: 1f

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .background(Surface2, RoundedCornerShape(18.dp))
            .padding(12.dp),
    ) {
        repeat(4) { index ->
            val y = size.height * index / 3f
            drawLine(
                color = Border,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f,
            )
        }

        val path = Path()
        points.forEachIndexed { index, point ->
            val x = if (points.size == 1) size.width / 2f else size.width * index / points.lastIndex.coerceAtLeast(1)
            val y = size.height - (((point.weightKg.toFloat() - min) / span) * size.height)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            drawCircle(color = Gold, radius = 6f, center = Offset(x, y))
        }

        drawPath(
            path = path,
            color = Gold,
            style = Stroke(width = 5f, cap = StrokeCap.Round),
        )
    }
}

@Composable
private fun MiniBarChart(values: List<Float>, labels: List<String>) {
    val max = values.maxOrNull()?.takeIf { it > 0f } ?: 1f
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(Surface2, RoundedCornerShape(18.dp))
            .padding(12.dp),
    ) {
        val gap = 24.dp.toPx()
        val barWidth = (size.width - (gap * (values.size + 1))) / values.size
        values.forEachIndexed { index, value ->
            val left = gap + index * (barWidth + gap)
            val height = (value / max) * (size.height - 12.dp.toPx())
            val top = size.height - height
            drawRoundRect(
                color = if (index == values.lastIndex) Info else Gold,
                topLeft = Offset(left, top),
                size = Size(barWidth, height),
                cornerRadius = CornerRadius(14f, 14f),
            )
        }
    }
    Spacer(Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        labels.forEach { label ->
            Text(label, color = TextMuted, fontSize = 11.sp)
        }
    }
}

private fun Double.toPrettyNumber(): String =
    if (this % 1.0 == 0.0) toInt().toString() else String.format("%.1f", this)

private fun Double.toPrettySigned(): String {
    val value = toPrettyNumber()
    return if (value.startsWith("-")) "$value kg" else "+$value kg"
}
