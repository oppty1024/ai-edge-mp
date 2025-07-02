package com.example.oppty1024.ai_edge_mp.features.chat.data

import kotlin.random.Random

/**
 * Represents a chat message in the conversation
 */
data class ChatMessage(
    val id: String = generateUniqueId(),
    val rawMessage: String = "",
    val author: String,
    val isLoading: Boolean = false,
    val isThinking: Boolean = false,
) {
    val isEmpty: Boolean
        get() = rawMessage.trim().isEmpty()
    
    val isFromUser: Boolean
        get() = author == USER_PREFIX
    
    val message: String
        get() = rawMessage.trim()
    
    companion object {
        const val USER_PREFIX = "user"
        const val MODEL_PREFIX = "model"
        const val THINKING_MARKER_END = "</think>"
        
        private fun generateUniqueId(): String {
            return "${Random.nextLong()}_${Random.nextInt(1000, 9999)}"
        }
    }
} 