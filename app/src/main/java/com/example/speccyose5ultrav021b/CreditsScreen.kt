package com.example.speccyose5ultrav021b

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun CreditsScreen() {
    val scrollState = rememberScrollState()
    val primaryColor = ThemeManager.primaryColor
    
    LaunchedEffect(Unit) {
        delay(1000)
        while (true) {
            scrollState.animateScrollTo(
                value = scrollState.maxValue,
                animationSpec = tween(durationMillis = 50000, easing = LinearEasing)
            )
            delay(2000)
            scrollState.scrollTo(0)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black).padding(horizontal = 32.dp)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(300.dp))

            Text(
                text = "GENERACIÓN ARCADE IA",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = primaryColor,
                letterSpacing = 6.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = "LA CÚSPIDE DE LA EMULACIÓN",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Generación Arcade IA es el resultado de una ingeniería dedicada a la preservación y optimización del gaming retro. Hemos fusionado el control total del hardware con la inteligencia de vanguardia.",
                fontSize = 14.sp, color = Color.LightGray, textAlign = TextAlign.Center, lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text("CRÉDITOS DE ARQUITECTURA", fontWeight = FontWeight.Bold, color = primaryColor)
            Spacer(modifier = Modifier.height(24.dp))

            CreditSection("Alekfull NX", "Por el diseño minimalista que redefine la navegación en consolas portátiles.")
            CreditSection("Diamond & Comic Themes", "Por inspirar la versatilidad visual y el dinamismo de nuestra interfaz.")
            CreditSection("Daijishou & ES-DE", "Las bases maestras sobre las que hemos construido este nuevo estándar.")
            CreditSection("RetroArch Core Team", "Por el motor incansable que impulsa cada píxel de nostalgia.")
            CreditSection("GammaOS", "Por enseñarnos a domar los chipsets Unisoc de alto rendimiento.")
            
            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = "NUEVA ERA: SISTEMA DE TEMAS DINÁMICO",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = "Implementación de descarga e importación de skins universales finalizada.",
                fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(100.dp))

            Text(
                text = "DESARROLLADO CON PASIÓN POR LA COMUNIDAD\nGENERACIÓN ARCADE - ULTIMATE EDITION",
                fontSize = 10.sp, color = Color.DarkGray, textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(300.dp))
        }
    }
}

@Composable
fun CreditSection(title: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title.uppercase(), fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color.White)
        Text(description, fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 450.dp))
    }
}