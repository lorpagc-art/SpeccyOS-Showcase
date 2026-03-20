package com.example.speccyose5ultrav021b

import android.util.Base64

object SecurityUtils {
    // API Key obfuscated
    private const val ENCODED_KEY = "QUl6YVN5QURmazRxRjFwUEliRFc1ZUFNNnRNRXJZZHRlcERYQjhv"

    fun getApiKey(): String {
        return String(Base64.decode(ENCODED_KEY, Base64.DEFAULT))
    }
}
