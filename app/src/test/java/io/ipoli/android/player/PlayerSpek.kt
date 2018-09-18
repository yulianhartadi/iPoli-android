package io.ipoli.android.player

import io.ipoli.android.Constants
import io.ipoli.android.TestUtil
import org.amshove.kluent.`should be false`
import org.amshove.kluent.`should be true`
import org.amshove.kluent.`should be`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 09/12/2018.
 */
class PlayerSpek : Spek({

    describe("Player") {

        it("should be dead after removing all HP") {
            val p = TestUtil.player
            p.removeHealthPoints(p.health.current).isDead.`should be true`()
        }

        it("should not be dead at 1 HP") {
            val p = TestUtil.player
            p.removeHealthPoints(p.health.current - 1).isDead.`should be false`()
        }

        it("should revive Player") {
            val p = TestUtil.player.removeHealthPoints(TestUtil.player.health.current)
            val newPlayer = p.revive()
            newPlayer.health.current.`should be`((TestUtil.player.health.max * Constants.PLAYER_REVIVE_HEALTH_PERCENTAGE).toInt())
            newPlayer.level.`should be`(1)
            newPlayer.experience.`should be`(ExperienceForLevelGenerator.forLevel(newPlayer.level))
            newPlayer.coins.`should be`(0)
        }
    }
})