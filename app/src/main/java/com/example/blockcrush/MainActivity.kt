package com.example.blockcrush

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blockcrush.game.GameViewModel
import com.example.blockcrush.ui.GameScreen
import com.example.blockcrush.ui.SettingsScreen
import com.example.blockcrush.ui.theme.BlockCrushTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlockCrushApp()
        }
    }
}

@Composable
fun BlockCrushApp() {
    val context = LocalContext.current.applicationContext as Application
    val viewModel: GameViewModel = viewModel(factory = GameViewModel.Factory(context))
    val state by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    var isSettingsOpen by remember { mutableStateOf(false) }

    BlockCrushTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            if (isSettingsOpen) {
                SettingsScreen(
                    settings = settings,
                    onThemeChanged = { viewModel.updateTheme(it) },
                    onSoundChanged = { viewModel.setSound(it) },
                    onHapticsChanged = { viewModel.setHaptics(it) },
                    onBack = { isSettingsOpen = false }
                )
            } else {
                GameScreen(
                    state = state,
                    settings = settings,
                    onBlockPlaced = { id, x, y -> viewModel.tryPlaceBlock(id, x, y) },
                    onUndo = { viewModel.undo() },
                    onOpenSettings = { isSettingsOpen = true }
                )
            }
        }
    }
}
