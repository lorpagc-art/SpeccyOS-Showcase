package com.example.speccyose5ultrav021b

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speccyose5ultrav021b.ui.theme.NeonBlue

@Composable
fun ArchitectChatScreen(
    mainViewModel: MainViewModel, 
    hardwareViewModel: HardwareViewModel, 
    onSafeClose: () -> Unit
) {
    val chatMessages by mainViewModel.chatMessages.collectAsState()
    val isAiThinking by mainViewModel.isAiThinking.collectAsState()

    var messageText by remember { mutableStateOf("") }
    
    val aiManager = mainViewModel.aiManager

    LaunchedEffect(Unit) {
        if (chatMessages.isEmpty()) {
            mainViewModel.addChatMessage(ChatMessage("CONEXIÓN NEURAL ESTABLECIDA. Soy el Arquitecto. ¿En qué puedo optimizar tu experiencia hoy?", isUser = false))
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("ARCHITECT IA", fontSize = 24.sp, fontWeight = FontWeight.Black, color = ThemeManager.primaryColor, letterSpacing = 4.sp)
                    Text("NÚCLEO ACTIVO // STATUS: STANDBY", fontSize = 10.sp, color = Color.Green, fontFamily = FontFamily.Monospace)
                }
                IconButton(onClick = onSafeClose) {
                    Icon(Icons.Default.Close, null, tint = Color.Gray)
                }
            }

            Spacer(Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.2f)))

            LazyColumn(
                modifier = Modifier.weight(1f).padding(vertical = 16.dp),
                reverseLayout = true,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(chatMessages) { msg ->
                    ChatBubble(message = msg, isAi = !msg.isUser) 
                }
                if (isAiThinking) {
                    item { 
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = ThemeManager.primaryColor, strokeWidth = 2.dp)
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                TextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f).border(BorderStroke(1.dp, ThemeManager.primaryColor.copy(alpha = 0.5f)), RoundedCornerShape(12.dp)),
                    placeholder = { Text("Consultar al Arquitecto...", color = Color.Gray) },
                    enabled = !isAiThinking,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF111111),
                        unfocusedContainerColor = Color(0xFF111111),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.width(12.dp))
                FloatingActionButton(
                    onClick = {
                        if (messageText.isNotBlank() && !isAiThinking) {
                            val userMsg = messageText
                            mainViewModel.addChatMessage(ChatMessage(userMsg, isUser = true))
                            messageText = ""
                            mainViewModel.generateAiResponse(userMsg, aiManager) 
                        }
                    },
                    containerColor = if (isAiThinking) Color.DarkGray else ThemeManager.primaryColor,
                    contentColor = Color.Black,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, isAi: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isAi) Alignment.Start else Alignment.End
    ) {
        Surface(
            color = if (isAi) ThemeManager.primaryColor.copy(alpha = 0.1f) else Color.DarkGray.copy(alpha = 0.5f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, if (isAi) ThemeManager.primaryColor.copy(alpha = 0.5f) else Color.Gray)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                fontSize = 14.sp,
                color = if (isAi) ThemeManager.primaryColor else Color.White
            )
        }
    }
}
