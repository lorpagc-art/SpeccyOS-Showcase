package com.example.speccyose5ultrav021b

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.core.app.NotificationCompat
import com.example.speccyose5ultrav021b.ui.theme.NeonBlue
import com.topjohnwu.superuser.Shell

class OverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private var edgeView: ComposeView? = null
    private var menuView: ComposeView? = null
    private val TAG = "SpeccyOverlay"
    
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    
    private val store = ViewModelStore()
    override val viewModelStore: ViewModelStore get() = store
    
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        startForegroundService()
        showEdgeTrigger() 
    }

    private fun startForegroundService() {
        val channelId = "overlay_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Speccy OS Imperial", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Speccy OS Imperial")
            .setContentText("Panel Quick-Access Activo")
            .setSmallIcon(R.drawable.ic_generic_hardware)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        startForeground(1, notification)
    }

    private fun showEdgeTrigger() {
        if (edgeView != null) return

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            45, 
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
        }

        edgeView = ComposeView(this).apply {
            setContent {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, NeonBlue.copy(alpha = 0.15f))
                            )
                        )
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { change, dragAmount ->
                                if (dragAmount < -10) { 
                                    showFullMenu()
                                }
                            }
                        }
                )
            }
        }

        setupView(edgeView!!)
        windowManager.addView(edgeView, params)
    }

    private fun showFullMenu() {
        if (menuView != null) return
        edgeView?.let { windowManager.removeView(it); edgeView = null }

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )

        menuView = ComposeView(this).apply {
            setContent {
                QuickAccessOverlayUI(onClose = { hideFullMenu() })
            }
        }

        setupView(menuView!!)
        windowManager.addView(menuView, params)
    }

    private fun hideFullMenu() {
        menuView?.let {
            windowManager.removeView(it)
            menuView = null
            showEdgeTrigger() 
        }
    }

    private fun setupView(view: ComposeView) {
        view.setViewTreeLifecycleOwner(this)
        view.setViewTreeViewModelStoreOwner(this)
        view.setViewTreeSavedStateRegistryOwner(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        edgeView?.let { try { windowManager.removeView(it) } catch(e: Exception) {} }
        menuView?.let { try { windowManager.removeView(it) } catch(e: Exception) {} }
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}

@Composable
fun QuickAccessOverlayUI(onClose: () -> Unit) {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val hardware = HardwareControlManagerBeta
    
    var volume by remember { mutableStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) }
    var currentProfile by remember { mutableStateOf("BALANCED") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onClose() },
        contentAlignment = Alignment.CenterEnd
    ) {
        Surface(
            modifier = Modifier
                .width(340.dp)
                .fillMaxHeight()
                .clickable(enabled = false) {}
                .shadow(25.dp, RoundedCornerShape(topStart = 40.dp, bottomStart = 40.dp), spotColor = NeonBlue),
            color = Color(0xFF050507),
            shape = RoundedCornerShape(topStart = 40.dp, bottomStart = 40.dp),
            border = BorderStroke(1.dp, Brush.verticalGradient(listOf(NeonBlue, Color.Transparent)))
        ) {
            Column(modifier = Modifier.padding(28.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Dashboard, null, tint = NeonBlue, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("IMPERIAL ACCESS", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                }
                
                Spacer(Modifier.height(32.dp))

                OverlaySlider("VOLUMEN", volume, Icons.AutoMirrored.Filled.VolumeUp) {
                    volume = it
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (it * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)).toInt(), 0)
                }
                
                Spacer(Modifier.height(40.dp))
                
                Text("NÚCLEO DE RENDIMIENTO", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ECO", "BAL", "PERF", "MAX").forEach { p ->
                        val isSelected = (p == "BAL" && currentProfile == "BALANCED") || (currentProfile.startsWith(p))
                        Surface(
                            modifier = Modifier.weight(1f).height(44.dp).clickable { 
                                val fullProfile = when(p) { "BAL" -> "BALANCED"; "PERF" -> "PERFORMANCE"; "MAX" -> "EXTREME"; else -> "ECO" }
                                currentProfile = fullProfile
                                hardware.setPerformanceMode(fullProfile == "PERFORMANCE" || fullProfile == "EXTREME")
                            },
                            color = if (isSelected) NeonBlue.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.03f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isSelected) NeonBlue else Color.White.copy(alpha = 0.1f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(p, color = if (isSelected) NeonBlue else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
                
                // Botones de acción rápida
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionButton(Icons.Default.Psychology, "ARCHITECT", Modifier.weight(1f)) {
                        Shell.cmd("am start -n com.example.speccyose5ultrav021b/.MainActivity --es screen architect").submit()
                        onClose()
                    }
                    QuickActionButton(Icons.Default.DeleteForever, "KILL TASK", Modifier.weight(1f)) {
                        // Implementación simple de cierre de app en primer plano (requiere root/shizuku idealmente)
                        Shell.cmd("am force-stop $(dumpsys window | grep -E 'mCurrentFocus|mFocusedApp' | cut -d '/' -f1 | rev | cut -d ' ' -f1 | rev)").submit()
                        onClose()
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { 
                        Shell.cmd("am start -n com.example.speccyose5ultrav021b/.MainActivity").submit()
                        onClose() 
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Home, null, tint = Color.Red)
                    Spacer(Modifier.width(12.dp))
                    Text("DASHBOARD PRINCIPAL", color = Color.Red, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(icon: ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.height(60.dp).clickable { onClick() },
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = NeonBlue, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OverlaySlider(label: String, value: Float, icon: ImageVector, onValueChange: (Float) -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = NeonBlue, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(12.dp))
            Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(thumbColor = NeonBlue, activeTrackColor = NeonBlue, inactiveTrackColor = Color.White.copy(alpha = 0.05f))
        )
    }
}
