package nas.example.sudoku

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import nas.example.sudoku.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var backgroundMusic: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Optional background music
        backgroundMusic = MediaPlayer.create(this, R.raw.magic_music)
        backgroundMusic.isLooping = true
        backgroundMusic.setVolume(0.3f, 0.3f)
        backgroundMusic.start()

        // Load and start animations
        val titleAnim = AnimationUtils.loadAnimation(this, R.anim.splash_title)
        val hatAnim = AnimationUtils.loadAnimation(this, R.anim.splash_hat)
        val starsAnim = AnimationUtils.loadAnimation(this, R.anim.splash_stars)

        binding.txtTitle.startAnimation(titleAnim)
        binding.wizardHat.startAnimation(hatAnim)
        binding.stars.startAnimation(starsAnim)

        // Navigate to MainMenuActivity after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainMenuActivity::class.java))
            finish()
        }, 3000)
    }

    override fun onPause() {
        super.onPause()
        if (backgroundMusic.isPlaying) {
            backgroundMusic.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        backgroundMusic.release()
    }
}
