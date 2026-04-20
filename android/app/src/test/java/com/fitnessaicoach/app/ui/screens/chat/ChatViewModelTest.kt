package com.fitnessaicoach.app.ui.screens.chat

import com.fitnessaicoach.app.data.network.ChatMessageDto
import com.fitnessaicoach.app.data.network.Conversation
import com.fitnessaicoach.app.data.repository.ChatRepository
import com.fitnessaicoach.app.ui.MainDispatcherRule
import com.fitnessaicoach.app.ui.common.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repo: ChatRepository

    @Before
    fun setup() {
        repo = mock()
    }

    @Test
    fun `loadChat uses existing conversation and loads messages`() = runTest {
        val conversation = Conversation(7L, "Plan nutricional", "2026-04-18T09:00:00Z")
        val messages = listOf(
            ChatMessageDto(1L, "USER", "Necesito ideas de cena", "2026-04-18T09:01:00Z"),
            ChatMessageDto(2L, "ASSISTANT", "Haz una tortilla con verduras.", "2026-04-18T09:01:05Z"),
        )
        whenever(repo.getConversations()).thenReturn(Result.success(listOf(conversation)))
        whenever(repo.getMessages(7L)).thenReturn(Result.success(messages))

        val viewModel = ChatViewModel(repo)
        advanceUntilIdle()

        assertEquals(UiState.Success(messages), viewModel.messagesState.value)
        assertEquals(conversation, viewModel.activeConversation.value)
        assertTrue(viewModel.composerState.value.isBlank())
    }

    @Test
    fun `loadChat creates conversation when user has none`() = runTest {
        val conversation = Conversation(11L, "Nueva conversacion", "2026-04-18T09:00:00Z")
        whenever(repo.getConversations()).thenReturn(Result.success(emptyList()))
        whenever(repo.createConversation()).thenReturn(Result.success(conversation))
        whenever(repo.getMessages(11L)).thenReturn(Result.success(emptyList()))

        val viewModel = ChatViewModel(repo)
        advanceUntilIdle()

        verify(repo).createConversation()
        assertEquals(conversation, viewModel.activeConversation.value)
        assertEquals(UiState.Success(emptyList<ChatMessageDto>()), viewModel.messagesState.value)
    }

    @Test
    fun `sendMessage appends assistant response and clears composer`() = runTest {
        val conversation = Conversation(7L, "Plan nutricional", "2026-04-18T09:00:00Z")
        val initialMessages = listOf(
            ChatMessageDto(1L, "USER", "Necesito ideas de cena", "2026-04-18T09:01:00Z"),
        )
        val response = ChatMessageDto(2L, "ASSISTANT", "Prueba salmon con arroz.", "2026-04-18T09:02:00Z")
        whenever(repo.getConversations()).thenReturn(Result.success(listOf(conversation)))
        whenever(repo.getMessages(7L)).thenReturn(Result.success(initialMessages))
        whenever(repo.sendMessage(7L, "Dame una opcion alta en proteina")).thenReturn(Result.success(response))

        val viewModel = ChatViewModel(repo)
        advanceUntilIdle()

        viewModel.updateComposer("Dame una opcion alta en proteina")
        viewModel.sendMessage()
        advanceUntilIdle()

        val expectedMessages = listOf(
            initialMessages.first(),
            ChatMessageDto(
                id = -1L,
                role = "USER",
                content = "Dame una opcion alta en proteina",
                createdAt = "pending",
            ),
            response,
        )
        verify(repo).sendMessage(7L, "Dame una opcion alta en proteina")
        assertEquals(UiState.Success(expectedMessages), viewModel.messagesState.value)
        assertEquals("", viewModel.composerState.value)
        assertEquals(UiState.Success(Unit), viewModel.sendState.value)
    }

    @Test
    fun `sendMessage exposes error when api fails`() = runTest {
        val conversation = Conversation(7L, "Plan nutricional", "2026-04-18T09:00:00Z")
        whenever(repo.getConversations()).thenReturn(Result.success(listOf(conversation)))
        whenever(repo.getMessages(7L)).thenReturn(Result.success(emptyList()))
        whenever(repo.sendMessage(7L, "Hola")).thenReturn(Result.failure(RuntimeException("rate limit")))

        val viewModel = ChatViewModel(repo)
        advanceUntilIdle()

        viewModel.updateComposer("Hola")
        viewModel.sendMessage()
        advanceUntilIdle()

        assertEquals(UiState.Error("rate limit"), viewModel.sendState.value)
        assertEquals("Hola", viewModel.composerState.value)
    }
}
