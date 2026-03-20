package com.example.speccyose5ultrav021b

import android.content.Context
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.provider.Settings
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager

class SystemControlManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    // --- VOLUMEN ---
    fun getVolume(): Int = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    fun setVolume(level: Int) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, level, 0)
    }
    fun getMaxVolume(): Int = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    // --- BRILLO ---
    fun getBrightness(): Int {
        return Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128)
    }
    fun setBrightness(level: Int) {
        Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, level)
    }

    // --- WIFI ---
    fun isWifiEnabled(): Boolean = wifiManager.isWifiEnabled
    fun setWifiEnabled(enabled: Boolean) {
        // Nota: En Android moderno esto puede requerir que el usuario lo haga manualmente o ser System App
        // wifiManager.isWifiEnabled = enabled 
    }

    // --- BLUETOOTH ---
    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled ?: false
}