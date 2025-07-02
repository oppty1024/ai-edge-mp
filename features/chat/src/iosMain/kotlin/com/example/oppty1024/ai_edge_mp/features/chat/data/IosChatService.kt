package com.example.oppty1024.ai_edge_mp.features.chat.data

import com.example.oppty1024.ai_edge_mp.features.chat.domain.ChatService
import com.example.oppty1024.ai_edge_mp.features.chat.domain.LlmModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * iOS implementation of ChatService using MediaPipe LLM Inference
 * TODO: Implement actual iOS MediaPipe integration
 */
class IosChatService : ChatService {

    override suspend fun initialize(model: LlmModel) {
        // TODO: Implement iOS MediaPipe LLM initialization
        throw NotImplementedError("iOS implementation will be added later")
    }

    override suspend fun sendMessage(message: String): Flow<String> = flow {
        // TODO: Implement iOS message sending
        throw NotImplementedError("iOS implementation will be added later")
    }

    override fun estimateTokensRemaining(message: String): Int {
        // TODO: Implement iOS token estimation
        return -1
    }

    override suspend fun resetSession() {
        // TODO: Implement iOS session reset
    }

    override suspend fun close() {
        // TODO: Implement iOS cleanup
    }

    override fun isModelAvailable(model: LlmModel): Boolean {
        // TODO: Implement iOS model availability check
        return false
    }
} 