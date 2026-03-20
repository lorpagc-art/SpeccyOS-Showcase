package com.example.speccyose5ultrav021b

import android.content.Context
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import com.example.speccyose5ultrav021b.ui.theme.NeonBlue

object ThemeManager {
    var primaryColor: Color by mutableStateOf(NeonBlue)
        private set

    val colorScheme get() = darkColorScheme(
        primary = primaryColor,
        background = Color.Black,
        surface = Color(0xFF080808)
    )

    val titleFont = FontFamily.Monospace

    /**
     * Inicializa el color desde las preferencias.
     */
    fun initialize(context: Context) {
        val settings = SettingsManager(context)
        primaryColor = Color(settings.themePrimaryColor)
    }

    /**
     * Adapta el ADN visual de Speccy OS según el sistema enfocado.
     */
    fun adaptDNA(systemId: String) {
        val newColor = when(systemId.lowercase()) {
            "nintendo", "nes", "snes", "gb", "gba", "gbc" -> Color(0xFFFF0000) // Rojo Nintendo
            "playstation", "psx", "ps2", "psp", "vita" -> Color(0xFF003791)    // Azul PlayStation
            "xbox" -> Color(0xFF107C10)                                        // Verde Xbox
            "sega", "genesis", "mastersystem", "gamegear", "dreamcast" -> Color(0xFF0080FF) // Azul Sega
            "arcade", "mame", "fbneo" -> Color(0xFFFFA500)                     // Naranja Arcade
            "coleco", "intellivision" -> Color(0xFF800080)                     // Púrpura Clásico
            "favoritos" -> Color(0xFFFFD700)                                   // Oro
            else -> NeonBlue                                                   // ADN Speccy Original
        }
        updatePrimaryColor(newColor)
    }

    fun updatePrimaryColor(color: Color) {
        primaryColor = color
    }
    
    /**
     * Guarda el color de forma persistente.
     */
    fun savePrimaryColor(context: Context, color: Color) {
        updatePrimaryColor(color)
        SettingsManager(context).themePrimaryColor = color.toArgb()
    }
}
