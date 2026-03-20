package com.example.speccyose5ultrav021b

import android.app.Activity
import android.util.Log
import com.android.billingclient.api.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

class BillingManager(private val activity: Activity, private val settingsManager: SettingsManager) {

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        }
    }

    private val billingClient = if (isGooglePlayAvailable()) {
        BillingClient.newBuilder(activity)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
    } else null

    private fun isGooglePlayAvailable(): Boolean {
        val availability = GoogleApiAvailability.getInstance()
        val result = availability.isGooglePlayServicesAvailable(activity)
        return result == ConnectionResult.SUCCESS
    }

    fun startConnection() {
        if (billingClient == null) {
            Log.w("BillingManager", "Google Play no disponible. Esperando autorización vía Web.")
            return
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    checkPurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.e("BillingManager", "Desconectado de Play Store.")
            }
        })
    }

    private fun checkPurchases() {
        billingClient?.let { client ->
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()

            client.queryPurchasesAsync(params) { billingResult: BillingResult, purchases: List<Purchase> ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    var isPaid = false
                    for (purchase in purchases) {
                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                            isPaid = true
                            if (!purchase.isAcknowledged) {
                                acknowledgePurchase(purchase)
                            }
                        }
                    }
                    settingsManager.isAuthorized = isPaid
                }
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult: BillingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d("BillingManager", "Compra confirmada en Play Store.")
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            settingsManager.isAuthorized = true
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            }
        }
    }
    
    /**
     * Permite autorizar manualmente al usuario (usado tras el login web exitoso).
     */
    fun forceAuthorize(email: String) {
        settingsManager.userEmail = email
        settingsManager.isAuthorized = true
        Log.i("BillingManager", "Usuario autorizado mediante protocolo Imperial (Web Auth).")
    }
}
