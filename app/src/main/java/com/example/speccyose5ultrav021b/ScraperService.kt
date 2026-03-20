package com.example.speccyose5ultrav021b

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import java.io.File

class ScraperService(private val context: Context) {

    private val TAG = "ScraperService"
    
    // Extensiones soportadas
    private val IMG_EXTENSIONS = listOf(".png", ".jpg", ".jpeg", ".webp")
    private val VID_EXTENSIONS = listOf(".mp4", ".mkv", ".avi", ".webm")

    // Carpetas estándar (ES-DE style y personalizadas)
    private val IMG_SUBFOLDERS = listOf("images", "box2d", "box3d", "screenshots", "mixrb", "covers")
    private val VID_SUBFOLDERS = listOf("videos", "previews", "movies")

    /**
     * Busca media local para un juego.
     * Busca en:
     * 1. [roms]/[platform]/media/[type]/[game].[ext]
     * 2. [roms]/media/[platform]/[type]/[game].[ext] (ES-DE style)
     */
    fun findLocalMediaForGame(game: Game, romsRootUri: String?): Pair<String?, String?> {
        var boxArt: String? = null
        var video: String? = null

        try {
            val platformId = game.platformId.lowercase()
            val gameFileName = game.fileName
            val rawName = gameFileName.substringBeforeLast(".")
            
            // Traducir la URI del juego a ruta real si es posible
            val gameRealPath = translateSafToRealPath(game.path) ?: return Pair(null, null)
            val gameFile = File(gameRealPath)
            val platformDir = gameFile.parentFile ?: return Pair(null, null)
            val romsRootDir = if (romsRootUri != null) File(translateSafToRealPath(romsRootUri) ?: "") else null

            // --- BUSCAR IMAGEN ---
            val imageBaseDirs = mutableListOf<File>()
            imageBaseDirs.add(File(platformDir, "media")) // [roms]/[platform]/media/
            if (romsRootDir != null) {
                imageBaseDirs.add(File(romsRootDir, "media/$platformId")) // [roms]/media/[platform]/
                imageBaseDirs.add(File(romsRootDir, "ES-DE/downloaded_media/$platformId")) // Soporte ES-DE real
            }
            
            boxArt = searchMedia(imageBaseDirs, IMG_SUBFOLDERS, rawName, IMG_EXTENSIONS)

            // --- BUSCAR VIDEO ---
            val videoBaseDirs = mutableListOf<File>()
            videoBaseDirs.add(File(platformDir, "media"))
            if (romsRootDir != null) {
                videoBaseDirs.add(File(romsRootDir, "media/$platformId"))
                videoBaseDirs.add(File(romsRootDir, "ES-DE/downloaded_media/$platformId"))
            }

            video = searchMedia(videoBaseDirs, VID_SUBFOLDERS, rawName, VID_EXTENSIONS)

        } catch (e: Exception) {
            Log.e(TAG, "Media search error for ${game.title}", e)
        }
        
        return Pair(boxArt, video)
    }

    private fun searchMedia(baseDirs: List<File>, subFolders: List<String>, fileName: String, extensions: List<String>): String? {
        for (base in baseDirs) {
            if (!base.exists() || !base.isDirectory) continue
            
            for (sub in subFolders) {
                val folder = File(base, sub)
                if (folder.exists() && folder.isDirectory) {
                    for (ext in extensions) {
                        // Búsqueda por nombre de archivo exacto
                        val file = File(folder, "$fileName$ext")
                        if (file.exists()) return Uri.fromFile(file).toString()
                        
                        // Fallback con sufijo (algunos scrapers usan -image, -video, -thumb)
                        val suffix = if (IMG_EXTENSIONS.contains(ext)) "-image" else "-video"
                        val fallback = File(folder, "$fileName$suffix$ext")
                        if (fallback.exists()) return Uri.fromFile(fallback).toString()
                    }
                }
            }
        }
        return null
    }

    /**
     * Utilidad idéntica a LauncherManager para traducir URIs de Scoped Storage a rutas reales.
     */
    private fun translateSafToRealPath(uriStr: String): String? {
        if (uriStr.startsWith("/")) return uriStr
        if (!uriStr.startsWith("content://")) return null
        
        return try {
            val uri = Uri.parse(uriStr)
            val documentId = DocumentsContract.getDocumentId(uri)
            val split = documentId.split(":")
            val type = split[0]
            if ("primary".equals(type, ignoreCase = true)) {
                "/storage/emulated/0/" + split[1]
            } else {
                "/storage/$type/" + split[1]
            }
        } catch (e: Exception) {
            null
        }
    }
    
    // Compatibilidad legacy
    fun findLocalArtForGame(game: Game): String? = findLocalMediaForGame(game, null).first
}
