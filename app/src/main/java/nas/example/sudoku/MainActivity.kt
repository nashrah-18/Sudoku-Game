package nas.example.sudoku

import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.widget.EditText
import android.widget.TableRow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import nas.example.sudoku.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val cells = Array(9) { arrayOfNulls<EditText>(9) }
    private val isGiven = Array(9) { BooleanArray(9) }
    private var clickSound: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clickSound = MediaPlayer.create(this, R.raw.click_sound)

        createSudokuGrid()

        // Button click sounds
        binding.btnSolve.setOnClickListener {
            playClickSound()
            solveSudoku()
        }

        binding.btnClear.setOnClickListener {
            playClickSound()
            clearGrid()
        }
    }

    private fun createSudokuGrid() {
        for (r in 0 until 9) {
            val row = TableRow(this)

            for (c in 0 until 9) {
                val cell = EditText(this).apply {
                    gravity = Gravity.CENTER
                    textSize = 18f
                    inputType = InputType.TYPE_CLASS_NUMBER
                    filters = arrayOf(InputFilter.LengthFilter(1))
                    setBackgroundResource(R.drawable.sudoku_cell_bg)
                    setPadding(0, 16, 0, 16)
                    setTextColor(Color.BLACK)
                    isCursorVisible = true
                }

                val params = TableRow.LayoutParams(
                    0,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1f
                )

                params.setMargins(
                    if (c % 3 == 0) 6 else 2,
                    if (r % 3 == 0) 6 else 2,
                    if (c == 8) 6 else 2,
                    if (r == 8) 6 else 2
                )

                row.addView(cell, params)
                cells[r][c] = cell
            }
            binding.sudokuTable.addView(row)
        }
    }

    private fun playClickSound() {
        clickSound?.seekTo(0)
        clickSound?.start()
    }

    private fun solveSudoku() {
        val board = Array(9) { IntArray(9) }
        var hasInput = false

        for (r in 0 until 9) {
            for (c in 0 until 9) {
                val text = cells[r][c]?.text.toString()
                if (text.isNotEmpty()) {
                    hasInput = true
                    val num = text.toInt()
                    if (num !in 1..9) {
                        showError("Invalid number at row ${r + 1}, col ${c + 1}")
                        return
                    }
                    board[r][c] = num
                    isGiven[r][c] = true
                    cells[r][c]?.setTextColor(Color.RED) // user input
                } else {
                    board[r][c] = 0
                    isGiven[r][c] = false
                }
            }
        }

        if (!hasInput) {
            Toast.makeText(this, "Please add a Sudoku puzzle first! 📝", Toast.LENGTH_SHORT).show()
            return
        }

        if (!SudokuSolver.isValidInitialBoard(board)) {
            showError("Invalid Sudoku input!")
            return
        }

        if (SudokuSolver.solve(board)) {
            for (r in 0 until 9)
                for (c in 0 until 9)
                    if (!isGiven[r][c]) {
                        cells[r][c]?.setText(board[r][c].toString())
                        cells[r][c]?.setTextColor(Color.BLACK) // solved
                    }
        } else {
            Toast.makeText(this, "No solution exists 😢", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearGrid() {
        for (r in 0 until 9)
            for (c in 0 until 9) {
                cells[r][c]?.setText("")
                cells[r][c]?.setTextColor(Color.BLACK)
                isGiven[r][c] = false
            }
    }

    private fun showError(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        clickSound?.release()
    }
}
