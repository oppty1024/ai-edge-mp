package com.example.oppty1024.ai_edge_mp.shared.core

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform 