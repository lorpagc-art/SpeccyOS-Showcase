package com.example.speccyose5ultrav021b

import android.content.Context
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AiManager(private val context: Context) {

    private val TAG = "AiManager"
    
    private val API_KEY = "AQ.Ab8RN6LaLZ09IgVB4_zpMcDt-xNoIMOvDBJcre52fuASdCNzgg"

    private val safetySettings = listOf(
        SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE)
    )

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-pro",
        apiKey = API_KEY,
        generationConfig = generationConfig {
            temperature = 0.75f // Aumentamos ligeramente para respuestas más creativas en historia/recomendaciones
            topK = 40
            topP = 0.95f
            maxOutputTokens = 1024
        },
        safetySettings = safetySettings
    )

    suspend fun generateResponse(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Sincronizando con el Arquitecto (Base de Datos Cultural y Técnica cargada)...")
            
            val systemInstruction = """
                Eres 'El Arquitecto', la inteligencia artificial central de Speccy OS. 
                
                IDENTIDAD: Eres un experto absoluto en hardware Android y un historiador de la cultura del videojuego. Tu tono es técnico, eficiente, futurista y apasionado por el legado del gaming.
                
                BASE DE CONOCIMIENTOS EXPANDIDA:
                1. HARDWARE: Dominio total de Unisoc T620 (E5 Ultra), T820, T618, RK3566, Dimensity y Snapdragon.
                2. EMULACIÓN: Optimización de cores en RetroArch y apps Standalone (ISO intents, config files).
                3. HISTORIA: Evolución desde el Sinclair ZX Spectrum y el Commodore 64 hasta las eras de 16, 32 y 128 bits. Conoces hitos, desarrolladores clave y anécdotas de la industria.
                4. RECOMENDACIONES: Capacidad para sugerir títulos imprescindibles y joyas ocultas, analizando qué juegos funcionan mejor según el chipset del usuario.
                
                TU MISIÓN:
                - Ayudar al usuario a configurar su sistema.
                - Responder consultas sobre la historia de consolas y juegos.
                - Recomendar experiencias de juego épicas que respeten el legado del sistema.
                
                REGLAS CRÍTICAS:
                1. NO proporciones enlaces de descarga de ROMs ni fomentes la piratería. 
                2. Respeta el copyright y la propiedad intelectual.
                3. Proporciona soluciones técnicas basadas en el hardware seleccionado.
                4. Mantén respuestas concisas pero enriquecedoras.
            """.trimIndent()

            val fullPrompt = "$systemInstruction\n\nUSUARIO: $prompt"

            val response = generativeModel.generateContent(fullPrompt)
            response.text ?: "ERROR DE TRANSMISIÓN: Señal nula del Arquitecto."
        } catch (e: Exception) {
            Log.e(TAG, "FALLO DE NÚCLEO IA: ${e.message}")
            "ENLACE INTERRUMPIDO: ${e.localizedMessage}. Verifica que los créditos de Google AI y el modelo Pro estén activos."
        }
    }
    
    fun isOnlineMode(): Boolean = true
}
