package com.example.oppty1024.ai_edge_mp.features.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oppty1024.ai_edge_mp.features.chat.data.ChatMessage
import com.example.oppty1024.ai_edge_mp.features.chat.data.ChatUiState
import com.example.oppty1024.ai_edge_mp.features.chat.domain.ChatService
import com.example.oppty1024.ai_edge_mp.features.chat.domain.LlmModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max

/**
 * ViewModel for managing chat interactions and UI state
 */
class ChatViewModel(
    private val chatService: ChatService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _tokensRemaining = MutableStateFlow(-1)
    val tokensRemaining: StateFlow<Int> = _tokensRemaining.asStateFlow()

    private val _isTextInputEnabled = MutableStateFlow(false)
    val isTextInputEnabled: StateFlow<Boolean> = _isTextInputEnabled.asStateFlow()

    private val _currentModel = MutableStateFlow<LlmModel?>(null)
    val currentModel: StateFlow<LlmModel?> = _currentModel.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Initialize the chat with a specific model
     */
    fun initializeChat(model: LlmModel) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                if (!chatService.isModelAvailable(model)) {
                    _errorMessage.value = "Model file not found: ${model.localPath}"
                    _isLoading.value = false
                    return@launch
                }
                
                chatService.initialize(model)
                _currentModel.value = model
                _uiState.value = ChatUiState(supportsThinking = model.supportsThinking)
                _isTextInputEnabled.value = true
                _tokensRemaining.value = estimateTokensRemaining("")
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to initialize model: ${e.message}"
                _isTextInputEnabled.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Send a message to the chat
     */
    fun sendMessage(userMessage: String) {
        if (userMessage.trim().isEmpty()) return
        
        viewModelScope.launch(Dispatchers.Default) {
            try {
                // Add user message
                _uiState.value.addMessage(userMessage, ChatMessage.USER_PREFIX)
                
                // Create loading message for model response
                _uiState.value.createLoadingMessage()
                
                _isTextInputEnabled.value = false
                
                chatService.sendMessage(userMessage)
                    .catch { error ->
                        _uiState.value.addMessage(
                            error.message ?: "Unknown error occurred",
                            ChatMessage.MODEL_PREFIX
                        )
                        _isTextInputEnabled.value = true
                    }
                    .collect { partialResponse ->
                        _uiState.value.appendMessage(partialResponse)
                        // Reduce current token count estimate only occasionally
                        _tokensRemaining.update { max(0, it - 1) }
                    }
                
                // Re-enable input and recompute tokens when done
                _isTextInputEnabled.value = true
                recomputeTokensRemaining(userMessage)
                
            } catch (e: Exception) {
                _uiState.value.addMessage(
                    e.message ?: "Failed to send message",
                    ChatMessage.MODEL_PREFIX
                )
                _isTextInputEnabled.value = true
            }
        }
    }

    /**
     * Recompute the remaining token count
     */
    fun recomputeTokensRemaining(message: String) {
        val remainingTokens = estimateTokensRemaining(message)
        _tokensRemaining.value = remainingTokens
    }

    private fun estimateTokensRemaining(message: String): Int {
        return try {
            chatService.estimateTokensRemaining(message)
        } catch (e: Exception) {
            -1
        }
    }

    /**
     * Reset the chat session
     */
    fun resetSession() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                chatService.resetSession()
                _uiState.value.clearMessages()
                _tokensRemaining.value = estimateTokensRemaining("")
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to reset session: ${e.message}"
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            chatService.close()
        }
    }
} 