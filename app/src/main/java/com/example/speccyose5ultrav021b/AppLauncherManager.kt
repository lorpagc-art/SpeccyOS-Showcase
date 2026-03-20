package com.example.speccyose5ultrav021b

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log

data class AndroidApp(
    val packageName: String,
    val label: String,
    val icon: Drawable,
    val isSystemShortcut: Boolean = false
)

class AppLauncherManager(private val context: Context) {

    /**
     * Obtiene la lista de juegos instalados y accesos directos del sistema.
     */
    fun getInstalledGames(): List<AndroidApp> {
        val pm = context.packageManager
        val apps = mutableListOf<AndroidApp>()

        // 1. Añadir accesos directos imperiales (Tiendas)
        addShortcutIfInstalled(apps, "com.android.vending", "PLAY STORE")
        addShortcutIfInstalled(apps, "com.google.android.play.games", "PLAY GAMES")

        // 2. Escanear apps instaladas
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (app in installedApps) {
            // Filtro: Solo apps que tengan lanzador y que sean juegos (o no sean de sistema críticas)
            if (pm.getLaunchIntentForPackage(app.packageName) != null) {
                
                val isGame = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    app.category == ApplicationInfo.CATEGORY_GAME
                } else {
                    // Fallback para versiones antiguas: buscar "game" en el nombre
                    app.packageName.contains("game", ignoreCase = true)
                }

                if (isGame && app.packageName != context.packageName) {
                    apps.add(AndroidApp(
                        packageName = app.packageName,
                        label = pm.getApplicationLabel(app).toString(),
                        icon = pm.getApplicationIcon(app)
                    ))
                }
            }
        }
        return apps.sortedBy { it.label }
    }

    private fun addShortcutIfInstalled(list: MutableList<AndroidApp>, pkg: String, label: String) {
        try {
            val icon = context.packageManager.getApplicationIcon(pkg)
            list.add(AndroidApp(pkg, label, icon, true))
        } catch (e: Exception) {
            Log.w("AppLauncher", "Acceso directo omitido: $pkg no instalado.")
        }
    }

    fun launchApp(packageName: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
