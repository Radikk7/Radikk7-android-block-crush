package com.example.blockcrush.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.blockcrush.data.SettingsRepository
import com.example.blockcrush.data.SettingsState
import com.example.blockcrush.model.BlockShape
import com.example.blockcrush.model.ThemeMode
import com.example.blockcrush.model.ShapesCatalog
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(
    application: Application,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(application) {

    private val _settings = MutableStateFlow(SettingsState())
    val settings: StateFlow<SettingsState> = _settings.asStateFlow()

    private val _uiState = MutableStateFlow(GameState.empty(initialBlocks(), bestScore = 0))
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    private var lastMove: LastMove? = null
    private var settingsJob: Job? = null

    init {
        observeSettings()
    }

    private fun observeSettings() {
        settingsJob?.cancel()
        settingsRepository.settings
            .onEach { prefs ->
                _settings.value = prefs
                if (_uiState.value.bestScore != prefs.bestScore) {
                    _uiState.value = _uiState.value.copy(bestScore = prefs.bestScore)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun initialBlocks(): List<BlockShape> = (ShapesCatalog.shapes).shuffled(Random(System.currentTimeMillis())).take(3)

    fun resetGame() {
        lastMove = null
        _uiState.value = GameState.empty(initialBlocks(), _settings.value.bestScore)
    }

    fun tryPlaceBlock(shapeId: Int, anchorX: Int, anchorY: Int) {
        val state = _uiState.value
        val shape = state.availableBlocks.find { it.id == shapeId } ?: return
        if (!canPlace(shape, anchorX, anchorY, state.grid) || state.gameOver) return

        val previous = LastMove(state.grid, state.score, state.availableBlocks)
        val placedGrid = placeBlock(shape, anchorX, anchorY, state.grid)
        val (clearedGrid, clearedLines) = clearCompletedLines(placedGrid)
        val scoreGain = Scoring.onPlace(shape) + Scoring.onClear(clearedLines)
        val newScore = state.score + scoreGain
        val bestScore = maxOf(state.bestScore, newScore)

        val remaining = state.availableBlocks.filterNot { it.id == shapeId }
        val replenished = if (remaining.isEmpty()) initialBlocks() else remaining
        val gameOver = !anyPlacementAvailable(replenished, clearedGrid)

        _uiState.value = state.copy(
            grid = clearedGrid,
            score = newScore,
            bestScore = bestScore,
            availableBlocks = replenished,
            canUndo = true,
            gameOver = gameOver
        )

        lastMove = previous
        viewModelScope.launch { settingsRepository.saveBestScore(newScore) }
    }

    fun undo() {
        val move = lastMove ?: return
        _uiState.value = _uiState.value.copy(
            grid = move.grid,
            score = move.score,
            availableBlocks = move.availableBlocks,
            canUndo = false,
            gameOver = false
        )
        lastMove = null
    }

    fun updateTheme(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setTheme(mode) }
    }

    fun setSound(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSoundEnabled(enabled) }
    }

    fun setHaptics(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setHapticsEnabled(enabled) }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val repo = SettingsRepository(application.applicationContext)
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(application, repo) as T
        }
    }
}
