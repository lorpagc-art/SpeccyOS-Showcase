package com.example.speccyose5ultrav021b

import android.content.Context
import android.view.KeyEvent

class KeyMapper(context: Context) {
    private val prefs = context.getSharedPreferences("key_mappings", Context.MODE_PRIVATE)

    // Botones Principales
    var actionA: Int
        get() = prefs.getInt("btn_a", KeyEvent.KEYCODE_BUTTON_A)
        set(value) = prefs.edit().putInt("btn_a", value).apply()

    var actionB: Int
        get() = prefs.getInt("btn_b", KeyEvent.KEYCODE_BUTTON_B)
        set(value) = prefs.edit().putInt("btn_b", value).apply()

    var actionX: Int
        get() = prefs.getInt("btn_x", KeyEvent.KEYCODE_BUTTON_X)
        set(value) = prefs.edit().putInt("btn_x", value).apply()

    var actionY: Int
        get() = prefs.getInt("btn_y", KeyEvent.KEYCODE_BUTTON_Y)
        set(value) = prefs.edit().putInt("btn_y", value).apply()

    // Cruceta (DPAD)
    var dpadUp: Int
        get() = prefs.getInt("dpad_up", KeyEvent.KEYCODE_DPAD_UP)
        set(value) = prefs.edit().putInt("dpad_up", value).apply()

    var dpadDown: Int
        get() = prefs.getInt("dpad_down", KeyEvent.KEYCODE_DPAD_DOWN)
        set(value) = prefs.edit().putInt("dpad_down", value).apply()

    var dpadLeft: Int
        get() = prefs.getInt("dpad_left", KeyEvent.KEYCODE_DPAD_LEFT)
        set(value) = prefs.edit().putInt("dpad_left", value).apply()

    var dpadRight: Int
        get() = prefs.getInt("dpad_right", KeyEvent.KEYCODE_DPAD_RIGHT)
        set(value) = prefs.edit().putInt("dpad_right", value).apply()

    // Gatillos y Centrales
    var btnL1: Int
        get() = prefs.getInt("btn_l1", KeyEvent.KEYCODE_BUTTON_L1)
        set(value) = prefs.edit().putInt("btn_l1", value).apply()

    var btnR1: Int
        get() = prefs.getInt("btn_r1", KeyEvent.KEYCODE_BUTTON_R1)
        set(value) = prefs.edit().putInt("btn_r1", value).apply()

    var btnM: Int
        get() = prefs.getInt("btn_m", KeyEvent.KEYCODE_MENU)
        set(value) = prefs.edit().putInt("btn_m", value).apply()

    var btnT: Int
        get() = prefs.getInt("btn_t", KeyEvent.KEYCODE_HOME)
        set(value) = prefs.edit().putInt("btn_t", value).apply()

    var btnK: Int
        get() = prefs.getInt("btn_k", KeyEvent.KEYCODE_BACK)
        set(value) = prefs.edit().putInt("btn_k", value).apply()

    fun reset() {
        prefs.edit().clear().apply()
    }
}
