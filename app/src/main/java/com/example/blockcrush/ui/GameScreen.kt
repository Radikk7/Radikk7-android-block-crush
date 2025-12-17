package com.example.blockcrush.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consume
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.example.blockcrush.data.SettingsState
import com.example.blockcrush.game.BOARD_SIZE
import com.example.blockcrush.game.GameState
import com.example.blockcrush.game.canPlace
import com.example.blockcrush.model.BlockShape
import com.example.blockcrush.model.ThemeMode
import com.example.blockcrush.ui.theme.BoardLine
import com.example.blockcrush.ui.theme.ColorPalette
import com.example.blockcrush.ui.theme.DarkWalnut
import com.example.blockcrush.ui.theme.Sand
import com.example.blockcrush.ui.theme.Walnut
import kotlin.math.floor

private data class DragState(
    val shape: BlockShape,
    val pointerOffset: Offset,
    val origin: Offset
) {
    fun blockOrigin(): Offset = origin - pointerOffset
}

@Composable
fun GameScreen(
    state: GameState,
    settings: SettingsState,
    onBlockPlaced: (shapeId: Int, x: Int, y: Int) -> Unit,
    onUndo: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val dragState = remember { mutableStateOf<DragState?>(null) }
    val boardBounds = remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    val cellSize = remember { mutableFloatStateOf(0f) }
    val haptic = LocalHapticFeedback.current

    val showGameOver = state.gameOver

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Sand)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Score", style = MaterialTheme.typography.labelMedium, color = DarkWalnut)
                    Text(text = state.score.toString(), style = MaterialTheme.typography.headlineMedium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Best", style = MaterialTheme.typography.labelMedium, color = DarkWalnut)
                    Text(text = state.bestScore.toString(), style = MaterialTheme.typography.headlineMedium)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onUndo,
                    enabled = state.canUndo,
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Undo")
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }

            Board(
                grid = state.grid,
                themeMode = settings.theme,
                onBounds = { rect, cell ->
                    boardBounds.value = rect
                    cellSize.floatValue = cell
                },
                ghost = dragState.value
            )

            Text(text = "Blocks", style = MaterialTheme.typography.titleMedium)
            BlockTray(
                blocks = state.availableBlocks,
                themeMode = settings.theme,
                onDragStart = { shape, pointerOffset, origin ->
                    dragState.value = DragState(shape, pointerOffset, origin)
                },
                onDrag = { delta ->
                    dragState.value = dragState.value?.copy(origin = dragState.value!!.origin + delta)
                },
                onDragEnd = {
                    val boardRect = boardBounds.value
                    val drag = dragState.value
                    if (boardRect != null && drag != null && cellSize.floatValue > 0f) {
                        val blockOrigin = drag.blockOrigin()
                        val x = floor((blockOrigin.x - boardRect.left) / cellSize.floatValue).toInt()
                        val y = floor((blockOrigin.y - boardRect.top) / cellSize.floatValue).toInt()
                        if (canPlace(drag.shape, x, y, state.grid)) {
                            onBlockPlaced(drag.shape.id, x, y)
                            if (settings.hapticsEnabled) {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            }
                        }
                    }
                    dragState.value = null
                }
            )
        }

        if (showGameOver) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "Game Over", style = MaterialTheme.typography.headlineSmall)
                    Button(onClick = onUndo, enabled = state.canUndo) { Text("Undo") }
                }
            }
        }
    }
}

@Composable
private fun Board(
    grid: List<List<Int?>>, themeMode: ThemeMode, ghost: DragState?, onBounds: (androidx.compose.ui.geometry.Rect, Float) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .onGloballyPositioned { coords ->
                val rect = androidx.compose.ui.geometry.Rect(
                    coords.positionInRoot(),
                    coords.size.toSize()
                )
                onBounds(rect, coords.size.width / BOARD_SIZE.toFloat())
            }
            .background(color = Color.White, shape = RoundedCornerShape(20.dp))
            .padding(12.dp)
    ) {
        val cellSizePx = constraints.maxWidth / BOARD_SIZE.toFloat()
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw grid background
            for (y in 0 until BOARD_SIZE) {
                for (x in 0 until BOARD_SIZE) {
                    val topLeft = Offset(x * cellSizePx, y * cellSizePx)
                    drawRoundRect(
                        color = BoardLine,
                        topLeft = topLeft,
                        size = Size(cellSizePx - 4f, cellSizePx - 4f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                    )
                    grid[y][x]?.let { colorIndex ->
                        drawRoundRect(
                            color = tileColor(themeMode, colorIndex),
                            topLeft = topLeft + Offset(3f, 3f),
                            size = Size(cellSizePx - 8f, cellSizePx - 8f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                        )
                    }
                }
            }

            ghost?.let { drag ->
                val origin = drag.blockOrigin()
                val anchorX = (origin.x / cellSizePx).toInt()
                val anchorY = (origin.y / cellSizePx).toInt()
                val valid = canPlace(drag.shape, anchorX, anchorY, grid)
                val overlayColor = if (valid) tileColor(themeMode, drag.shape.colorIndex).copy(alpha = 0.4f) else Color(0x55FF5252)
                drag.shape.cells.forEach { cell ->
                    val tl = Offset((anchorX + cell.x) * cellSizePx, (anchorY + cell.y) * cellSizePx)
                    drawRoundRect(
                        color = overlayColor,
                        topLeft = tl,
                        size = Size(cellSizePx - 6f, cellSizePx - 6f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BlockTray(
    blocks: List<BlockShape>,
    themeMode: ThemeMode,
    onDragStart: (BlockShape, Offset, Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        blocks.forEach { shape ->
            BlockTile(shape = shape, themeMode = themeMode, onDragStart, onDrag, onDragEnd)
        }
    }
}

@Composable
private fun BlockTile(
    shape: BlockShape,
    themeMode: ThemeMode,
    onDragStart: (BlockShape, Offset, Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
) {
    var origin by remember { mutableStateOf(Offset.Zero) }
    Card(
        modifier = Modifier
            .size(96.dp)
            .onGloballyPositioned { coords -> origin = coords.positionInRoot() }
            .pointerInput(shape.id) {
                detectDragGestures(
                    onDragStart = { offset ->
                        onDragStart(shape, offset, origin + offset)
                    },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    }
                )
            },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(72.dp)) {
                val cellSize = size.width / maxOf(shape.width, shape.height).toFloat()
                shape.cells.forEach { cell ->
                    val tl = Offset(cell.x * cellSize, cell.y * cellSize)
                    drawRoundRect(
                        color = tileColor(themeMode, shape.colorIndex),
                        topLeft = tl,
                        size = Size(cellSize - 6f, cellSize - 6f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                    )
                }
            }
        }
    }
}

private fun tileColor(themeMode: ThemeMode, index: Int): Color {
    return if (themeMode == ThemeMode.MONO) Walnut else ColorPalette[index % ColorPalette.size]
}
