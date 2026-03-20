package com.example.speccyose5ultrav021b

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.speccyose5ultrav021b.network.OnlineScraperService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val gameDao = database.gameDao()
    private val scanner = RomScanner(application)
    private val settingsManager = SettingsManager(application)
    private val onlineScraper = OnlineScraperService(application, settingsManager)
    val aiManager = AiManager(application)
    val cloudSaveManager = CloudSaveManager(application)

    val favoriteGames = gameDao.getFavoriteGames()
    val recentGames = gameDao.getRecentGames()
    
    private val _platformGameCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val platformGameCounts = _platformGameCounts.asStateFlow()

    private val _platformProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val platformProgress = _platformProgress.asStateFlow()

    private val _discoveredPlatformIds = MutableStateFlow(settingsManager.cachedPlatformIds)
    val activePlatforms = _discoveredPlatformIds.map { it.toSet() }

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress = _scanProgress.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private val _scanStatusMessage = MutableStateFlow("")
    val scanStatusMessage = _scanStatusMessage.asStateFlow()

    private val _isReady = MutableStateFlow(true) 
    val isReady = _isReady.asStateFlow()

    private val _isInitializing = MutableStateFlow(true)
    val isInitializing = _isInitializing.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages = _chatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking = _isAiThinking.asStateFlow()

    private var scanJob: Job? = null

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val gamesList = gameDao.getAllGamesList()
                val platforms = gamesList.map { it.platformId }.toSet()
                
                if (gamesList.isNotEmpty()) {
                    _scanProgress.value = 1.0f
                } else {
                    _scanProgress.value = 0f
                }

                if (platforms.isNotEmpty()) {
                    _discoveredPlatformIds.value = platforms
                    settingsManager.cachedPlatformIds = platforms
                    
                    val counts = mutableMapOf<String, Int>()
                    platforms.forEach { id ->
                        counts[id] = gameDao.getGameCountByPlatform(id)
                    }
                    _platformGameCounts.value = counts
                }
            } catch (e: Exception) { 
                Log.e("MainViewModel", "Init error", e) 
            } finally {
                _isInitializing.value = false
            }
        }
    }

    fun getGamesForPlatform(platformId: String) = gameDao.getGamesByPlatform(platformId)

    fun startSmartSync() {
        val romsUriStr = settingsManager.romsLocation
        if (romsUriStr.isEmpty() || _isScanning.value) return

        scanJob = viewModelScope.launch(Dispatchers.IO) {
            _isScanning.value = true
            _scanProgress.value = 0.01f
            _scanStatusMessage.value = "Iniciando escaneo neural..."
            
            try {
                val totalGamesList = java.util.Collections.synchronizedList(mutableListOf<Game>())
                val localPlatformCounts = java.util.concurrent.ConcurrentHashMap<String, Int>()
                var totalCount = 0
                var lastUiUpdateTime = 0L

                scanner.scanAllRecursive(
                    rootUri = romsUriStr,
                    onGameFound = { game ->
                        totalGamesList.add(game)
                        totalCount++
                        
                        val count = localPlatformCounts.getOrDefault(game.platformId, 0)
                        localPlatformCounts[game.platformId] = count + 1
                        
                        val currentTime = System.currentTimeMillis()
                        if (totalGamesList.size >= 200 || (currentTime - lastUiUpdateTime > 500)) {
                            if (totalGamesList.isNotEmpty()) {
                                val batch = ArrayList(totalGamesList)
                                totalGamesList.clear()
                                gameDao.insertAll(batch)
                            }
                            
                            lastUiUpdateTime = currentTime
                            withContext(Dispatchers.Main) {
                                _discoveredPlatformIds.value = localPlatformCounts.keys.toSet()
                                _platformGameCounts.value = HashMap(localPlatformCounts)
                                _scanStatusMessage.value = "Detectados: $totalCount items"
                                _scanProgress.value = (0.01f + (totalCount / 15000f)).coerceAtMost(0.95f)
                            }
                        }
                    },
                    onPlatformDiscovered = { platformId ->
                        withContext(Dispatchers.Main) {
                            val current = _discoveredPlatformIds.value.toMutableSet()
                            if (current.add(platformId)) {
                                _discoveredPlatformIds.value = current
                            }
                        }
                    }
                )

                if (totalGamesList.isNotEmpty()) {
                    gameDao.insertAll(totalGamesList)
                }

                loadInitialData()
                _scanProgress.value = 1.0f
                _scanStatusMessage.value = "Sincronización completa: $totalCount juegos."
                settingsManager.lastScanTimestamp = System.currentTimeMillis()
                settingsManager.cachedPlatformIds = _discoveredPlatformIds.value

            } catch (e: Exception) {
                Log.e("MainViewModel", "Sync Error", e)
                _scanStatusMessage.value = "Error en el escaneo."
            } finally {
                delay(1500)
                _isScanning.value = false
            }
        }
    }

    fun forceScrapeAll() = viewModelScope.launch(Dispatchers.IO) {
        if (_isScanning.value) return@launch
        _isScanning.value = true
        try {
            val gamesMissing = gameDao.getAllGamesList().filter { it.boxArt == null }
            val total = gamesMissing.size
            if (total == 0) {
                _scanStatusMessage.value = "Biblioteca ya está al 100%"
                delay(1000)
                return@launch
            }
            
            gamesMissing.forEachIndexed { index, game ->
                _scanStatusMessage.value = "Scraping (${index + 1}/$total): ${game.title}"
                _scanProgress.value = index.toFloat() / total.toFloat()
                
                val updatedProgress = _platformProgress.value.toMutableMap()
                updatedProgress[game.platformId] = index.toFloat() / total.toFloat()
                _platformProgress.value = updatedProgress

                val updated = onlineScraper.scrapeGame(game)
                gameDao.updateGame(updated)
            }
            _scanStatusMessage.value = "Scraping total completado."
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error en forceScrapeAll", e)
        } finally {
            _isScanning.value = false
            _scanProgress.value = 1.0f
            _platformProgress.value = emptyMap()
        }
    }

    fun forceScrapePlatform(platformId: String) = viewModelScope.launch(Dispatchers.IO) {
        if (_isScanning.value) return@launch
        _isScanning.value = true
        try {
            val games = gameDao.getGamesByPlatform(platformId).first()
            val missingArt = games.filter { it.boxArt == null }
            val total = missingArt.size
            if (total == 0) {
                _scanStatusMessage.value = "$platformId ya está completo."
                delay(1000)
                return@launch
            }
            
            missingArt.forEachIndexed { index, game ->
                _scanStatusMessage.value = "Scraping $platformId (${index + 1}/$total): ${game.title}"
                _scanProgress.value = index.toFloat() / total.toFloat()
                
                val updatedProgress = _platformProgress.value.toMutableMap()
                updatedProgress[platformId] = index.toFloat() / total.toFloat()
                _platformProgress.value = updatedProgress

                val updatedGame = onlineScraper.scrapeGame(game)
                gameDao.updateGame(updatedGame)
            }
            _scanStatusMessage.value = "Scraping de $platformId completado."
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error en scraping manual", e)
        } finally {
            _isScanning.value = false
            _scanProgress.value = 1.0f
            val updatedProgress = _platformProgress.value.toMutableMap()
            updatedProgress.remove(platformId)
            _platformProgress.value = updatedProgress
        }
    }

    fun scrapeGame(game: Game) = viewModelScope.launch(Dispatchers.IO) {
        val updatedGame = onlineScraper.scrapeGame(game)
        gameDao.updateGame(updatedGame)
    }

    fun fullRescan() {
        if (_isScanning.value) scanJob?.cancel()
        viewModelScope.launch(Dispatchers.IO) {
            _isScanning.value = true
            _scanProgress.value = 0f
            _scanStatusMessage.value = "Purgando base de datos..."
            gameDao.deleteAll()
            settingsManager.lastScanTimestamp = 0L
            settingsManager.cachedPlatformIds = emptySet()
            withContext(Dispatchers.Main) {
                _discoveredPlatformIds.value = emptySet()
                _platformGameCounts.value = emptyMap()
            }
            delay(500)
            _isScanning.value = false 
            withContext(Dispatchers.Main) {
                startSmartSync()
            }
        }
    }

    fun markGameAsPlayed(game: Game) = viewModelScope.launch(Dispatchers.IO) { 
        gameDao.updateGame(game.copy(lastPlayed = System.currentTimeMillis(), playCount = game.playCount + 1)) 
    }
    
    fun toggleFavorite(game: Game) = viewModelScope.launch(Dispatchers.IO) { 
        gameDao.updateGame(game.copy(isFavorite = !game.isFavorite)) 
    }

    fun addChatMessage(message: ChatMessage) { 
        _chatMessages.value = listOf(message) + _chatMessages.value 
    }

    fun generateAiResponse(prompt: String, aiManagerInstance: AiManager) {
        viewModelScope.launch {
            _isAiThinking.value = true
            val response = aiManagerInstance.generateResponse(prompt)
            addChatMessage(ChatMessage(response, isUser = false))
            _isAiThinking.value = false
        }
    }

    fun syncSavesToCloud() = viewModelScope.launch(Dispatchers.IO) {
        _scanStatusMessage.value = "Sincronizando partidas en la nube..."
        cloudSaveManager.syncAllRetroArchSaves()
        _scanStatusMessage.value = "Sincronización de nube completada."
    }
}
