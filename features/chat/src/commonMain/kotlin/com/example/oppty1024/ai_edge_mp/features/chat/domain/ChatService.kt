package com.example.oppty1024.ai_edge_mp.features.chat.domain

import kotlinx.coroutines.flow.Flow

/**
 * Platform-independent interface for chat functionality
 */
interface ChatService {
    /**
     * Initialize the chat service with a specific model
     */
    suspend fun initialize(model: LlmModel)
    
    /**
     * Send a message and get streaming response
     */
    suspend fun sendMessage(message: String): Flow<String>
    
    /**
     * Estimate remaining tokens for the current context
     */
    fun estimateTokensRemaining(message: String): Int
    
    /**
     * Reset the chat session
     */
    suspend fun resetSession()
    
    /**
     * Close and cleanup resources
     */
    suspend fun close()
    
    /**
     * Check if the model file exists locally
     */
    fun isModelAvailable(model: LlmModel): Boolean
}

/**
 * Exception types for chat operations
 */
class ModelInitializationException(message: String) : Exception(message)
class ChatSessionException(message: String) : Exception(message) 