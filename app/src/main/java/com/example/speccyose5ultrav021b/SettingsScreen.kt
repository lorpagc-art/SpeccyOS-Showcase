package com.example.speccyose5ultrav021b

import android.content.Intent
import android.net.Uri
import android.view.KeyEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.speccyose5ultrav021b.ui.theme.NeonBlue
import kotlinx.coroutines.launch
import com.topjohnwu.superuser.Shell
import android.app.Application

enum class SettingsTab { HARDWARE, DEVICE, LIBRARY, EMULATORS, CONTROLS, APPEARANCE, SCRAPER, ACHIEVEMENTS, ARCHITECT, STORE, CREDITS }

@Composable
fun SettingsScreen(
    viewModel: HardwareViewModel, 
    mainViewModel: MainViewModel, 
    settingsManager: SettingsManager, 
    soundManager: SoundManager? = null, 
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(SettingsTab.HARDWARE) }
    val isScanning by mainViewModel.isScanning.collectAsState()
    val scanProgress by mainViewModel.scanProgress.collectAsState()
    val scanStatus by mainViewModel.scanStatusMessage.collectAsState()
    
    val context = LocalContext.current
    val keyMapper = remember { KeyMapper(context) }
    val primaryColor = ThemeManager.primaryColor
    val sidebarScrollState = rememberScrollState()
    val lang = settingsManager.appLanguage

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Row(modifier = Modifier.fillMaxSize()) {
            // SIDEBAR
            Column(
                modifier = Modifier
                    .width(160.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF080808))
                    .verticalScroll(sidebarScrollState)
                    .padding(vertical = 12.dp)
            ) {
                Text(Translator.getString(lang, "SYS_HARDWARE"), modifier = Modifier.padding(16.dp), color = primaryColor, fontSize = 14.sp, fontWeight = FontWeight.Black)
                val onSelect = { tab: SettingsTab -> selectedTab = tab }

                SidebarItemCompact(Icons.Default.Memory, Translator.getString(lang, "SYS_HARDWARE"), selectedTab == SettingsTab.HARDWARE, onClick = { onSelect(SettingsTab.HARDWARE) }, onFocus = { onSelect(SettingsTab.HARDWARE) })
                SidebarItemCompact(Icons.Default.Smartphone, Translator.getString(lang, "SYS_DEVICE"), selectedTab == SettingsTab.DEVICE, onClick = { onSelect(SettingsTab.DEVICE) }, onFocus = { onSelect(SettingsTab.DEVICE) })
                SidebarItemCompact(Icons.AutoMirrored.Filled.LibraryBooks, Translator.getString(lang, "LIBRARY_TITLE"), selectedTab == SettingsTab.LIBRARY, onClick = { onSelect(SettingsTab.LIBRARY) }, onFocus = { onSelect(SettingsTab.LIBRARY) })
                SidebarItemCompact(Icons.Default.SettingsInputComponent, "EMULADORES", selectedTab == SettingsTab.EMULATORS, onClick = { onSelect(SettingsTab.EMULATORS) }, onFocus = { onSelect(SettingsTab.EMULATORS) })
                SidebarItemCompact(Icons.Default.Gamepad, "CONTROLES", selectedTab == SettingsTab.CONTROLS, onClick = { onSelect(SettingsTab.CONTROLS) }, onFocus = { onSelect(SettingsTab.CONTROLS) })
                SidebarItemCompact(Icons.Default.Palette, "APARIENCIA", selectedTab == SettingsTab.APPEARANCE, onClick = { onSelect(SettingsTab.APPEARANCE) }, onFocus = { onSelect(SettingsTab.APPEARANCE) })
                SidebarItemCompact(Icons.Default.CloudDownload, "SCRAPER", selectedTab == SettingsTab.SCRAPER, onClick = { onSelect(SettingsTab.SCRAPER) }, onFocus = { onSelect(SettingsTab.SCRAPER) })
                SidebarItemCompact(Icons.Default.EmojiEvents, "LOGROS", selectedTab == SettingsTab.ACHIEVEMENTS, onClick = { onSelect(SettingsTab.ACHIEVEMENTS) }, onFocus = { onSelect(SettingsTab.ACHIEVEMENTS) })
                SidebarItemCompact(Icons.Default.SmartToy, "ARCHITECT", selectedTab == SettingsTab.ARCHITECT, onClick = { onSelect(SettingsTab.ARCHITECT) }, onFocus = { onSelect(SettingsTab.ARCHITECT) })
                SidebarItemCompact(Icons.Default.ShoppingCart, "TIENDA PRO", selectedTab == SettingsTab.STORE, onClick = { onSelect(SettingsTab.STORE) }, onFocus = { onSelect(SettingsTab.STORE) })
                SidebarItemCompact(Icons.Default.Info, "CRÉDITOS", selectedTab == SettingsTab.CREDITS, onClick = { onSelect(SettingsTab.CREDITS) }, onFocus = { onSelect(SettingsTab.CREDITS) })
                
                Spacer(modifier = Modifier.height(32.dp))
                SidebarItemCompact(Icons.AutoMirrored.Filled.ArrowBack, Translator.getString(lang, "BACK_HUB"), false, onClick = onBack, onFocus = {})
            }
            
            // CONTENT AREA
            Box(modifier = Modifier.weight(1f).fillMaxHeight().padding(24.dp)) {
                when (selectedTab) {
                    SettingsTab.HARDWARE -> HardwareSettingsUltra(viewModel, settingsManager)
                    SettingsTab.DEVICE -> DeviceSettingsScreen(settingsManager, context.applicationContext as Application)
                    SettingsTab.LIBRARY -> LibrarySettingsCompact(mainViewModel, settingsManager)
                    SettingsTab.EMULATORS -> EmulatorSettingsScreen(mainViewModel, settingsManager)
                    SettingsTab.CONTROLS -> AdvancedControlsMapper(keyMapper)
                    SettingsTab.APPEARANCE -> AppearanceSettingsScreen(settingsManager, soundManager)
                    SettingsTab.SCRAPER -> ScraperSettingsScreen(mainViewModel, settingsManager)
                    SettingsTab.ACHIEVEMENTS -> RetroAchievementsSettingsScreen(settingsManager)
                    SettingsTab.ARCHITECT -> ArchitectChatScreen(mainViewModel = mainViewModel, hardwareViewModel = viewModel, onSafeClose = {})
                    SettingsTab.STORE -> ProStoreScreen(settingsManager)
                    SettingsTab.CREDITS -> CreditsScreen()
                }
            }
        }

        // VENTANA DE SINCRONIZACIÓN NEÓN (Overlay)
        if (isScanning) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f)).clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color(0xFF0A0A0A),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(2.dp, primaryColor),
                    modifier = Modifier.width(450.dp).padding(24.dp).shadow(30.dp, RoundedCornerShape(24.dp), ambientColor = primaryColor, spotColor = primaryColor)
                ) {
                    Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        val infiniteTransition = rememberInfiniteTransition(label = "neon")
                        val glowAlpha by infiniteTransition.animateFloat(initialValue = 0.5f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "glow")
                        val rotation by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)), label = "rotation")
                        
                        Icon(Icons.Default.Sync, null, tint = primaryColor, modifier = Modifier.size(56.dp).graphicsLayer { rotationZ = rotation }.alpha(glowAlpha))
                        Spacer(Modifier.height(24.dp))
                        Text(Translator.getString(lang, "NEURAL_LINK"), color = primaryColor, fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = 2.sp, modifier = Modifier.alpha(glowAlpha))
                        Spacer(Modifier.height(32.dp))
                        LinearProgressIndicator(progress = { scanProgress }, color = primaryColor, trackColor = Color.White.copy(alpha = 0.05f), modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape))
                        Spacer(Modifier.height(20.dp))
                        
                        Text(
                            text = scanStatus.uppercase(), 
                            color = Color(0xFFFF00FF),
                            fontSize = 11.sp, 
                            textAlign = TextAlign.Center, 
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.alpha(glowAlpha)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SidebarItemCompact(icon: ImageVector, text: String, isSelected: Boolean, onClick: () -> Unit, onFocus: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp).onFocusChanged { if (it.isFocused) onFocus() }.clickable(onClick = onClick).focusable(),
        color = if (isSelected) ThemeManager.primaryColor.copy(alpha = 0.2f) else Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (isSelected) ThemeManager.primaryColor else Color.Gray, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Text(text, color = if (isSelected) Color.White else Color.Gray, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

@Composable
fun DeviceSettingsScreen(settingsManager: SettingsManager, application: Application) {
    val lang = settingsManager.appLanguage
    val primaryColor = ThemeManager.primaryColor
    val allProfiles = HardwareControlManagerBeta.hardwareProfiles.values.toList()
    var selectedProfileId by remember { mutableStateOf(settingsManager.manualProfile) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(Translator.getString(lang, "SYS_DEVICE"), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = ThemeManager.titleFont)
        Text(Translator.getString(lang, "DEVICE_SELECT_DESC"), color = Color.Gray, fontSize = 11.sp)
        Spacer(Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 200.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(allProfiles) { profile ->
                DeviceProfileCard(
                    profile = profile,
                    primaryColor = primaryColor,
                    isSelected = selectedProfileId == profile.id,
                    onClick = {
                        selectedProfileId = profile.id
                        settingsManager.manualProfile = profile.id
                        HardwareControlManagerBeta.initialize(profile.id, application)
                    }
                )
            }
        }
    }
}

@Composable
fun DeviceProfileCard(
    profile: HardwareControlManagerBeta.HardwareProfile, 
    primaryColor: Color, 
    isSelected: Boolean, 
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .scale(if (isFocused || isSelected) 1.02f else 1f),
        color = if (isSelected) primaryColor.copy(alpha = 0.2f) else if (isFocused) Color.White.copy(alpha = 0.05f) else Color(0xFF111111),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (isSelected || isFocused) 2.dp else 1.dp, if (isSelected || isFocused) primaryColor else Color.White.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.05f), CircleShape), 
                contentAlignment = Alignment.Center
            ) { 
                Icon(
                    painter = painterResource(profile.iconRes), 
                    contentDescription = null, 
                    tint = if (isSelected || isFocused) primaryColor else Color.Gray, 
                    modifier = Modifier.size(24.dp)
                ) 
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(profile.name.uppercase(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, maxLines = 1)
                Text(profile.chipset, color = primaryColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, null, tint = primaryColor, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun EmulatorSettingsScreen(mainViewModel: MainViewModel, settingsManager: SettingsManager) {
    val platforms by mainViewModel.activePlatforms.collectAsState(initial = emptySet())
    var coreSelectorTarget by remember { mutableStateOf<String?>(null) }
    val lang = settingsManager.appLanguage

    Column(modifier = Modifier.fillMaxSize()) {
        Text("CONFIGURACIÓN DE MOTORES", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = ThemeManager.titleFont)
        Text(Translator.getString(lang, "SETTINGS_CORE"), color = Color.Gray, fontSize = 11.sp)
        Spacer(Modifier.height(24.dp))

        Box(Modifier.fillMaxSize()) {
            if (platforms.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se han detectado plataformas aún.", color = Color.DarkGray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val platformList = platforms.toList().sorted()
                    items(platformList) { platformId ->
                        EmulatorOverrideRow(
                            platformId = platformId, 
                            settingsManager = settingsManager, 
                            onScrape = { mainViewModel.forceScrapePlatform(platformId) },
                            onOpenCoreSelector = { coreSelectorTarget = platformId }
                        )
                    }
                }
            }
        }
    }

    if (coreSelectorTarget != null) {
        CoreSelectorDialog(
            platformId = coreSelectorTarget!!,
            settingsManager = settingsManager,
            onDismiss = { coreSelectorTarget = null }
        )
    }
}

@Composable
fun EmulatorOverrideRow(platformId: String, settingsManager: SettingsManager, onScrape: () -> Unit, onOpenCoreSelector: () -> Unit) {
    var currentMode by remember { mutableStateOf(settingsManager.getPreferredEmulator(platformId)) }
    val currentCore = settingsManager.getPreferredCore(platformId)
    val systemInfo = RetroArchDatabase.findSystemById(platformId)
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF111111),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(platformId.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                val coreText = if (currentMode == "RETROARCH") {
                    "Core: ${currentCore ?: systemInfo?.defaultCore ?: "Default"}"
                } else {
                    "Aplicación Independiente"
                }
                Text(coreText, color = Color.Gray, fontSize = 10.sp)
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (currentMode == "RETROARCH" && systemInfo != null && systemInfo.alternativeCores.isNotEmpty()) {
                    IconButton(onClick = onOpenCoreSelector, modifier = Modifier.size(36.dp).background(Color.White.copy(alpha = 0.05f), CircleShape)) {
                        Icon(Icons.Default.Settings, null, tint = ThemeManager.primaryColor, modifier = Modifier.size(18.dp))
                    }
                }

                IconButton(onClick = onScrape, modifier = Modifier.size(36.dp).background(Color.White.copy(alpha = 0.05f), CircleShape)) {
                    Icon(Icons.Default.CloudDownload, null, tint = ThemeManager.primaryColor, modifier = Modifier.size(18.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    EmulatorToggleButton("RA", currentMode == "RETROARCH") {
                        currentMode = "RETROARCH"
                        settingsManager.setPreferredEmulator(platformId, "RETROARCH")
                    }
                    EmulatorToggleButton("EXT", currentMode == "STANDALONE") {
                        currentMode = "STANDALONE"
                        settingsManager.setPreferredEmulator(platformId, "STANDALONE")
                    }
                }
            }
        }
    }
}

@Composable
fun CoreSelectorDialog(platformId: String, settingsManager: SettingsManager, onDismiss: () -> Unit) {
    val systemInfo = RetroArchDatabase.findSystemById(platformId) ?: return
    val cores = listOf(systemInfo.defaultCore) + systemInfo.alternativeCores
    var selectedCore by remember { mutableStateOf(settingsManager.getPreferredCore(platformId) ?: systemInfo.defaultCore) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Color(0xFF0F0F0F),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(2.dp, ThemeManager.primaryColor),
            modifier = Modifier.width(320.dp).padding(16.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("SELECCIONAR CORE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text(platformId.uppercase(), color = ThemeManager.primaryColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(cores) { core ->
                        val isSelected = selectedCore == core
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { 
                                selectedCore = core
                                settingsManager.setPreferredCore(platformId, core)
                                onDismiss()
                            },
                            color = if (isSelected) ThemeManager.primaryColor.copy(alpha = 0.2f) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (isSelected) ThemeManager.primaryColor else Color.White.copy(alpha = 0.1f))
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(core, color = if (isSelected) Color.White else Color.Gray, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                                if (isSelected) Icon(Icons.Default.Check, null, tint = ThemeManager.primaryColor, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("CANCELAR", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun EmulatorToggleButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.width(48.dp).height(32.dp).clickable { onClick() },
        color = if (isSelected) ThemeManager.primaryColor else Color(0xFF222222),
        shape = RoundedCornerShape(6.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = if (isSelected) Color.Black else Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun HardwareSettingsUltra(viewModel: HardwareViewModel, settingsManager: SettingsManager) {
    val state by viewModel.uiState.collectAsState()
    val primaryColor = ThemeManager.primaryColor
    val focusRequester = remember { FocusRequester() }
    val profiles = listOf("ECO", "BALANCED", "PERFORMANCE", "EXTREME")
    val selectedProfileIndex = profiles.indexOf(state.currentProfile).coerceAtLeast(0)
    val lang = settingsManager.appLanguage

    val hasRoot = Shell.rootAccess()
    val hasShizuku = HardwareControlManagerBeta.isShizukuAvailable()

    Column(modifier = Modifier.fillMaxSize().focusRequester(focusRequester).focusable().onKeyEvent { false }) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(Translator.getString(lang, "TELEMETRY_TITLE"), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = ThemeManager.titleFont)
            Row(verticalAlignment = Alignment.CenterVertically) {
                PrivilegeBadge("ROOT", hasRoot, Color.Red)
                Spacer(Modifier.width(8.dp))
                PrivilegeBadge("SHIZUKU", hasShizuku, Color.Green) {
                    if (!hasShizuku) HardwareControlManagerBeta.requestShizukuPermission(1001)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        PerformanceMonitorGraph(state, primaryColor)
        Spacer(Modifier.height(32.dp))
        
        Text(Translator.getString(lang, "ENERGY_PROFILES"), color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = ThemeManager.titleFont)
        Spacer(Modifier.height(12.dp))
        Slider(
            value = selectedProfileIndex.toFloat(),
            onValueChange = { newValue ->
                val newIndex = newValue.toInt()
                val profile = profiles[newIndex]
                val isLocked = profile == "EXTREME" && !state.isPro
                if (!isLocked) viewModel.setManualProfile(profile)
            },
            steps = 2,
            valueRange = 0f..3f,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = SliderDefaults.colors(activeTrackColor = primaryColor, inactiveTrackColor = primaryColor.copy(alpha = 0.3f), thumbColor = primaryColor)
        ) 
        Text(
            text = "MODO: ${state.currentProfile}" + if (state.currentProfile == "EXTREME" && !state.isPro) " (PRO)" else "",
            color = if (state.currentProfile == "EXTREME" && !state.isPro) Color.Red else Color.White,
            fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp), fontFamily = ThemeManager.titleFont
        )

        Spacer(Modifier.height(32.dp))

        Text("REFRIGERACIÓN MANUAL", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = ThemeManager.titleFont)
        Spacer(Modifier.height(12.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            FanButton("OFF", 0, state.fanSpeedLevel, primaryColor) { viewModel.setManualFanSpeed(0) }
            FanButton("SILENT", 1, state.fanSpeedLevel, primaryColor) { viewModel.setManualFanSpeed(1) }
            FanButton("BALANCED", 2, state.fanSpeedLevel, primaryColor) { viewModel.setManualFanSpeed(2) }
            FanButton("MAX", 3, state.fanSpeedLevel, Color.Red) { viewModel.setManualFanSpeed(3) }
        }

        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }
}

@Composable
fun PrivilegeBadge(label: String, active: Boolean, activeColor: Color, onClick: () -> Unit = {}) {
    Surface(
        color = if (active) activeColor.copy(alpha = 0.2f) else Color.DarkGray.copy(alpha = 0.3f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, if (active) activeColor else Color.DarkGray),
        modifier = Modifier.clickable(enabled = !active) { onClick() }
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            color = if (active) activeColor else Color.Gray,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun FanButton(text: String, level: Int, currentLevelProgress: Float, activeColor: Color, onClick: () -> Unit) {
    val currentLevelInt = (currentLevelProgress * 3).toInt()
    val isSelected = currentLevelInt == level
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) activeColor else Color(0xFF222222), contentColor = if (isSelected) Color.Black else Color.Gray),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.width(80.dp).height(40.dp)
    ) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
fun PerformanceMonitorGraph(state: HardwareUiState, primaryColor: Color) {
    Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF0A0A0A), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("TELEMETRÍA", color = primaryColor, fontSize = 10.sp, fontWeight = FontWeight.Black, fontFamily = ThemeManager.titleFont)
                Text("FPS: ${state.estimatedFps}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = ThemeManager.titleFont)
            }
            Spacer(Modifier.height(16.dp))
            TelemetryBar("CPU", state.cpuLoad, primaryColor)
            TelemetryBar("RAM", state.ramUsage, primaryColor)
            TelemetryBar("TEMP", state.temperature / 90f, if (state.temperature > 65) Color.Red else primaryColor, isWarning = state.temperature > 65)
            TelemetryBar("FAN", state.fanSpeedLevel, Color.White)
        }
    }
}

@Composable
fun TelemetryBar(label: String, progress: Float, barColor: Color, isWarning: Boolean = false) {
    val infiniteTransition = rememberInfiniteTransition(label = "warning")
    val alphaAnim by infiniteTransition.animateFloat(initialValue = 1f, targetValue = 0.4f, animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "alpha")
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp), fontFamily = ThemeManager.titleFont)
        LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, color = barColor, trackColor = Color.White.copy(alpha = 0.1f), modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)).alpha(if(isWarning) alphaAnim else 1f))
        Text(text = "${(progress * 100).toInt()}%", color = if(isWarning) Color.Red else Color.White, fontSize = 9.sp, modifier = Modifier.padding(start = 8.dp), fontFamily = ThemeManager.titleFont)
    }
}

@Composable
fun LibrarySettingsCompact(mainViewModel: MainViewModel, settingsManager: SettingsManager) {
    val context = LocalContext.current
    val lang = settingsManager.appLanguage
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri -> 
        uri?.let { 
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            settingsManager.romsLocation = it.toString()
            mainViewModel.startSmartSync()
        } 
    }
    val platformCounts by mainViewModel.platformGameCounts.collectAsState()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text(Translator.getString(lang, "LIBRARY_TITLE"), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = ThemeManager.titleFont)
        Spacer(Modifier.height(24.dp))
        
        SettingCard("UBICACIÓN DE ROMS", settingsManager.romsLocation.ifEmpty { "No vinculada" }, Icons.Default.Folder) { launcher.launch(null) }
        
        Spacer(Modifier.height(16.dp))
        Text("OPERACIONES DE MANTENIMIENTO", color = ThemeManager.primaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        
        SettingCard("RE-ESCANEO COMPLETO", "Limpiar DB y buscar cambios en archivos.", Icons.Default.Refresh) { mainViewModel.fullRescan() }
        Spacer(Modifier.height(8.dp))
        SettingCard("SCRAPING TOTAL (POR LOTES)", "Busca arte para todos los juegos que falten.", Icons.Default.CloudDownload) { mainViewModel.forceScrapeAll() }
        
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val totalGames = platformCounts.values.sum()
            StatBox("JUEGOS", "$totalGames")
            StatBox("PLATAFORMAS", "${platformCounts.size}")
        }
    }
}

@Composable
fun StatBox(label: String, value: String) {
    Surface(color = Color(0xFF111111), shape = RoundedCornerShape(8.dp), modifier = Modifier.width(100.dp)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = ThemeManager.primaryColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(label, color = Color.Gray, fontSize = 10.sp)
        }
    }
}

@Composable
fun AppearanceSettingsScreen(settingsManager: SettingsManager, soundManager: SoundManager?) {
    var showFps by remember { mutableStateOf(true) }
    var currentLanguage by remember { mutableStateOf(settingsManager.appLanguage) }
    var currentTheme by remember { mutableStateOf(settingsManager.currentTheme) }
    var bgMusicEnabled by remember { mutableStateOf(settingsManager.isBackgroundMusicEnabled) }
    val context = LocalContext.current
    val lang = settingsManager.appLanguage

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("APARIENCIA Y SENTIDOS", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = ThemeManager.titleFont)
        Spacer(Modifier.height(24.dp))
        
        Text("TEMA DEL DASHBOARD", color = ThemeManager.primaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeButton("CLÁSICO", SettingsManager.THEME_CLASSIC, currentTheme) { 
                currentTheme = SettingsManager.THEME_CLASSIC
                settingsManager.currentTheme = SettingsManager.THEME_CLASSIC
            }
            ThemeButton("INMERSIVO", SettingsManager.THEME_INMERSIVE, currentTheme) { 
                currentTheme = SettingsManager.THEME_INMERSIVE
                settingsManager.currentTheme = SettingsManager.THEME_INMERSIVE
            }
            ThemeButton("ULTRA", SettingsManager.THEME_ULTRA, currentTheme) { 
                currentTheme = SettingsManager.THEME_ULTRA
                settingsManager.currentTheme = SettingsManager.THEME_ULTRA
            }
        }

        Spacer(Modifier.height(32.dp))
        Text("AUDIO DEL SISTEMA", color = ThemeManager.primaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        SwitchSetting("Música de Fondo", "Activa el BGM Neon Dreams", bgMusicEnabled) { 
            bgMusicEnabled = it
            soundManager?.toggleBgm(it)
        }

        Spacer(Modifier.height(32.dp))
        Text("IDIOMA DEL SISTEMA", color = ThemeManager.primaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LanguageButton("ES", "es_ES", currentLanguage) { currentLanguage = "es_ES"; settingsManager.appLanguage = "es_ES" }
            LanguageButton("EN", "en_US", currentLanguage) { currentLanguage = "en_US"; settingsManager.appLanguage = "en_US" }
            LanguageButton("FR", "fr_FR", currentLanguage) { currentLanguage = "fr_FR"; settingsManager.appLanguage = "fr_FR" }
            LanguageButton("PT", "pt_BR", currentLanguage) { currentLanguage = "pt_BR"; settingsManager.appLanguage = "pt_BR" }
            LanguageButton("ZH", "zh_CN", currentLanguage) { currentLanguage = "zh_CN"; settingsManager.appLanguage = "zh_CN" }
            LanguageButton("JA", "ja_JP", currentLanguage) { currentLanguage = "ja_JP"; settingsManager.appLanguage = "ja_JP" }
        }
        Spacer(Modifier.height(24.dp))
        SwitchSetting("Mostrar FPS", "Contador de frames en tiempo real", showFps) { showFps = it }
        
        Spacer(Modifier.height(24.dp))
        Text(Translator.getString(lang, "NEURAL_LINK"), color = ThemeManager.primaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ColorSwatch(NeonBlue, ThemeManager.primaryColor == NeonBlue) { ThemeManager.savePrimaryColor(context, NeonBlue) }
            ColorSwatch(Color.Cyan, ThemeManager.primaryColor == Color.Cyan) { ThemeManager.savePrimaryColor(context, Color.Cyan) }
            ColorSwatch(Color.Magenta, ThemeManager.primaryColor == Color.Magenta) { ThemeManager.savePrimaryColor(context, Color.Magenta) }
            ColorSwatch(Color.Green, ThemeManager.primaryColor == Color.Green) { ThemeManager.savePrimaryColor(context, Color.Green) }
            ColorSwatch(Color(0xFFFFA500), ThemeManager.primaryColor == Color(0xFFFFA500)) { ThemeManager.savePrimaryColor(context, Color(0xFFFFA500)) }
        }
    }
}

@Composable
fun ThemeButton(label: String, themeId: String, currentTheme: String, onClick: () -> Unit) {
    val isSelected = currentTheme == themeId
    Surface(
        color = if (isSelected) ThemeManager.primaryColor else Color(0xFF111111),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(text = label, color = if (isSelected) Color.Black else Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontSize = 11.sp)
    }
}

@Composable
fun LanguageButton(label: String, langCode: String, currentLang: String, onClick: () -> Unit) {
    val isSelected = currentLang == langCode
    Surface(
        color = if (isSelected) ThemeManager.primaryColor else Color(0xFF111111),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(text = label, color = if (isSelected) Color.Black else Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontSize = 11.sp)
    }
}

@Composable
fun SwitchSetting(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().background(Color(0xFF111111), RoundedCornerShape(8.dp)).padding(16.dp)) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, color = Color.Gray, fontSize = 11.sp)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = ThemeManager.primaryColor, checkedTrackColor = ThemeManager.primaryColor.copy(alpha = 0.5f)))
    }
}

@Composable
fun ColorSwatch(color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(color).border(2.dp, if (isSelected) Color.White else Color.Transparent, CircleShape).clickable { onClick() })
}

@Composable
fun ScraperSettingsScreen(mainViewModel: MainViewModel, settingsManager: SettingsManager) {
    var apiKey by remember { mutableStateOf(settingsManager.scraperApiKey) }
    var user by remember { mutableStateOf(settingsManager.scraperUsername) }
    var pass by remember { mutableStateOf(settingsManager.scraperPassword) }
    var selectedSource by remember { mutableStateOf(settingsManager.scraperSource) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("SCRAPER DE ARTE", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = ThemeManager.titleFont)
        Text("Selecciona el motor de búsqueda y tus credenciales.", color = Color.Gray, fontSize = 11.sp)
        Spacer(Modifier.height(24.dp))
        
        Text("FUENTE DE DATOS", color = ThemeManager.primaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SourceButton("SCREEN SCRAPER", selectedSource == "SCREENSCRAPER") { 
                selectedSource = "SCREENSCRAPER"
                settingsManager.scraperSource = "SCREENSCRAPER"
            }
            SourceButton("THE GAMES DB", selectedSource == "THEGAMESDB") { 
                selectedSource = "THEGAMESDB"
                settingsManager.scraperSource = "THEGAMESDB"
            }
        }
        
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(value = user, onValueChange = { user = it; settingsManager.scraperUsername = it }, label = { Text("Usuario (ScreenScraper)") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.Gray, focusedBorderColor = ThemeManager.primaryColor, unfocusedBorderColor = Color.Gray))
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = pass, onValueChange = { pass = it; settingsManager.scraperPassword = it }, label = { Text("Contraseña (ScreenScraper)") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.Gray, focusedBorderColor = ThemeManager.primaryColor, unfocusedBorderColor = Color.Gray))
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = apiKey, onValueChange = { apiKey = it; settingsManager.scraperApiKey = it }, label = { Text("API Key (TheGamesDB)") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.Gray, focusedBorderColor = ThemeManager.primaryColor, unfocusedBorderColor = Color.Gray))
        
        Spacer(Modifier.height(24.dp))
        Text("CONSEJO: ScreenScraper es más preciso para roms exactas, TheGamesDB es mejor para títulos generales.", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun SourceButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) ThemeManager.primaryColor else Color(0xFF111111),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(text = label, color = if (isSelected) Color.Black else Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontSize = 11.sp)
    }
}

@Composable
fun RetroAchievementsSettingsScreen(settingsManager: SettingsManager) {
    var username by remember { mutableStateOf(settingsManager.raUsername) }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val raManager = remember { RetroAchievementsManager(context, settingsManager) }
    var statusText by remember { mutableStateOf("Desconectado") }
    var isLoading by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("LOGROS RETRO (RETROACHIEVEMENTS)", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = ThemeManager.titleFont)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Usuario") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.Gray, focusedBorderColor = ThemeManager.primaryColor, unfocusedBorderColor = Color.Gray))
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.Gray, focusedBorderColor = ThemeManager.primaryColor, unfocusedBorderColor = Color.Gray))
        Spacer(Modifier.height(24.dp))
        
        Button(
            onClick = {
                isLoading = true
                raManager.login(username, password) { success, msg ->
                    isLoading = false
                    statusText = msg
                }
            }, 
            modifier = Modifier.fillMaxWidth(), 
            colors = ButtonDefaults.buttonColors(containerColor = ThemeManager.primaryColor),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
            else Text("INICIAR SESIÓN", color = Color.Black, fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(16.dp))
        Text("ESTADO: $statusText", color = if (statusText == "Conectado") Color.Green else Color.White, fontSize = 14.sp)
    }
}

@Composable
fun ProStoreScreen(settingsManager: SettingsManager) {
    val isAuth = settingsManager.isAuthorized
    val userEmail = settingsManager.userEmail ?: "No identificado"

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = if (isAuth) Icons.Default.VerifiedUser else Icons.Default.ShoppingCart, contentDescription = null, tint = ThemeManager.primaryColor, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("ESTADO DE LICENCIA", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(if (isAuth) "AUTORIZADO" else "VERIFICANDO COMPRA...", color = if (isAuth) Color.Green else Color.Yellow, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Cuenta: $userEmail", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun SettingCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), color = Color(0xFF111111), shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.Gray)
            Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}

@Composable
fun AdvancedControlsMapper(keyMapper: KeyMapper) {
    var mappingAction by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }
    val primaryColor = ThemeManager.primaryColor
    Column(modifier = Modifier.fillMaxSize()) {
        Text("MAPEADO DE HARDWARE", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = ThemeManager.titleFont)
        Text("Toca una entrada para asignar su botón físico.", color = Color.Gray, fontSize = 11.sp)
        Spacer(Modifier.height(24.dp))
        if (mappingAction != null) {
            Box(modifier = Modifier.fillMaxSize().focusRequester(focusRequester).focusable().onKeyEvent {
                val code = it.nativeKeyEvent.keyCode
                when (mappingAction) {
                    "A" -> keyMapper.actionA = code; "B" -> keyMapper.actionB = code; "X" -> keyMapper.actionX = code; "Y" -> keyMapper.actionY = code
                    "UP" -> keyMapper.dpadUp = code; "DOWN" -> keyMapper.dpadDown = code; "LEFT" -> keyMapper.dpadLeft = code; "RIGHT" -> keyMapper.dpadRight = code
                    "L1" -> keyMapper.btnL1 = code; "R1" -> keyMapper.btnR1 = code
                    "M" -> keyMapper.btnM = code; "T" -> keyMapper.btnT = code; "K" -> keyMapper.btnK = code
                }
                mappingAction = null; true
            }, contentAlignment = Alignment.Center) {
                Text("PULSA BOTÓN PARA: $mappingAction", color = primaryColor, fontSize = 24.sp, fontWeight = FontWeight.Black, fontFamily = ThemeManager.titleFont)
                LaunchedEffect(Unit) { focusRequester.requestFocus() }
            }
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(0.4f).fillMaxHeight(), contentAlignment = Alignment.Center) { GamepadVisualizerUltra(primaryColor) }
                LazyColumn(modifier = Modifier.weight(0.6f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    item { Text("PRINCIPALES", color = primaryColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = ThemeManager.titleFont) }
                    items(listOf("A", "B", "X", "Y")) { a -> KeyMappingRow("BOTÓN $a", primaryColor) { mappingAction = a } }
                    item { Spacer(Modifier.height(12.dp)); Text("CRUCETA Y GATILLOS", color = primaryColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = ThemeManager.titleFont) }
                    items(listOf("UP", "DOWN", "LEFT", "RIGHT", "L1", "R1", "M", "T", "K")) { a -> KeyMappingRow(a, primaryColor) { mappingAction = a } }
                    item { 
                        Button(onClick = { keyMapper.reset() }, modifier = Modifier.padding(top = 16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                            Text("RESETEAR TODO", color = Color.White, fontSize = 12.sp, fontFamily = ThemeManager.titleFont)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GamepadVisualizerUltra(primaryColor: Color) {
    Canvas(modifier = Modifier.size(240.dp)) {
        drawRoundRect(Color.DarkGray, size = size.copy(height = size.height * 0.7f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(30f), style = Stroke(4f), topLeft = Offset(0f, size.height * 0.15f))
        drawCircle(primaryColor.copy(alpha = 0.5f), radius = 25f, center = Offset(60f, size.height * 0.35f), style = Stroke(2f))
        drawCircle(Color.White, radius = 20f, center = Offset(60f, size.height * 0.65f), style = Stroke(2f))
    }
}

@Composable
fun KeyMappingRow(label: String, primaryColor: Color, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable { onClick() }, color = Color(0xFF111111), shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.White, fontSize = 12.sp, fontFamily = ThemeManager.titleFont)
            Text("CONFIGURAR", color = primaryColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = ThemeManager.titleFont)
        }
    }
}
