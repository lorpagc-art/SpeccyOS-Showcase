package com.example.speccyose5ultrav021b

import android.os.Environment
import java.io.File

object StorageManager {
    val rootPath = File(Environment.getExternalStorageDirectory(), "ROMS")
    val metadataPath = File(rootPath, ".metadata") // Carpeta para info de la app
    
    private val systems = listOf(
        "nes", "snes", "n64", "gc", "wii", "gb", "gbc", "gba", "nds", "3ds",
        "psx", "ps2", "psp", "genesis", "dreamcast", "zxspectrum", "mame"
    )

    fun initStorageStructure(): Boolean {
        try {
            if (!rootPath.exists()) rootPath.mkdirs()
            if (!metadataPath.exists()) metadataPath.mkdirs()

            // Asegurar carpetas básicas de sistema
            listOf("bios", "saves", "states", "themes", "contentimg").forEach {
                val folder = File(rootPath, it)
                if (!folder.exists()) folder.mkdirs()
            }

            for (system in systems) {
                val systemFolder = File(rootPath, system)
                if (!systemFolder.exists()) systemFolder.mkdirs()
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}