package com.example.oppty1024.ai_edge_mp.features.chat

import androidx.compose.runtime.Composable
import com.example.oppty1024.ai_edge_mp.features.chat.data.IosChatService
import com.example.oppty1024.ai_edge_mp.features.chat.domain.ChatService

/**
 * iOS-specific ChatService factory
 * Simple implementation without context dependencies
 */
actual fun createPlatformChatService(): ChatService {
    return IosChatService()
}

/**
 * Platform initialization for iOS
 * No special setup required
 */
@Composable
actual fun PlatformInitialization() {
    // No initialization needed for iOS
}
