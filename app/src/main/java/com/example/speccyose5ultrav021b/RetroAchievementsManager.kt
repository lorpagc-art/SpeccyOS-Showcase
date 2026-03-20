package com.example.speccyose5ultrav021b

import android.content.Context
import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class RetroAchievementsManager(private val context: Context, private val settingsManager: SettingsManager) {
    private val client = OkHttpClient()
    private val BASE_URL = "https://retroachievements.org/API/"

    /**
     * Realiza el login en RetroAchievements para obtener el Token de API.
     */
    fun login(user: String, pass: String, onResult: (Boolean, String) -> Unit) {
        val url = "${BASE_URL}API_Login.php?u=$user&p=$pass"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult(false, "Error de red")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (body != null) {
                    try {
                        val json = JSONObject(body)
                        val success = json.optBoolean("Success", false)
                        if (success) {
                            val token = json.optString("Token", "")
                            settingsManager.raUsername = user
                            settingsManager.raToken = token
                            onResult(true, "Conectado")
                        } else {
                            onResult(false, "Credenciales incorrectas")
                        }
                    } catch (e: Exception) {
                        onResult(false, "Error de respuesta")
                    }
                } else {
                    onResult(false, "Respuesta vacía")
                }
            }
        })
    }

    fun getSummary(onResult: (String) -> Unit) {
        val user = settingsManager.raUsername
        val token = settingsManager.raToken
        if (user.isEmpty() || token.isEmpty()) {
            onResult("No conectado")
            return
        }

        // Nota: Para UserSummary suele requerirse la Web API Key, pero probamos con el Token de Login
        val url = "${BASE_URL}API_GetUserSummary.php?u=$user&y=$token&z=$user"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult("Error de conexión")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                try {
                    val json = JSONObject(body)
                    val points = json.optString("TotalPoints", "0")
                    val rank = json.optString("Rank", "N/A")
                    onResult("Puntos: $points | Rango: $rank")
                } catch (e: Exception) {
                    onResult("Error al leer datos")
                }
            }
        })
    }
}
