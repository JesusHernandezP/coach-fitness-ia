package com.fitnessaicoach.app.ui.screens.logs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.fitnessaicoach.app.ui.common.UiState
import com.fitnessaicoach.app.ui.screens.auth.DiagonalAccent
import com.fitnessaicoach.app.ui.screens.auth.GoldButton
import com.fitnessaicoach.app.ui.theme.Background
import com.fitnessaicoach.app.ui.theme.Border
import com.fitnessaicoach.app.ui.theme.Gold
import com.fitnessaicoach.app.ui.theme.Surface
import com.fitnessaicoach.app.ui.theme.Surface2
import com.fitnessaicoach.app.ui.theme.TextMuted
import com.fitnessaicoach.app.ui.theme.TextPrimary

@Composable
fun LogWeightScreen(
    navController: NavController,
    viewModel: LogWeightViewModel = hiltViewModel(),
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val submitState by viewModel.submitState.collectAsStateWithLifecycle()

    LaunchedEffect(submitState) {
        if (submitState is UiState.Success) {
            viewModel.clearSubmitState()
            navController.popBackStack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
    ) {
        DiagonalAccent()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            LogHeader("REGISTRAR PESO", "Guarda un nuevo peso para refrescar tu progreso.")
            LogCard {
                Text("FECHA", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(formState.date, color = TextPrimary, fontSize = 15.sp)
                Spacer(Modifier.height(16.dp))
                Text("PESO (KG)", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = formState.weight,
                    onValueChange = viewModel::updateWeight,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = logFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    placeholder = { Text("Ej. 81.4") },
                )
                if (submitState is UiState.Error) {
                    Spacer(Modifier.height(12.dp))
                    Text((submitState as UiState.Error).message, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
                Spacer(Modifier.height(18.dp))
                GoldButton(
                    text = if (submitState is UiState.Loading) "Guardando..." else "Guardar peso",
                    enabled = submitState !is UiState.Loading,
                    loading = submitState is UiState.Loading,
                    onClick = viewModel::submit,
                )
            }
        }
    }
}

