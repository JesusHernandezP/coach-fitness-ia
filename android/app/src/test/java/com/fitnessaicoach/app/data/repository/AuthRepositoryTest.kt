package com.fitnessaicoach.app.data.repository

import com.fitnessaicoach.app.data.local.TokenStore
import com.fitnessaicoach.app.data.network.ApiService
import com.fitnessaicoach.app.data.network.AuthRequest
import com.fitnessaicoach.app.data.network.AuthResponse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class AuthRepositoryTest {

    private lateinit var api:        ApiService
    private lateinit var tokenStore: TokenStore
    private lateinit var repo:       AuthRepository

    @Before
    fun setup() {
        api        = mock()
        tokenStore = mock()
        repo       = AuthRepository(api, tokenStore)
    }

    @Test
    fun `login saves token on success`() = runTest {
        whenever(api.login(AuthRequest("a@b.com", "pass"))).thenReturn(AuthResponse("tok123"))

        val result = repo.login("a@b.com", "pass")

        assertTrue(result.isSuccess)
        verify(tokenStore).save("tok123")
    }

    @Test
    fun `login returns failure on api error`() = runTest {
        whenever(api.login(any())).thenThrow(RuntimeException("401"))

        val result = repo.login("a@b.com", "wrong")

        assertTrue(result.isFailure)
        verify(tokenStore, never()).save(any())
    }

    @Test
    fun `logout clears token`() = runTest {
        repo.logout()
        verify(tokenStore).clear()
    }
}
