package com.example.speccyose5ultrav021b

import android.content.Context

object RetroArchDatabase {

    data class SystemInfo(
        val id: String,
        val name: String,
        val extensions: List<String>,
        val defaultCore: String,
        val alternativeCores: List<String> = emptyList(),
        val icon: String = "ic_generic_console",
        val scrapingPlatform: String? = null // Nuevo campo para el scraping
    )

    private val ARCHIVE_EXTS = listOf(".zip", ".7z", ".rar")

    private val hardcodedSystems = listOf(
        SystemInfo("3do", "3DO", listOf(".iso", ".chd", ".cue"), "opera", listOf("4do"), scrapingPlatform = "3do"),
        SystemInfo("3ds", "Nintendo 3DS", listOf(".3ds", ".cia"), "citra", listOf("citra_canary", "citra_mmj"), scrapingPlatform = "n3ds"),
        SystemInfo("alg", "American Laser Games", listOf(".daphne", ".zip"), "daphne", scrapingPlatform = "arcade"),
        SystemInfo("amiga", "Amiga", listOf(".adf", ".lha", ".chd"), "puae", listOf("uae4arm"), scrapingPlatform = "amiga"),
        SystemInfo("amstradcpc", "Amstrad CPC", listOf(".dsk", ".zip"), "caprice32", listOf("crocods"), scrapingPlatform = "amstradcpc"),
        SystemInfo("apple2", "Apple II", listOf(".dsk", ".do"), "apple2nh", scrapingPlatform = "apple2"),
        SystemInfo("arcade", "Arcade", listOf(".zip", ".7z", ".chd"), "mame", listOf("mame2003_plus", "mame2010", "fbneo"), scrapingPlatform = "arcade"),
        SystemInfo("atari2600", "Atari 2600", listOf(".a26"), "stella", listOf("stella2014"), scrapingPlatform = "atari2600"),
        SystemInfo("atari5200", "Atari 5200", listOf(".a52"), "atari800", listOf("a5200"), scrapingPlatform = "atari5200"),
        SystemInfo("atari7800", "Atari 7800", listOf(".a78"), "prosystem", scrapingPlatform = "atari7800"),
        SystemInfo("atari800", "Atari 800/ST", listOf(".atr", ".st"), "atari800", listOf("hatari"), scrapingPlatform = "atari800"),
        SystemInfo("atarijaguar", "Atari Jaguar", listOf(".j64"), "virtualjaguar", scrapingPlatform = "atarijaguar"),
        SystemInfo("atarilynx", "Atari Lynx", listOf(".lnx"), "handy", listOf("mednafen_lynx"), scrapingPlatform = "atarilynx"),
        SystemInfo("c64", "Commodore 64", listOf(".d64", ".crt", ".tap"), "vice_x64", listOf("vice_x64sc", "frodo"), scrapingPlatform = "c64"),
        SystemInfo("coleco", "ColecoVision", listOf(".col", ".bin"), "blue_msx", listOf("gearcoleco"), scrapingPlatform = "coleco"),
        SystemInfo("cps1", "Capcom CPS1", listOf(".zip"), "fbneo", listOf("mame2003_plus"), scrapingPlatform = "cps1"),
        SystemInfo("cps2", "Capcom CPS2", listOf(".zip"), "fbneo", listOf("mame2003_plus"), scrapingPlatform = "cps2"),
        SystemInfo("cps3", "Capcom CPS3", listOf(".zip"), "fbneo", listOf("mame2003_plus"), scrapingPlatform = "cps3"),
        SystemInfo("dos", "DOS", listOf(".exe", ".com", ".conf"), "dosbox_pure", listOf("dosbox_svn", "dosbox_core"), scrapingPlatform = "pc"),
        SystemInfo("dreamcast", "Sega Dreamcast", listOf(".gdi", ".cdi", ".chd"), "flycast", listOf("reicast"), scrapingPlatform = "dreamcast"),
        SystemInfo("gb", "Game Boy", listOf(".gb"), "gambatte", listOf("sameboy", "mgba", "gearboy"), scrapingPlatform = "gb"),
        SystemInfo("gba", "Game Boy Advance", listOf(".gba"), "mgba", listOf("gpsp", "vba_next", "meteor"), scrapingPlatform = "gba"),
        SystemInfo("gbc", "Game Boy Color", listOf(".gbc"), "gambatte", listOf("sameboy", "mgba", "gearboy"), scrapingPlatform = "gbc"),
        SystemInfo("gc", "GameCube", listOf(".iso", ".rvz"), "dolphin", scrapingPlatform = "gc"),
        SystemInfo("gamegear", "Game Gear", listOf(".gg"), "genesis_plus_gx", listOf("picodrive", "smsplus"), scrapingPlatform = "gamegear"),
        SystemInfo("genesis", "Sega Genesis", listOf(".md", ".gen"), "genesis_plus_gx", listOf("picodrive", "blastem"), scrapingPlatform = "genesis"),
        SystemInfo("mame", "MAME", listOf(".zip", ".7z"), "mame", listOf("mame2003_plus", "mame2010", "fbneo"), scrapingPlatform = "mame"),
        SystemInfo("msx", "MSX", listOf(".rom", ".mx1"), "bluemsx", listOf("fmsx"), scrapingPlatform = "msx"),
        SystemInfo("n64", "Nintendo 64", listOf(".n64", ".v64", ".z64"), "mupen64plus_next", listOf("parallel_n64"), scrapingPlatform = "n64"),
        SystemInfo("nds", "Nintendo DS", listOf(".nds"), "melonds", listOf("desmume", "desmume2015"), scrapingPlatform = "nds"),
        SystemInfo("neogeo", "Neo Geo", listOf(".zip", ".7z"), "fbneo", listOf("fbalpha2012_neogeo"), scrapingPlatform = "neogeo"),
        SystemInfo("nes", "NES", listOf(".nes"), "fceumm", listOf("nestopia", "mesen", "quicknes"), scrapingPlatform = "nes"),
        SystemInfo("ngp", "Neo Geo Pocket", listOf(".ngp", ".ngc"), "mednafen_ngp", scrapingPlatform = "ngp"),
        SystemInfo("openbor", "OpenBOR", listOf(".pak"), "openbor", scrapingPlatform = "arcade"),
        SystemInfo("pce", "PC Engine", listOf(".pce", ".cue"), "mednafen_pce_fast", listOf("mednafen_pce", "sgx"), scrapingPlatform = "pce"),
        SystemInfo("psx", "PlayStation", listOf(".cue", ".bin", ".chd", ".pbp", ".iso"), "pcsx_rearmed", listOf("beetle_psx", "beetle_psx_hw", "duckstation"), scrapingPlatform = "psx"),
        SystemInfo("ps2", "PlayStation 2", listOf(".iso", ".chd"), "pcsx2", listOf("play"), scrapingPlatform = "ps2"),
        SystemInfo("psp", "PSP", listOf(".iso", ".cso"), "ppsspp", scrapingPlatform = "psp"),
        SystemInfo("saturn", "Sega Saturn", listOf(".cue", ".iso", ".chd"), "yabause", listOf("beetle_saturn", "kronos"), scrapingPlatform = "saturn"),
        SystemInfo("snes", "Super Nintendo", listOf(".sfc", ".smc"), "snes9x", listOf("snes9x2010", "bsnes", "mesen-s"), scrapingPlatform = "snes"),
        SystemInfo("wii", "Nintendo Wii", listOf(".iso", ".wbfs", ".rvz"), "dolphin", scrapingPlatform = "wii"),
        SystemInfo("zx_spectrum", "ZX Spectrum", listOf(".tap", ".tzx", ".z80"), "fuse", listOf("81"), scrapingPlatform = "zxspectrum"),
        SystemInfo("pico8", "PICO-8", listOf(".p8", ".png"), "fake08"),
        SystemInfo("pokemini", "Pokémon Mini", listOf(".min"), "pokemini"),
        SystemInfo("supergrafx", "SuperGrafx", listOf(".pce", ".sgx"), "mednafen_supergrafx"),
        SystemInfo("videopac", "Magnavox Odyssey 2", listOf(".bin", ".zip"), "o2em"),
        SystemInfo("virtualboy", "Virtual Boy", listOf(".vb"), "mednafen_vb"),
        SystemInfo("wswan", "WonderSwan", listOf(".ws", ".wsc"), "mednafen_wswan"),
        SystemInfo("watara", "Watara Supervision", listOf(".bin"), "potator"),
        SystemInfo("tic80", "TIC-80", listOf(".tic"), "tic80")
    ).map { sys ->
        val extendedExtensions = if (sys.id !in listOf("ps2", "wii", "gc", "psx", "psp")) {
            (sys.extensions + ARCHIVE_EXTS).distinct()
        } else {
            sys.extensions
        }
        sys.copy(extensions = extendedExtensions)
    }

    private var dynamicSystems: List<SystemInfo> = emptyList()

    /**
     * Inicializa la base de datos permitiendo cargar información dinámica desde archivos assets.
     */
    fun initialize(context: Context) {
        val allPlatformIds = context.assets.list("contentimg/ROMs") ?: emptyArray()
        val loaded = mutableListOf<SystemInfo>()
        
        for (id in allPlatformIds) {
            SystemInfoParser.parseSystemInfo(context, id)?.let { loaded.add(it) }
        }
        dynamicSystems = loaded
    }

    val systems: List<SystemInfo> get() = (hardcodedSystems + dynamicSystems).distinctBy { it.id }

    fun findSystemById(id: String): SystemInfo? {
        val cleanId = id.lowercase().replace("_", "").replace(" ", "").replace("vision", "")
        return systems.find { 
            val sysCleanId = it.id.lowercase().replace("_", "")
            sysCleanId == cleanId || cleanId.startsWith(sysCleanId) || sysCleanId.startsWith(cleanId)
        }
    }
}
