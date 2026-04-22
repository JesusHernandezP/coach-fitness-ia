package com.fitnessaicoach.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fitnessaicoach.app.data.network.NutritionTarget
import com.fitnessaicoach.app.navigation.Screen
import com.fitnessaicoach.app.ui.common.UiState
import com.fitnessaicoach.app.ui.screens.auth.DiagonalAccent
import com.fitnessaicoach.app.ui.screens.auth.GoldButton
import com.fitnessaicoach.app.ui.theme.Background
import com.fitnessaicoach.app.ui.theme.Border
import com.fitnessaicoach.app.ui.theme.Danger
import com.fitnessaicoach.app.ui.theme.Gold
import com.fitnessaicoach.app.ui.theme.Surface
import com.fitnessaicoach.app.ui.theme.Surface2
import com.fitnessaicoach.app.ui.theme.TextMuted
import com.fitnessaicoach.app.ui.theme.TextPrimary

private val sexOptions = listOf("MALE" to "Hombre", "FEMALE" to "Mujer")
private val activityOptions = listOf(
    "SEDENTARY" to "Sedentario",
    "LIGHTLY_ACTIVE" to "Ligero",
    "MODERATELY_ACTIVE" to "Moderado",
    "VERY_ACTIVE" to "Activo",
    "EXTRA_ACTIVE" to "Muy activo",
)
private val goalOptions = listOf(
    "LOSE" to "Perder grasa",
    "MAINTAIN" to "Mantener",
    "GAIN" to "Ganar masa",
)
private val dietOptions = listOf(
    "STANDARD" to "Estandar",
    "KETO" to "Keto",
    "VEGETARIAN" to "Vegetariana",
    "INTERMITTENT_FASTING" to "Ayuno",
)

@Composable
fun ProfileScreen(
    navController: NavController,
    forceOnboarding: Boolean = false,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val isOnboarding by viewModel.isOnboarding.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()
    val targetsState by viewModel.targetsState.collectAsStateWithLifecycle()

    var navigateAfterSave by remember { mutableStateOf(false) }

    val effectiveOnboarding = forceOnboarding || isOnboarding

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.reloadProfile()
        }
    }

    LaunchedEffect(saveState, navigateAfterSave) {
        if (saveState is UiState.Success && navigateAfterSave) {
            navigateAfterSave = false
            viewModel.clearSaveState()
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.ProfileOnboarding.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .imePadding(),
    ) {
        DiagonalAccent()

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Gold, strokeWidth = 2.dp)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                ProfileHeader(effectiveOnboarding)

                SurfaceCard {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "DATOS METABOLICOS",
                            color = Gold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.8.sp,
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ProfileNumberField(
                                label = "Edad",
                                value = formState.age.asText(),
                                onValueChange = viewModel::updateAge,
                                modifier = Modifier.weight(1f),
                            )
                            ProfileNumberField(
                                label = "Altura (cm)",
                                value = formState.heightCm.asText(),
                                onValueChange = viewModel::updateHeightCm,
                                decimal = true,
                                modifier = Modifier.weight(1f),
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ProfileNumberField(
                                label = "Peso actual (kg)",
                                value = formState.currentWeightKg.asText(),
                                onValueChange = viewModel::updateCurrentWeightKg,
                                decimal = true,
                                modifier = Modifier.weight(1f),
                            )
                            ProfileNumberField(
                                label = "Pasos diarios",
                                value = formState.dailySteps.asText(),
                                onValueChange = viewModel::updateDailySteps,
                                modifier = Modifier.weight(1f),
                                placeholder = "Opcional",
                            )
                        }

                        ChoiceGroup("Sexo", sexOptions, formState.sex, viewModel::updateSex)
                        ChoiceGroup("Actividad", activityOptions, formState.activityLevel, viewModel::updateActivityLevel)
                        ChoiceGroup("Objetivo", goalOptions, formState.goal, viewModel::updateGoal)
                        ChoiceGroup("Dieta", dietOptions, formState.dietType, viewModel::updateDietType)

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ProfileNumberField(
                                label = "Dias ejercicio",
                                value = formState.weeklyExerciseDays.asText(),
                                onValueChange = viewModel::updateWeeklyExerciseDays,
                                modifier = Modifier.weight(1f),
                            )
                            ProfileNumberField(
                                label = "Min/sesion",
                                value = formState.exerciseMinutes.asText(),
                                onValueChange = viewModel::updateExerciseMinutes,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                TargetsCard(targetsState)

                val saveError = (saveState as? UiState.Error)?.message
                if (saveError != null) {
                    ErrorBanner(saveError)
                }

                GoldButton(
                    text = if (saveState is UiState.Loading) "Guardando..." else if (effectiveOnboarding) "Guardar y continuar" else "Guardar perfil",
                    enabled = saveState !is UiState.Loading && formState.canSubmit(),
                    loading = saveState is UiState.Loading,
                    onClick = {
                        navigateAfterSave = effectiveOnboarding
                        viewModel.saveProfile()
                    },
                )

                OutlinedButton(
                        onClick = {
                            viewModel.logout()
                            navController.navigate(com.fitnessaicoach.app.navigation.Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, com.fitnessaicoach.app.ui.theme.Danger),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    ) {
                        Text("Cerrar sesión", color = com.fitnessaicoach.app.ui.theme.Danger)
                    }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ProfileHeader(isOnboarding: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = if (isOnboarding) "COMPLETA TU" else "AJUSTA TU",
            color = Gold,
            fontSize = 34.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp,
            lineHeight = 34.sp,
        )
        Text(
            text = "PERFIL",
            color = TextPrimary,
            fontSize = 34.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp,
            lineHeight = 38.sp,
        )
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(3.dp)
                .background(Gold, RoundedCornerShape(2.dp)),
        )
        Text(
            text = if (isOnboarding) {
                "Necesitamos tus datos para calcular calorias y macros antes de entrar al panel."
            } else {
                "Actualiza tus metricas y objetivos. Las macros se recalculan al guardar."
            },
            color = TextMuted,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun SurfaceCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(20.dp))
            .border(1.dp, Border, RoundedCornerShape(20.dp))
            .padding(18.dp),
        content = content,
    )
}

@Composable
private fun ProfileNumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    decimal: Boolean = false,
    placeholder: String? = null,
) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = profileFieldColors(),
            placeholder = placeholder?.let { { Text(it, color = TextMuted) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number,
            ),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChoiceGroup(
    title: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title.uppercase(),
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { (value, label) ->
                val active = value == selected
                Box(
                    modifier = Modifier
                        .background(
                            color = if (active) Gold.copy(alpha = 0.14f) else Surface2,
                            shape = RoundedCornerShape(12.dp),
                        )
                        .border(
                            width = 1.dp,
                            color = if (active) Gold else Border,
                            shape = RoundedCornerShape(12.dp),
                        )
                        .clickable { onSelected(value) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Text(
                        text = label,
                        color = if (active) Gold else TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun TargetsCard(targetsState: UiState<NutritionTarget>) {
    SurfaceCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "OBJETIVOS CALCULADOS",
                color = Gold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.8.sp,
            )

            when (targetsState) {
                UiState.Idle -> {
                    Text(
                        text = "Guarda tu perfil para calcular tus calorias y macros.",
                        color = TextMuted,
                        fontSize = 14.sp,
                    )
                }

                UiState.Loading -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Gold,
                            strokeWidth = 2.dp,
                        )
                        Text("Calculando objetivos...", color = TextMuted, fontSize = 14.sp)
                    }
                }

                is UiState.Error -> ErrorBanner(targetsState.message)
                is UiState.Success -> {
                    val targets = targetsState.data
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TargetMetric("Kcal", targets.calories, Modifier.weight(1f))
                        TargetMetric("Prote", targets.proteinG, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TargetMetric("Carbs", targets.carbsG, Modifier.weight(1f))
                        TargetMetric("Grasa", targets.fatG, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TargetMetric(label: String, value: Double, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Surface2, RoundedCornerShape(16.dp))
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label.uppercase(), color = TextMuted, fontSize = 11.sp, letterSpacing = 1.sp)
        Text(
            text = value.toInt().toString(),
            color = Gold,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Danger.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .border(1.dp, Danger.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            text = message,
            color = Danger,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun profileFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Surface2,
    unfocusedContainerColor = Surface2,
    disabledContainerColor = Surface2,
    focusedBorderColor = Gold,
    unfocusedBorderColor = Border,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = Gold,
)

private fun Int.asText(): String = if (this == 0) "" else toString()

private fun Double.asText(): String = if (this == 0.0) {
    ""
} else if (this % 1.0 == 0.0) {
    this.toInt().toString()
} else {
    toString()
}

private fun ProfileFormState.canSubmit(): Boolean =
    age > 0 && heightCm > 0.0 && currentWeightKg > 0.0
