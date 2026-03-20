package com.example.speccyose5ultrav021b

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.util.Log

class SpeccyAccessibilityService : AccessibilityService() {

    private lateinit var keyMapper: KeyMapper
    private val TAG = "SpeccyAccess"

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "DENTRO: Servicio de Accesibilidad conectado.")
        
        // CONFIGURACIÓN FORZADA DE ESCUCHA DE TECLAS
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS or 
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 100
        }
        this.serviceInfo = info
        
        keyMapper = KeyMapper(this)
        Log.i(TAG, "Configuración imperial aplicada. Vigilando botones...")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val action = event.action

        if (action == KeyEvent.ACTION_DOWN) {
            Log.d(TAG, "TECLA DETECTADA -> Código: $keyCode")
            
            val mappedMenuKey = keyMapper.btnM
            
            // Si el código coincide con M, el menú estándar o el 109 (común en E5 Ultra)
            if (keyCode == mappedMenuKey || keyCode == KeyEvent.KEYCODE_MENU || keyCode == 109) {
                Log.i(TAG, "¡COINCIDENCIA! Disparando Overlay para KeyCode: $keyCode")
                toggleOverlay()
                return true 
            }
        }
        
        return super.onKeyEvent(event)
    }

    private fun toggleOverlay() {
        val intent = Intent(this, OverlayService::class.java).apply {
            action = "TOGGLE_OVERLAY"
        }
        Log.d(TAG, "Lanzando Intent TOGGLE_OVERLAY...")
        startService(intent)
    }
}
