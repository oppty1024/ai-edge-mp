package com.example.oppty1024.ai_edge_mp.features.chat.domain

/**
 * Configuration for different LLM models that can be used for chat
 */
enum class LlmModel(
    val modelName: String,
    val localPath: String,
    val supportsThinking: Boolean,
    val temperature: Float,
    val topK: Int,
    val topP: Float,
) {
    GEMMA_3_1B_IT(
        modelName = "Gemma 3 1B IT",
        localPath = "/data/local/tmp/Gemma3-1B-IT_multi-prefill-seq_q8_ekv2048.task",
        supportsThinking = false,
        temperature = 1.0f,
        topK = 64,
        topP = 0.95f
    ),
    QWEN2_0_5B_INSTRUCT(
        modelName = "Qwen 2 0.5B Instruct",
        localPath = "/data/local/tmp/Qwen2.5-0.5B-Instruct_multi-prefill-seq_q8_ekv1280.task",
        supportsThinking = false,
        temperature = 0.95f,
        topK = 40,
        topP = 1.0f
    );

    companion object {
        /**
         * Get the default model for initial chat setup
         */
        fun getDefault(): LlmModel = GEMMA_3_1B_IT
    }
}