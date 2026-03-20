@file:OptIn(androidx.media3.common.util.UnstableApi::class)
package com.example.speccyose5ultrav021b

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.Coil
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.speccyose5ultrav021b.ui.theme.NeonBlue
import com.example.speccyose5ultrav021b.ui.theme.Typography
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MainActivity : ComponentActivity() {

    private val tag = "SpeccyMain"
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var hardwareViewModel: HardwareViewModel
    private lateinit var settingsManager: SettingsManager
    private lateinit var soundManager: SoundManager 
    private lateinit var hapticHandler: HapticHandler 
    private lateinit var billingManager: BillingManager
    private var navController: NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        settingsManager = SettingsManager(this)
        hapticHandler = HapticHandler(this)
        
        ThemeManager.initialize(this)
        RetroArchDatabase.initialize(this)

        lifecycleScope.launch(Dispatchers.IO) {
            val imageLoader = ImageLoader.Builder(applicationContext)
                .memoryCache { MemoryCache.Builder(applicationContext).maxSizePercent(0.25).build() }
                .diskCache { DiskCache.Builder().directory(applicationContext.cacheDir.resolve("image_cache_v2")).maxSizePercent(0.10).build() }
                .components {
                    if (Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(GifDecoder.Factory())
                    add(SvgDecoder.Factory())
                }
                .crossfade(true)
                .build()
            
            withContext(Dispatchers.Main) {
                Coil.setImageLoader(imageLoader)
                soundManager = SoundManager(this@MainActivity)
                
                if (settingsManager.isBackgroundMusicEnabled) {
                    soundManager.startBgm()
                }
                
                val hardwareManager = HardwareControlManagerBeta
                hardwareViewModel = HardwareViewModel(hardwareManager, settingsManager, application)
                
                billingManager = BillingManager(this@MainActivity, settingsManager)
                billingManager.startConnection()
            }
        }

        setContent {
            val colorScheme = ThemeManager.colorScheme
            MaterialTheme(colorScheme = colorScheme, typography = Typography) {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                    val currentNavController = rememberNavController()
                    navController = currentNavController
                    val progress by mainViewModel.scanProgress.collectAsState()
                    val isScanning by mainViewModel.isScanning.collectAsState()
                    val isReady by mainViewModel.isReady.collectAsState() 
                    val statusMsg by mainViewModel.scanStatusMessage.collectAsState()
                    
                    LaunchedEffect(statusMsg) {
                        if (statusMsg.contains("Re-vincula")) {
                            currentNavController.navigate("setup_folder") { popUpTo(0) }
                        }
                    }

                    NavHost(navController = currentNavController, startDestination = "intro") {
                        composable("intro") { 
                            IntroScreen {
                                val nextDest = when {
                                    settingsManager.userEmail == null -> "auth"
                                    !settingsManager.isHardwareConfigured -> "welcome"
                                    settingsManager.romsLocation.isEmpty() -> "setup_folder"
                                    !Settings.canDrawOverlays(this@MainActivity) -> "setup_permissions"
                                    else -> "loading"
                                }
                                currentNavController.navigate(nextDest) { popUpTo("intro") { inclusive = true } }
                            } 
                        }
                        composable("auth") { LoginScreen { currentNavController.navigate("welcome") { popUpTo("auth") { inclusive = true } } } }
                        
                        // WELCOME SCREEN CON FALLBACK MANUAL
                        composable("welcome") { 
                            WelcomeScreen(
                                onAutoDetect = { 
                                    if(::soundManager.isInitialized) soundManager.playClick()
                                    currentNavController.navigate("tech_info") 
                                },
                                onManualSelect = {
                                    if(::soundManager.isInitialized) soundManager.playClick()
                                    currentNavController.navigate("setup_hardware")
                                }
                            ) 
                        }

                        composable("tech_info") { 
                            TechnicalInfoScreen(
                                onConfirmDetected = { id -> 
                                    if(::soundManager.isInitialized) soundManager.playClick()
                                    settingsManager.manualHardwareId = id
                                    settingsManager.isHardwareConfigured = true
                                    HardwareControlManagerBeta.initialize(id, application)
                                    currentNavController.navigate("setup_folder") 
                                },
                                onManualSelection = { 
                                    if(::soundManager.isInitialized) soundManager.playClick()
                                    currentNavController.navigate("setup_hardware") 
                                },
                                onBack = {
                                    if(::soundManager.isInitialized) soundManager.playBack()
                                    currentNavController.popBackStack()
                                }
                            ) 
                        }
                        composable("setup_hardware") { HardwareCarouselSelector(
                            onSelected = { id -> 
                                if(::soundManager.isInitialized) soundManager.playClick()
                                hapticHandler.playClick()
                                settingsManager.manualHardwareId = id
                                settingsManager.isHardwareConfigured = true
                                HardwareControlManagerBeta.initialize(id, application)
                                currentNavController.navigate("setup_folder") 
                            },
                            onBack = {
                                if(::soundManager.isInitialized) soundManager.playBack()
                                currentNavController.popBackStack()
                            }
                        ) }
                        composable("setup_folder") { FolderSetupScreen(
                            onNext = { if(::soundManager.isInitialized) soundManager.playClick(); currentNavController.navigate("setup_permissions") },
                            onBack = { if(::soundManager.isInitialized) soundManager.playBack(); currentNavController.popBackStack() }
                        ) }
                        composable("setup_permissions") { MediaPermissionsScreen(
                            onNext = { if(::soundManager.isInitialized) soundManager.playClick(); currentNavController.navigate("loading") },
                            onBack = { if(::soundManager.isInitialized) soundManager.playBack(); currentNavController.popBackStack() }
                        ) }
                        composable("loading") {
                            LaunchedEffect(Unit) { 
                                HardwareControlManagerBeta.initialize(settingsManager.manualHardwareId, application)
                                if (Settings.canDrawOverlays(this@MainActivity)) {
                                    startService(Intent(this@MainActivity, OverlayService::class.java))
                                }
                                if (progress < 0.1f && !isScanning && settingsManager.romsLocation.isNotEmpty()) {
                                    mainViewModel.startSmartSync()
                                }
                            }
                            
                            LaunchedEffect(isReady, isScanning, progress) { 
                                if (isReady && !isScanning && (progress >= 1f || progress == 0f)) {
                                    delay(500)
                                    currentNavController.navigate("dashboard") { popUpTo("loading") { inclusive = true } } 
                                } 
                            }
                            
                            Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = NeonBlue, strokeWidth = 4.dp, modifier = Modifier.size(56.dp))
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(text = statusMsg.ifEmpty { "INICIALIZANDO SISTEMA..." }, color = NeonBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                                    if (isScanning) {
                                        Text(text = "${(progress * 100).toInt()}%", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, modifier = Modifier.padding(top = 8.dp))
                                    }
                                }
                            }
                        }
                        composable("dashboard") { 
                            if (settingsManager.isAuthorized) {
                                if(::soundManager.isInitialized && ::hardwareViewModel.isInitialized) {
                                    SpeccyDashboard(mainViewModel, hardwareViewModel, settingsManager, soundManager, hapticHandler, { soundManager.playClick(); currentNavController.navigate("settings") }, { soundManager.playClick(); currentNavController.navigate("architect") }) 
                                } else {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = NeonBlue) }
                                }
                            } else {
                                LicenseGateScreen { if(::billingManager.isInitialized) billingManager.startConnection() }
                            }
                        }
                        composable("web_auth") { WebAuthScreen { currentNavController.navigate("welcome") { popUpTo("auth") { inclusive = true } } } }
                        composable("settings") { SettingsScreen(hardwareViewModel, mainViewModel, settingsManager, soundManager) { soundManager.playBack(); currentNavController.popBackStack() } }
                        composable("architect") { ArchitectChatScreen(mainViewModel = mainViewModel, hardwareViewModel = hardwareViewModel) { soundManager.playBack(); currentNavController.popBackStack() } } 
                    }
                }
            }
        }
    }

    private fun isGooglePlayAvailable(): Boolean {
        val availability = com.google.android.gms.common.GoogleApiAvailability.getInstance()
        val result = availability.isGooglePlayServicesAvailable(this)
        return result == com.google.android.gms.common.ConnectionResult.SUCCESS
    }

    @Composable
    fun ActionChip(btn: String, label: String, color: Color, onClick: () -> Unit = {}) {
        Surface(
            modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { onClick() },
            color = Color.White.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, color.copy(alpha = 0.8f))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
                    Text(text = btn, color = if (color == Color.White || color == Color.Gray) Color.Black else Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.width(12.dp))
                Text(text = label.uppercase(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
    }

    @Composable
    fun IntroScreen(onFinished: () -> Unit) {
        val lastIdx = settingsManager.lastIntroImageIndex
        val currentIdx = if (lastIdx == 2) 3 else 2
        LaunchedEffect(Unit) { settingsManager.lastIntroImageIndex = currentIdx; delay(5000); onFinished() }
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            AsyncImage(model = "file:///android_asset/contentimg/$currentIdx.jpg", contentDescription = "Intro", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
    }

    @Composable
    fun LoginScreen(onSuccess: () -> Unit) {
        val context = LocalContext.current
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                settingsManager.userEmail = account?.email
                if(::billingManager.isInitialized) billingManager.startConnection()
                onSuccess()
            } catch (e: ApiException) { Toast.makeText(context, "Error al identificar la cuenta.", Toast.LENGTH_LONG).show() }
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AsyncImage(model = "file:///android_asset/contentimg/banner-neon.jpg", contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(24.dp)); Text("SPECCY OS", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                Text("CONTROL DE ACCESO", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, letterSpacing = 2.sp)
                Spacer(Modifier.height(64.dp))
                Button(onClick = { if (isGooglePlayAvailable()) { mGoogleSignInClient.signOut().addOnCompleteListener { launcher.launch(mGoogleSignInClient.signInIntent) } } else { navController?.navigate("web_auth") } }, colors = ButtonDefaults.buttonColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().height(60.dp), shape = RoundedCornerShape(12.dp)) { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.Black), contentAlignment = Alignment.Center) { Text("A", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black) }
                        Spacer(Modifier.width(12.dp)); Text(if (isGooglePlayAvailable()) "IDENTIFICARSE CON GOOGLE" else "IDENTIFICACIÓN WEB IMPERIAL", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }

    @Composable
    fun WebAuthScreen(onSuccess: () -> Unit) {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            AndroidView(factory = { context ->
                WebView(context).apply {
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            val url = request?.url.toString()
                            if (url.contains("auth_success?email=")) {
                                val email = url.substringAfter("email="); billingManager.forceAuthorize(email); onSuccess(); return true
                            }
                            return false
                        }
                    }
                    settings.javaScriptEnabled = true
                    loadUrl("https://speccy-os-auth.web.app/login") 
                }
            }, modifier = Modifier.fillMaxSize())
        }
    }

    @Composable
    fun LicenseGateScreen(onRetry: () -> Unit) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AsyncImage(model = "file:///android_asset/contentimg/banner-neon.jpg", contentDescription = null, modifier = Modifier.fillMaxSize().blur(8.dp), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                Text("LICENCIA NO ENCONTRADA", color = Color.Red, fontWeight = FontWeight.Black, fontSize = 24.sp)
                Spacer(Modifier.height(32.dp)); Button(onClick = onRetry) { ActionChip("A", "REINTENTAR VERIFICACIÓN", Color.White) }
            }
        }
    }

    @Composable
    fun WelcomeScreen(onAutoDetect: () -> Unit, onManualSelect: () -> Unit) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            AsyncImage(model = "file:///android_asset/contentimg/banner-neon.jpg", contentDescription = null, modifier = Modifier.fillMaxSize().alpha(0.3f).blur(4.dp), contentScale = ContentScale.Crop)
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                Text("Speccy OS E5 Ultra", color = MaterialTheme.colorScheme.primary, fontSize = 44.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(64.dp))
                
                Button(onClick = onAutoDetect, modifier = Modifier.width(320.dp).height(64.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(16.dp)) { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.Black), contentAlignment = Alignment.Center) { Text("A", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black) }
                        Spacer(Modifier.width(12.dp)); Text("DETECCIÓN AUTOMÁTICA", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                OutlinedButton(onClick = onManualSelect, modifier = Modifier.width(320.dp).height(64.dp), border = BorderStroke(2.dp, Color.White.copy(alpha = 0.5f)), shape = RoundedCornerShape(16.dp)) { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) { Text("X", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Black) }
                        Spacer(Modifier.width(12.dp)); Text("SELECCIÓN MANUAL", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }

    @Composable
    fun TechnicalInfoScreen(onConfirmDetected: (String) -> Unit, onManualSelection: () -> Unit, onBack: () -> Unit) {
        val detectedProfile = remember { HardwareControlManagerBeta.detectActualHardware() }
        val primaryColor = MaterialTheme.colorScheme.primary
        BackHandler { onBack() }
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            AsyncImage(model = "file:///android_asset/contentimg/banner-neon.jpg", contentDescription = null, modifier = Modifier.fillMaxSize().alpha(0.2f).blur(8.dp), contentScale = ContentScale.Crop)
            Column(modifier = Modifier.padding(horizontal = 40.dp, vertical = 24.dp).widthIn(max = 500.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("INFORME DE SISTEMA", color = primaryColor, fontSize = 28.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Spacer(Modifier.height(24.dp))
                Surface(color = Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(24.dp)) {
                        TechnicalItem("MODELO", Build.MODEL); TechnicalItem("MANUFACTURADOR", Build.MANUFACTURER); TechnicalItem("SOC / CHIPSET", Build.HARDWARE); TechnicalItem("NIVEL API", Build.VERSION.SDK_INT.toString())
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text("ADN DETECTADO", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.height(8.dp))
                Surface(color = primaryColor.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp), border = BorderStroke(2.dp, primaryColor)) {
                    Row(Modifier.padding(horizontal = 24.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoFixHigh, null, tint = primaryColor, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(16.dp)); Text(detectedProfile.name.uppercase(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                }
                Spacer(Modifier.weight(1f))
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    ActionChip("B", "VOLVER", Color.Gray, onClick = onBack)
                    ActionChip("A", "CONFIRMAR", primaryColor, onClick = { onConfirmDetected(detectedProfile.id) })
                }
            }
        }
    }

    @Composable
    fun TechnicalItem(label: String, value: String) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold); Text(value, color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        }
    }

    @Composable
    fun HardwareCarouselSelector(onSelected: (String) -> Unit, onBack: () -> Unit) {
        val allProfiles = HardwareControlManagerBeta.hardwareProfiles.values.toList()
        val brands = remember { listOf("TODOS", "SAMSUNG", "XIAOMI", "POCO", "OPPO", "GOOGLE", "GAMING", "TABLETS", "ANBERNIC", "RETROID", "AYN", "AYANEO", "GAMEMT", "OTROS") }
        var selectedBrand by remember { mutableStateOf("TODOS") }
        var selectedProfileId by remember { mutableStateOf<String?>(null) }
        val filteredProfiles = remember(selectedBrand) { if (selectedBrand == "TODOS") allProfiles else allProfiles.filter { it.category == selectedBrand } }
        val primaryColor = MaterialTheme.colorScheme.primary
        BackHandler { onBack() }
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(model = "file:///android_asset/contentimg/banner-neon.jpg", contentDescription = null, modifier = Modifier.fillMaxSize().blur(10.dp), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)))
            Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("SINCRONIZACIÓN DE HARDWARE", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
                Text("SELECCIONA EL ADN DE TU DISPOSITIVO", color = primaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Spacer(Modifier.height(24.dp))
                LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(horizontal = 16.dp)) {
                    items(brands) { brand ->
                        val isSelected = selectedBrand == brand
                        Surface(modifier = Modifier.clickable { selectedBrand = brand }, color = if (isSelected) primaryColor else Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, if (isSelected) Color.White else primaryColor.copy(alpha = 0.3f))) {
                            Text(text = brand, color = if (isSelected) Color.Black else Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Surface(modifier = Modifier.fillMaxSize().weight(1f), color = Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 240.dp), modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(filteredProfiles) { profile -> HardwareCard(profile, primaryColor, isSelected = selectedProfileId == profile.id) { selectedProfileId = profile.id } }
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    ActionChip("B", "VOLVER ATRÁS", Color.Gray, onClick = onBack)
                    if (selectedProfileId != null) { ActionChip("A", "VINCULAR DISPOSITIVO", primaryColor, onClick = { onSelected(selectedProfileId!!) }) }
                }
            }
        }
    }

    @Composable
    fun HardwareCard(profile: HardwareControlManagerBeta.HardwareProfile, primaryColor: Color, isSelected: Boolean = false, onClick: () -> Unit) {
        val focusRequester = remember { FocusRequester() }
        var isFocused by remember { mutableStateOf(false) }
        Surface(modifier = Modifier.fillMaxWidth().height(120.dp).focusRequester(focusRequester).onFocusChanged { isFocused = it.isFocused }.focusable().clickable { onClick() }.scale(if (isFocused || isSelected) 1.05f else 1f), color = if (isSelected) primaryColor.copy(alpha = 0.3f) else if (isFocused) primaryColor.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.4f), shape = RoundedCornerShape(16.dp), border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected || isFocused) primaryColor else Color.White.copy(alpha = 0.1f))) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(64.dp).background(Color.White.copy(alpha = 0.05f), CircleShape), contentAlignment = Alignment.Center) { Icon(painter = painterResource(profile.iconRes), contentDescription = null, tint = if (isFocused || isSelected) primaryColor else Color.Gray, modifier = Modifier.size(32.dp)) }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(profile.name.uppercase(), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black, maxLines = 1)
                    Text(profile.chipset, color = primaryColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text(profile.emulationCapacity, color = Color.Gray, fontSize = 9.sp, maxLines = 1)
                }
                if (isFocused || isSelected) { Icon(if (isSelected) Icons.Default.CheckCircle else Icons.Default.ChevronRight, null, tint = primaryColor) }
            }
        }
    }

    @Composable
    fun FolderSetupScreen(onNext: () -> Unit, onBack: () -> Unit) {
        val context = LocalContext.current
        var isFolderSelected by remember { mutableStateOf(settingsManager.romsLocation.isNotEmpty()) }
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri -> 
            uri?.let { 
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(it, flags)
                settingsManager.romsLocation = it.toString()
                isFolderSelected = true
            } 
        }
        BackHandler { onBack() }
        Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Card(modifier = Modifier.padding(32.dp).widthIn(max = 500.dp).border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(24.dp)), colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A))) {
                Column(Modifier.padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FolderSpecial, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(72.dp))
                    Spacer(Modifier.height(24.dp)); Text("PROTOCOLO DE ACCESO", color = MaterialTheme.colorScheme.primary, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(16.dp)); Text("Selecciona la carpeta principal de tus ROMs para generar el permiso de acceso persistente.", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(40.dp))
                    if (!isFolderSelected) {
                        Button(onClick = { launcher.launch(null) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { ActionChip("A", "VINCULAR CARPETA", Color.White) }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Carpeta vinculada correctamente.", color = Color.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = onNext, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { ActionChip("A", "CONTINUAR", Color.White) }
                        }
                    }
                    Spacer(Modifier.height(24.dp)); ActionChip("B", "VOLVER ATRÁS", Color.Gray, onClick = onBack)
                }
            }
        }
    }

    @Composable
    fun MediaPermissionsScreen(onNext: () -> Unit, onBack: () -> Unit) {
        val context = LocalContext.current
        val hasOverlayPermission = remember { Settings.canDrawOverlays(context) }
        var isPermissionGranted by remember { mutableStateOf(hasOverlayPermission) }
        BackHandler { onBack() }
        Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Card(modifier = Modifier.padding(32.dp).widthIn(max = 500.dp).border(BorderStroke(2.dp, NeonBlue), RoundedCornerShape(24.dp)), colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A))) {
                Column(Modifier.padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Security, null, tint = NeonBlue, modifier = Modifier.size(72.dp))
                    Spacer(Modifier.height(24.dp)); Text("PERMISOS DE SISTEMA", color = NeonBlue, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(16.dp)); Text("Speccy OS necesita el permiso de 'Mostrar sobre otras aplicaciones' para el Panel de Control Imperial.", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(40.dp))
                    if (!isPermissionGranted) {
                        Button(onClick = { val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")); context.startActivity(intent) }, colors = ButtonDefaults.buttonColors(containerColor = NeonBlue)) { ActionChip("A", "CONCEDER PERMISO", Color.White) }
                        TextButton(onClick = { isPermissionGranted = true }) { Text("YA LO HE CONCEDIDO", color = Color.Gray) }
                    } else {
                        Button(onClick = onNext, colors = ButtonDefaults.buttonColors(containerColor = NeonBlue)) { ActionChip("A", "FINALIZAR CONFIGURACIÓN", Color.White) }
                    }
                    Spacer(Modifier.height(24.dp)); ActionChip("B", "VOLVER ATRÁS", Color.Gray, onClick = onBack)
                }
            }
        }
    }
}
