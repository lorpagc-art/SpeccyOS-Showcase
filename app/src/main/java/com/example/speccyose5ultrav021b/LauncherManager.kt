package com.example.speccyose5ultrav021b

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.StrictMode
import android.provider.DocumentsContract
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import org.json.JSONObject
import java.io.File

class LauncherManager(private val context: Context) {

    private val settingsManager = SettingsManager(context)
    private val prefs = context.getSharedPreferences("launcher_stats", Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "SpeccyLauncher"
        private val RETROARCH_PACKAGES = listOf("com.retroarch.aarch64", "com.retroarch", "com.retroarch.plus", "com.retroarch.ra32")
        private const val PKG_NETHERSX2 = "xyz.aethersx2.android"
        private const val PKG_AETHERSX2 = "com.tahlreth.aethersx2.android"
        private const val PKG_DOLPHIN = "org.dolphinemu.dolphinemu"
        private const val PKG_CITRA = "org.citra.citra_emu"
        private const val PKG_DRASTIC = "com.dsemu.drastic"
        private const val PKG_XANITE = "org.xanite.emu"
        private const val PKG_PPSSPP = "org.ppsspp.ppsspp"
        private const val PKG_M64PLUS_FZ = "org.mupen64plusae.v3.fxyz"
        private const val PKG_M64PLUS_FZ_PRO = "org.mupen64plusae.v3.fxyz.pro"
        private const val PKG_YUZU = "org.yuzu.yuzu_emu"
    }

    private fun openStoreOrLink(pkg: String, fallbackUrl: String? = null) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            val url = fallbackUrl ?: "https://play.google.com/store/apps/details?id=$pkg"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    private fun registerLaunch(game: Game) {
        prefs.edit().apply {
            putString("last_launched_game_title", game.title)
            putString("last_launched_platform", game.platformId)
            putLong("last_launch_timestamp", System.currentTimeMillis())
            putBoolean("returning_from_game", true)
            apply()
        }
    }

    fun launchGame(game: Game) {
        registerLaunch(game)
        
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        val platformId = game.platformId.lowercase()
        val romPathUri = Uri.parse(game.path)
        val safePath = getRealPathFromUri(romPathUri)
        val romUri = Uri.fromFile(File(safePath))

        val emulatorType = settingsManager.getPreferredEmulator(platformId)

        try {
            if (emulatorType == SettingsManager.EMULATOR_STANDALONE) {
                when (platformId) {
                    "ps2" -> launchNetherSX2(romUri)
                    "wii", "gamecube", "gc" -> launchDolphin(romUri)
                    "3ds", "n3ds" -> launchCitra(romUri)
                    "nds" -> launchDraStic(romUri)
                    "psp" -> launchPPSSPP(romUri)
                    "n64" -> launchN64(romUri)
                    "xbox" -> launchXanite(romUri)
                    "switch" -> launchSwitch(romUri)
                    else -> launchRetroArch(safePath, platformId)
                }
            } else {
                launchRetroArch(safePath, platformId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al lanzar", e)
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun launchN64(romUri: Uri) {
        val pkg = if (isPackageInstalled(PKG_M64PLUS_FZ_PRO)) PKG_M64PLUS_FZ_PRO 
                  else if (isPackageInstalled(PKG_M64PLUS_FZ)) PKG_M64PLUS_FZ 
                  else null
        
        if (pkg == null) {
            openStoreOrLink(PKG_M64PLUS_FZ)
            return
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            component = ComponentName(pkg, "org.mupen64plusae.v3.fxyz.MainActivity")
            data = romUri
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra("StateSlot", 0)
        }
        context.startActivity(intent)
    }

    private fun launchNetherSX2(romUri: Uri) {
        val pkg = if (isPackageInstalled(PKG_NETHERSX2)) PKG_NETHERSX2 else if (isPackageInstalled(PKG_AETHERSX2)) PKG_AETHERSX2 else null
        if (pkg == null) {
             openStoreOrLink(PKG_NETHERSX2, "https://github.com/Tahlreth/aethersx2/releases")
             return
        }
        val intent = Intent(Intent.ACTION_VIEW).apply {
            component = ComponentName(pkg, "xyz.aethersx2.android.EmulationActivity")
            setDataAndType(romUri, "application/x-iso9660-image")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }

    private fun launchDolphin(romUri: Uri) {
        if (!isPackageInstalled(PKG_DOLPHIN)) {
             openStoreOrLink(PKG_DOLPHIN)
             return
        }
        val intent = Intent(Intent.ACTION_VIEW).apply {
            component = ComponentName(PKG_DOLPHIN, "org.dolphinemu.dolphinemu.ui.main.EmulationActivity")
            data = romUri
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }

    private fun launchPPSSPP(romUri: Uri) {
        if (!isPackageInstalled(PKG_PPSSPP)) {
            openStoreOrLink(PKG_PPSSPP)
            return
        }
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setPackage(PKG_PPSSPP)
            data = romUri
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }

    private fun launchCitra(romUri: Uri) {
        if (!isPackageInstalled(PKG_CITRA)) {
            openStoreOrLink(PKG_CITRA, "https://github.com/citra-emu/citra-android/releases")
            return
        }
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setPackage(PKG_CITRA)
            data = romUri
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }

    private fun launchSwitch(romUri: Uri) {
        if (!isPackageInstalled(PKG_YUZU)) {
            Toast.makeText(context, "Yuzu no instalado", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setPackage(PKG_YUZU)
            data = romUri
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }

    private fun launchXanite(romUri: Uri) {
        if (!isPackageInstalled(PKG_XANITE)) {
            openStoreOrLink(PKG_XANITE, "https://github.com/dev-Ali2008/xanite/releases")
            return
        }
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setPackage(PKG_XANITE)
            setDataAndType(romUri, "application/octet-stream")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }

    private fun launchDraStic(romUri: Uri) {
        if (!isPackageInstalled(PKG_DRASTIC)) {
            openStoreOrLink(PKG_DRASTIC)
            return
        }
        launchStandalone(PKG_DRASTIC, "com.dsemu.drastic.DraSticActivity", romUri)
    }

    private fun launchStandalone(pkg: String, activity: String, romUri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW)
        if (activity.isNotEmpty()) {
            intent.component = ComponentName(pkg, activity)
        } else {
            intent.setPackage(pkg)
        }
        intent.data = romUri
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            context.packageManager.getLaunchIntentForPackage(pkg)?.let { context.startActivity(it) }
        }
    }

    private fun launchRetroArch(safePath: String, platformId: String) {
        val installedPkg = RETROARCH_PACKAGES.find { isPackageInstalled(it) }
        if (installedPkg == null) {
            openStoreOrLink("com.retroarch.aarch64")
            return
        }

        val system = RetroArchDatabase.findSystemById(platformId)
        val core = settingsManager.getPreferredCore(platformId) ?: system?.defaultCore ?: "detect"
        
        val intent = Intent(Intent.ACTION_MAIN).apply {
            component = ComponentName(installedPkg, "com.retroarch.browser.retroactivity.RetroActivityFuture")
            putExtra("ROM", safePath)
            putExtra("LIBRETRO", "/data/data/$installedPkg/cores/${core}_libretro_android.so")
            putExtra("CONFIGFILE", "/storage/emulated/0/Android/data/$installedPkg/files/retroarch.cfg")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                intent.component = ComponentName(installedPkg, "com.retroarch.browser.retroactivity.RetroActivity")
                context.startActivity(intent)
            } catch (e2: Exception) {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(installedPkg)
                launchIntent?.let {
                    it.putExtra("ROM", safePath)
                    it.putExtra("LIBRETRO", "/data/data/$installedPkg/cores/${core}_libretro_android.so")
                    context.startActivity(it)
                }
            }
        }
    }

    private fun getRealPathFromUri(uri: Uri): String {
        if (uri.scheme != "content") return uri.path ?: ""
        return try {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":")
            val type = split[0]
            val path = split[1]
            if (type == "primary") "/storage/emulated/0/$path" else "/storage/$type/$path"
        } catch (e: Exception) { 
            uri.toString().substringAfter("document/").replace("primary%3A", "/storage/emulated/0/").replace("%2F", "/")
        }
    }

    private fun isPackageInstalled(p: String): Boolean = try { context.packageManager.getPackageInfo(p, 0); true } catch (e: Exception) { false }
}
