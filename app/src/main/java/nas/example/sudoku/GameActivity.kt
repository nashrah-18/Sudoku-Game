package nas.example.sudoku

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.TableRow
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import nas.example.sudoku.databinding.ActivityGameBinding
import nas.example.sudoku.models.SudokuPuzzle

class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding
    private lateinit var gamePrefs: GamePrefs
    private var clickSound: MediaPlayer? = null
    private var successSound: MediaPlayer? = null
    private var failSound: MediaPlayer? = null
    private var hintSound: MediaPlayer? = null
    private var resetSound: MediaPlayer? = null

    private var currentLevel = 1
    private var currentPuzzle: SudokuPuzzle? = null
    private val cells = Array(9) { arrayOfNulls<EditText>(9) }
    private val isGiven = Array(9) { BooleanArray(9) }
    private var timer: CountDownTimer? = null
    private var timeLeft = 0L
    private var isGameCompleted = false

    // Undo functionality
    private val moveHistory = mutableListOf<Triple<Int, Int, String>>() // row, col, previous value

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentLevel = intent.getIntExtra("LEVEL", 1)
        gamePrefs = GamePrefs(this)

        // Initialize ALL sounds safely
        clickSound = MediaPlayer.create(this, R.raw.button_click)
        successSound = MediaPlayer.create(this, R.raw.success_sound)
        failSound = MediaPlayer.create(this, R.raw.fail_sound)
        hintSound = MediaPlayer.create(this, R.raw.button_click) // You can create a specific hint sound
        resetSound = MediaPlayer.create(this, R.raw.button_click) // You can create a specific reset sound

        // Load puzzle safely
        currentPuzzle = SudokuGame.getPuzzleForLevel(currentLevel)
        if (currentPuzzle == null) {
            Toast.makeText(this, "Error loading puzzle!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        timeLeft = currentPuzzle!!.timeLimit * 1000L

        setupUI()
        createSudokuGrid()
        setupTimer()
        updatePotionCount()
    }

    private fun setupUI() {
        binding.levelTitle.text = "Level $currentLevel - ${currentPuzzle!!.difficulty}"
        binding.timerText.text = formatTime(timeLeft / 1000)

        binding.btnHint.setOnClickListener {
            playHintSound()
            showHintConfirmation()
        }

        binding.btnReset.setOnClickListener {
            playResetSound()
            Toast.makeText(this, "Puzzle reset!", Toast.LENGTH_SHORT).show()
            resetPuzzle()
        }

        binding.btnCheck.setOnClickListener {
            playClickSound()
            checkSolution()
        }

        binding.btnBack.setOnClickListener {
            playClickSound()
            finish()
        }

        // Undo button functionality added
        binding.btnUndo.setOnClickListener {
            playClickSound()
            undoLastMove()
        }
    }

    private fun createSudokuGrid() {
        binding.sudokuGrid.removeAllViews()
        moveHistory.clear() // Clear history when creating new grid

        for (r in 0 until 9) {
            val row = TableRow(this)

            for (c in 0 until 9) {
                val cell = EditText(this).apply {
                    gravity = Gravity.CENTER
                    textSize = 20f
                    inputType = InputType.TYPE_CLASS_NUMBER
                    filters = arrayOf(InputFilter.LengthFilter(1))
                    setPadding(0, 16, 0, 16)
                    setTextColor(Color.WHITE)
                    isCursorVisible = true

                    val bgRes = when {
                        (r in 0..2 || r in 6..8) && (c in 0..2 || c in 6..8) -> R.drawable.cell_bg_purple
                        (r in 3..5) && (c in 3..5) -> R.drawable.cell_bg_purple
                        else -> R.drawable.cell_bg_blue
                    }
                    setBackgroundResource(bgRes)

                    // Set initial value if given
                    val initialValue = currentPuzzle!!.initialBoard[r][c]
                    if (initialValue != 0) {
                        setText(initialValue.toString())
                        setTextColor(Color.parseColor("#FFD700")) // Gold
                        isEnabled = false
                        isGiven[r][c] = true
                    } else {
                        isGiven[r][c] = false
                    }

                    addTextChangedListener(object : android.text.TextWatcher {
                        private var previousText = ""

                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                            // Record the previous value before change for undo functionality
                            if (!isGiven[r][c]) {
                                previousText = s?.toString() ?: ""
                            }
                        }

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            // Add to move history when text changes
                            if (!isGiven[r][c] && previousText != s.toString()) {
                                moveHistory.add(Triple(r, c, previousText))
                            }
                        }

                        override fun afterTextChanged(s: android.text.Editable?) {
                            val text = s.toString()
                            if (text.isNotEmpty()) {
                                val num = text.toIntOrNull()
                                if (num == null || num !in 1..9) {
                                    setText("")
                                    showToast("Enter numbers 1-9 only")
                                } else if (!SudokuGame.isSafe(getCurrentBoard(), r, c, num)) {
                                    // Changed from bright red to light pink
                                    setTextColor(Color.parseColor("#FFB6C1"))
                                    startCellShakeAnimation(this@apply)
                                } else {
                                    setTextColor(Color.WHITE)
                                }
                            }
                        }
                    })
                }

                val params = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                params.setMargins(
                    if (c % 3 == 0) 4 else 1,
                    if (r % 3 == 0) 4 else 1,
                    if (c == 8) 4 else 1,
                    if (r == 8) 4 else 1
                )

                row.addView(cell, params)
                cells[r][c] = cell
            }

            binding.sudokuGrid.addView(row)
        }
    }

    private fun startCellShakeAnimation(view: View) {
        val shake = AnimationUtils.loadAnimation(this, R.anim.shake)
        view.startAnimation(shake)
    }

    private fun getCurrentBoard(): Array<IntArray> {
        val board = Array(9) { IntArray(9) }
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                board[r][c] = cells[r][c]?.text.toString().toIntOrNull() ?: 0
            }
        }
        return board
    }

    private fun setupTimer() {
        timer?.cancel()

        timer = object : CountDownTimer(timeLeft, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished
                binding.timerText.text = formatTime(millisUntilFinished / 1000)

                if (millisUntilFinished < 60000) {
                    binding.timerText.setTextColor(Color.RED)
                    startTimerPulseAnimation()
                }
            }

            override fun onFinish() {
                timeLeft = 0
                binding.timerText.text = "00:00"
                showTimeUpDialog()
            }
        }.start()
    }

    private fun startTimerPulseAnimation() {
        val animator = ValueAnimator.ofFloat(1f, 1.2f, 1f)
        animator.duration = 500
        animator.repeatCount = ValueAnimator.INFINITE
        animator.addUpdateListener {
            val scale = it.animatedValue as Float
            binding.timerText.scaleX = scale
            binding.timerText.scaleY = scale
        }
        animator.start()
    }

    private fun formatTime(seconds: Long): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    private fun undoLastMove() {
        if (moveHistory.isEmpty()) {
            showToast("No moves to undo!")
            return
        }

        val lastMove = moveHistory.removeAt(moveHistory.size - 1)
        val (row, col, previousValue) = lastMove

        cells[row][col]?.apply {
            // Set previous value (empty string if it was cleared)
            if (previousValue.isEmpty()) {
                text?.clear()
            } else {
                setText(previousValue)
            }

            // Reset text color to white
            setTextColor(Color.WHITE)

            // Show undo animation
            startAnimation(AnimationUtils.loadAnimation(this@GameActivity, R.anim.undo_pulse))
        }

        showToast("Undo successful!")
    }

    private fun showHintConfirmation() {
        val potions = gamePrefs.getPotionCount()
        if (potions <= 0) {
            showToast("No potions left!")
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Use Potion?")
            .setMessage("Do you want to use 1 potion to reveal a cell?")
            .setPositiveButton("Yes") { _, _ ->
                useHint()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun useHint() {
        val potions = gamePrefs.getPotionCount()
        if (potions <= 0) {
            showToast("No potions left!")
            return
        }

        for (r in 0 until 9) {
            for (c in 0 until 9) {
                if (cells[r][c]?.text.isNullOrEmpty() && !isGiven[r][c]) {
                    cells[r][c]?.setText(currentPuzzle!!.solution[r][c].toString())
                    cells[r][c]?.setTextColor(Color.parseColor("#90EE90"))
                    cells[r][c]?.isEnabled = false
                    gamePrefs.usePotion()
                    updatePotionCount()
                    cells[r][c]?.startAnimation(AnimationUtils.loadAnimation(this, R.anim.hint_pulse))
                    Toast.makeText(this, "Hint used! Cell revealed.", Toast.LENGTH_SHORT).show()
                    return
                }
            }
        }

        showToast("No empty cells!")
    }

    private fun updatePotionCount() {
        val potions = gamePrefs.getPotionCount()
        binding.potionCount.text = "x$potions"
    }

    private fun checkSolution() {
        if (isGameCompleted) return

        val userBoard = getCurrentBoard()
        for (r in 0 until 9) for (c in 0 until 9) {
            if (userBoard[r][c] == 0) {
                showToast("Complete all cells first!")
                return
            }
        }

        if (SudokuGame.checkSolution(userBoard, currentPuzzle!!.solution)) {
            completeGame(true)
        } else {
            completeGame(false)
        }
    }

    private fun completeGame(success: Boolean) {
        isGameCompleted = true
        timer?.cancel()

        if (success) {
            successSound?.start()
            val timeTaken = (currentPuzzle!!.timeLimit * 1000L - timeLeft) / 1000
            val previousBest = gamePrefs.getLevelBestTime(currentLevel)
            if (timeTaken < previousBest) gamePrefs.setLevelBestTime(currentLevel, timeTaken.toInt())
            gamePrefs.addPotion()
            if (currentLevel == gamePrefs.getMaxUnlockedLevel()) gamePrefs.setMaxUnlockedLevel(currentLevel + 1)
            gamePrefs.setLevelCompleted(currentLevel)
            showSuccessDialog(timeTaken)
        } else {
            failSound?.start()
            showFailureDialog()
        }
    }

    private fun showSuccessDialog(timeTaken: Long) {
        AlertDialog.Builder(this)
            .setTitle("🎉 Congratulations!")
            .setMessage("You solved Level $currentLevel in ${formatTime(timeTaken)}!\nYou earned 1 potion! 🧪")
            .setPositiveButton("Next Level") { _, _ ->
                if (currentLevel < 10) {
                    startActivity(Intent(this, GameActivity::class.java).apply { putExtra("LEVEL", currentLevel + 1) })
                }
                finish()
            }
            .setNegativeButton("Back to Levels") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun showFailureDialog() {
        AlertDialog.Builder(this)
            .setTitle("😔 Try Again!")
            .setMessage("Your solution has some errors. Take another look or use a hint!")
            .setPositiveButton("Retry") { _, _ -> resetPuzzle() }
            .setNegativeButton("Back to Levels") { _, _ -> finish() }
            .show()
    }

    private fun showTimeUpDialog() {
        AlertDialog.Builder(this)
            .setTitle("⏰ Time's Up!")
            .setMessage("You ran out of time on Level $currentLevel.")
            .setPositiveButton("Try Again") { _, _ -> resetPuzzle() }
            .setNegativeButton("Back to Levels") { _, _ -> finish() }
            .show()
    }

    private fun resetPuzzle() {
        timer?.cancel()
        isGameCompleted = false
        moveHistory.clear()
        currentPuzzle = SudokuGame.getPuzzleForLevel(currentLevel)
        if (currentPuzzle != null) {
            timeLeft = currentPuzzle!!.timeLimit * 1000L
            createSudokuGrid()
            setupTimer()
            Toast.makeText(this, "Puzzle has been reset!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error resetting puzzle", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playClickSound() {
        clickSound?.let {
            if (!it.isPlaying) {
                it.seekTo(0)
                it.start()
            }
        }
    }

    private fun playHintSound() {
        hintSound?.let {
            if (!it.isPlaying) {
                it.seekTo(0)
                it.start()
            }
        }
    }

    private fun playResetSound() {
        resetSound?.let {
            if (!it.isPlaying) {
                it.seekTo(0)
                it.start()
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        clickSound?.release()
        successSound?.release()
        failSound?.release()
        hintSound?.release()
        resetSound?.release()
    }
}