package com.example.oppty1024.ai_edge_mp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform