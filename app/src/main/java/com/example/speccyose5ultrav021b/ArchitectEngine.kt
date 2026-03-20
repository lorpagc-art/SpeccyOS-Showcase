package com.example.speccyose5ultrav021b

import android.content.Context
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ArchitectEngine(private val context: Context) {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = SecurityUtils.getApiKey()
    )

    data class ArchitectResponse(
        val message: String,
        val action: (() -> Unit)? = null
    )

    private val systemPrompt = """
        Eres el Arquitecto de Speccy Frontier IA, un asistente experto en emulación. 
        Solo respondes sobre:
        - Emulación en RetroArch y otros sistemas.
        - Historia de consolas y videojuegos.
        - Optimización de hardware (chipsets Unisoc, Snapdragon, etc.).
        
        REGLAS:
        - NO facilites piratería ni enlaces de descarga de ROMs.
        - NO generes imágenes ni vídeos.
        - Tono: Profesional y técnico.
        - Si la pregunta no es sobre emulación, responde educadamente que tu núcleo solo procesa datos de gaming retro.
    """.trimIndent()

    suspend fun processQuery(query: String, temp: Float, profile: String): ArchitectResponse = withContext(Dispatchers.IO) {
        val specs = HardwareControlManagerBeta.getCurrentSpecs()
        
        val q = query.lowercase()
        if (q.contains("modo rendimiento") || q.contains("máxima potencia")) {
            return@withContext ArchitectResponse(
                "Protocolo PERFORMANCE activado. El SoC operará a frecuencia máxima.",
                { HardwareControlManagerBeta.setPerformanceMode(true); HardwareControlManagerBeta.setFanSpeed(3) }
            )
        }

        return@withContext try {
            val fullPrompt = systemPrompt + "\n\nContexto Consola:\n- Modelo: " + specs + "\n- Temperatura: " + temp + "°C\n-- Perfil: " + profile + "\n\nUsuario: " + query
            
            val response = generativeModel.generateContent(fullPrompt)
            ArchitectResponse(response.text ?: "Conexión inestable. Reintenta el enlace neural.")
        } catch (e: Exception) {
            ArchitectResponse("ERROR DE RED: El núcleo central no responde. Comprueba tu conexión a Internet o tu API Key.")
        }
    }
}
