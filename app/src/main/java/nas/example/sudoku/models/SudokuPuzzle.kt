package nas.example.sudoku.models

data class SudokuPuzzle(
    val id: Int,
    val level: Int,
    val initialBoard: Array<IntArray>,
    val solution: Array<IntArray>,
    val timeLimit: Int, // in seconds
    val difficulty: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SudokuPuzzle

        if (id != other.id) return false
        if (!initialBoard.contentDeepEquals(other.initialBoard)) return false
        return solution.contentDeepEquals(other.solution)
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + initialBoard.contentDeepHashCode()
        result = 31 * result + solution.contentDeepHashCode()
        return result
    }
}