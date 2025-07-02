package com.example.oppty1024.ai_edge_mp.features.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.oppty1024.ai_edge_mp.features.chat.data.ChatMessage
import com.example.oppty1024.ai_edge_mp.features.chat.domain.LlmModel
import com.example.oppty1024.ai_edge_mp.features.chat.presentation.ChatViewModel

/**
 * Main chat screen composable
 */
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val tokensRemaining by viewModel.tokensRemaining.collectAsState()
    val isTextInputEnabled by viewModel.isTextInputEnabled.collectAsState()
    val currentModel by viewModel.currentModel.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var userMessage by rememberSaveable { mutableStateOf("") }

    // Initialize with default model on first load
    LaunchedEffect(Unit) {
        if (currentModel == null) {
            viewModel.initializeChat(LlmModel.getDefault())
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top bar
        ChatTopBar(
            modelName = currentModel?.modelName ?: "Loading...",
            tokensRemaining = tokensRemaining,
            isEnabled = isTextInputEnabled,
            onClearChat = { viewModel.resetSession() },
            onBack = onBack
        )

        // Error message
        errorMessage?.let { error ->
            Snackbar(
                modifier = Modifier.padding(8.dp),
                action = {
                    IconButton(onClick = { viewModel.clearError() }) {
                        Text("✕")
                    }
                }
            ) {
                Text(error)
            }
        }

        // Context full warning - adopted from sample project
        if (tokensRemaining == 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Context is full! Please clear the chat to continue.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Loading indicator
        if (isLoading) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.width(8.dp))
                Text("Initializing model...")
            }
        }

        // Chat messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            reverseLayout = true
        ) {
            items(uiState.messages) { message ->
                ChatMessageItem(message = message)
            }
        }

        // Input section
        ChatInputSection(
            message = userMessage,
            onMessageChange = { 
                userMessage = it
                viewModel.recomputeTokensRemaining(it)
            },
            onSendMessage = { 
                viewModel.sendMessage(userMessage)
                userMessage = ""
            },
            isEnabled = isTextInputEnabled && currentModel != null,
            tokensRemaining = tokensRemaining
        )
    }
}

@Composable
private fun ChatTopBar(
    modelName: String,
    tokensRemaining: Int,
    isEnabled: Boolean,
    onClearChat: () -> Unit,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = modelName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = if (tokensRemaining >= 0) "$tokensRemaining tokens remaining" else "",
            style = MaterialTheme.typography.bodySmall,
            color = if (tokensRemaining < 100) Color.Red else MaterialTheme.colorScheme.onSurface
        )
        
        Row {
            IconButton(
                onClick = onClearChat,
                enabled = isEnabled
            ) {
                Text("🔄")
            }
        }
    }
}

@Composable
private fun ChatMessageItem(message: ChatMessage) {
    val backgroundColor = when {
        message.isFromUser -> MaterialTheme.colorScheme.tertiaryContainer
        message.isThinking -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    
    val bubbleShape = if (message.isFromUser) {
        RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)
    } else {
        RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
    }
    
    val horizontalAlignment = if (message.isFromUser) {
        Alignment.End
    } else {
        Alignment.Start
    }
    
    Column(
        horizontalAlignment = horizontalAlignment,
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth()
    ) {
        val author = when {
            message.isFromUser -> "You"
            message.isThinking -> "Thinking..."
            else -> "Assistant"
        }
        
        Text(
            text = author,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Row {
            BoxWithConstraints {
                Card(
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    shape = bubbleShape,
                    modifier = Modifier.widthIn(0.dp, maxWidth * 0.9f)
                ) {
                    if (message.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        Text(
                            text = message.message,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatInputSection(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isEnabled: Boolean,
    tokensRemaining: Int
) {
    Column {
        // Warning if tokens are low
        if (tokensRemaining in 1..50) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Yellow.copy(alpha = 0.2f))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Context almost full! Consider clearing chat.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column { }

            Spacer(modifier = Modifier.width(8.dp))

            // Adopted TextField design from sample project
            TextField(
                value = message,
                onValueChange = onMessageChange,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                ),
                label = { Text("Type your message...") },
                modifier = Modifier.weight(0.85f),
                enabled = isEnabled,
                maxLines = 4
            )

            IconButton(
                onClick = {
                    if (message.isNotBlank()) {
                        onSendMessage()
                    }
                },
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(0.15f),
                enabled = isEnabled && tokensRemaining > 0 && message.trim().isNotEmpty()
            ) {
                // Use cross-platform compatible arrow
                Text(
                    text = "➤",
                    color = if (isEnabled && tokensRemaining > 0 && message.trim().isNotEmpty()) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    }
                )
            }
        }
    }
} 