package com.example.speccyose5ultrav021b

import java.io.File

object BiosManager {
    private val biosFolder = File("/storage/emulated/0/ROMS/BIOS")

    fun getMissingBios(requiredFiles: List<String>): List<String> {
        if (!biosFolder.exists()) return requiredFiles
        
        val existingFiles = biosFolder.listFiles()?.map { it.name.lowercase() } ?: emptyList()
        return requiredFiles.filter { it.lowercase() !in existingFiles }
    }

    fun getBiosStats(): String {
        val files = biosFolder.listFiles()
        return if (files == null || files.isEmpty()) {
            "La carpeta BIOS está vacía"
        } else {
            "Se detectaron ${files.size} archivos en la carpeta BIOS"
        }
    }
}