package com.example.speccyose5ultrav021b

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("speccy_settings", Context.MODE_PRIVATE)

    companion object {
        const val EMULATOR_RETROARCH = "RETROARCH"
        const val EMULATOR_STANDALONE = "STANDALONE"
        
        const val THEME_CLASSIC = "CLASSIC"
        const val THEME_INMERSIVE = "INMERSIVE"
        const val THEME_ULTRA = "ULTRA"
    }

    // --- CONFIGURACIÓN INICIAL ---
    var isHardwareConfigured: Boolean
        get() = prefs.getBoolean("hw_configured", false)
        set(value) = prefs.edit().putBoolean("hw_configured", value).apply()

    // --- PUBLICIDAD / INTRO ALTERNA ---
    var lastIntroImageIndex: Int
        get() = prefs.getInt("last_intro_img", 2)
        set(value) = prefs.edit().putInt("last_intro_img", value).apply()

    // --- TEMAS ---
    var currentTheme: String
        get() = prefs.getString("current_theme", THEME_CLASSIC) ?: THEME_CLASSIC
        set(value) = prefs.edit().putString("current_theme", value).apply()

    // --- EMULADORES ---
    fun setPreferredEmulator(platformId: String, emulatorType: String) {
        prefs.edit().putString("emu_pref_$platformId", emulatorType).apply()
    }

    fun getPreferredEmulator(platformId: String): String {
        val default = when(platformId.lowercase()) {
            "ps2", "gc", "gamecube", "wii", "3ds", "n3ds", "psp", "xbox" -> EMULATOR_STANDALONE
            else -> EMULATOR_RETROARCH
        }
        return prefs.getString("emu_pref_$platformId", default) ?: default
    }

    fun setPreferredCore(platformId: String, coreName: String) {
        prefs.edit().putString("core_pref_$platformId", coreName).apply()
    }

    fun getPreferredCore(platformId: String): String? {
        return prefs.getString("core_pref_$platformId", null)
    }

    fun getEmulatorType(platformId: String): String = getPreferredEmulator(platformId)

    // --- PERSONALIZACIÓN VISUAL ---
    var themePrimaryColor: Int
        get() = prefs.getInt("theme_primary_color", 0xFF00E676.toInt()) 
        set(value) = prefs.edit().putInt("theme_primary_color", value).apply()

    var showCrtEffect: Boolean
        get() = prefs.getBoolean("show_crt_effect", false)
        set(value) = prefs.edit().putBoolean("show_crt_effect", value).apply()

    var isBackgroundMusicEnabled: Boolean
        get() = prefs.getBoolean("bg_music_enabled", true)
        set(value) = prefs.edit().putBoolean("bg_music_enabled", value).apply()

    // --- CACHÉ DE ESCANEO ---
    var lastScanTimestamp: Long
        get() = prefs.getLong("last_scan_ts", 0L)
        set(value) = prefs.edit().putLong("last_scan_ts", value).apply()

    var cachedPlatformIds: Set<String>
        get() = prefs.getStringSet("cached_platforms", emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet("cached_platforms", value).apply()

    // --- IDIOMA ---
    var appLanguage: String
        get() = prefs.getString("app_language", "es_ES") ?: "es_ES"
        set(value) = prefs.edit().putString("app_language", value).apply()

    // --- ESTADO PRO ---
    var isAuthorized: Boolean
        get() {
            val admins = listOf("lorpagc@gmail.com", "lolavicoele@gmail.com", "lpadillavico@gmail.com")
            if (admins.contains(userEmail)) return true
            return prefs.getBoolean("is_authorized", false)
        }
        set(value) = prefs.edit().putBoolean("is_authorized", value).apply()

    var userEmail: String?
        get() = prefs.getString("user_email", null)
        set(value) = prefs.edit().putString("user_email", value).apply()

    var isProUser: Boolean
        get() = isAuthorized
        set(value) { isAuthorized = value }

    // --- SCRAPER SETTINGS ---
    var scraperApiKey: String
        get() = prefs.getString("scraper_api_key", "fb80bb030d65f54d4a0e23488757950ae518ed8dc614e7a569b18353e6f833b3") ?: "fb80bb030d65f54d4a0e23488757950ae518ed8dc614e7a569b18353e6f833b3"
        set(value) = prefs.edit().putString("scraper_api_key", value).apply()

    var scraperUsername: String
        get() = prefs.getString("scraper_username", "") ?: ""
        set(value) = prefs.edit().putString("scraper_username", value).apply()

    var scraperPassword: String
        get() = prefs.getString("scraper_password", "") ?: ""
        set(value) = prefs.edit().putString("scraper_password", value).apply()

    var scraperSource: String
        get() = prefs.getString("scraper_source", "THEGAMESDB") ?: "THEGAMESDB"
        set(value) = prefs.edit().putString("scraper_source", value).apply()

    // --- RETROACHIEVEMENTS ---
    var raUsername: String
        get() = prefs.getString("ra_username", "") ?: ""
        set(value) = prefs.edit().putString("ra_username", value).apply()

    var raToken: String
        get() = prefs.getString("ra_token", "") ?: ""
        set(value) = prefs.edit().putString("ra_token", value).apply()

    var isRAEnabled: Boolean
        get() = prefs.getBoolean("ra_enabled", false)
        set(value) = prefs.edit().putBoolean("ra_enabled", value).apply()

    // --- PERFORMANCE ---
    var isManualPerformanceMode: Boolean
        get() = prefs.getBoolean("manual_perf_mode", false)
        set(value) = prefs.edit().putBoolean("manual_perf_mode", value).apply()

    // --- GENERAL ---
    var romsLocation: String
        get() = prefs.getString("roms_location", "") ?: ""
        set(value) = prefs.edit().putString("roms_location", value).apply()

    var manualProfile: String
        get() = prefs.getString("current_manual_profile", "BALANCED") ?: "BALANCED"
        set(value) = prefs.edit().putString("current_manual_profile", value).apply()

    var manualHardwareId: String
        get() = prefs.getString("manual_hw_id", "generic") ?: "generic"
        set(value) = prefs.edit().putString("manual_hw_id", value).apply()

    var isFirstRun: Boolean
        get() = prefs.getBoolean("is_first_run", true)
        set(value) = prefs.edit().putBoolean("is_first_run", value).apply()
}
