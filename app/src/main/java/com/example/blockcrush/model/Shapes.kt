package com.example.blockcrush.model

/** Defines the catalog of block shapes used in the game. */
object ShapesCatalog {
    val shapes: List<BlockShape> = listOf(
        BlockShape(1, listOf(Cell(0, 0)), colorIndex = 0),
        BlockShape(2, listOf(Cell(0, 0), Cell(1, 0)), colorIndex = 1),
        BlockShape(3, listOf(Cell(0, 0), Cell(1, 0), Cell(2, 0)), colorIndex = 2),
        BlockShape(4, listOf(Cell(0, 0), Cell(0, 1)), colorIndex = 3),
        BlockShape(5, listOf(Cell(0, 0), Cell(0, 1), Cell(0, 2)), colorIndex = 4),
        BlockShape(6, listOf(Cell(0, 0), Cell(1, 0), Cell(0, 1), Cell(1, 1)), colorIndex = 5),
        BlockShape(7, listOf(Cell(0, 0), Cell(1, 0), Cell(2, 0), Cell(2, 1)), colorIndex = 6),
        BlockShape(8, listOf(Cell(0, 1), Cell(1, 1), Cell(2, 1), Cell(0, 0)), colorIndex = 7),
        BlockShape(9, listOf(Cell(0, 0), Cell(1, 0), Cell(1, 1), Cell(2, 1)), colorIndex = 8),
        BlockShape(10, listOf(Cell(1, 0), Cell(0, 1), Cell(1, 1), Cell(2, 1)), colorIndex = 9),
        BlockShape(11, listOf(Cell(0, 0), Cell(1, 0), Cell(2, 0), Cell(3, 0)), colorIndex = 10),
        BlockShape(12, listOf(Cell(0, 0), Cell(0, 1), Cell(0, 2), Cell(0, 3)), colorIndex = 11)
    )
}
