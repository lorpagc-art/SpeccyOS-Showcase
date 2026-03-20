package com.example.speccyose5ultrav021b

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader

class RomScanner(private val context: Context) {

    private val TAG = "RomScanner"
    private val scraperService = ScraperService(context)
    
    // Cache de configuración de sistemas desde assets
    private val systemConfigs = mutableMapOf<String, Set<String>>()
    private var allowedPlatforms = setOf<String>()
    
    // Filtro de plataformas excluidas (Herramientas, Scripts, etc.)
    private val excludedPlatforms = setOf("tools", "scripts")

    init {
        loadSystemConfigsFromAssets()
    }

    private fun loadSystemConfigsFromAssets() {
        try {
            val romsDir = "contentimg/ROMs"
            val folders = context.assets.list(romsDir) ?: return
            allowedPlatforms = folders.map { it.lowercase() }.toSet()
            
            for (folder in folders) {
                val infoFile = "$romsDir/$folder/systeminfo.txt"
                try {
                    context.assets.open(infoFile).use { inputStream ->
                        val reader = BufferedReader(InputStreamReader(inputStream))
                        var line: String?
                        var foundExtensions = false
                        val extensions = mutableSetOf<String>()
                        
                        while (reader.readLine().also { line = it } != null) {
                            if (line?.contains("Supported file extensions:", ignoreCase = true) == true) {
                                val extLine = reader.readLine() ?: ""
                                extLine.split(" ")
                                    .map { it.trim().lowercase().removePrefix(".") }
                                    .filter { it.isNotEmpty() }
                                    .forEach { extensions.add(it) }
                                foundExtensions = true
                                break
                            }
                        }
                        if (foundExtensions) {
                            systemConfigs[folder.lowercase()] = extensions
                        } else {
                            // Default extensions if not specified in systeminfo
                            systemConfigs[folder.lowercase()] = setOf("zip", "7z", "iso", "bin")
                        }
                    }
                } catch (e: Exception) {
                    // Si no hay systeminfo.txt, asignamos extensiones genéricas para que la carpeta sea válida
                    systemConfigs[folder.lowercase()] = setOf("zip", "7z", "iso", "bin")
                }
            }
            Log.d(TAG, "Loaded ${systemConfigs.size} systems. Allowed platforms: $allowedPlatforms")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading system configs", e)
        }
    }

    suspend fun scanAllRecursive(
        rootUri: String,
        onGameFound: suspend (Game) -> Unit,
        onPlatformDiscovered: suspend (String) -> Unit
    ) = coroutineScope {
        val rootDoc = try {
            DocumentFile.fromTreeUri(context, Uri.parse(rootUri))
        } catch (e: Exception) { null } 

        if (rootDoc == null || !rootDoc.canRead()) return@coroutineScope

        val rootFolders = rootDoc.listFiles()
        
        // FILTRADO ESTRICTO: Solo procesamos carpetas que existan en el listado de assets
        rootFolders.filter { it.isDirectory && it.name != null }.forEach { folder ->
            var dirName = folder.name!!.lowercase()
            
            // Remapeo de 3ds a n3ds según requerimiento del usuario
            if (dirName == "3ds") dirName = "n3ds"
            
            // Ignorar explícitamente tools y scripts
            if (dirName in excludedPlatforms) return@forEach

            if (dirName in allowedPlatforms) {
                val validExtensions = systemConfigs[dirName] ?: emptySet()
                launch(Dispatchers.IO) {
                    // Ya no llamamos a onPlatformDiscovered aquí para evitar mostrar sistemas vacíos.
                    // Se descubrirán automáticamente cuando se encuentre el primer juego.
                    scanDirectoryTurbo(folder, dirName, validExtensions, rootUri, onGameFound)
                }
            }
        }
    }

    private suspend fun scanDirectoryTurbo(
        directory: DocumentFile,
        platformId: String,
        validExtensions: Set<String>,
        rootUri: String,
        onGameFound: suspend (Game) -> Unit
    ) {
        val items = directory.listFiles()
        for (item in items) {
            val subName = item.name ?: continue
            
            if (platformId == "ports") {
                // LÓGICA ESPECIAL PORTS: Solo se listan las carpetas de su interior como juegos
                if (item.isDirectory) {
                    val game = Game(
                        title = cleanRomName(subName),
                        path = item.uri.toString(),
                        platformId = platformId,
                        extension = ".dir",
                        fileName = subName
                    )
                    val media = scraperService.findLocalMediaForGame(game, rootUri)
                    onGameFound(game.copy(boxArt = media.first, videoPreview = media.second))
                }
                continue // Ignoramos archivos sueltos en la raíz de ports si el usuario prefiere carpetas
            }

            if (item.isDirectory) {
                scanDirectoryTurbo(item, platformId, validExtensions, rootUri, onGameFound)
            } else {
                val ext = subName.substringAfterLast(".", "").lowercase()
                
                if (ext in validExtensions) {
                    val title = cleanRomName(subName.substringBeforeLast("."))
                    val game = Game(
                        title = title,
                        path = item.uri.toString(),
                        platformId = platformId,
                        extension = ".$ext",
                        fileName = subName
                    )
                    
                    val media = scraperService.findLocalMediaForGame(game, rootUri)
                    onGameFound(game.copy(boxArt = media.first, videoPreview = media.second))
                }
            }
        }
    }

    private fun cleanRomName(rawName: String): String = try {
        rawName.replace(Regex("^\\d+\\s*-\\s*"), "") 
              .replace(Regex("\\s*\\(.*?\\)"), "")
              .replace(Regex("\\s*\\[.*?\\]"), "")
              .trim()
    } catch (e: Exception) { rawName }
}
