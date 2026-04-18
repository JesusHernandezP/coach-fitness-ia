package com.fitnessaicoach.app.data.repository

import com.fitnessaicoach.app.data.network.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(private val api: ApiService) {

    suspend fun getConversations(): Result<List<Conversation>> =
        runCatching { api.getConversations() }

    suspend fun createConversation(): Result<Conversation> =
        runCatching { api.createConversation() }

    suspend fun getMessages(conversationId: Long): Result<List<ChatMessageDto>> =
        runCatching { api.getMessages(conversationId) }

    suspend fun sendMessage(conversationId: Long, content: String): Result<ChatMessageDto> =
        runCatching { api.sendMessage(conversationId, SendMessageRequest(content)) }
}
