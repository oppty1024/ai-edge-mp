package com.example.oppty1024.ai_edge_mp.features.greeting

import com.example.oppty1024.ai_edge_mp.shared.core.getPlatform

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return "Hello, ${platform.name}!"
    }
} 