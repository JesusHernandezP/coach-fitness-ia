package com.fitnessaicoach.app.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnessaicoach.app.data.network.ChatMessageDto
import com.fitnessaicoach.app.data.network.Conversation
import com.fitnessaicoach.app.data.repository.ChatRepository
import com.fitnessaicoach.app.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repo: ChatRepository,
) : ViewModel() {

    private val _activeConversation = MutableStateFlow<Conversation?>(null)
    val activeConversation: StateFlow<Conversation?> = _activeConversation.asStateFlow()

    private val _messagesState = MutableStateFlow<UiState<List<ChatMessageDto>>>(UiState.Loading)
    val messagesState: StateFlow<UiState<List<ChatMessageDto>>> = _messagesState.asStateFlow()

    private val _composerState = MutableStateFlow("")
    val composerState: StateFlow<String> = _composerState.asStateFlow()

    private val _sendState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val sendState: StateFlow<UiState<Unit>> = _sendState.asStateFlow()

    init {
        bootstrapChat()
    }

    fun bootstrapChat() {
        viewModelScope.launch {
            _messagesState.value = UiState.Loading

            runCatching {
                val conversation = repo.getConversations().getOrThrow().firstOrNull()
                    ?: repo.createConversation().getOrThrow()
                val messages = repo.getMessages(conversation.id).getOrThrow()
                conversation to messages
            }.onSuccess { (conversation, messages) ->
                _activeConversation.value = conversation
                _messagesState.value = UiState.Success(messages)
            }.onFailure {
                _messagesState.value = UiState.Error(it.message ?: "No se pudo cargar el chat")
            }
        }
    }

    fun updateComposer(value: String) {
        _composerState.value = value
    }

    fun sendMessage() {
        val conversation = _activeConversation.value ?: return
        val content = _composerState.value.trim()
        if (content.isBlank()) return

        val previousMessages = (_messagesState.value as? UiState.Success)?.data.orEmpty()
        val pendingMessage = ChatMessageDto(
            id = -1L,
            role = "USER",
            content = content,
            createdAt = "pending",
        )

        viewModelScope.launch {
            _sendState.value = UiState.Loading
            _messagesState.value = UiState.Success(previousMessages + pendingMessage)

            repo.sendMessage(conversation.id, content)
                .onSuccess { response ->
                    _composerState.value = ""
                    _messagesState.value = UiState.Success(previousMessages + pendingMessage + response)
                    _sendState.value = UiState.Success(Unit)
                }
                .onFailure {
                    _messagesState.value = UiState.Success(previousMessages)
                    _sendState.value = UiState.Error(it.message ?: "No se pudo enviar el mensaje")
                }
        }
    }

    fun clearSendState() {
        _sendState.value = UiState.Idle
    }
}
