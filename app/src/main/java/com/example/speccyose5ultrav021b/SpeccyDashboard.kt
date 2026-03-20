@file:OptIn(ExperimentalFoundationApi::class)
package com.example.speccyose5ultrav021b

import android.view.KeyEvent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.text.SimpleDateFormat
import kotlin.math.absoluteValue

// --- FUNCIONES MATEMÁTICAS ---
private fun lerpFloat(start: Float, stop: Float, fraction: Float): Float = (1 - fraction) * start + fraction * stop

// --- MODIFICADORES VISUALES (EFECTO CRT LEVE) ---
fun Modifier.crtEffect() = this.drawWithCache {
    val gradient = Brush.radialGradient(
        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
        center = Offset(size.width / 2, size.height / 2),
        radius = size.width * 0.75f
    )
    onDrawWithContent {
        drawContent()
        val stripeWidth = 4f
        var y = 0f
        while (y < size.height) {
            drawRect(color = Color.Black.copy(alpha = 0.12f), topLeft = Offset(0f, y), size = Size(size.width, stripeWidth))
            y += stripeWidth * 2
        }
        drawRect(brush = gradient)
    }
}

// --- FILTRADO INTELIGENTE POR IDIOMA ---
private fun filterGamesByLanguage(games: List<Game>, lang: String): List<Game> {
    if (games.isEmpty()) return games
    
    // Agrupamos por título limpio para detectar duplicados de distintas regiones
    return games.groupBy { it.title }.map { (_, versions) ->
        if (versions.size == 1) {
            versions.first()
        } else {
            // Si hay varias versiones del mismo juego, elegimos la mejor según el idioma del sistema
            versions.maxByOrNull { game ->
                val fileName = game.fileName.lowercase()
                when (lang) {
                    "es_ES" -> when {
                        fileName.contains("(spain)") -> 10
                        fileName.contains("(europe)") -> 8
                        fileName.contains("(es)") || fileName.contains(",es") -> 7
                        fileName.contains("(usa)") -> 5
                        fileName.contains("(world)") -> 4
                        else -> 1
                    }
                    "en_US" -> when {
                        fileName.contains("(usa)") -> 10
                        fileName.contains("(world)") -> 9
                        fileName.contains("(europe)") -> 7
                        else -> 1
                    }
                    "fr_FR" -> when {
                        fileName.contains("(france)") -> 10
                        fileName.contains("(europe)") -> 8
                        fileName.contains("(fr)") || fileName.contains(",fr") -> 7
                        fileName.contains("(usa)") -> 5
                        else -> 1
                    }
                    "pt_BR" -> when {
                        fileName.contains("(brazil)") || fileName.contains("(portugal)") -> 10
                        fileName.contains("(europe)") -> 8
                        fileName.contains("(pt)") || fileName.contains(",pt") -> 7
                        fileName.contains("(usa)") -> 5
                        else -> 1
                    }
                    else -> when {
                        fileName.contains("(usa)") -> 10
                        fileName.contains("(world)") -> 9
                        else -> 1
                    }
                }
            } ?: versions.first()
        }
    }.sortedBy { it.title }
}

@UnstableApi
@Composable
fun SpeccyDashboard(
    mainViewModel: MainViewModel,
    hardwareViewModel: HardwareViewModel,
    settingsManager: SettingsManager,
    soundManager: SoundManager,
    hapticHandler: HapticHandler,
    onSettingsClick: () -> Unit,
    onArchitectClick: () -> Unit
) {
    val currentTheme = settingsManager.currentTheme
    val favoriteGames by mainViewModel.favoriteGames.collectAsState(initial = emptyList())
    val recentGames by mainViewModel.recentGames.collectAsState(initial = emptyList())
    val activePlatforms by mainViewModel.activePlatforms.collectAsState(initial = emptySet())
    val hardwareState by hardwareViewModel.uiState.collectAsState()
    val platformProgress by mainViewModel.platformProgress.collectAsState()
    
    var selectedPlatform by remember { mutableStateOf<String?>(null) }
    var focusedPlatformInCarousel by remember { mutableStateOf<String?>(null) }
    var isHardwarePanelVisible by remember { mutableStateOf(false) }
    var isAppViewActive by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val launcherManager = remember { LauncherManager(context) }
    val lang = settingsManager.appLanguage

    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = Color(0xFF050505)

    val displayPlatforms = remember(activePlatforms, favoriteGames, recentGames) {
        val list = mutableListOf<String>()
        list.add("SYS_APPS")      
        list.add("SYS_IA")        
        list.add("SYS_HARDWARE")  
        if (favoriteGames.isNotEmpty()) list.add("favoritos")
        if (recentGames.isNotEmpty()) list.add("recientes")
        list.addAll(activePlatforms)
        list.add("SYS_SETTINGS")
        list
    }

    var bootAlpha by remember { mutableFloatStateOf(1f) }
    LaunchedEffect(Unit) {
        delay(300)
        bootAlpha = 0f
    }

    Scaffold(containerColor = backgroundColor) { padding ->
        val isImmersive = currentTheme == SettingsManager.THEME_INMERSIVE
        val isUltra = currentTheme == SettingsManager.THEME_ULTRA
        
        val dashboardModifier = if (isImmersive || isUltra) {
            Modifier.fillMaxSize().padding(padding)
        } else {
            Modifier.fillMaxSize().padding(padding).crtEffect()
        }

        Box(modifier = dashboardModifier) {
            
            if (!isImmersive && !isUltra) {
                RetroGridBackground(primaryColor = primaryColor)
            }

            if (selectedPlatform != null || isAppViewActive) {
                val bgImage = when(selectedPlatform) {
                    "favoritos" -> "file:///android_asset/contentimg/fanart/favorites_bg.jpg"
                    "recientes" -> "file:///android_asset/contentimg/fanart/recent_bg.jpg"
                    else -> "file:///android_asset/contentimg/fanart/${selectedPlatform?.lowercase() ?: "system_bg"}.jpg"
                }

                AsyncImage(
                    model = bgImage,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().alpha(0.2f).blur(16.dp),
                    contentScale = ContentScale.Crop
                )
            }

            AnimatedContent(
                targetState = selectedPlatform == null && !isAppViewActive,
                label = "View",
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
                }
            ) { isCarousel ->
                if (isCarousel) {
                    when (currentTheme) {
                        SettingsManager.THEME_INMERSIVE -> {
                            InmersiveDashboardContent(
                                platforms = displayPlatforms,
                                lang = lang,
                                onFocused = { id ->
                                    if (focusedPlatformInCarousel != id) {
                                        soundManager.playClick()
                                        focusedPlatformInCarousel = id
                                    }
                                },
                                onSelect = { id ->
                                    soundManager.playClick()
                                    hapticHandler.playClick()
                                    when(id) {
                                        "SYS_SETTINGS" -> onSettingsClick()
                                        "SYS_IA" -> onArchitectClick()
                                        "SYS_HARDWARE" -> isHardwarePanelVisible = true
                                        "SYS_APPS" -> isAppViewActive = true
                                        else -> selectedPlatform = id
                                    }
                                },
                                primaryColor = primaryColor
                            )
                        }
                        SettingsManager.THEME_ULTRA -> {
                            UltraDashboardContent(
                                platforms = displayPlatforms,
                                lang = lang,
                                hardwareState = hardwareState,
                                onFocused = { id ->
                                    if (focusedPlatformInCarousel != id) {
                                        soundManager.playClick()
                                        focusedPlatformInCarousel = id
                                    }
                                },
                                onSelect = { id ->
                                    soundManager.playClick()
                                    hapticHandler.playClick()
                                    when(id) {
                                        "SYS_SETTINGS" -> onSettingsClick()
                                        "SYS_IA" -> onArchitectClick()
                                        "SYS_HARDWARE" -> isHardwarePanelVisible = true
                                        "SYS_APPS" -> isAppViewActive = true
                                        else -> selectedPlatform = id
                                    }
                                },
                                primaryColor = primaryColor
                            )
                        }
                        else -> {
                            PlatformBookPager(
                                platforms = displayPlatforms,
                                progressMap = platformProgress,
                                mainViewModel = mainViewModel,
                                lang = lang,
                                onFocused = { 
                                    if (focusedPlatformInCarousel != it) {
                                        soundManager.playClick()
                                        focusedPlatformInCarousel = it 
                                    }
                                },
                                onSelect = { id ->
                                    soundManager.playClick()
                                    hapticHandler.playClick()
                                    when(id) {
                                        "SYS_SETTINGS" -> onSettingsClick()
                                        "SYS_IA" -> onArchitectClick()
                                        "SYS_HARDWARE" -> isHardwarePanelVisible = true
                                        "SYS_APPS" -> isAppViewActive = true
                                        else -> selectedPlatform = id
                                    }
                                }
                            )
                        }
                    }
                } else if (isAppViewActive) {
                    AppCarouselBeta(lang) { 
                        soundManager.playBack()
                        isAppViewActive = false 
                    }
                } else {
                    val gamesForPlatform by mainViewModel.getGamesForPlatform(selectedPlatform ?: "").collectAsState(initial = emptyList())
                    val filtered = remember(selectedPlatform, gamesForPlatform, favoriteGames, recentGames, lang) {
                        val baseList = when(selectedPlatform) {
                            "favoritos" -> favoriteGames
                            "recientes" -> recentGames
                            else -> gamesForPlatform
                        }
                        filterGamesByLanguage(baseList, lang)
                    }
                    
                    GameDetailedListBeta(
                        platform = selectedPlatform ?: "",
                        games = filtered,
                        lang = lang,
                        keyMapper = KeyMapper(context),
                        onBack = { 
                            soundManager.playBack()
                            selectedPlatform = null 
                        },
                        onGameClick = { 
                            soundManager.playLaunch()
                            hapticHandler.playLaunchEffect()
                            mainViewModel.markGameAsPlayed(it)
                            launcherManager.launchGame(it) 
                        },
                        onToggleFavorite = { mainViewModel.toggleFavorite(it) },
                        onSelectionChanged = { }
                    )
                }
            }

            if (currentTheme != SettingsManager.THEME_ULTRA) {
                TelemetryHeader(hardwareState, primaryColor, lang)
            }
            
            if (isHardwarePanelVisible) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
                    HardwareQuickPanelBeta(
                        state = hardwareState,
                        onClose = { 
                            soundManager.playBack()
                            isHardwarePanelVisible = false 
                        },
                        onProfileChange = { 
                            soundManager.playClick()
                            hardwareViewModel.setManualProfile(it) 
                        },
                        onFanSpeedChange = { speed ->
                            hardwareViewModel.setManualFanSpeed(speed)
                        }
                    )
                }
            }

            if (bootAlpha > 0f) {
                Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = bootAlpha)))
            }
        }
    }
}

@Composable
fun UltraDashboardContent(
    platforms: List<String>,
    lang: String,
    hardwareState: HardwareUiState,
    onFocused: (String) -> Unit,
    onSelect: (String) -> Unit,
    primaryColor: Color
) {
    val context = LocalContext.current
    val centerIndex = (Int.MAX_VALUE / 2) - ((Int.MAX_VALUE / 2) % platforms.size)
    val pagerState = rememberPagerState(initialPage = centerIndex, pageCount = { Int.MAX_VALUE })
    
    LaunchedEffect(pagerState.currentPage) {
        onFocused(platforms[pagerState.currentPage % platforms.size])
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF020202))) {
        // FONDO TÁCTICO: Circuitos sutiles
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.1f)) {
            val stroke = 2f
            for (i in 0..20) {
                drawLine(primaryColor, Offset(0f, i * 100f), Offset(size.width, i * 100f), stroke)
                drawLine(primaryColor, Offset(i * 100f, 0f), Offset(i * 100f, size.height), stroke)
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // ZONA SUPERIOR: TELEMETRÍA INTEGRADA ULTRA (Reducida)
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text("SYSTEM STATUS: OPTIMAL", color = primaryColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(primaryColor, CircleShape))
                        Spacer(Modifier.width(8.dp))
                        Text("IMPERIAL CORE v5.0", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    UltraSmallStat("CPU", "${(hardwareState.cpuLoad * 100).toInt()}%", hardwareState.cpuLoad, primaryColor)
                    UltraSmallStat("RAM", "${(hardwareState.ramUsage * 100).toInt()}%", hardwareState.ramUsage, primaryColor)
                    UltraSmallStat("TMP", "${hardwareState.temperature.toInt()}°", hardwareState.temperature / 90f, if(hardwareState.temperature > 65) Color.Red else primaryColor)
                }
            }

            // ZONA CENTRAL: VISOR / CARRUSEL
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 160.dp)
                ) { page ->
                    val id = platforms[page % platforms.size]
                    val isFocused = pagerState.currentPage == page
                    val fraction = 1f - ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue.coerceIn(0f, 1f)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(lerpFloat(0.85f, 1.1f, fraction))
                            .alpha(lerpFloat(0.4f, 1f, fraction))
                            .clickable { if(isFocused) onSelect(id) },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.size(450.dp, 300.dp)) {
                            val bgPath = if (id.startsWith("SYS_")) "file:///android_asset/contentimg/banner-neon.jpg"
                                         else "file:///android_asset/contentimg/fanart/${id.lowercase()}.jpg"

                            AsyncImage(
                                model = bgPath,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).border(2.dp, primaryColor.copy(alpha = fraction), RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            
                            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)))), contentAlignment = Alignment.BottomStart) {
                                val logoPath = when(id) {
                                    "SYS_APPS" -> "file:///android_asset/contentimg/androidapps.png"
                                    "SYS_IA" -> "file:///android_asset/contentimg/IA.png"
                                    "SYS_HARDWARE" -> "file:///android_asset/contentimg/hardware.png"
                                    "SYS_SETTINGS" -> "file:///android_asset/contentimg/ajustes.png"
                                    else -> "file:///android_asset/contentimg/logos/${id.lowercase()}.png"
                                }
                                AsyncImage(
                                    model = logoPath,
                                    contentDescription = null,
                                    modifier = Modifier.padding(24.dp).height(60.dp).width(140.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val s = 40f
                                val p = 2f
                                drawLine(primaryColor, Offset(0f, s), Offset(0f, 0f), p); drawLine(primaryColor, Offset(0f, 0f), Offset(s, 0f), p)
                                drawLine(primaryColor, Offset(size.width, size.height - s), Offset(size.width, size.height), p); drawLine(primaryColor, Offset(size.width, size.height), Offset(size.width - s, size.height), p)
                            }
                        }
                    }
                }
            }

            // ZONA INFERIOR: DOCK STATION FIJA (Reducida)
            Surface(
                modifier = Modifier.fillMaxWidth().height(80.dp),
                color = Color.Black.copy(alpha = 0.9f),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f))
            ) {
                Row(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        UltraDockButton(Icons.Default.Settings, "SETTINGS", primaryColor) { onSelect("SYS_SETTINGS") }
                        UltraDockButton(Icons.Default.Memory, "HARDWARE", primaryColor) { onSelect("SYS_HARDWARE") }
                        UltraDockButton(Icons.Default.AutoAwesome, "ARCHITECT", primaryColor) { onSelect("SYS_IA") }
                    }
                    
                    val currentId = platforms[pagerState.currentPage % platforms.size]
                    val metadata = MetadataReader.getMetadata(context, currentId, lang)
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 48.dp)) {
                        Text(text = "SCANNING SYSTEM DATA...", color = primaryColor, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text(
                            text = "${metadata.name.uppercase()} | ${metadata.manufacturer} | ${metadata.year} | ${metadata.description}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE, velocity = 50.dp)
                        )
                    }
                    
                    val time = remember { mutableStateOf(SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())) }
                    LaunchedEffect(Unit) {
                        while(true) {
                            delay(1000)
                            time.value = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                        }
                    }
                    Text(
                        time.value,
                        color = primaryColor, fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun UltraSmallStat(label: String, value: String, progress: Float, color: Color) {
    Column(horizontalAlignment = Alignment.End) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = color.copy(alpha = 0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Spacer(Modifier.width(8.dp))
            Text(value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, color = color, trackColor = Color.White.copy(alpha = 0.05f), modifier = Modifier.width(60.dp).height(2.dp))
    }
}

@Composable
fun UltraDockButton(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, 
        modifier = Modifier
            .clickable { onClick() }
            .onFocusChanged { isFocused = it.isFocused }
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp).scale(if(isFocused) 1.2f else 1f),
            tint = if (isFocused) color else Color.Gray
        )
        Spacer(Modifier.height(4.dp))
        Text(label, color = if(isFocused) color else Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun InmersiveDashboardContent(
    platforms: List<String>,
    lang: String,
    onFocused: (String) -> Unit,
    onSelect: (String) -> Unit,
    primaryColor: Color
) {
    val context = LocalContext.current
    val centerIndex = (Int.MAX_VALUE / 2) - ((Int.MAX_VALUE / 2) % platforms.size)
    val pagerState = rememberPagerState(initialPage = centerIndex, pageCount = { Int.MAX_VALUE })
    
    LaunchedEffect(pagerState.currentPage) {
        onFocused(platforms[pagerState.currentPage % platforms.size])
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val id = platforms[page % platforms.size]
            val isSystem = id.startsWith("SYS_")

            val bgPath = if (isSystem) "file:///android_asset/contentimg/banner-neon.jpg"
                         else when(id) {
                             "favoritos" -> "file:///android_asset/contentimg/fanart/favorites_bg.jpg"
                             "recientes" -> "file:///android_asset/contentimg/fanart/recent_bg.jpg"
                             else -> "file:///android_asset/contentimg/fanart/${id.lowercase()}.jpg"
                         }

            Box(Modifier.fillMaxSize()) {
                AsyncImage(
                    model = bgPath,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)))
                
                Box(modifier = Modifier.fillMaxSize().padding(64.dp).clickable { onSelect(id) }) {
                    
                    val logoPath = when(id) {
                        "SYS_APPS" -> "file:///android_asset/contentimg/androidapps.png"
                        "SYS_IA" -> "file:///android_asset/contentimg/IA.png"
                        "SYS_HARDWARE" -> "file:///android_asset/contentimg/hardware.png"
                        "SYS_SETTINGS" -> "file:///android_asset/contentimg/ajustes.png"
                        else -> "file:///android_asset/contentimg/logos/${id.lowercase()}.png"
                    }
                    AsyncImage(
                        model = logoPath,
                        contentDescription = "Logo",
                        modifier = Modifier.align(Alignment.TopStart).height(80.dp).width(175.dp),
                        contentScale = ContentScale.Fit
                    )

                    val controllerName = when(id.lowercase()) {
                        "gamecube", "gc" -> "gc.svg"; "megadrive" -> "megadrive.svg"
                        "nintendo64" -> "n64.svg"; "snes" -> "snes.svg"
                        "psx" -> "psx.svg"; "ps2" -> "ps2.svg"; "psp" -> "psp.svg"
                        "gba" -> "gba.svg"; "gbc" -> "gbc.svg"; "gb" -> "gb.svg"
                        "dreamcast" -> "dreamcast.svg"; "genesis" -> "genesis.svg"
                        "mastersystem" -> "mastersystem.svg"; "gamegear" -> "gamegear.svg"
                        "mame" -> "mame.svg"; "cps1" -> "cps1.svg"; "cps2" -> "cps2.svg"
                        "cps3" -> "cps3.svg"; "neogeo" -> "neogeo.svg"; "nds" -> "nds.svg"
                        "arcade" -> "arcade.svg"; "saturn" -> "saturn.svg"; "amiga" -> "amiga.svg"
                        else -> "${id.lowercase()}.svg"
                    }
                    val controllerPath = "file:///android_asset/contentimg/system-controllers-outline/$controllerName"

                    AsyncImage(
                        model = ImageRequest.Builder(context).data(controllerPath).decoderFactory(SvgDecoder.Factory()).build(),
                        contentDescription = "Controller",
                        modifier = Modifier.align(Alignment.BottomEnd).height(80.dp).alpha(0.85f),
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }
            }
        }

        val currentId = platforms[pagerState.currentPage % platforms.size]
        val description: String
        if (currentId.startsWith("SYS_")) {
            description = when(currentId) {
                "SYS_SETTINGS" -> Translator.getString(lang, "SETTINGS_CORE")
                "SYS_IA" -> Translator.getString(lang, "SETTINGS_IA")
                "SYS_APPS" -> Translator.getString(lang, "SETTINGS_APPS")
                "SYS_HARDWARE" -> Translator.getString(lang, "SETTINGS_HW")
                else -> Translator.getString(lang, "SETTINGS_SYS")
            }
        } else {
            val metadata = MetadataReader.getMetadata(context, currentId, lang)
            description = metadata.description.ifEmpty { "Sistema operativo Speccy OS detectado. Protocolo inmersivo activado." }
        }

        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(60.dp),
            color = Color.Black.copy(alpha = 0.8f),
            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.4f))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.background(primaryColor).padding(horizontal = 24.dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    Text("SPECCY NEWS", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                }
                Text(
                    text = "$description  ***  $description",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp).basicMarquee(iterations = Int.MAX_VALUE, velocity = 70.dp)
                )
            }
        }
    }
}

@Composable
fun RetroGridBackground(primaryColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "gridAnim")
    val moveY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 100f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "moveY"
    )
    
    val neonGlow = primaryColor.copy(alpha = 0.4f)

    Canvas(modifier = Modifier.fillMaxSize().alpha(0.3f)) {
        val w = size.width
        val h = size.height
        val horizonY = h * 0.45f
        
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(primaryColor.copy(alpha = 0.2f), Color.Transparent),
                startY = 0f, endY = horizonY
            ),
            size = Size(w, horizonY)
        )

        drawLine(primaryColor, Offset(0f, horizonY), Offset(w, horizonY), strokeWidth = 6f)
        drawLine(Color.White, Offset(0f, horizonY), Offset(w, horizonY), strokeWidth = 2f)

        val vanishingPoint = Offset(w / 2, horizonY)
        val numLines = 24
        for (i in 0..numLines) {
            val startX = (w / numLines) * i
            val endX = startX * 4 - (w * 1.5f)
            drawLine(neonGlow, vanishingPoint, Offset(endX, h), 3f)
        }
        
        var y = horizonY + moveY
        var gap = 12f
        while (y < h) {
            drawLine(neonGlow, Offset(0f, y), Offset(w, y), strokeWidth = (gap / 10f).coerceAtMost(6f))
            gap *= 1.35f
            y += gap
        }
    }
}

@androidx.annotation.OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlatformBookPager(
    platforms: List<String>, 
    progressMap: Map<String, Float>, 
    mainViewModel: MainViewModel,
    lang: String,
    onFocused: (String) -> Unit, 
    onSelect: (String) -> Unit
) {
    if (platforms.isEmpty()) return

    val centerIndex = (Int.MAX_VALUE / 2) - ((Int.MAX_VALUE / 2) % platforms.size)
    val pagerState = rememberPagerState(initialPage = centerIndex, pageCount = { Int.MAX_VALUE })
    
    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary
    var showFullInfo by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState.currentPage) {
        val currentId = platforms[pagerState.currentPage % platforms.size]
        onFocused(currentId)
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 120.dp)
    ) { page ->
        
        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
        val fraction = 1f - pageOffset.coerceIn(0f, 1f)
        val scale = lerpFloat(0.75f, 1f, fraction)
        val alpha = lerpFloat(0.3f, 1f, fraction)
        val rotation = lerpFloat(25f, 0f, fraction) * if (page < pagerState.currentPage) 1f else -1f

        val id = platforms[page % platforms.size]
        val progress = progressMap[id]
        val isCurrentPage = pagerState.currentPage == page
        val isSystem = id.startsWith("SYS_")
        
        val gamesForPlatform by mainViewModel.getGamesForPlatform(id).collectAsState(initial = emptyList())
        val platformGames = if (!isSystem && id != "favoritos" && id != "recientes") {
            gamesForPlatform
        } else if (id == "favoritos") {
            val favs by mainViewModel.favoriteGames.collectAsState(initial = emptyList())
            favs
        } else if (id == "recientes") {
            val recents by mainViewModel.recentGames.collectAsState(initial = emptyList())
            recents
        } else {
            emptyList()
        }

        val totalRoms = platformGames.size
        val totalPlayCount = platformGames.sumOf { it.playCount }
        val hoursPlayed = (totalPlayCount * 0.4f).toInt()
        val lastGame = platformGames.filter { it.lastPlayed > 0 }.maxByOrNull { it.lastPlayed }
        val lastGameTitle = lastGame?.title ?: Translator.getString(lang, "NONE")

        val description: String
        if (isSystem) {
            description = when(id) {
                "SYS_SETTINGS" -> Translator.getString(lang, "SETTINGS_CORE")
                "SYS_IA" -> Translator.getString(lang, "SETTINGS_IA")
                "SYS_APPS" -> Translator.getString(lang, "SETTINGS_APPS")
                "SYS_HARDWARE" -> Translator.getString(lang, "SETTINGS_HW")
                else -> Translator.getString(lang, "SETTINGS_SYS")
            }
        } else {
            val metadata = MetadataReader.getMetadata(context, id, lang)
            description = metadata.description.ifEmpty { "Información clasificada." }
        }

        val logoPath = when(id) {
            "SYS_APPS" -> "file:///android_asset/contentimg/androidapps.png"
            "SYS_IA" -> "file:///android_asset/contentimg/IA.png"
            "SYS_HARDWARE" -> "file:///android_asset/contentimg/hardware.png"
            "SYS_SETTINGS" -> "file:///android_asset/contentimg/ajustes.png"
            else -> "file:///android_asset/contentimg/logos/${id.lowercase()}.png"
        }
        val bgPath = if (isSystem) "file:///android_asset/contentimg/banner-neon.jpg"
                     else when(id) {
                         "favoritos" -> "file:///android_asset/contentimg/fanart/favorites_bg.jpg"
                         "recientes" -> "file:///android_asset/contentimg/fanart/recent_bg.jpg"
                         else -> "file:///android_asset/contentimg/fanart/${id.lowercase()}.jpg"
                     }

        val controllerName = when(id.lowercase()) {
            "gamecube", "gc" -> "gc.svg"; "megadrive" -> "megadrive.svg"
            "nintendo64" -> "n64.svg"; "snes" -> "snes.svg"
            "psx" -> "psx.svg"; "ps2" -> "ps2.svg"; "psp" -> "psp.svg"
            "gba" -> "gba.svg"; "gbc" -> "gbc.svg"; "gb" -> "gb.svg"
            "dreamcast" -> "dreamcast.svg"; "genesis" -> "genesis.svg"
            "mastersystem" -> "mastersystem.svg"; "gamegear" -> "gamegear.svg"
            "mame" -> "mame.svg"; "cps1" -> "cps1.svg"; "cps2" -> "cps2.svg"
            "cps3" -> "cps3.svg"; "neogeo" -> "neogeo.svg"; "nds" -> "nds.svg"
            "arcade" -> "arcade.svg"; "saturn" -> "saturn.svg"; "amiga" -> "amiga.svg"
            else -> "${id.lowercase()}.svg"
        }
        val controllerPath = "file:///android_asset/contentimg/system-controllers-outline/$controllerName"

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 48.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                    rotationY = rotation
                    cameraDistance = 8 * density
                }
                .clip(RoundedCornerShape(24.dp))
                .border(BorderStroke(if (isCurrentPage) 3.dp else 1.dp, primaryColor.copy(alpha = alpha)), RoundedCornerShape(24.dp))
                .clickable { if (isCurrentPage) onSelect(id) }
        ) {
            AsyncImage(model = bgPath, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.9f), Color.Black.copy(alpha = 0.6f), Color.Black.copy(alpha = 0.9f)))))

            Row(modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 40.dp)) {
                Box(modifier = Modifier.weight(1.2f).fillMaxHeight(), contentAlignment = Alignment.TopStart) {
                    Column {
                        AsyncImage(model = logoPath, contentDescription = null, modifier = Modifier.height(140.dp).fillMaxWidth(0.9f), contentScale = ContentScale.Fit)
                        Spacer(modifier = Modifier.height(24.dp))
                        if (progress != null && progress < 1f) {
                            Text(Translator.getString(lang, "SYNCING"), color = primaryColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(progress = { progress }, color = primaryColor, modifier = Modifier.width(200.dp).height(6.dp))
                        } else if (!isSystem) {
                            Column(modifier = Modifier.padding(start = 8.dp).background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(12.dp)).border(1.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)).padding(16.dp)) {
                                StatRowArcade(Translator.getString(lang, "ROMS"), "$totalRoms", primaryColor, FontFamily.Monospace)
                                Spacer(Modifier.height(8.dp))
                                StatRowArcade(Translator.getString(lang, "HOURS_PLAYED"), "${hoursPlayed}h", primaryColor, FontFamily.Monospace)
                                Spacer(Modifier.height(8.dp))
                                StatRowArcade(Translator.getString(lang, "LAST_GAME"), lastGameTitle, primaryColor, FontFamily.Monospace)
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.BottomEnd) {
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Bottom, modifier = Modifier.padding(bottom = 20.dp)) {
                        Box(modifier = Modifier.shadow(if (isCurrentPage) 24.dp else 0.dp, spotColor = primaryColor, ambientColor = primaryColor)) {
                            if (!isSystem && id != "favoritos" && id != "recientes") {
                                AsyncImage(
                                    model = ImageRequest.Builder(context).data(controllerPath).decoderFactory(SvgDecoder.Factory()).build(),
                                    contentDescription = "Controller", modifier = Modifier.height(180.dp).alpha(0.9f),
                                    contentScale = ContentScale.Fit, colorFilter = ColorFilter.tint(Color.White)
                                )
                            } else {
                                val iconIcon: Any = when(id) {
                                    "SYS_SETTINGS" -> "file:///android_asset/contentimg/ajustes.png"
                                    "SYS_IA" -> "file:///android_asset/contentimg/IA.png"
                                    "SYS_HARDWARE" -> "file:///android_asset/contentimg/hardware.png"
                                    "SYS_APPS" -> "file:///android_asset/contentimg/androidapps.png"
                                    "favoritos" -> Icons.Default.Star
                                    "recientes" -> Icons.Default.History
                                    else -> Icons.Default.VideogameAsset
                                }
                                if (iconIcon is String) {
                                    AsyncImage(model = iconIcon, contentDescription = null, modifier = Modifier.size(140.dp).alpha(0.9f))
                                } else if (iconIcon is androidx.compose.ui.graphics.vector.ImageVector) {
                                    Icon(iconIcon, null, modifier = Modifier.size(140.dp).alpha(0.9f), tint = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        val infiniteTransition = rememberInfiniteTransition(label = "blink")
                        val blinkAlpha by infiniteTransition.animateFloat(initialValue = 0.2f, targetValue = 1f, animationSpec = infiniteRepeatable(animation = tween(600, easing = LinearEasing), repeatMode = RepeatMode.Reverse), label = "blinkAlpha")

                        Surface(
                            color = primaryColor,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.alpha(if (isCurrentPage) blinkAlpha else 0f).padding(8.dp)
                        ) {
                            Text(text = " [ ENTER ] TO ACCESS ", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                        }
                    }
                }
            }
            
            Surface(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(40.dp).alpha(0.9f).clickable { showFullInfo = true }, color = Color.Black, border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f))) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.background(primaryColor).padding(horizontal = 12.dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        Text("INFO", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                    Text(text = "$description  ***  $description", color = Color.White, fontSize = 14.sp, fontFamily = FontFamily.Monospace, maxLines = 1, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).basicMarquee(iterations = Int.MAX_VALUE, velocity = 60.dp))
                }
            }
        }
    }

    if (showFullInfo) {
        val currentId = platforms[pagerState.currentPage % platforms.size]
        val metadata = MetadataReader.getMetadata(context, currentId, lang)
        Dialog(onDismissRequest = { showFullInfo = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(modifier = Modifier.fillMaxSize(), color = Color.Black.copy(alpha = 0.95f)) {
                Column(modifier = Modifier.padding(48.dp).verticalScroll(rememberScrollState())) {
                    Text(text = metadata.name.uppercase(), color = primaryColor, fontSize = 32.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    Spacer(Modifier.height(8.dp))
                    Text(text = "MANUFACTURER: ${metadata.manufacturer} | YEAR: ${metadata.year}", color = Color.Gray, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                    Spacer(Modifier.height(32.dp))
                    Text(text = metadata.description, color = Color.White, fontSize = 18.sp, lineHeight = 28.sp, textAlign = TextAlign.Justify, fontFamily = FontFamily.Monospace)
                    Spacer(Modifier.height(48.dp))
                    Button(onClick = { showFullInfo = false }, colors = ButtonDefaults.buttonColors(containerColor = primaryColor)) {
                        Text("CERRAR", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StatRowArcade(label: String, value: String, color: Color, font: FontFamily) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.widthIn(min = 200.dp, max = 280.dp)) {
        Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = font)
        Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = font, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(start = 16.dp))
    }
}

@Composable
fun HardwareQuickPanelBeta(
    state: HardwareUiState, 
    onClose: () -> Unit, 
    onProfileChange: (String) -> Unit,
    onFanSpeedChange: (Int) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val currentDevice = HardwareControlManagerBeta.getProfile(HardwareControlManagerBeta.currentProfileId)

    Surface(
        modifier = Modifier.fillMaxHeight().width(350.dp), 
        color = Color.Black.copy(alpha = 0.95f), 
        border = BorderStroke(2.dp, primaryColor.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("HARDWARE ENGINE", color = primaryColor, fontWeight = FontWeight.Black, fontSize = 18.sp, fontFamily = FontFamily.Monospace)
                IconButton(onClick = onClose) { Icon(Icons.Default.Close, null, tint = Color.Gray) }
            }
            
            Spacer(Modifier.height(24.dp))

            Surface(
                color = Color.DarkGray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(currentDevice.name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Spacer(Modifier.height(12.dp))
                    StatRowArcade("CHIPSET", currentDevice.chipset, primaryColor, FontFamily.Monospace)
                    Spacer(Modifier.height(4.dp))
                    StatRowArcade("GPU", currentDevice.gpu, primaryColor, FontFamily.Monospace)
                    Spacer(Modifier.height(4.dp))
                    StatRowArcade("RAM", currentDevice.ram, primaryColor, FontFamily.Monospace)
                    Spacer(Modifier.height(4.dp))
                    StatRowArcade("THERMALS", if(currentDevice.hasActiveCooling) "Activa (Ventilador)" else "Pasiva", primaryColor, FontFamily.Monospace)
                    Spacer(Modifier.height(4.dp))
                    StatRowArcade("TARGET", currentDevice.emulationCapacity, primaryColor, FontFamily.Monospace)
                }
            }

            Spacer(Modifier.height(32.dp))

            Text("CPU GOVERNOR (TDP)", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Spacer(Modifier.height(16.dp))

            val profiles = listOf("ECO", "BALANCED", "PERFORMANCE", "EXTREME")
            profiles.forEach { profile ->
                val isSelected = state.currentProfile == profile
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(50.dp).clickable { onProfileChange(profile) }, 
                    color = if (isSelected) primaryColor.copy(alpha = 0.2f) else Color.Transparent, 
                    shape = RoundedCornerShape(12.dp), 
                    border = if (isSelected) BorderStroke(1.dp, primaryColor) else BorderStroke(1.dp, Color.DarkGray)
                ) {
                    Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.padding(horizontal = 16.dp)) { 
                        Text(profile, color = if (isSelected) primaryColor else Color.LightGray, fontWeight = if(isSelected) FontWeight.Black else FontWeight.Normal, fontFamily = FontFamily.Monospace) 
                    }
                }
            }

            if (currentDevice.hasActiveCooling) {
                Spacer(Modifier.height(32.dp))
                Text("SISTEMA DE REFRIGERACIÓN", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Spacer(Modifier.height(16.dp))
                
                Text("Velocidad: ${(state.fanSpeedLevel * 100).toInt()}%", color = primaryColor, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                Slider(
                    value = state.fanSpeedLevel,
                    onValueChange = { onFanSpeedChange((it * currentDevice.maxFanLevel).toInt()) },
                    steps = currentDevice.maxFanLevel - 1,
                    colors = SliderDefaults.colors(thumbColor = primaryColor, activeTrackColor = primaryColor, inactiveTrackColor = Color.DarkGray)
                )
            }
        }
    }
}

@Composable
fun AppCarouselBeta(lang: String, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(Translator.getString(lang, "APP_BETA"), color = MaterialTheme.colorScheme.primary, fontSize = 24.sp, fontWeight = FontWeight.Black); Spacer(modifier = Modifier.height(32.dp)); Button(onClick = onBack) { Text(Translator.getString(lang, "BACK_HUB")) } }
    }
}

@UnstableApi
@androidx.annotation.OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameDetailedListBeta(
    platform: String, games: List<Game>, lang: String, keyMapper: KeyMapper,
    onBack: () -> Unit, onGameClick: (Game) -> Unit, onToggleFavorite: (Game) -> Unit, onSelectionChanged: (Int) -> Unit
) {
    val context = LocalContext.current
    var selectedGameIndex by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val selectedGame = if (games.isNotEmpty()) games[selectedGameIndex.coerceIn(0, games.lastIndex)] else null
    val primaryColor = MaterialTheme.colorScheme.primary
    val cabinetPath = "file:///android_asset/contentimg/cabinets/${platform.lowercase()}.png"
    val logoPath = "file:///android_asset/contentimg/logos/${platform.lowercase()}.png"
    
    val customFont = when(platform.lowercase()) {
        "nes", "snes", "n64", "gb", "gbc", "gba", "virtualboy" -> FontFamily.Serif
        "genesis", "mastersystem", "gamegear", "dreamcast" -> FontFamily.SansSerif
        "psx", "ps2", "psp" -> FontFamily.Default
        "mame", "cps1", "cps2", "cps3", "neogeo", "arcade" -> FontFamily.Cursive
        else -> FontFamily.Monospace
    }
    val isArcade = platform.lowercase() in listOf("mame", "cps1", "cps2", "cps3", "neogeo", "arcade")
    val fontSize = if (isArcade) 22.sp else 14.sp
    val fontWeight = if (isArcade) FontWeight.ExtraBold else FontWeight.Medium

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val exoPlayer = remember { ExoPlayer.Builder(context).build().apply { repeatMode = Player.REPEAT_MODE_ONE; playWhenReady = true } }
    LaunchedEffect(selectedGame?.videoPreview) {
        exoPlayer.stop(); exoPlayer.clearMediaItems()
        selectedGame?.videoPreview?.let { exoPlayer.setMediaItem(MediaItem.fromUri(it.toUri())); exoPlayer.prepare() }
    }
    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }

    val infiniteTransition = rememberInfiniteTransition(label = "neonPulse")
    val glowAlpha by infiniteTransition.animateFloat(initialValue = 0.3f, targetValue = 0.8f, animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "glowAlpha")
    
    Box(modifier = Modifier.fillMaxSize().focusRequester(focusRequester).focusable().onKeyEvent { event ->
        if (event.type == KeyEventType.KeyDown) {
            when (event.nativeKeyEvent.keyCode) {
                KeyEvent.KEYCODE_DPAD_DOWN, keyMapper.dpadDown -> { if (selectedGameIndex < games.size - 1) { selectedGameIndex++; onSelectionChanged(selectedGameIndex); scope.launch { listState.animateScrollToItem(selectedGameIndex) } }; true }
                KeyEvent.KEYCODE_DPAD_UP, keyMapper.dpadUp -> { if (selectedGameIndex > 0) { selectedGameIndex--; onSelectionChanged(selectedGameIndex); scope.launch { listState.animateScrollToItem(selectedGameIndex) } }; true }
                KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_ENTER, keyMapper.actionA -> { if (games.isNotEmpty()) onGameClick(games[selectedGameIndex]); true }
                KeyEvent.KEYCODE_BUTTON_B, KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_ESCAPE, keyMapper.actionB -> { onBack(); true }
                else -> false
            }
        } else false
    }) {
        Row(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Box(modifier = Modifier.weight(0.55f).fillMaxHeight().padding(end = 32.dp), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.fillMaxWidth(0.6f).fillMaxHeight(0.6f).clip(RoundedCornerShape(12.dp)).background(Color.Black).combinedClickable(onLongClick = onBack, onClick = {})) {
                    if (selectedGame?.videoPreview != null) {
                        AndroidView(factory = { ctx -> PlayerView(ctx).apply { player = exoPlayer; useController = false; resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM } }, modifier = Modifier.fillMaxSize().crtEffect())
                    } else if (selectedGame?.boxArt != null) {
                        AsyncImage(model = selectedGame.boxArt, contentDescription = null, modifier = Modifier.fillMaxSize().crtEffect(), contentScale = ContentScale.Crop)
                    } else {
                        Box(modifier = Modifier.fillMaxSize().crtEffect(), contentAlignment = Alignment.Center) { Text(Translator.getString(lang, "NO_MEDIA"), color = primaryColor, fontFamily = FontFamily.Monospace, fontSize = 14.sp) }
                    }
                }
                AsyncImage(model = cabinetPath, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                Row(modifier = Modifier.align(Alignment.TopStart).padding(top = 16.dp, start = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(model = logoPath, contentDescription = null, modifier = Modifier.height(70.dp), contentScale = ContentScale.Fit)
                }
            }
            
            LazyColumn(
                state = listState, 
                modifier = Modifier.weight(0.45f).fillMaxHeight().background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(16.dp)).border(BorderStroke(2.dp, primaryColor.copy(alpha = 0.5f)), RoundedCornerShape(16.dp)).padding(16.dp), 
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(games) { index, game ->
                    val isSelected = index == selectedGameIndex
                    Surface(
                        modifier = Modifier.fillMaxWidth().scale(if (isSelected) 1.05f else 1f).shadow(if (isSelected) 16.dp else 0.dp, spotColor = primaryColor, ambientColor = primaryColor, shape = RoundedCornerShape(12.dp)).clickable { if(isSelected) onGameClick(game) else { selectedGameIndex = index; onSelectionChanged(index); scope.launch { listState.animateScrollToItem(index) } } },
                        color = if (isSelected) primaryColor.copy(alpha = glowAlpha) else Color.Transparent, shape = RoundedCornerShape(12.dp), border = if (isSelected) BorderStroke(2.dp, primaryColor) else null
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (game.isFavorite) { Icon(Icons.Default.Star, null, tint = if (isSelected) Color.White else Color.Yellow, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(12.dp)) }
                            Text(text = game.title, color = if (isSelected) Color.White else Color.LightGray, fontWeight = if (isSelected) FontWeight.Black else fontWeight, fontFamily = customFont, fontSize = if (isSelected) fontSize * 1.1f else fontSize, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            if (isSelected) { IconButton(onClick = { onToggleFavorite(game) }, modifier = Modifier.size(24.dp)) { Icon(if (game.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = Color.White) } }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TelemetryHeader(state: HardwareUiState, color: Color, lang: String) {
    val currentTime = remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) }
    LaunchedEffect(Unit) { while(true) { delay(30000); currentTime.value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) } }
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Surface(color = Color.Black.copy(alpha = 0.8f), shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp), border = BorderStroke(1.dp, color.copy(alpha = 0.5f)), modifier = Modifier.fillMaxWidth(0.9f).height(50.dp)) { 
            Row(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) { 
                Row(verticalAlignment = Alignment.CenterVertically) { 
                    Text(Translator.getString(lang, "TELEMETRY_TITLE"), color = color, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, letterSpacing = 2.sp, fontSize = 14.sp)
                    Spacer(Modifier.width(24.dp)) 
                    Text(currentTime.value, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 14.sp) 
                }
                Row(horizontalArrangement = Arrangement.spacedBy(32.dp), verticalAlignment = Alignment.CenterVertically) { 
                    TelemetryStatBeta("TEMP", "${state.temperature.toInt()}°", state.temperature / 90f, if(state.temperature > 60) Color.Red else color) 
                    TelemetryStatBeta("RAM", "${(state.ramUsage * 100).toInt()}%", state.ramUsage, color) 
                }
            }
        }
    }
}

@Composable
fun TelemetryStatBeta(label: String, value: String, progress: Float, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) { 
        Row(verticalAlignment = Alignment.Bottom) { 
            Text(label, color = color.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) 
            Spacer(Modifier.width(4.dp)) 
            Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace) 
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, color = color, trackColor = Color.White.copy(alpha = 0.1f), modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape))
    }
}
