package com.example.blockcrush.game

import com.example.blockcrush.model.BlockShape
import com.example.blockcrush.model.Cell

const val BOARD_SIZE = 10

/** Represents the immutable state of the game board and score. */
data class GameState(
    val grid: List<List<Int?>>, // null = empty, otherwise color index
    val score: Int,
    val bestScore: Int,
    val availableBlocks: List<BlockShape>,
    val canUndo: Boolean,
    val gameOver: Boolean
) {
    companion object {
        fun empty(initialBlocks: List<BlockShape>, bestScore: Int) = GameState(
            grid = List(BOARD_SIZE) { List<Int?>(BOARD_SIZE) { null } },
            score = 0,
            bestScore = bestScore,
            availableBlocks = initialBlocks,
            canUndo = false,
            gameOver = false
        )
    }
}

data class LastMove(
    val grid: List<List<Int?>>,
    val score: Int,
    val availableBlocks: List<BlockShape>
)

/** Simple game scoring model. */
object Scoring {
    private const val BASE_PLACE_POINTS = 1
    private const val LINE_CLEAR_POINTS = 10
    private const val COMBO_BONUS = 5

    fun onPlace(block: BlockShape): Int = block.cells.size * BASE_PLACE_POINTS

    fun onClear(lines: Int): Int = when {
        lines <= 0 -> 0
        lines == 1 -> LINE_CLEAR_POINTS
        else -> lines * LINE_CLEAR_POINTS + COMBO_BONUS * (lines - 1)
    }
}

fun canPlace(shape: BlockShape, anchorX: Int, anchorY: Int, grid: List<List<Int?>>): Boolean {
    if (anchorX < 0 || anchorY < 0) return false
    if (anchorX + shape.width > BOARD_SIZE || anchorY + shape.height > BOARD_SIZE) return false
    return shape.cells.all { cell ->
        grid[anchorY + cell.y][anchorX + cell.x] == null
    }
}

fun placeBlock(
    shape: BlockShape,
    anchorX: Int,
    anchorY: Int,
    grid: List<List<Int?>>
): List<List<Int?>> {
    val mutableGrid = grid.map { it.toMutableList() }
    shape.cells.forEach { cell ->
        mutableGrid[anchorY + cell.y][anchorX + cell.x] = shape.colorIndex
    }
    return mutableGrid.map { it.toList() }
}

fun clearCompletedLines(grid: List<List<Int?>>): Pair<List<List<Int?>>, Int> {
    val rowsToClear = grid.indices.filter { row -> grid[row].all { it != null } }
    val colsToClear = grid[0].indices.filter { col -> grid.all { it[col] != null } }

    if (rowsToClear.isEmpty() && colsToClear.isEmpty()) return grid to 0

    val mutableGrid = grid.map { it.toMutableList() }
    rowsToClear.forEach { row ->
        for (col in grid[row].indices) mutableGrid[row][col] = null
    }
    colsToClear.forEach { col ->
        for (row in grid.indices) mutableGrid[row][col] = null
    }
    return mutableGrid.map { it.toList() } to (rowsToClear.size + colsToClear.size)
}

fun anyPlacementAvailable(blocks: List<BlockShape>, grid: List<List<Int?>>): Boolean {
    return blocks.any { shape ->
        (0..BOARD_SIZE - shape.width).any { x ->
            (0..BOARD_SIZE - shape.height).any { y -> canPlace(shape, x, y, grid) }
        }
    }
}
