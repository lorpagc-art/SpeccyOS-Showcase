package com.example.speccyose5ultrav021b

import android.os.Build

data class ConsoleProfile(
    val id: String,
    val name: String,
    val chipset: String,
    val manufacturer: String,
    val ram: String,
    val hasFan: Boolean = false,
    val fanPath: String? = null,
    val hasRgb: Boolean = false,
    val rgbPath: String? = null,
    val cpuGovernorPath: String = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor",
    val buildModelPatterns: List<String> = emptyList(),
    val iconRes: Int,
    val history: String = "Información no disponible.",
    val emulationCapacity: String = "Desconocida.",
    val recommendedPower: String = "BALANCED"
)

object HardwareDatabase {
    val profiles = listOf(
        // GAMEMT
        ConsoleProfile(
            id = "e5_plus",
            name = "GameMT E5 Plus",
            chipset = "RK3566",
            manufacturer = "GameMT",
            ram = "4GB",
            iconRes = R.drawable.ic_handheld_widescreen,
            buildModelPatterns = listOf("e5_plus"),
            history = "Lanzada en 2025, destaca por su diseño ergonómico inspirado en la PSP.",
            emulationCapacity = "Hasta PSP y algo de Saturn.",
            recommendedPower = "BALANCED"
        ),
        ConsoleProfile(
            id = "e5_ultra",
            name = "GameMT E5 Ultra",
            chipset = "Unisoc T620",
            manufacturer = "GameMT",
            ram = "6GB",
            hasFan = true,
            fanPath = "/sys/class/backlight/sprd_backlight_fan/brightness",
            cpuGovernorPath = "/sys/devices/system/cpu/cpufreq/policy0/scaling_governor",
            iconRes = R.drawable.ic_handheld_widescreen,
            buildModelPatterns = listOf("e5_ultra", "gamemt_e5u"),
            history = "Novedad 2026. Un salto de potencia con sticks Hall Effect y refrigeración activa.",
            emulationCapacity = "PS2 y GameCube con fluidez.",
            recommendedPower = "PERFORMANCE"
        ),
        ConsoleProfile(
            id = "rg557",
            name = "Anbernic RG557",
            chipset = "Dimensity 8300",
            manufacturer = "Anbernic",
            ram = "12GB",
            hasFan = true,
            fanPath = "/sys/class/fan/level",
            hasRgb = true,
            rgbPath = "/sys/class/leds/rgb/brightness",
            iconRes = R.drawable.ic_handheld_widescreen,
            buildModelPatterns = listOf("rg557"),
            history = "Modelo estrella de Anbernic para 2026 con pantalla AMOLED.",
            emulationCapacity = "Todo el catálogo de Android y consolas 3D pesadas.",
            recommendedPower = "PERFORMANCE"
        ),
        ConsoleProfile(
            id = "rp6",
            name = "Retroid Pocket 6",
            chipset = "Snapdragon 8 Gen 2",
            manufacturer = "Retroid",
            ram = "12GB",
            hasFan = true,
            fanPath = "/sys/class/fan/fan_speed",
            iconRes = R.drawable.ic_handheld_widescreen,
            buildModelPatterns = listOf("rp6"),
            history = "Referencia en calidad-precio del 2026.",
            emulationCapacity = "Switch y sistemas de alta gama.",
            recommendedPower = "EXTREME"
        ),
        ConsoleProfile(
            id = "generic",
            name = "Dispositivo Genérico",
            chipset = "Desconocido",
            manufacturer = "Android",
            ram = "N/A",
            iconRes = R.drawable.ic_handheld_widescreen
        )
    )

    fun detectActualHardware(): ConsoleProfile {
        val model = Build.MODEL.lowercase()
        val device = Build.DEVICE.lowercase()
        return profiles.find { it.buildModelPatterns.any { p -> model.contains(p) || device.contains(p) } } ?: getProfile("generic")
    }

    fun getProfile(id: String): ConsoleProfile = profiles.find { it.id == id } ?: profiles.last()
}
