package com.fitnessaicoach.app.data.repository

import com.fitnessaicoach.app.data.local.TokenStore
import com.fitnessaicoach.app.data.network.ApiService
import com.fitnessaicoach.app.data.network.AuthRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api:        ApiService,
    private val tokenStore: TokenStore,
) {
    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        val resp = api.login(AuthRequest(email, password))
        tokenStore.save(resp.token)
    }

    suspend fun register(email: String, password: String): Result<Unit> = runCatching {
        val resp = api.register(AuthRequest(email, password))
        tokenStore.save(resp.token)
    }

    suspend fun logout() = tokenStore.clear()
}
