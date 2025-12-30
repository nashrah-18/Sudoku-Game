package nas.example.sudoku

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import nas.example.sudoku.databinding.ActivityMainMenuBinding

class MainMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainMenuBinding
    private var clickSound: MediaPlayer? = null  // Nullable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Safe MediaPlayer
        clickSound = try {
            MediaPlayer.create(this, R.raw.button_click)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        // Animations
        binding.titleTextView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))
        binding.wizardHatImage.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce))

        // Buttons
        binding.btnChallenge.setOnClickListener {
            playClickSound()
            startActivity(Intent(this, LevelSelectionActivity::class.java))
        }

        binding.btnSolver.setOnClickListener {
            playClickSound()
            startActivity(Intent(this, SolverActivity::class.java))
        }

        binding.btnScoreboard.setOnClickListener {
            playClickSound()
            startActivity(Intent(this, ScoreboardActivity::class.java))
        }
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
