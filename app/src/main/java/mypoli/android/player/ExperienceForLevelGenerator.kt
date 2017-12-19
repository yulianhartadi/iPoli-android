package mypoli.android.player

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 11/14/17.
 */
object ExperienceForLevelGenerator {

    private const val SWITCH_LEVEL = 6
    private const val LARGE_INCREASE = 80
    private const val SMALL_INCREASE = 50

    fun forLevel(level: Int): Long {

        if (level <= 1) {
            return 0
        }

        if (level == SWITCH_LEVEL) {
            val xpPrevLvl = forLevel(level - 1)
            val prevLevelDif = xpPrevLvl - forLevel(level - 2)
            return xpPrevLvl + prevLevelDif + LARGE_INCREASE * 2
        }

        val c = if (level < SWITCH_LEVEL) SMALL_INCREASE else LARGE_INCREASE

        return (c * (level - 1)) + forLevel(level - 1)
    }
}