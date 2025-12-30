package nas.example.sudoku

import nas.example.sudoku.models.SudokuPuzzle

object SudokuGame {

    // Pre-defined puzzles for each level (1-10)
    private val levelPuzzles = mapOf(
        1 to createPuzzle(1, 35), // 35 cells filled (easiest)
        2 to createPuzzle(2, 32),
        3 to createPuzzle(3, 30),
        4 to createPuzzle(4, 28),
        5 to createPuzzle(5, 26),
        6 to createPuzzle(6, 24),
        7 to createPuzzle(7, 22),
        8 to createPuzzle(8, 20),
        9 to createPuzzle(9, 18),
        10 to createPuzzle(10, 17) // 17 cells filled (hardest)
    )

    fun getPuzzleForLevel(level: Int): SudokuPuzzle {
        return levelPuzzles[level] ?: levelPuzzles[1]!!
    }

    fun getTimeLimitForLevel(level: Int): Int {
        return when (level) {
            1 -> 1800 // 30 minutes
            2 -> 1500 // 25 minutes
            3 -> 1200 // 20 minutes
            4 -> 900  // 15 minutes
            5 -> 600  // 10 minutes
            6 -> 480  // 8 minutes
            7 -> 360  // 6 minutes
            8 -> 300  // 5 minutes
            9 -> 240  // 4 minutes
            10 -> 180 // 3 minutes
            else -> 300
        }
    }

    private fun createPuzzle(level: Int, filledCells: Int): SudokuPuzzle {
        val solution = generateValidSudoku()
        val initialBoard = removeNumbers(solution, 81 - filledCells)

        return SudokuPuzzle(
            id = level,
            level = level,
            initialBoard = initialBoard,
            solution = solution,
            timeLimit = getTimeLimitForLevel(level),
            difficulty = when (level) {
                in 1..3 -> "Beginner"
                in 4..6 -> "Intermediate"
                in 7..8 -> "Advanced"
                else -> "Expert"
            }
        )
    }

    private fun generateValidSudoku(): Array<IntArray> {
        val board = Array(9) { IntArray(9) }
        solveSudoku(board)
        return board
    }

    private fun removeNumbers(board: Array<IntArray>, cellsToRemove: Int): Array<IntArray> {
        val puzzle = board.map { it.copyOf() }.toTypedArray()
        val positions = mutableListOf<Pair<Int, Int>>()

        for (i in 0 until 9) {
            for (j in 0 until 9) {
                positions.add(i to j)
            }
        }

        positions.shuffle()

        for (k in 0 until minOf(cellsToRemove, positions.size)) {
            val (i, j) = positions[k]
            val temp = puzzle[i][j]
            puzzle[i][j] = 0

            // Check if puzzle still has unique solution
            if (countSolutions(puzzle) != 1) {
                puzzle[i][j] = temp
            }
        }

        return puzzle
    }

    private fun countSolutions(board: Array<IntArray>): Int {
        val copy = board.map { it.copyOf() }.toTypedArray()
        return countSolutionsHelper(copy)
    }

    private fun countSolutionsHelper(board: Array<IntArray>): Int {
        var solutions = 0
        var row = -1
        var col = -1
        var isEmpty = true

        // Find first empty cell
        for (i in 0 until 9) {
            for (j in 0 until 9) {
                if (board[i][j] == 0) {
                    row = i
                    col = j
                    isEmpty = false
                    break
                }
            }
            if (!isEmpty) break
        }

        // If no empty cells, we have a solution
        if (isEmpty) return 1

        // Try numbers 1-9
        for (num in 1..9) {
            if (isSafe(board, row, col, num)) {
                board[row][col] = num
                solutions += countSolutionsHelper(board)
                board[row][col] = 0

                // Stop if more than one solution
                if (solutions > 1) return solutions
            }
        }

        return solutions
    }

    fun solveSudoku(board: Array<IntArray>): Boolean {
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                if (board[row][col] == 0) {
                    for (num in 1..9) {
                        if (isSafe(board, row, col, num)) {
                            board[row][col] = num
                            if (solveSudoku(board)) return true
                            board[row][col] = 0
                        }
                    }
                    return false
                }
            }
        }
        return true
    }

    fun isSafe(board: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
        // Check row
        for (i in 0 until 9) {
            if (board[row][i] == num) return false
        }

        // Check column
        for (i in 0 until 9) {
            if (board[i][col] == num) return false
        }

        // Check 3x3 box
        val startRow = row - row % 3
        val startCol = col - col % 3
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (board[startRow + i][startCol + j] == num) return false
            }
        }

        return true
    }

    fun isValidBoard(board: Array<IntArray>): Boolean {
        // Check rows
        for (row in 0 until 9) {
            val seen = BooleanArray(10)
            for (col in 0 until 9) {
                val num = board[row][col]
                if (num != 0) {
                    if (seen[num]) return false
                    seen[num] = true
                }
            }
        }

        // Check columns
        for (col in 0 until 9) {
            val seen = BooleanArray(10)
            for (row in 0 until 9) {
                val num = board[row][col]
                if (num != 0) {
                    if (seen[num]) return false
                    seen[num] = true
                }
            }
        }

        // Check 3x3 boxes
        for (box in 0 until 9) {
            val seen = BooleanArray(10)
            for (i in 0 until 3) {
                for (j in 0 until 3) {
                    val row = (box / 3) * 3 + i
                    val col = (box % 3) * 3 + j
                    val num = board[row][col]
                    if (num != 0) {
                        if (seen[num]) return false
                        seen[num] = true
                    }
                }
            }
        }

        return true
    }

    fun checkSolution(userBoard: Array<IntArray>, solution: Array<IntArray>): Boolean {
        for (i in 0 until 9) {
            for (j in 0 until 9) {
                if (userBoard[i][j] != solution[i][j]) {
                    return false
                }
            }
        }
        return true
    }
}