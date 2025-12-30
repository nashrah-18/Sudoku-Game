package nas.example.sudoku

import android.media.MediaPlayer
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import nas.example.sudoku.databinding.ActivityScoreboardBinding

class ScoreboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScoreboardBinding
    private lateinit var gamePrefs: GamePrefs
    private var clickSound: MediaPlayer? = null  // Nullable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScoreboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gamePrefs = GamePrefs(this)

        // Safe MediaPlayer initialization
        clickSound = try {
            MediaPlayer.create(this, R.raw.button_click)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        displayScores()
        setupAnimations()

        binding.btnBack.setOnClickListener {
            playClickSound()
            finish()
        }

        binding.btnReset.setOnClickListener {
            playClickSound()
            showResetConfirmation()
        }
    }

    private fun displayScores() {
        val potions = gamePrefs.getPotionCount()
        binding.potionCount.text = "x$potions"

        val maxLevel = gamePrefs.getMaxUnlockedLevel()
        binding.unlockedLevels.text = "Levels Unlocked: $maxLevel/10"

        // Calculate completed levels
        val completedCount = (1..10).count { gamePrefs.isLevelCompleted(it) }
        binding.completedLevels.text = "Levels Completed: $completedCount/10"

        // Display best times
        val bestTimes = StringBuilder()
        for (level in 1..maxLevel) {
            val time = gamePrefs.getLevelBestTime(level)
            val formatted = if (time != Int.MAX_VALUE) formatTime(time.toLong()) else "Not completed"
            bestTimes.append("Level $level: $formatted\n")
        }
        binding.bestTimes.text = bestTimes.toString()

        // Total score
        val totalScore = (1..10).sumOf { level ->
            if (gamePrefs.isLevelCompleted(level)) {
                val time = gamePrefs.getLevelBestTime(level)
                if (time != Int.MAX_VALUE) (1000 * level) / (time + 1) else 0
            } else 0
        }
        binding.totalScore.text = "Total Score: $totalScore"
    }

    private fun formatTime(seconds: Long) = String.format("%02d:%02d", seconds / 60, seconds % 60)

    private fun setupAnimations() {
        binding.trophyImage.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_slow))
        binding.potionImage.startAnimation(AnimationUtils.loadAnimation(this, R.anim.pulse))
        binding.scoreContainer.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))
    }

    private fun showResetConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Reset All Progress")
            .setMessage("Are you sure you want to reset all your progress? This cannot be undone!")
            .setPositiveButton("Reset") { _, _ ->
                gamePrefs.clearAllData()
                displayScores()
                Toast.makeText(this, "Progress reset successfully!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun playClickSound() {
        clickSound?.takeIf { !it.isPlaying }?.apply {
            seekTo(0)
            start()
        }
    }

    override fun onPause() {
        super.onPause()
        clickSound?.release()
        clickSound = null
    }
}
