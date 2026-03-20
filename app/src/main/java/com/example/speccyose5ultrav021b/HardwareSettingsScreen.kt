package com.example.speccyose5ultrav021b

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.speccyose5ultrav021b.ui.theme.NeonBlue

@Composable
fun HardwareSettingsScreen(viewModel: HardwareViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("ESTADO DEL SISTEMA", color = NeonBlue, fontSize = 20.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(24.dp))

        // Monitor de Temperatura
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111111))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("TEMPERATURA SOC", color = Color.Gray, fontSize = 12.sp)
                    Text("${state.temperature.toInt()}°C", color = if (state.temperature > 65) Color.Red else Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                CircularProgressIndicator(
                    progress = { (state.temperature / 100f).coerceIn(0f, 1f) },
                    color = if (state.temperature > 65) Color.Red else NeonBlue,
                    trackColor = Color.DarkGray
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Perfiles de Rendimiento
        Text("PERFILES DE NÚCLEO", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        
        val profiles = listOf("ECO", "BALANCED", "PERFORMANCE", "EXTREME")
        profiles.forEach { profile ->
            val isSelected = state.currentProfile == profile
            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                onClick = { viewModel.setManualProfile(profile) },
                color = if (isSelected) NeonBlue.copy(alpha = 0.2f) else Color(0xFF111111),
                shape = RoundedCornerShape(8.dp),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, NeonBlue) else null
            ) {
                Text(profile, modifier = Modifier.padding(16.dp), color = if (isSelected) NeonBlue else Color.White, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}
