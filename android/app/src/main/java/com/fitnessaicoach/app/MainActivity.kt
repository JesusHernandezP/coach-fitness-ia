package com.fitnessaicoach.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import com.fitnessaicoach.app.data.local.TokenStore
import com.fitnessaicoach.app.data.network.AuthEventBus
import com.fitnessaicoach.app.navigation.AppNavigation
import com.fitnessaicoach.app.ui.theme.FitnessAICoachTheme
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var tokenStore: TokenStore
    @Inject lateinit var authEventBus: AuthEventBus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            authEventBus.unauthorizedEvent.collect {
                tokenStore.clear()
            }
        }

        setContent {
            FitnessAICoachTheme {
                val token by tokenStore.token.collectAsState(initial = null)
                AppNavigation(isLoggedIn = token != null)
            }
        }
    }
}
