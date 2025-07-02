package com.example.oppty1024.ai_edge_mp.features.chat

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.oppty1024.ai_edge_mp.features.chat.data.AndroidChatService
import com.example.oppty1024.ai_edge_mp.features.chat.domain.ChatService

/**
 * Android Context holder for non-Composable access
 * Follows KMP pattern: minimal platform-specific code
 */
object AndroidContextProvider {
    private var applicationContext: Context? = null
    
    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }
    
    internal fun getContext(): Context = applicationContext 
        ?: error("AndroidContextProvider not initialized. Call initialize(context) first.")
}

/**
 * Android-specific ChatService factory
 * Uses previously initialized application context
 */
actual fun createPlatformChatService(): ChatService {
    val context = AndroidContextProvider.getContext()
    return AndroidChatService(context)
}

/**
 * Platform initialization for Android
 * Auto-initializes context for ChatService
 */
@Composable
actual fun PlatformInitialization() {
    val context = LocalContext.current
    AndroidContextProvider.initialize(context)
}
 