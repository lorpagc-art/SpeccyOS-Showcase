package com.example.speccyose5ultrav021b.network

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.example.speccyose5ultrav021b.Game
import com.example.speccyose5ultrav021b.RetroArchDatabase
import com.example.speccyose5ultrav021b.SettingsManager
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream

class OnlineScraperService(private val context: Context, private val settingsManager: SettingsManager) {

    private val TAG = "OnlineScraperService"
    private val httpClient = OkHttpClient()

    // Mapping for TheGamesDB (Numeric IDs)
    private val platformNameToIdTGDB = mapOf(
        "nes" to 1, "snes" to 2, "n64" to 3, "gc" to 4, "wii" to 9,
        "gb" to 5, "gbc" to 6, "gba" to 7, "nds" to 8, "n3ds" to 38,
        "psx" to 10, "ps2" to 11, "psp" to 13, "dreamcast" to 21,
        "genesis" to 15, "megadrive" to 15, "sms" to 16, "gg" to 17,
        "saturn" to 20, "mame" to 23, "arcade" to 23, "neogeo" to 24,
        "cps1" to 23, "cps2" to 23, "cps3" to 23, "zxspectrum" to 46,
        "amiga" to 32, "amstradcpc" to 48, "atari2600" to 25, "msx" to 42,
        "3do" to 28, "atari7800" to 27, "pce" to 34, "ngp" to 39, "wswan" to 41
    )

    // Mapping for ScreenScraper (Numeric IDs)
    private val platformNameToIdSS = mapOf(
        "nes" to 3, "snes" to 4, "n64" to 14, "gc" to 13, "wii" to 16,
        "gb" to 9, "gbc" to 10, "gba" to 12, "nds" to 15, "n3ds" to 17,
        "psx" to 57, "ps2" to 58, "psp" to 61, "dreamcast" to 23,
        "genesis" to 1, "megadrive" to 1, "sms" to 2, "gg" to 21,
        "saturn" to 22, "mame" to 75, "arcade" to 75, "neogeo" to 142,
        "zxspectrum" to 76, "amiga" to 64, "amstradcpc" to 65, "atari2600" to 26, "msx" to 113,
        "3do" to 29, "atari7800" to 28, "pce" to 31, "ngp" to 40, "wswan" to 45
    )

    suspend fun scrapeGame(game: Game): Game {
        return try {
            val source = settingsManager.scraperSource
            Log.d(TAG, "Iniciando scraping para ${game.title} usando $source")
            when (source) {
                "SCREENSCRAPER" -> scrapeFromScreenScraper(game)
                "THEGAMESDB" -> scrapeFromTheGamesDB(game)
                else -> scrapeFromScreenScraper(game)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scraping online para ${game.title}", e)
            game
        }
    }

    private suspend fun scrapeFromScreenScraper(game: Game): Game {
        val user = settingsManager.scraperUsername
        val pass = settingsManager.scraperPassword
        
        val systemInfo = RetroArchDatabase.findSystemById(game.platformId)
        val platformString = systemInfo?.scrapingPlatform ?: game.platformId.lowercase()
        val systemId = platformNameToIdSS[platformString]

        Log.d(TAG, "Consultando ScreenScraper para: ${game.fileName} (SystemID: $systemId)")

        val response = try {
            ScreenScraperRetrofitInstance.api.getGameInfo(
                devId = ScreenScraperRetrofitInstance.DEV_ID,
                devPassword = ScreenScraperRetrofitInstance.DEV_PASSWORD,
                softwareName = ScreenScraperRetrofitInstance.SOFTWARE_NAME,
                user = user,
                pass = pass,
                romName = game.fileName,
                systemId = systemId
            )
        } catch (e: Exception) {
            Log.e(TAG, "Network error in ScreenScraper", e)
            null
        }

        if (response?.isSuccessful == true && response.body() != null) {
            val gameData = response.body()?.response?.game
            if (gameData != null) {
                val description = gameData.synopsis?.find { it.language == "es" }?.text 
                                ?: gameData.synopsis?.find { it.language == "en" }?.text
                                ?: gameData.synopsis?.firstOrNull()?.text

                // Prefer 'box-2D' with region 'eu' or 'us'
                val media = gameData.medias?.find { it.type == "box-2D" && (it.region == "eu" || it.region == "us" || it.region == "ss") }
                          ?: gameData.medias?.find { it.type == "box-2D" }
                          ?: gameData.medias?.find { it.type == "box-3D" }
                          ?: gameData.medias?.find { it.type == "screenshot" }
                
                var localArtUri: String? = game.boxArt
                if (media?.url != null) {
                    val downloadedUri = downloadImageToSystemFolder(game, media.url)
                    if (downloadedUri != null) {
                        localArtUri = downloadedUri
                    }
                }

                return game.copy(
                    description = description ?: game.description,
                    developer = gameData.developer?.text ?: game.developer,
                    boxArt = localArtUri
                )
            } else {
                Log.w(TAG, "ScreenScraper: No se encontró información para ${game.fileName}")
            }
        } else {
            Log.e(TAG, "ScreenScraper Error: ${response?.code()} - ${response?.errorBody()?.string()}")
        }
        // Fallback to TGDB if SS fails or returns nothing
        return scrapeFromTheGamesDB(game)
    }

    private suspend fun scrapeFromTheGamesDB(game: Game): Game {
        val apiKey = settingsManager.scraperApiKey.ifEmpty { RetrofitInstance.API_KEY }
        val rawName = game.title
        
        val systemInfo = RetroArchDatabase.findSystemById(game.platformId)
        val platformString = systemInfo?.scrapingPlatform ?: game.platformId.lowercase()
        val platformIdNum = platformNameToIdTGDB[platformString]

        Log.d(TAG, "Consultando TheGamesDB para: $rawName (ID: $platformIdNum)")
        
        val response = try {
            RetrofitInstance.api.searchGameByName(
                apiKey = apiKey, 
                name = rawName, 
                platformId = platformIdNum
            )
        } catch (e: Exception) {
            Log.e(TAG, "Network error in TGDB", e)
            null
        }

        if (response?.isSuccessful == true && response.body() != null) {
            val games = response.body()?.data?.games
            if (!games.isNullOrEmpty()) {
                val gameData = games.first()
                
                val imgResponse = try {
                    RetrofitInstance.api.getGameImages(apiKey = apiKey, gameId = gameData.id)
                } catch (e: Exception) { null }

                var remoteUrl: String? = null
                if (imgResponse?.isSuccessful == true && imgResponse.body() != null) {
                    val imgData = imgResponse.body()?.data
                    val images = imgData?.images?.get(gameData.id.toString())
                    
                    val image = images?.find { it.type == "boxart" && it.side == "front" } 
                             ?: images?.find { it.type == "boxart" }
                             ?: images?.find { it.type == "fanart" }
                    
                    if (image != null && imgData != null) {
                        remoteUrl = "${imgData.baseUrl.original}${image.filename}"
                    }
                }

                var localArtUri: String? = game.boxArt
                if (remoteUrl != null) {
                    val downloadedUri = downloadImageToSystemFolder(game, remoteUrl)
                    if (downloadedUri != null) {
                        localArtUri = downloadedUri
                    }
                }

                return game.copy(
                    description = gameData.overview ?: game.description,
                    releaseDate = gameData.releaseDate ?: game.releaseDate,
                    boxArt = localArtUri
                )
            }
        }
        return game
    }

    private fun downloadImageToSystemFolder(game: Game, url: String): String? {
        return try {
            val romsRootUriStr = settingsManager.romsLocation
            if (romsRootUriStr.isEmpty()) {
                Log.w(TAG, "No se puede descargar: romsLocation está vacío")
                return null
            }
            
            val rootDoc = DocumentFile.fromTreeUri(context, Uri.parse(romsRootUriStr)) ?: return null
            
            // Navigate to [platform]/media/covers/ case-insensitively
            val platformFolder = findFileCaseInsensitive(rootDoc, game.platformId) 
                                 ?: rootDoc.createDirectory(game.platformId) ?: return null
            
            var mediaFolder = findFileCaseInsensitive(platformFolder, "media")
            if (mediaFolder == null || !mediaFolder.isDirectory) {
                mediaFolder = platformFolder.createDirectory("media")
            }
            if (mediaFolder == null) return null

            var coversFolder = findFileCaseInsensitive(mediaFolder, "covers")
            if (coversFolder == null || !coversFolder.isDirectory) {
                coversFolder = mediaFolder.createDirectory("covers")
            }
            val targetDir = coversFolder ?: mediaFolder

            val sanitizedTitle = sanitizeFileName(game.title)
            val fileName = "$sanitizedTitle.jpg"
            
            var targetFile = targetDir.findFile(fileName)
            if (targetFile == null) {
                targetFile = targetDir.createFile("image/jpeg", fileName)
            }
            if (targetFile == null) {
                Log.e(TAG, "No se pudo crear el archivo de destino: $fileName")
                return null
            }

            Log.d(TAG, "Descargando imagen desde $url a ${targetFile.uri}")
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val inputStream: InputStream = response.body.byteStream()
                context.contentResolver.openOutputStream(targetFile.uri)?.use { output ->
                    inputStream.copyTo(output)
                }
                Log.d(TAG, "Imagen guardada exitosamente: ${targetFile.uri}")
                targetFile.uri.toString()
            } else {
                Log.e(TAG, "Error en descarga: ${response.code} - ${response.message}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fallo al descargar boxart para ${game.title}", e)
            null
        }
    }

    private fun findFileCaseInsensitive(parent: DocumentFile, name: String): DocumentFile? {
        return parent.findFile(name) ?: parent.listFiles().find { it.name?.equals(name, ignoreCase = true) == true }
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|]"), "_")
    }
}
