package nas.example.sudoku

object SudokuSolver {

    fun solve(board: Array<IntArray>): Boolean {
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                if (board[row][col] == 0) {
                    for (num in 1..9) {
                        if (isSafe(board, row, col, num)) {
                            board[row][col] = num
                            if (solve(board)) return true
                            board[row][col] = 0
                        }
                    }
                    return false
                }
            }
        }
        return true
    }

    fun isValidInitialBoard(board: Array<IntArray>): Boolean {
        for (r in 0 until 9)
            for (c in 0 until 9)
                if (board[r][c] != 0) {
                    val temp = board[r][c]
                    board[r][c] = 0
                    if (!isSafe(board, r, c, temp)) {
                        board[r][c] = temp
                        return false
                    }
                    board[r][c] = temp
                }
        return true
    }

    private fun isSafe(board: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
        for (i in 0 until 9)
            if (board[row][i] == num || board[i][col] == num) return false

        val startRow = row - row % 3
        val startCol = col - col % 3

        for (r in 0 until 3)
            for (c in 0 until 3)
                if (board[startRow + r][startCol + c] == num) return false

        return true
    }
}
