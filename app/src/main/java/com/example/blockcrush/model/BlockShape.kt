package com.example.blockcrush.model

/** Represents a puzzle block shape as a set of occupied cells relative to the shape's origin. */
data class BlockShape(
    val id: Int,
    val cells: List<Cell>,
    val colorIndex: Int
) {
    val width: Int = (cells.maxOfOrNull { it.x } ?: 0) + 1
    val height: Int = (cells.maxOfOrNull { it.y } ?: 0) + 1
}

data class Cell(val x: Int, val y: Int)
