package com.example.oppty1024.ai_edge_mp

import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview

import com.example.oppty1024.ai_edge_mp.shared.ui.AppTheme
import com.example.oppty1024.ai_edge_mp.features.chat.ChatFeatureScreen

@Composable
fun App() {
    AppTheme {
        ChatFeatureScreen()
    }
}

@Composable
@Preview
fun AppPreview() {
    App()
} 