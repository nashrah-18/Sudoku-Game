package nas.example.sudoku

import android.content.Context
import android.content.SharedPreferences

class GamePrefs(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sudoku_wizard", Context.MODE_PRIVATE)

    // Level progression
    fun getMaxUnlockedLevel(): Int = prefs.getInt("max_unlocked_level", 1)
    fun setMaxUnlockedLevel(level: Int) = prefs.edit().putInt("max_unlocked_level", level).apply()

    // Scores (time in seconds)
    fun getLevelBestTime(level: Int): Int = prefs.getInt("best_time_$level", Int.MAX_VALUE)
    fun setLevelBestTime(level: Int, time: Int) = prefs.edit().putInt("best_time_$level", time).apply()

    // Potions (hints)
    fun getPotionCount(): Int = prefs.getInt("potion_count", 3)
    fun setPotionCount(count: Int) = prefs.edit().putInt("potion_count", count).apply()
    fun addPotion() = setPotionCount(getPotionCount() + 1)
    fun usePotion() = setPotionCount(maxOf(0, getPotionCount() - 1))

    // Level completion
    fun isLevelCompleted(level: Int): Boolean = prefs.getBoolean("completed_$level", false)
    fun setLevelCompleted(level: Int) = prefs.edit().putBoolean("completed_$level", true).apply()

    // Total score
    fun getTotalScore(): Int = prefs.getInt("total_score", 0)
    fun addToTotalScore(points: Int) = prefs.edit().putInt("total_score", getTotalScore() + points).apply()

    // Clear all data (for testing)
    fun clearAllData() = prefs.edit().clear().apply()
}