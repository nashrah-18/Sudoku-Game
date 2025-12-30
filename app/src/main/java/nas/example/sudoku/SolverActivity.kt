package nas.example.sudoku

import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.TableRow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import nas.example.sudoku.databinding.ActivitySolverBinding

class SolverActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySolverBinding
    private val cells = Array(9) { arrayOfNulls<EditText>(9) }
    private val isGiven = Array(9) { BooleanArray(9) }
    private var clickSound: MediaPlayer? = null
    private var solveSound: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySolverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Safe MediaPlayer
        clickSound = try { MediaPlayer.create(this, R.raw.button_click) } catch (e: Exception) { null }
        solveSound = try { MediaPlayer.create(this, R.raw.solve_sound) } catch (e: Exception) { null }

        createSudokuGrid()

        binding.cardSolve.setOnClickListener {
            playClickSound()
            solveSudoku()
        }

        binding.cardClear.setOnClickListener {
            playClickSound()
            clearGrid()
        }

        binding.btnBack.setOnClickListener {
            playClickSound()
            finish()
        }

        binding.hintText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.blink))
    }

    private fun createSudokuGrid() {
        binding.sudokuTable.removeAllViews()
        for (r in 0 until 9) {
            val row = TableRow(this)
            for (c in 0 until 9) {
                val cell = EditText(this).apply {
                    gravity = Gravity.CENTER
                    textSize = 18f
                    inputType = InputType.TYPE_CLASS_NUMBER
                    filters = arrayOf(InputFilter.LengthFilter(1))
                    setBackgroundResource(
                        if ((r / 3 + c / 3) % 2 == 0) R.drawable.cell_bg_purple else R.drawable.cell_bg_blue
                    )
                    setPadding(0, 16, 0, 16)
                    setTextColor(Color.BLACK)
                }
                val params = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                params.setMargins(if (c % 3 == 0) 6 else 2, if (r % 3 == 0) 6 else 2, if (c == 8) 6 else 2, if (r == 8) 6 else 2)
                row.addView(cell, params)
                cells[r][c] = cell
            }
            binding.sudokuTable.addView(row)
        }
    }

    private fun playClickSound() { clickSound?.takeIf { !it.isPlaying }?.apply { seekTo(0); start() } }

    private fun solveSudoku() {
        val board = Array(9) { IntArray(9) }
        var hasInput = false

        for (r in 0 until 9) {
            for (c in 0 until 9) {
                val text = cells[r][c]?.text.toString()
                if (text.isNotEmpty()) {
                    hasInput = true
                    val num = text.toIntOrNull()
                    if (num == null || num !in 1..9) {
                        showError("Invalid number at row ${r + 1}, col ${c + 1}")
                        return
                    }
                    board[r][c] = num
                    isGiven[r][c] = true
                    cells[r][c]?.setTextColor(Color.RED)
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

        if (!SudokuGame.isValidBoard(board)) {
            showError("Invalid Sudoku input! Check for duplicates.")
            return
        }

        val boardCopy = board.map { it.copyOf() }.toTypedArray()
        if (SudokuGame.solveSudoku(boardCopy)) {
            solveSound?.start()
            for (r in 0 until 9) {
                for (c in 0 until 9) {
                    if (!isGiven[r][c]) {
                        cells[r][c]?.setText(boardCopy[r][c].toString())
                        cells[r][c]?.setTextColor(Color.parseColor("#2E8B57"))
                        cells[r][c]?.postDelayed({
                            cells[r][c]?.startAnimation(AnimationUtils.loadAnimation(this, R.anim.cell_pop))
                        }, (r * 9 + c) * 20L)
                    }
                }
            }
            Toast.makeText(this, "Puzzle solved successfully! ✨", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No solution exists 😢", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearGrid() {
        for (r in 0 until 9) for (c in 0 until 9) {
            cells[r][c]?.setText("")
            cells[r][c]?.setTextColor(Color.BLACK)
            isGiven[r][c] = false
        }
    }

    private fun showError(msg: String) { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }

    override fun onDestroy() {
        super.onDestroy()
        clickSound?.release()
        clickSound = null
        solveSound?.release()
        solveSound = null
    }
}
