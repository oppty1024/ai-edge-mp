package com.example.oppty1024.ai_edge_mp.features.chat.data

import android.content.Context
import android.util.Log
import com.example.oppty1024.ai_edge_mp.features.chat.domain.ChatService
import com.example.oppty1024.ai_edge_mp.features.chat.domain.ChatSessionException
import com.example.oppty1024.ai_edge_mp.features.chat.domain.LlmModel
import com.example.oppty1024.ai_edge_mp.features.chat.domain.ModelInitializationException
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File
import kotlin.math.max

/**
 * Android implementation of ChatService using MediaPipe LLM Inference
 */
class AndroidChatService(
    private val context: Context
) : ChatService {
    
    private var llmInference: LlmInference? = null
    private var llmInferenceSession: LlmInferenceSession? = null
    private var currentModel: LlmModel? = null

    companion object {
        private const val TAG = "AndroidChatService"
        private const val MAX_TOKENS = 1024
        private const val DECODE_TOKEN_OFFSET = 256
    }

    override suspend fun initialize(model: LlmModel) {
        try {
            Log.d(TAG, "Initializing model: ${model.modelName}")
            
            // Close previous session if exists
            close()
            
            // Create LLM Inference options
            val inferenceOptions = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(model.localPath)
                .setMaxTokens(MAX_TOKENS)
                .build()

            // Create LLM Inference instance
            llmInference = LlmInference.createFromOptions(context, inferenceOptions)
            
            // Create session with model parameters
            val sessionOptions = LlmInferenceSession.LlmInferenceSessionOptions.builder()
                .setTemperature(model.temperature)
                .setTopK(model.topK)
                .setTopP(model.topP)
                .build()

            llmInferenceSession = LlmInferenceSession.createFromOptions(
                llmInference!!, 
                sessionOptions
            )
            
            currentModel = model
            Log.d(TAG, "Model initialized successfully: ${model.modelName}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize model: ${model.modelName}", e)
            throw ModelInitializationException("Failed to initialize model: ${e.message}")
        }
    }

    override suspend fun sendMessage(message: String): Flow<String> = callbackFlow {
        val session = llmInferenceSession 
            ?: throw ChatSessionException("Chat session not initialized")

        try {
            session.addQueryChunk(message)
            
            // Implement proper streaming response using callbackFlow
            val asyncInference = session.generateResponseAsync { partialResult, done ->
                try {
                    // Emit each partial result immediately for streaming
                    if (partialResult.isNotEmpty()) {
                        trySend(partialResult).getOrThrow()
                    }
                    
                    if (done) {
                        // Close the flow when inference is complete
                        close()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in inference callback", e)
                    close(e)
                }
            }
            
            // Wait for the flow to complete or be cancelled
            awaitClose {
                try {
                    asyncInference.cancel(true)
                } catch (e: Exception) {
                    Log.w(TAG, "Error cancelling inference", e)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message", e)
            close(ChatSessionException("Failed to send message: ${e.message}"))
        }
    }

    override fun estimateTokensRemaining(message: String): Int {
        val session = llmInferenceSession ?: return -1
        
        return try {
            // Use MediaPipe's built-in token counting for accurate estimation
            val context = message // For full context, you'd include conversation history
            if (context.isEmpty()) return -1 // Special marker if no content has been added

            val sizeOfAllMessages = session.sizeInTokens(context)
            val approximateControlTokens = 3 // Estimate for control tokens per message
            val remainingTokens = MAX_TOKENS - sizeOfAllMessages - approximateControlTokens - DECODE_TOKEN_OFFSET
            
            // Token size is approximate so, let's not return anything below 0
            max(0, remainingTokens)
        } catch (e: Exception) {
            Log.e(TAG, "Error estimating tokens", e)
            -1
        }
    }

    override suspend fun resetSession() {
        try {
            llmInferenceSession?.close()
            
            val model = currentModel ?: return
            
            // Recreate session with same model
            val sessionOptions = LlmInferenceSession.LlmInferenceSessionOptions.builder()
                .setTemperature(model.temperature)
                .setTopK(model.topK)
                .setTopP(model.topP)
                .build()

            llmInferenceSession = LlmInferenceSession.createFromOptions(
                llmInference!!, 
                sessionOptions
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting session", e)
            throw ChatSessionException("Failed to reset session: ${e.message}")
        }
    }

    override suspend fun close() {
        try {
            llmInferenceSession?.close()
            llmInference?.close()
            llmInferenceSession = null
            llmInference = null
            currentModel = null
        } catch (e: Exception) {
            Log.e(TAG, "Error closing chat service", e)
        }
    }

    override fun isModelAvailable(model: LlmModel): Boolean {
        return File(model.localPath).exists()
    }
} 