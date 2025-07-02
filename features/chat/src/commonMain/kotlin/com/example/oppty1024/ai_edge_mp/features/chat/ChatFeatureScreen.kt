package com.example.oppty1024.ai_edge_mp.features.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.oppty1024.ai_edge_mp.features.chat.domain.ChatService
import com.example.oppty1024.ai_edge_mp.features.chat.presentation.ChatViewModel
import com.example.oppty1024.ai_edge_mp.features.chat.ui.ChatScreen

/**
 * Chat feature entry point
 */
@Composable
fun ChatFeatureScreen() {
    PlatformInitialization()
    
    val chatService = remember { createPlatformChatService() }
    val viewModel = remember(chatService) { ChatViewModel(chatService) }
    
    ChatScreen(
        viewModel = viewModel,
        onBack = { /* TODO: Handle navigation */ }
    )
}

/**
 * Platform-specific ChatService factory
 */
expect fun createPlatformChatService(): ChatService

/**
 * Platform-specific initialization
 * Handles any required setup (e.g., Android context)
 */
@Composable
expect fun PlatformInitialization() 