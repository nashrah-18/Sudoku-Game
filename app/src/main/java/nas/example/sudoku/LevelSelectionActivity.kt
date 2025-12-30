package nas.example.sudoku

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import nas.example.sudoku.databinding.ActivityLevelSelectionBinding

class LevelSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLevelSelectionBinding
    private lateinit var gamePrefs: GamePrefs
    private var clickSound: MediaPlayer? = null
    private var maxUnlockedLevel = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLevelSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gamePrefs = GamePrefs(this)
        clickSound = MediaPlayer.create(this, R.raw.button_click)

        binding.btnBack.setOnClickListener {
            playClickSound()
            finish()
        }

        startBlinkingCurrentLevel()
        startCatFloatAnimation()
    }

    override fun onResume() {
        super.onResume()
        maxUnlockedLevel = gamePrefs.getMaxUnlockedLevel()

        updateCurrentLevelText()
        setupLevels()
        updateCatPosition()
    }

    // ---------------- CURRENT LEVEL ----------------

    private fun updateCurrentLevelText() {
        binding.currentLevelText.text = "Current Level: $maxUnlockedLevel"
    }

    private fun startBlinkingCurrentLevel() {
        val blink = AlphaAnimation(0.3f, 1f).apply {
            duration = 600
            repeatMode = Animation.REVERSE
            repeatCount = Animation.INFINITE
        }
        binding.currentLevelText.startAnimation(blink)
    }

    // ---------------- LEVEL SETUP ----------------

    private fun setupLevels() {

        val levelButtons = listOf(
            binding.btnLevel1, binding.btnLevel2, binding.btnLevel3,
            binding.btnLevel4, binding.btnLevel5, binding.btnLevel6,
            binding.btnLevel7, binding.btnLevel8, binding.btnLevel9,
            binding.btnLevel10
        )

        val cloudImages = listOf(
            binding.cloud1, binding.cloud2, binding.cloud3,
            binding.cloud4, binding.cloud5, binding.cloud6,
            binding.cloud7, binding.cloud8, binding.cloud9,
            binding.cloud10
        )

        val frameLayouts = listOf(
            binding.levelGrid.getChildAt(0) as FrameLayout,
            binding.levelGrid.getChildAt(1) as FrameLayout,
            binding.levelGrid.getChildAt(2) as FrameLayout,
            binding.levelGrid.getChildAt(3) as FrameLayout,
            binding.levelGrid.getChildAt(4) as FrameLayout,
            binding.levelGrid.getChildAt(5) as FrameLayout,
            binding.levelGrid.getChildAt(6) as FrameLayout,
            binding.levelGrid.getChildAt(7) as FrameLayout,
            binding.levelGrid.getChildAt(8) as FrameLayout,
            binding.levelGrid.getChildAt(9) as FrameLayout
        )

        levelButtons.forEachIndexed { index, button ->

            val level = index + 1
            val cloud = cloudImages[index]
            val container = frameLayouts[index]

            val starId = resources.getIdentifier("star$level", "id", packageName)
            val star = findViewById<ImageView>(starId)

            fun handleClick() {
                playClickSound()

                if (level <= maxUnlockedLevel) {
                    startActivity(
                        Intent(this, GameActivity::class.java)
                            .putExtra("LEVEL", level)
                    )
                } else {
                    Toast.makeText(
                        this,
                        "Complete previous challenge first!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            // Visual state
            if (level <= maxUnlockedLevel) {
                button.alpha = 1f
                cloud.alpha = 1f
                star?.isVisible = gamePrefs.isLevelCompleted(level)
            } else {
                button.alpha = 0.5f
                cloud.alpha = 0.5f
                star?.isVisible = false
            }

            // 👇 SOUND GUARANTEED ON BOTH CLOUD + BUTTON
            button.setOnClickListener { handleClick() }
            cloud.setOnClickListener { handleClick() }
            container.setOnClickListener { handleClick() }
        }
    }

    // ---------------- CAT POSITION ----------------

    private fun updateCatPosition() {

        val clouds = listOf(
            binding.cloud1, binding.cloud2, binding.cloud3,
            binding.cloud4, binding.cloud5, binding.cloud6,
            binding.cloud7, binding.cloud8, binding.cloud9,
            binding.cloud10
        )

        val index = (maxUnlockedLevel - 1).coerceIn(0, clouds.size - 1)
        val cloud = clouds[index]

        cloud.post {

            val rootHeight = binding.root.height

            val targetX =
                cloud.x + cloud.width / 2f - binding.broomImage.width / 2f

            val rawY = cloud.y + cloud.height * 0.7f

            val maxY = rootHeight - binding.broomImage.height - 32f
            val safeY = rawY.coerceIn(0f, maxY)

            binding.broomImage.animate()
                .x(targetX)
                .y(safeY)
                .setDuration(500)
                .start()
        }
    }

    private fun startCatFloatAnimation() {
        val anim = AnimationUtils.loadAnimation(this, R.anim.float_up_down)
        binding.broomImage.startAnimation(anim)
    }

    // ---------------- SOUND ----------------

    private fun playClickSound() {
        clickSound?.let {
            it.seekTo(0)
            it.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clickSound?.release()
        clickSound = null
    }
}
