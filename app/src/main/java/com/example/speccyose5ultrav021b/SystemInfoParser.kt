package com.example.speccyose5ultrav021b

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

object SystemInfoParser {
    private const val TAG = "SystemInfoParser"

    fun parseSystemInfo(context: Context, platformId: String): RetroArchDatabase.SystemInfo? {
        val assetPath = "contentimg/ROMs/$platformId/systeminfo.txt"
        return try {
            val inputStream = context.assets.open(assetPath)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val lines = reader.readLines()
            
            var fullName = platformId
            var extensions = mutableListOf<String>()
            var defaultCore = ""
            var scrapingPlatform: String? = null
            val alternativeCores = mutableListOf<String>()

            var currentSection = ""
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.isEmpty()) continue

                if (trimmed.endsWith(":")) {
                    currentSection = trimmed.lowercase()
                    continue
                }

                when (currentSection) {
                    "full system name:" -> fullName = trimmed
                    "supported file extensions:" -> {
                        extensions.addAll(trimmed.split(" ").filter { it.startsWith(".") }.map { it.lowercase() })
                    }
                    "launch command:" -> {
                        if (trimmed.contains("EXTRA_LIBRETRO")) {
                            defaultCore = extractCoreName(trimmed)
                        }
                    }
                    "alternative launch commands:", "alternative launch command:" -> {
                        if (trimmed.contains("EXTRA_LIBRETRO")) {
                            alternativeCores.add(extractCoreName(trimmed) + " (Alt)")
                        }
                    }
                    "platform (for scraping):" -> {
                        scrapingPlatform = trimmed
                    }
                }
            }

            if (defaultCore.isEmpty()) return null

            RetroArchDatabase.SystemInfo(
                id = platformId,
                name = fullName,
                extensions = extensions.distinct(),
                defaultCore = defaultCore,
                alternativeCores = alternativeCores.distinct().filter { it != defaultCore },
                icon = "ic_generic_console",
                scrapingPlatform = scrapingPlatform
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun extractCoreName(command: String): String {
        return command.substringAfter("EXTRA_LIBRETRO%=").substringBefore("_libretro_android.so").substringBefore(" ")
    }
}
